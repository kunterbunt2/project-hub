# Index TTS Docker Container

This Docker container provides a FastAPI server for Index TTS with GPU acceleration.

## Features

- ✅ CUDA 12.1 GPU support enabled
- ✅ Actual Index TTS model loading (not placeholder)
- ✅ Emotional expression support (angry, happy, sad, surprise, neutral)
- ✅ Multiple voice support
- ✅ Speed control
- ✅ Health check endpoint
- ✅ **Live server.py editing** (no rebuild needed)
- ✅ Modern FastAPI lifespan event handlers

## Quick Start

```bash
# Build the container (first time only, or after Dockerfile changes)
index-tts-helper.bat build

# Start the service
index-tts-helper.bat start

# Check status
index-tts-helper.bat test

# View logs
index-tts-helper.bat logs
```

## Implementation Details

### Live Server Editing

The `server.py` file is mounted into the container, allowing you to edit it without rebuilding:

```yaml
# docker-compose-index-tts.yml
volumes:
  - ./docker/index-tts/server.py:/app/server.py:rw
```

**Workflow:**

1. Edit `docker/index-tts/server.py`
2. Restart container: `index-tts-helper.bat restart`
3. Changes take effect immediately!

No rebuild needed! 🎉

### Modern FastAPI Lifespan

The server uses FastAPI's modern lifespan event handler:

```python
@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup: load models
    logger.info("Starting Index TTS server...")
    model = IndexTTS(device=str(device))
    
    yield  # Server runs
    
    # Shutdown: cleanup
    if model:
        del model
        torch.cuda.empty_cache()

app = FastAPI(lifespan=lifespan)
```

This replaces the deprecated `@app.on_event("startup")` decorator.

### CUDA Support

The Dockerfile installs PyTorch with CUDA 12.1 support:

```dockerfile
RUN pip install --no-cache-dir torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu121
```

To use CPU instead, edit `docker-compose-index-tts.yml`:

```yaml
environment:
  - DEVICE=cpu
```

### Index TTS Installation

The container clones and installs Index TTS from GitHub:

```dockerfile
RUN git clone https://github.com/index-tts/index-tts.git /tmp/index-tts && \
    cd /tmp/index-tts && \
    pip install --no-cache-dir -e . && \
    cd /app
```

### Model Loading

Models are loaded on container startup using the lifespan handler:

```python
from index.tts import IndexTTS

model = IndexTTS(device=str(device))
```

If model loading fails, the server runs in **placeholder mode**:

- Returns test audio (440Hz beep)
- All endpoints remain functional
- Health check reports "degraded" status

Models are stored in the Docker volume `index-tts-models` and cached in `index-tts-cache`.

## API Endpoints

### Generate Speech

```bash
curl -X POST http://localhost:5124/v1/audio/speech \
  -H "Content-Type: application/json" \
  -d '{
    "input": "Hello world",
    "voice": "af_bella",
    "speed": 1.0,
    "temperature": 0.7,
    "emotions": {
      "happy": 0.8,
      "surprise": 0.3
    }
  }' \
  --output speech.wav
```

### List Voices

```bash
curl http://localhost:5124/voices
```

### List Emotions

```bash
curl http://localhost:5124/emotions
```

### Health Check

```bash
curl http://localhost:5124/health
```

## Troubleshooting

### Container fails to start

Check logs:

```bash
index-tts-helper.bat logs
```

### CUDA not available

Ensure:

1. NVIDIA drivers are installed
2. Docker has GPU support configured
3. NVIDIA Container Toolkit is installed

Verify GPU access:

```bash
docker run --rm --gpus all nvidia/cuda:12.1.0-base-ubuntu22.04 nvidia-smi
```

### Model loading fails

Check if models are downloaded:

```bash
docker exec index-tts ls -la /models
```

### Synthesis errors

Enable debug logging by editing `server.py`:

```python
logging.basicConfig(level=logging.DEBUG)
```

## Resource Requirements

### Minimum

- GPU: NVIDIA GPU with CUDA support
- RAM: 4GB
- Disk: 5GB (for models)

### Recommended

- GPU: NVIDIA RTX series
- RAM: 8GB+
- Disk: 10GB+

## Performance

Typical generation times (RTX 3080):

- Short text (10 words): ~0.5-1 second
- Medium text (50 words): ~2-3 seconds
- Long text (100+ words): ~5-10 seconds

## Environment Variables

| Variable   | Default | Description              |
|------------|---------|--------------------------|
| MODEL_PATH | /models | Path to store models     |
| CACHE_PATH | /cache  | Path for audio cache     |
| HOST       | 0.0.0.0 | Server bind address      |
| PORT       | 5000    | Server port (internal)   |
| DEVICE     | cuda    | Device to use (cuda/cpu) |

## Volume Mounts

- `index-tts-models:/models` - Persistent model storage
- `index-tts-cache:/cache` - Persistent audio cache
- `./docker/index-tts/server.py:/app/server.py:rw` - **Live server code** (editable without rebuild)

## Updates

To update Index TTS to the latest version:

```bash
# Rebuild with no cache
index-tts-helper.bat build

# Or manually
docker-compose -f docker-compose-index-tts.yml build --no-cache
```

## Notes

- First startup may be slow as models are downloaded
- GPU is highly recommended for real-time performance
- The container requires ~4-8GB GPU memory depending on models
- Models are cached to persist across container restarts

