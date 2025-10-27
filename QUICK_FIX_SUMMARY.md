# Quick Fix Summary - Index TTS Container

## ✅ Root Cause Found & Fixed!

The error `No module named 'index'` was because:

- Package is named **`indextts`** not `index`
- Need to import from **`indextts.infer_v2`**
- Need to use **`IndexTTS2`** class (version 2)

---

## 🔧 Changes Made

### 1. server.py - Line ~70

```python
# BEFORE ❌
from index.tts import IndexTTS

# AFTER ✅
from indextts.infer_v2 import IndexTTS2
```

### 2. server.py - Line ~76

```python
# BEFORE ❌
model = IndexTTS(device=str(device))

# AFTER ✅
model = IndexTTS2(
    cfg_path=config_path,
    model_dir=model_dir,
    device=str(device),
    use_fp16=(device_name == "cuda")
)
```

### 3. server.py - Line ~190

```python
# BEFORE ❌
model.synthesize(...)

# AFTER ✅
model.infer(
    spk_audio_prompt=spk_audio_prompt,
    text=request.input,
    output_path=output_path,
    emo_vector=emo_vector,
    use_random=False,
    verbose=True
)
```

### 4. Dockerfile - Line ~15

```dockerfile
# BEFORE ❌
RUN pip install --no-cache-dir torch torchvision torchaudio

# AFTER ✅ (Fixed version compatibility)
RUN pip install --no-cache-dir \
    torch==2.1.2 \
    torchvision==0.16.2 \
    torchaudio==2.1.2 \
    --index-url https://download.pytorch.org/whl/cu121
```

### 5. Dockerfile - Added model download

```dockerfile
# NEW ✅ (After line 28)
RUN pip install --no-cache-dir huggingface-hub && \
    python -c "from huggingface_hub import snapshot_download; \
    snapshot_download(repo_id='IndexTeam/IndexTTS-2', local_dir='/models', local_dir_use_symlinks=False)" && \
    echo "Models downloaded successfully"
```

---

## ⚡ Action Required

Since Dockerfile changed, you **MUST rebuild**:

```cmd
index-tts-helper.bat build
```

**Time estimate:** 10-20 minutes (downloading 3-5GB of models)

---

## ✅ After Rebuild - Expected Logs

```
INFO:__main__:Starting Index TTS server...
INFO:__main__:Using device: cuda
INFO:__main__:Loading Index TTS v2 model...
INFO:__main__:Using config: /models/config.yaml
INFO:__main__:Using model directory: /models
>> GPT weights restored from: /models/gpt.pth
INFO:__main__:Index TTS v2 model loaded successfully! ✅
INFO:     Application startup complete.
```

**No more:**

- ❌ "No module named 'index'"
- ❌ "on_event is deprecated"
- ❌ "Using placeholder mode"

---

## 📁 Files Changed

- ✅ `docker/index-tts/server.py` - Fixed imports and API calls
- ✅ `docker/index-tts/Dockerfile` - Fixed versions and added model download
- ✅ `docker-compose-index-tts.yml` - Already had server.py mount

---

## 🎯 Ready to Build?

```cmd
cd E:\github\project-hub
index-tts-helper.bat build
```

Watch for:

1. PyTorch installation
2. Index TTS clone
3. **Model download** (this takes the longest)
4. Build complete

Then:

```cmd
index-tts-helper.bat start
index-tts-helper.bat logs
```

🎉 **Your Index TTS v2 container will be ready!**

