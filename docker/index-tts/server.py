"""
Index TTS FastAPI Server

Provides a REST API for Index TTS (https://github.com/index-tts/index-tts)
Compatible with OpenAI-style TTS API endpoints with Index TTS emotional extensions.
"""

#
#  Copyright (C) 2025-2025 Abdalla Bushnaq
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

import io
import logging
import os
from contextlib import asynccontextmanager
from pathlib import Path
from typing import Optional, Dict

import numpy as np
import soundfile as sf
import torch
from fastapi import FastAPI, HTTPException, Response, UploadFile
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Global model instance
model = None
device = None
config = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Lifespan event handler for startup and shutdown"""
    global model, device, config

    # Startup
    model_path = os.getenv("MODEL_PATH", "/opt/index-tts/checkpoints")
    device_name = os.getenv("DEVICE", "cuda")

    logger.info(f"Starting Index TTS server...")
    logger.info(f"Model path: {model_path}")
    logger.info(f"Device: {device_name}")

    # Check if models are present, download if needed
    config_path = os.path.join(model_path, "config.yaml")
    gpt_model_path = os.path.join(model_path, "gpt.pth")

    if not os.path.exists(config_path) or not os.path.exists(gpt_model_path):
        logger.warning(f"Models not found in {model_path}, downloading from HuggingFace...")
        logger.info("This is a one-time download (~3-5GB), please be patient...")

        try:
            # Use Python API for more reliable download
            from huggingface_hub import snapshot_download

            logger.info("Downloading from HuggingFace (using Python API)...")
            snapshot_download(
                repo_id="IndexTeam/IndexTTS-2",
                local_dir=model_path,
                local_dir_use_symlinks=False
            )
            logger.info("Models downloaded successfully!")
        except Exception as e:
            logger.error(f"Failed to download models: {e}")
            logger.warning("Continuing anyway, but model loading may fail...")
    else:
        logger.info("Models found in mounted volume, skipping download!")

    # Check CUDA availability
    if device_name == "cuda" and not torch.cuda.is_available():
        logger.warning("CUDA requested but not available, falling back to CPU")
        device_name = "cpu"

    device = torch.device(device_name)
    logger.info(f"Using device: {device}")

    try:
        # Load Index TTS v2 model
        from indextts.infer_v2 import IndexTTS2

        logger.info("Loading Index TTS v2 model...")

        # Use MODEL_PATH from environment or default
        model_dir = os.getenv("MODEL_PATH", "/opt/index-tts/checkpoints")
        config_path = os.path.join(model_dir, "config.yaml")

        logger.info(f"Using config: {config_path}")
        logger.info(f"Using model directory: {model_dir}")

        model = IndexTTS2(
            cfg_path=config_path,
            model_dir=model_dir,
            device=str(device),
            use_fp16=(device_name == "cuda")
        )

        # Set model path if provided
        if os.path.exists(model_path):
            logger.info(f"Using model path: {model_path}")
            # Load custom model if available
            model_files = list(Path(model_path).glob("*.pth")) + list(Path(model_path).glob("*.pt"))
            if model_files:
                logger.info(f"Found model files: {[f.name for f in model_files]}")

        logger.info("Index TTS model loaded successfully!")
        config = {}  # Store any config if needed

    except ImportError as e:
        logger.error(f"Failed to import Index TTS: {e}")
        logger.error("Make sure Index TTS is properly installed in the container")
        # Create a placeholder for development
        logger.warning("⚠️  Using placeholder mode - Index TTS not available!")
        model = None
    except Exception as e:
        logger.error(f"Failed to load model: {e}", exc_info=True)
        model = None

    yield  # Server runs here

    # Shutdown
    logger.info("Shutting down Index TTS server...")
    if model:
        del model
        if torch.cuda.is_available():
            torch.cuda.empty_cache()


app = FastAPI(
    title="Index TTS API",
    version="1.0.0",
    description="Text-to-Speech API with emotional expression support",
    lifespan=lifespan
)


class SpeechRequest(BaseModel):
    """Request model for speech synthesis"""
    input: str = Field(..., description="Text to synthesize")
    voice_reference: Optional[str] = Field(
        None,
        description="Path to voice reference WAV file for cloning (server-side path). "
                    "NOTE: Index TTS uses voice cloning, NOT predefined voice names. "
                    "Provide a path to a WAV file with the voice you want to clone, or null for default."
    )
    speed: Optional[float] = Field(1.0, ge=0.5, le=2.0, description="Speech speed multiplier")
    temperature: Optional[float] = Field(0.7, ge=0.0, le=2.0, description="Sampling temperature")

    # Emotional parameters
    emotions: Optional[Dict[str, float]] = Field(
        None,
        description="Emotion levels: angry, happy, sad, surprise, neutral (0.0-1.0)"
    )

    # Advanced options
    sample_rate: Optional[int] = Field(22050, description="Output sample rate")


@app.post("/v1/audio/speech")
async def generate_speech(request: SpeechRequest):
    """
    Generate speech from text using Index TTS
    
    This endpoint is compatible with OpenAI TTS API structure but adds
    Index TTS specific features like emotional parameters.
    """
    if model is None:
        # Development placeholder response
        logger.warning("Model not loaded - generating placeholder audio")

        # Generate simple placeholder audio (1 second of silence with beep)
        sample_rate = request.sample_rate
        duration = 1.0
        t = np.linspace(0, duration, int(sample_rate * duration))
        audio_array = (0.1 * np.sin(2 * np.pi * 440 * t)).astype(np.float32)

        # Convert to WAV
        wav_buffer = io.BytesIO()
        sf.write(wav_buffer, audio_array, sample_rate, format='WAV', subtype='PCM_16')
        wav_buffer.seek(0)

        return Response(
            content=wav_buffer.read(),
            media_type="audio/wav",
            headers={"Content-Disposition": "attachment; filename=speech.wav"}
        )

    try:
        logger.info(f"Generating speech: voice_reference={request.voice_reference}, speed={request.speed}, "
                    f"emotions={request.emotions}, text_len={len(request.input)}")

        # Prepare emotion parameters for Index TTS v2
        # Index TTS v2 uses 8-dimensional emotion vector based on the training data
        emotions = request.emotions or {}

        # Map emotions to Index TTS v2 format (8 dimensions)
        # Based on emo_num: [3, 17, 2, 8, 4, 5, 10, 24] in config
        # We'll use a simplified mapping for now
        emo_vector = [
            emotions.get('neutral', 0.0),
            emotions.get('happy', 0.0),
            emotions.get('sad', 0.0),
            emotions.get('angry', 0.0),
            emotions.get('surprise', 0.0),
            0.0,  # Other emotions
            0.0,
            0.0
        ]

        # Normalize emotion vector
        total = sum(emo_vector)
        if total > 0:
            emo_vector = [e / total for e in emo_vector]
        else:
            emo_vector[0] = 1.0  # Default to neutral

        # Create temporary output path
        import tempfile
        with tempfile.NamedTemporaryFile(suffix='.wav', delete=False) as tmp_file:
            output_path = tmp_file.name

        try:
            # Get voice reference (speaker prompt)
            # Use provided voice_reference or fall back to default
            if request.voice_reference:
                if os.path.exists(request.voice_reference):
                    spk_audio_prompt = request.voice_reference
                    logger.info(f"Using provided voice reference: {spk_audio_prompt}")
                else:
                    logger.warning(f"Provided voice reference not found: {request.voice_reference}")
                    logger.warning(f"Falling back to default voice reference")
                    spk_audio_prompt = "/opt/index-tts/examples/voice_01.wav"
            else:
                logger.info("No voice reference provided, using default")
                spk_audio_prompt = "/opt/index-tts/examples/voice_01.wav"

            # Ensure default voice exists
            if not os.path.exists(spk_audio_prompt):
                # Create a dummy audio file if examples don't exist
                logger.warning("Default voice reference not found, creating generated reference")
                os.makedirs(os.path.dirname(spk_audio_prompt), exist_ok=True)
                dummy_audio = np.random.randn(24000) * 0.01  # 1 second of noise
                sf.write(spk_audio_prompt, dummy_audio, 24000)
                logger.info(f"Created dummy voice reference at: {spk_audio_prompt}")

            # Call Index TTS v2 infer method
            logger.info(f"Calling model.infer with emo_vector={emo_vector}")
            model.infer(
                spk_audio_prompt=spk_audio_prompt,
                text=request.input,
                output_path=output_path,
                emo_vector=emo_vector,
                use_random=False,
                verbose=True
            )

            # Read the generated audio
            audio_array, sample_rate = sf.read(output_path)

            # Clean up temp file
            os.unlink(output_path)

        except Exception as e:
            # Clean up temp file on error
            if os.path.exists(output_path):
                os.unlink(output_path)
            raise

        # Ensure audio is numpy array
        if torch.is_tensor(audio_array):
            audio_array = audio_array.cpu().numpy()

        # Ensure proper shape
        if len(audio_array.shape) > 1:
            audio_array = audio_array.squeeze()

        # Convert to float32 if needed
        if audio_array.dtype != np.float32:
            audio_array = audio_array.astype(np.float32)

        # Normalize audio to prevent clipping
        max_val = np.abs(audio_array).max()
        if max_val > 0:
            audio_array = audio_array / max_val * 0.95

        # Apply speed adjustment if needed (post-processing)
        if request.speed and request.speed != 1.0:
            import librosa
            audio_array = librosa.effects.time_stretch(audio_array, rate=request.speed)

        # Convert to WAV format with requested sample rate
        wav_buffer = io.BytesIO()
        # Resample if needed
        if sample_rate != request.sample_rate:
            import librosa
            audio_array = librosa.resample(audio_array, orig_sr=sample_rate, target_sr=request.sample_rate)
            sample_rate = request.sample_rate

        sf.write(
            wav_buffer,
            audio_array,
            sample_rate,
            format='WAV',
            subtype='PCM_16'
        )
        wav_buffer.seek(0)

        logger.info(f"Speech generated successfully: {len(audio_array)} samples")

        return Response(
            content=wav_buffer.read(),
            media_type="audio/wav",
            headers={
                "Content-Disposition": "attachment; filename=speech.wav"
            }
        )

    except Exception as e:
        logger.error(f"Error generating speech: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/v1/voice-references")
async def list_voice_references():
    """
    List available voice reference files

    Returns a list of WAV files that can be used as voice references for cloning.
    """
    voice_dir = "/opt/index-tts/voices"

    # Create directory if it doesn't exist
    os.makedirs(voice_dir, exist_ok=True)

    try:
        # List all WAV files
        wav_files = []
        for filename in os.listdir(voice_dir):
            if filename.lower().endswith('.wav'):
                file_path = os.path.join(voice_dir, filename)
                file_size = os.path.getsize(file_path)
                file_mtime = os.path.getmtime(file_path)

                wav_files.append({
                    "filename": filename,
                    "path": file_path,
                    "size_bytes": file_size,
                    "modified_timestamp": file_mtime
                })

        return {
            "voice_references": wav_files,
            "count": len(wav_files),
            "directory": voice_dir
        }
    except Exception as e:
        logger.error(f"Error listing voice references: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/v1/voice-references")
async def upload_voice_reference(file: UploadFile):
    """
    Upload a voice reference WAV file

    The uploaded file will be saved in the voice references directory and can be used
    for voice cloning in speech synthesis requests.

    Requirements:
    - File must be in WAV format
    - Recommended: 10-30 seconds of clear speech
    - Recommended sample rate: 16kHz, 22.05kHz, or 44.1kHz
    """
    voice_dir = "/opt/index-tts/voices"
    os.makedirs(voice_dir, exist_ok=True)

    # Validate file extension
    if not file.filename.lower().endswith('.wav'):
        raise HTTPException(
            status_code=400,
            detail="Only WAV files are supported. Please upload a .wav file."
        )

    # Sanitize filename to prevent directory traversal
    import re
    safe_filename = re.sub(r'[^\w\-\.]', '_', file.filename)
    file_path = os.path.join(voice_dir, safe_filename)

    try:
        # Read and save the file
        contents = await file.read()

        with open(file_path, "wb") as f:
            f.write(contents)

        file_size = os.path.getsize(file_path)

        logger.info(f"Voice reference uploaded: {safe_filename} ({file_size} bytes)")

        return {
            "filename": safe_filename,
            "path": file_path,
            "size_bytes": file_size,
            "message": "Voice reference uploaded successfully"
        }
    except Exception as e:
        logger.error(f"Error uploading voice reference: {e}", exc_info=True)
        # Clean up partial file if it exists
        if os.path.exists(file_path):
            os.unlink(file_path)
        raise HTTPException(status_code=500, detail=str(e))


@app.delete("/v1/voice-references/{filename}")
async def delete_voice_reference(filename: str):
    """
    Delete a voice reference file

    Removes the specified voice reference file from the server.
    """
    voice_dir = "/opt/index-tts/voices"

    # Sanitize filename to prevent directory traversal
    import re
    safe_filename = re.sub(r'[^\w\-\.]', '_', filename)
    file_path = os.path.join(voice_dir, safe_filename)

    # Check if file exists
    if not os.path.exists(file_path):
        raise HTTPException(
            status_code=404,
            detail=f"Voice reference '{safe_filename}' not found"
        )

    # Check if it's actually a file (not a directory)
    if not os.path.isfile(file_path):
        raise HTTPException(
            status_code=400,
            detail=f"'{safe_filename}' is not a file"
        )

    try:
        os.unlink(file_path)
        logger.info(f"Voice reference deleted: {safe_filename}")

        return {
            "filename": safe_filename,
            "message": "Voice reference deleted successfully"
        }
    except Exception as e:
        logger.error(f"Error deleting voice reference: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/emotions")
async def list_emotions():
    """List supported emotions"""
    return {
        "emotions": [
            "neutral",
            "happy",
            "sad",
            "angry",
            "surprise"
        ]
    }


@app.get("/models")
async def list_models():
    """List available models"""
    return {
        "models": [
            {
                "id": "index-tts-base",
                "name": "Index TTS Base Model",
                "languages": ["en"],
                "emotions": ["neutral", "happy", "sad", "angry", "surprise"]
            }
        ]
    }


@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {
        "status": "healthy" if model is not None else "degraded",
        "model_loaded": model is not None,
        "device": str(device) if device else "unknown",
        "cuda_available": torch.cuda.is_available()
    }


@app.get("/")
async def root():
    """Root endpoint with API info"""
    return {
        "name": "Index TTS API",
        "version": "1.0.0",
        "description": "Text-to-speech with emotional expression and voice cloning",
        "repository": "https://github.com/index-tts/index-tts",
        "endpoints": {
            "generate_speech": "/v1/audio/speech",
            "list_voice_references": "/v1/voice-references (GET)",
            "upload_voice_reference": "/v1/voice-references (POST)",
            "delete_voice_reference": "/v1/voice-references/{filename} (DELETE)",
            "list_emotions": "/emotions",
            "list_models": "/models",
            "health": "/health"
        },
        "note": "Index TTS uses voice cloning. Upload WAV files as voice references via /v1/voice-references"
    }


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        app,
        host="0.0.0.0",
        port=int(os.getenv("PORT", 5000)),
        log_level="info"
    )
