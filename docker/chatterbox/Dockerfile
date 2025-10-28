FROM python:3.11-slim-bullseye

# Install system dependencies
RUN apt-get update && apt-get install -y \
    git \
    build-essential \
    libsndfile1 \
    ffmpeg \
    curl \
    wget \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Install numpy first (required by spacy-pkuseg at build time)
RUN pip install --no-cache-dir "numpy>=1.24.0,<1.26.0"

# Install chatterbox-tts and dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy server code
COPY server.py .

# Create cache directory for models
RUN mkdir -p /cache/models

# Set environment variables
ENV PYTHONUNBUFFERED=1
ENV HF_HOME=/cache/models
ENV TRANSFORMERS_CACHE=/cache/models
ENV HF_HUB_CACHE=/cache/models

EXPOSE 4123

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=120s \
    CMD curl -f http://localhost:4123/health || exit 1

# Run server
CMD ["python", "server.py"]

