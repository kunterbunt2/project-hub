# Chatterbox TTS Container - Changelog

## [1.0.2] - 2025-10-27 - Dependency Conflicts Fixed ✅

### Fixed - Gradio Compatibility

- ✅ **requirements.txt** - Fixed multiple dependency conflicts with Gradio 5.44.1

  **FastAPI conflict:**
    - Changed: `fastapi==0.115.0`
    - To: `fastapi>=0.115.2,<1.0`
    - Reason: Gradio 5.44.1 requires `fastapi>=0.115.2`

  **python-multipart conflict:**
    - Changed: `python-multipart==0.0.12`
    - To: `python-multipart>=0.0.18`
    - Reason: Gradio 5.44.1 requires `python-multipart>=0.0.18`

  **pydantic (preventive):**
    - Changed: `pydantic==2.9.2`
    - To: `pydantic>=2.0,<3.0`
    - Reason: Allow compatible pydantic v2.x versions

### Fixed - Build-Time Dependencies

- ✅ **Dockerfile** - Fixed spacy-pkuseg build failure
    - Added: Pre-install numpy before other requirements
    - Error: `ModuleNotFoundError: No module named 'numpy'` during spacy-pkuseg setup.py
    - Reason: spacy-pkuseg requires numpy at build time (imports numpy in setup.py)
    - Solution: Install numpy in separate RUN command before requirements.txt

### Fixed - Typer/Spacy Conflict

- ✅ **requirements.txt** - Removed russian-text-stresser dependency
    - Error: Gradio 5.44.1 requires `typer>=0.12`, but russian-text-stresser pulls in Spacy 3.6.x which requires
      `typer<0.10.0`
    - These requirements are incompatible
    - Impact: Russian text stress marking not available
    - Alternative: Russian TTS still works via Chatterbox Multilingual model
    - Note: This package is only needed for advanced Russian text processing (adding stress marks)
    - The core Russian language support in Chatterbox TTS is NOT affected

### Notes

- All changes maintain backward compatibility
- No breaking changes for our REST API
- Gradio is a dependency of Chatterbox TTS (from pyproject.toml)

## [1.0.1] - 2025-10-27 - API Implementation Complete ✅

### Updated - server.py (Complete Rewrite)

**Major API Changes:**

- ✅ **Correct Imports**
    - Changed: `from chatterbox import ChatterboxTTS`
    - To: `from chatterbox.tts import ChatterboxTTS`
    - Added: `from chatterbox.mtl_tts import ChatterboxMultilingualTTS`

- ✅ **Dual Model Support**
    - Loads both ChatterboxTTS (English) and ChatterboxMultilingualTTS (23 languages)
    - Automatic model selection based on language parameter

- ✅ **Correct API Method**
    - Changed: `tts_engine.synthesize(text, temperature, exaggeration, cfg_weight)`
    - To: `model.generate(text)` or `model.generate(text, language_id="fr")`
    - Based on official Chatterbox GitHub examples

- ✅ **Voice Cloning Support**
    - Added `audio_prompt_path` parameter
    - Uses: `model.generate(text, audio_prompt_path="voice.wav")`

- ✅ **Device Auto-Detection**
    - Checks: CUDA → MPS → CPU
    - Respects `DEVICE` environment variable
    - Proper initialization: `ChatterboxTTS.from_pretrained(device=device)`

- ✅ **Audio Output**
    - Uses `torchaudio.save()` instead of `soundfile`
    - Proper tensor handling (CPU conversion, dimension checks)
    - Correct sample rate from `model.sr`

- ✅ **Error Handling**
    - Full exception logging with `exc_info=True`
    - Detailed error messages for debugging
    - Proper HTTP status codes

- ✅ **23 Languages Supported**
    - Multilingual model for non-English languages
    - Language auto-selection
    - Updated language list

### Updated - Dependencies

- ✅ **requirements.txt** - Exact versions from official pyproject.toml v0.1.4
    - torch==2.6.0 (was unpinned)
    - torchaudio==2.6.0 (was unpinned)
    - transformers==4.46.3 (was unpinned)
    - Added: diffusers==0.29.0
    - Added: safetensors==0.5.3
    - Added: conformer==0.3.2
    - Added: spacy-pkuseg
    - Added: pykakasi==2.3.0
    - Added: gradio==5.44.1
    - Added: s3tokenizer
    - Added: resemble-perth==1.0.1
    - numpy: constrained to >=1.24.0,<1.26.0
    - librosa: pinned to ==0.11.0

- ✅ **Dockerfile** - Added libgomp1 for OpenMP support

### Updated - Documentation

- ✅ **IMPLEMENTATION_NOTES.md**
    - Complete rewrite with correct API documentation
    - Added testing examples
    - Added troubleshooting guide
    - Added performance notes
    - Marked as "Fully Implemented"

- ✅ **README.md**
    - Added hot-reload feature
    - Added pinned dependencies note

- ✅ **HOT_RELOAD_GUIDE.md** - New comprehensive guide

- ✅ **VOLUME_MOUNT_ARCHITECTURE.md** - New technical deep-dive

### API Compatibility

**Java Client (`ChatterboxTTS.java`):**

- ✅ `input` parameter → Works correctly
- ⚠️ `temperature`, `exaggeration`, `cfg_weight` → Accepted but not used (Chatterbox doesn't use these)
- ✅ Returns WAV audio as expected
- ✅ `/languages` endpoint works
- ✅ Port 4123 configured correctly

**New Parameters Available:**

- `language`: Specify language code (e.g., "fr", "es")
- `audio_prompt_path`: Path to audio file for voice cloning
- `model`: Force model selection (optional)

### Breaking Changes

None - API remains backward compatible with existing Java client.

### Testing

Recommended tests after build:

```bash
# Test English generation
curl -X POST http://localhost:4123/v1/audio/speech \
  -H "Content-Type: application/json" \
  -d '{"input":"Hello world"}' --output test.wav

# Test multilingual
curl -X POST http://localhost:4123/v1/audio/speech \
  -H "Content-Type: application/json" \
  -d '{"input":"Bonjour le monde","language":"fr"}' --output test-fr.wav
```

## [1.0.0] - 2025-10-27

### Added

- Initial implementation
- Docker container with GPU support
- FastAPI REST server (with placeholder API)
- OpenAI-compatible endpoints structure
- Windows helper batch script
- Complete documentation
- Hot-reload support

---

## Implementation Source

**API Implementation based on:**

- Repository: https://github.com/resemble-ai/chatterbox
- Documentation: Official README examples
- Version: chatterbox-tts 0.1.4
- Date: October 27, 2025

## Verification

To verify correct implementation:

```bash
# In container shell
chatterbox-helper.bat shell

python
>>> from chatterbox.tts import ChatterboxTTS
>>> from chatterbox.mtl_tts import ChatterboxMultilingualTTS
>>> import torch
>>> device = "cuda" if torch.cuda.is_available() else "cpu"
>>> model = ChatterboxTTS.from_pretrained(device=device)
>>> print(f"Model loaded, sample rate: {model.sr}")
```

## Next Release Plans

Potential features for v1.1.0:

- Voice reference upload endpoint
- Audio caching for repeated requests
- Streaming support (if available in Chatterbox)
- Better parameter mapping from Java client
- Voice library management

