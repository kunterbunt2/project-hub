# 🔍 Index TTS Container - Connection Reset Issue

## ❓ Why "Connection Reset"?

When you get "The connection was reset" error, it means:

**The container is running BUT the server hasn't started listening on port 5000 yet.**

## 🎯 Root Cause

Looking at your logs:

```
INFO:__main__:Loading Index TTS v2 model...
INFO:accelerate.utils.modeling:We will use 90% of the memory on device 0...
```

**The model is still loading into GPU memory!**

The FastAPI server only starts AFTER the model finishes loading. This can take **2-5 minutes** depending on your GPU.

## ⏰ Current Status: Model Loading

The container is:

- ✅ Running
- ✅ Loading model into GPU
- ⏳ **Not yet ready to accept connections**

**Expected sequence:**

1. ✅ Container starts
2. ✅ Models found on disk (skip download)
3. ⏳ **Loading models to GPU (YOU ARE HERE)** ← Takes 2-5 min
4. ⏳ Initialize FastAPI server
5. ⏳ Start listening on port 5000
6. ✅ Ready to accept requests!

## 📊 What's Happening Now

The logs show:

```
INFO:accelerate.utils.modeling:We will use 90% of the memory on device 0...
```

This means:

- GPU memory is being allocated
- Large model files (gpt.pth ~1-2GB, s2mel.pth ~500MB) are being loaded
- This is **CPU/GPU intensive** and takes time

## 👀 How to Monitor Progress

### Option 1: Watch Logs Continuously

```cmd
docker logs index-tts -f
```

Press `Ctrl+C` to exit when done.

**Watch for these lines:**

```
>> GPT weights restored from: /opt/index-tts/checkpoints/gpt.pth
>> S2mel weights restored from: /opt/index-tts/checkpoints/s2mel.pth
>> Vocoder loaded
INFO:__main__:Index TTS v2 model loaded successfully!
INFO:     Application startup complete.
INFO:     Uvicorn running on http://0.0.0.0:5000  ← Server is NOW ready!
```

### Option 2: Check Latest Logs Periodically

```cmd
docker logs index-tts --tail 20
```

Run this every 30 seconds to see progress.

### Option 3: Check in Docker Desktop

1. Open Docker Desktop
2. Click on "index-tts" container
3. Go to "Logs" tab
4. Watch for "Application startup complete"

## ✅ When Model Loading Completes

You'll see in logs:

```
INFO:__main__:Index TTS v2 model loaded successfully!
INFO:     Application startup complete.
INFO:     Uvicorn running on http://0.0.0.0:5000
```

**THEN the server is ready!**

Now try:

```cmd
curl http://localhost:5124/health
```

Or open in browser:

```
http://localhost:5124/health
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

## ⏱️ How Long Does It Take?

**Model loading time depends on your GPU:**

| GPU      | VRAM     | Load Time |
|----------|----------|-----------|
| RTX 3060 | 12 GB    | ~3-4 min  |
| RTX 3070 | 8 GB     | ~2-3 min  |
| RTX 3080 | 10-12 GB | ~2-3 min  |
| RTX 3090 | 24 GB    | ~1-2 min  |
| RTX 4070 | 12 GB    | ~2-3 min  |
| RTX 4080 | 16 GB    | ~1-2 min  |
| RTX 4090 | 24 GB    | ~1-2 min  |

**First startup is slowest** because:

- Loading ~5 GB of models from disk to RAM
- Transferring models from RAM to GPU VRAM
- Initializing CUDA kernels
- Compiling model operations

**Subsequent restarts** may be faster due to caching.

## 🚨 If It Takes Too Long (>10 minutes)

If the model is still loading after 10 minutes, check for issues:

### 1. Check if process is stuck

```cmd
docker exec index-tts ps aux
```

Should show Python process running.

### 2. Check GPU is accessible

```cmd
docker exec index-tts nvidia-smi
```

Should show your GPU with memory usage.

### 3. Check for errors in logs

```cmd
docker logs index-tts | findstr ERROR
```

Should be empty or only show warnings (not errors).

### 4. Restart container

If truly stuck:

```cmd
docker restart index-tts
docker logs index-tts -f
```

## 💡 Why This Happens

Index TTS v2 is a **large neural network**:

- GPT model: ~1-2 GB
- S2mel model: ~500 MB
- Vocoder: ~100-200 MB
- Qwen emotion model: ~1 GB
- Total: ~3-4 GB in GPU memory

Loading this much data takes time!

## ✅ Summary

| Issue            | Explanation                        | Solution                                   |
|------------------|------------------------------------|--------------------------------------------|
| Connection reset | Server not started yet             | **Wait for model loading**                 |
| Why so long?     | Loading 3-4 GB to GPU              | Normal for first startup                   |
| How to check?    | Watch logs                         | `docker logs index-tts -f`                 |
| When ready?      | See "Application startup complete" | **Then test** http://localhost:5124/health |
| How long?        | 2-5 minutes typically              | Be patient! ☕                              |

## 🎯 Action Items

**Right now:**

1. **Wait** - The model is loading (be patient!)

2. **Watch logs:**
   ```cmd
   docker logs index-tts -f
   ```

3. **Look for:**
   ```
   INFO:__main__:Index TTS v2 model loaded successfully!
   INFO:     Application startup complete.
   ```

4. **Then test:**
   ```
   http://localhost:5124/health
   ```

**Estimated time remaining:** 1-4 minutes

---

## 📞 When It's Ready

Once you see "Application startup complete", let me know and we'll test generating emotional speech! 🎤

The container is working correctly - it just needs time to load the large models into GPU memory. ⏰

