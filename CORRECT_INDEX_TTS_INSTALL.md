# Index TTS Dockerfile - Following Official Instructions

## The Correct Approach

After reviewing the Index TTS README, they specifically recommend:

```bash
pip install -U uv
uv sync --all-extras
```

**They insist UV should handle ALL dependencies (including PyTorch) for compatibility reasons.**

## What We Changed

### ❌ Previous Approach (Wrong)

```dockerfile
# Pre-install PyTorch
RUN pip install torch==2.1.2 torchvision==0.16.2 torchaudio==2.1.2 \
    --index-url https://download.pytorch.org/whl/cu121

# Then run UV
RUN uv sync --all-extras
```

**Problem:** Ignores Index TTS's tested dependency resolution

### ✅ Current Approach (Correct)

```dockerfile
# Let UV handle EVERYTHING as Index TTS recommends
RUN pip install -U uv && \
    uv sync --all-extras
```

**Result:** Uses Index TTS's tested and verified dependency versions

## Why Index TTS Insists on UV

From their README, Index TTS uses UV because:

1. **Tested compatibility** - They've verified the exact dependency versions that work
2. **Reproducible builds** - Lock files ensure consistent installations
3. **Handles complex dependencies** - PyTorch, torchvision, CUDA libraries all compatible
4. **Faster than pip** - Parallel downloads and caching

## How UV Installs PyTorch

When UV installs PyTorch, it:

1. Reads Index TTS's `pyproject.toml` and lock files
2. Installs the specific PyTorch version they tested
3. Installs compatible torchvision, torchaudio versions
4. **Handles CUDA automatically** based on your system

The Index TTS team has already figured out the correct PyTorch/CUDA combination!

## UV's Virtual Environment

`uv sync` creates a virtual environment at `.venv/` by default. Our Dockerfile:

1. **Creates the venv**: `uv sync --all-extras`
2. **Installs server deps to venv**: `uv pip install -r requirements.txt`
3. **Uses venv Python**: `CMD ["/opt/index-tts/.venv/bin/python", "/app/server.py"]`

This ensures all dependencies (Index TTS + server) are in one environment.

## Dockerfile Structure

```dockerfile
# 1. Base image
FROM python:3.11-slim

# 2. Install system dependencies
RUN apt-get update && apt-get install -y git git-lfs build-essential libsndfile1 curl wget

# 3. Clone Index TTS
RUN git clone https://github.com/index-tts/index-tts.git /opt/index-tts
RUN cd /opt/index-tts && git lfs pull

# 4. Install Index TTS with UV (handles PyTorch + all deps)
WORKDIR /opt/index-tts
RUN pip install -U uv && uv sync --all-extras

# 5. Install server dependencies to same venv
RUN uv pip install -r /app/requirements.txt

# 6. Run using venv Python
CMD ["/opt/index-tts/.venv/bin/python", "/app/server.py"]
```

## Build Steps

```cmd
# Stop old container
.\index-tts-helper.bat stop

# Build with corrected Dockerfile
.\index-tts-helper.bat build

# Start container
.\index-tts-helper.bat start

# Check logs
.\index-tts-helper.bat logs
```

## Expected Build Time

```
[1/11] Base image pull: 30s
[2/11] Install system deps: 60s
[3/11] Clone Index TTS: 60s
[4/11] UV sync (PyTorch + all deps): 5-8 minutes ← This is the long step
[5/11] Install server deps: 30s
[6/11] Download models (optional): 2-3 minutes
Total: ~10-15 minutes (first build)
```

Yes, it's slow, but it's **correct** and follows Index TTS's tested approach.

## Verification

After build completes:

```bash
# Check PyTorch version (whatever Index TTS tested with)
docker exec index-tts /opt/index-tts/.venv/bin/python -c "import torch; print(torch.__version__)"

# Check CUDA availability
docker exec index-tts /opt/index-tts/.venv/bin/python -c "import torch; print(torch.cuda.is_available())"

# Test server
curl http://localhost:5124/health
```

## Why This Works

1. ✅ **Follows official instructions** - No customization, uses their tested approach
2. ✅ **Compatible dependencies** - Index TTS team verified these versions work together
3. ✅ **No version conflicts** - UV's lock files ensure reproducibility
4. ✅ **CUDA support** - Index TTS's pyproject.toml specifies CUDA PyTorch
5. ✅ **No torchvision::nms errors** - They've tested the torch/torchvision combination

## Key Takeaways

- 🚫 **Don't pre-install PyTorch** - Let UV handle it
- 🚫 **Don't use NVIDIA base images** - Causes version conflicts
- ✅ **Trust Index TTS's dependency management** - They know what works
- ✅ **Use UV's virtual environment** - Keeps everything isolated and consistent

## What About Build Time?

Yes, it's slower than pre-installing PyTorch. But:

- **First build**: 10-15 minutes (one-time cost)
- **Subsequent builds**: 2-3 minutes (Docker caching)
- **Result**: Working container with correct dependencies

The Index TTS team chose UV for a reason - trust their process! 🎯

