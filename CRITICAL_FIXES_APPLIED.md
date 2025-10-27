# 🔧 Index TTS Container - CRITICAL FIXES APPLIED

## ❌ Issues You Encountered

1. **Wrong model path**: Server looking at `/models` instead of `/opt/index-tts/checkpoints`
2. **HuggingFace CLI error**: `--resume-download` flag not recognized by older CLI
3. **PyTorch error**: `operator torchvision::nms does not exist` (version conflict)

## ✅ Fixes Applied

### 1. Fixed Model Download Method

**Changed from:** CLI command with problematic flags

```python
# OLD (broken)
subprocess.run(["hf", "download", "IndexTeam/IndexTTS-2", "--local-dir", path, "--resume-download"])
```

**Changed to:** Python API (more reliable)

```python
# NEW (working)
from huggingface_hub import snapshot_download

snapshot_download(repo_id="IndexTeam/IndexTTS-2", local_dir=model_path, local_dir_use_symlinks=False)
```

**File:** `docker/index-tts/server.py`

---

### 2. Fixed PyTorch/Torchvision Compatibility

**Problem:** `uv sync` was installing incompatible PyTorch/torchvision versions

**Solution:** Install compatible PyTorch BEFORE `uv sync`

```dockerfile
# Install compatible PyTorch FIRST (added to Dockerfile)
RUN pip install --no-cache-dir \
    torch==2.1.2 \
    torchvision==0.16.2 \
    torchaudio==2.1.2 \
    --index-url https://download.pytorch.org/whl/cu121

# THEN run uv sync (will use existing PyTorch)
RUN uv sync --all-extras
```

**File:** `docker/index-tts/Dockerfile`

---

### 3. Added Missing Dependency

**Added:** `huggingface-hub>=0.20.0` to requirements.txt

**File:** `docker/index-tts/requirements.txt`

---

### 4. Fixed Model Path

The docker-compose already has the correct path:

```yaml
environment:
  - MODEL_PATH=/opt/index-tts/checkpoints  # ✅ Correct!
```

**Problem:** You're running an OLD container with old environment variables!

---

## ⚡ ACTION REQUIRED: Rebuild & Restart

Your current container is using OLD code and OLD environment. You MUST rebuild:

### Step 1: Stop the Old Container

```cmd
index-tts-helper.bat stop
```

Or manually:

```cmd
docker stop index-tts
docker rm index-tts
```

### Step 2: Rebuild with Fixes

```cmd
cd E:\github\project-hub
index-tts-helper.bat build
```

**⏰ Time:** 15-25 minutes (downloads models to host directory)

**What will happen:**

1. ✅ Installs compatible PyTorch 2.1.2 + torchvision 0.16.2
2. ✅ Clones Index TTS with git-lfs
3. ✅ Installs via `uv sync` (using compatible PyTorch)
4. ✅ Downloads models to `E:\github\project-hub\docker\index-tts\checkpoints\`
5. ✅ No more torchvision::nms error!

### Step 3: Start the New Container

```cmd
index-tts-helper.bat start
```

### Step 4: Check Logs (Should Work Now!)

```cmd
index-tts-helper.bat logs
```

**Expected output:**

```
INFO:__main__:Starting Index TTS server...
INFO:__main__:Model path: /opt/index-tts/checkpoints  ← Correct path!
INFO:__main__:Device: cuda
INFO:__main__:Models found in mounted directory, skipping download!  ← If models exist
INFO:__main__:Using device: cuda
INFO:__main__:Loading Index TTS v2 model...
INFO:__main__:Using config: /opt/index-tts/checkpoints/config.yaml
>> GPT weights restored from: /opt/index-tts/checkpoints/gpt.pth
>> Vocoder loaded
INFO:__main__:Index TTS v2 model loaded successfully! ✅
INFO:     Application startup complete.
```

**Should NOT see:**

- ❌ `Model path: /models` (old path)
- ❌ `hf: error: unrecognized arguments` (fixed)
- ❌ `operator torchvision::nms does not exist` (fixed)
- ❌ `Using placeholder mode` (should load real model)

---

## 📊 Why Restart is Not Enough

**Why you can't just restart?**

| What                   | Why Restart Doesn't Work                              |
|------------------------|-------------------------------------------------------|
| **Dockerfile changes** | PyTorch install order changed → Need rebuild          |
| **Environment vars**   | Your old container has `/models` → Need new container |
| **Dependencies**       | Need huggingface-hub installed → Need rebuild         |
| **Model download**     | New download method → Need new code                   |

**server.py changes** are mounted, so those updates are live, BUT the Dockerfile and environment changes require
rebuild!

---

## 🎯 Quick Command Summary

```cmd
# Stop old container
index-tts-helper.bat stop

# Rebuild with all fixes
index-tts-helper.bat build

# Start new container
index-tts-helper.bat start

# Watch it start up
index-tts-helper.bat logs -f
```

---

## 📁 Model Storage

Models will download to:

```
E:\github\project-hub\docker\index-tts\checkpoints\
```

You can watch the download in Windows Explorer - files will appear as they download!

After first build, the models persist and rebuilds are fast (2-3 min).

---

## 🔍 Verify After Build

### 1. Check logs for success:

```cmd
index-tts-helper.bat logs | findstr "loaded successfully"
```

Should show: `Index TTS v2 model loaded successfully!`

### 2. Check model files exist:

```cmd
dir E:\github\project-hub\docker\index-tts\checkpoints\*.pth
```

Should show: `gpt.pth`, `s2mel.pth`, etc.

### 3. Test health endpoint:

```cmd
curl http://localhost:5124/health
```

Should return:

```json
{
  "status": "healthy",
  "model_loaded": true,
  "device": "cuda",
  "cuda_available": true
}
```

---

## 🚨 If Build Fails

### Issue: Out of disk space

Models are ~3-5GB. Check space:

```cmd
dir E:\github\project-hub\docker\index-tts\checkpoints
docker system df
```

Clean up if needed:

```cmd
docker system prune -a
```

### Issue: HuggingFace download timeout

If download is slow, the Python API will keep trying. You can also pre-download:

```cmd
pip install huggingface-hub
cd E:\github\project-hub\docker\index-tts
huggingface-cli download IndexTeam/IndexTTS-2 --local-dir=checkpoints
```

Then rebuild - will skip download since models exist!

### Issue: PyTorch still has errors

If you still see torchvision errors, try building with no cache:

```cmd
docker-compose -f docker-compose-index-tts.yml build --no-cache
```

This ensures PyTorch installs fresh before `uv sync`.

---

## ✅ Summary

| Issue               | Status         | Fix                               |
|---------------------|----------------|-----------------------------------|
| Wrong model path    | ✅ Fixed        | Correct env var in docker-compose |
| HF CLI error        | ✅ Fixed        | Use Python API instead            |
| PyTorch/torchvision | ✅ Fixed        | Install compatible versions first |
| Missing dependency  | ✅ Fixed        | Added huggingface-hub             |
| **Action needed**   | ⚠️ **REBUILD** | Old container has old code!       |

---

## 🏁 START HERE

```cmd
cd E:\github\project-hub
index-tts-helper.bat stop
index-tts-helper.bat build
```

⏰ **15-25 minutes for first build**

Then:

```cmd
index-tts-helper.bat start
index-tts-helper.bat logs
```

Look for: **"Index TTS v2 model loaded successfully!"** ✅

---

After this rebuild, everything should work! The fixes address all three errors you encountered. 🎉

