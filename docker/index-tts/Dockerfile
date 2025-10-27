FROM python:3.11-slim

# Install system dependencies including git-lfs
RUN apt-get update && apt-get install -y \
    git \
    git-lfs \
    build-essential \
    libsndfile1 \
    curl \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Enable Git LFS globally
RUN git lfs install

WORKDIR /app

# Clone Index TTS repository with LFS support (as per Index TTS README)
RUN git clone https://github.com/index-tts/index-tts.git /opt/index-tts && \
    cd /opt/index-tts && \
    git lfs pull

# Install Index TTS following their exact instructions
# They insist UV should handle ALL dependencies including PyTorch for compatibility
WORKDIR /opt/index-tts
RUN pip install -U uv && \
    uv sync --all-extras

# Download models from HuggingFace using mirror for faster download
# Using HF mirror as recommended in Index TTS documentation
ENV HF_ENDPOINT=https://hf-mirror.com

# Install huggingface CLI tools (model download can happen at build or runtime)
RUN uv tool install "huggingface-hub[cli,hf_xet]"

# Create checkpoints directory (will be mounted from volume at runtime)
RUN mkdir -p /opt/index-tts/checkpoints

# Create voices directory for voice reference uploads
RUN mkdir -p /opt/index-tts/voices

# Optional: Download models during build (comment out to download at runtime instead)
# This will be skipped if models are already in the mounted volume
RUN PATH="$HOME/.local/bin:$PATH" hf download IndexTeam/IndexTTS-2 --local-dir=/opt/index-tts/checkpoints --resume-download || \
    echo "Model download skipped - will use models from mounted volume or download at runtime"

# Verify GPU setup (will log CPU if no GPU available during build)
RUN uv run tools/gpu_check.py || echo "GPU check completed (may show CPU during build)"

# Install server dependencies into the UV virtual environment
WORKDIR /app
COPY requirements.txt .
RUN cd /opt/index-tts && uv pip install -r /app/requirements.txt

# Copy server code
COPY server.py .

# Create cache directory
RUN mkdir -p /cache

# Set Python path and use UV's virtual environment
ENV PYTHONPATH="/opt/index-tts:${PYTHONPATH}"
ENV HF_HUB_CACHE=/cache
ENV PATH="/opt/index-tts/.venv/bin:${PATH}"
ENV VIRTUAL_ENV="/opt/index-tts/.venv"

EXPOSE 5000

# Health check with longer start period for model loading
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=120s \
    CMD curl -f http://localhost:5000/health || exit 1

# Run server using UV's virtual environment Python
CMD ["/opt/index-tts/.venv/bin/python", "/app/server.py"]

