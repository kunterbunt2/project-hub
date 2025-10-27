# ✅ Index TTS with Persistent Model Storage

## 🎯 Models Now Persist Between Rebuilds!

The container now uses a **Docker volume** to store models persistently. This means:

- ✅ **First build:** Downloads models (~3-5GB) - takes 15-25 minutes
- ✅ **Subsequent rebuilds:** Uses existing models - takes 2-3 minutes!
- ✅ **Models survive container/image deletion**
- ✅ **Can reuse models across different projects**

---

## 📁 New Storage Architecture

```
Docker Volume (Persistent):
index-tts-models:/opt/index-tts/checkpoints
├── config.yaml              # ← Persisted in volume
├── gpt.pth (~XXX MB)        # ← Persisted in volume
├── s2mel.pth (~XXX MB)      # ← Persisted in volume
├── qwen0.6bemo4-merge/      # ← Persisted in volume
└── ... (other models)       # ← Persisted in volume

Container (Rebuilt each time):
/opt/index-tts/
├── indextts/                # Python package
├── examples/                # Example files
└── checkpoints/             # ← MOUNTED from volume above!

/app/
├── server.py                # ← Mounted from host (live editable)
└── requirements.txt
```

---

## 🚀 How It Works

### First Build (Models Downloaded)

```cmd
index-tts-helper.bat build
```

**What happens:**

1. Builds container (~2 min)
2. Tries to download models during build (~15-20 min)
3. Stores models in **volume** `index-tts-models`
4. ✅ Models persist even if container is deleted!

### Subsequent Rebuilds (Models Reused!)

```cmd
index-tts-helper.bat build
```

**What happens:**

1. Rebuilds container (~2 min)
2. Checks mounted volume for models
3. **Finds existing models** ✅
4. Skips download!
5. Ready in 2-3 minutes! 🚀

### First Container Start

If models weren't downloaded during build (or build was interrupted):

```cmd
index-tts-helper.bat start
index-tts-helper.bat logs
```

**What happens:**

```
INFO:__main__:Starting Index TTS server...
INFO:__main__:Model path: /opt/index-tts/checkpoints
WARNING:__main__:Models not found, downloading from HuggingFace...
INFO:__main__:This is a one-time download (~3-5GB), please be patient...
[Downloading progress...]
INFO:__main__:Models downloaded successfully!
INFO:__main__:Loading Index TTS v2 model...
```

**Next starts:** Models already exist, instant startup! ⚡

---

## 🔧 Volume Configuration

### docker-compose-index-tts.yml

```yaml
services:
  index-tts:
    volumes:
      # Models persisted in Docker volume
      - index-tts-models:/opt/index-tts/checkpoints

      # Cache for HuggingFace
      - index-tts-cache:/cache

      # Server code mounted for live editing
      - ./docker/index-tts/server.py:/app/server.py:rw

    environment:
      - MODEL_PATH=/opt/index-tts/checkpoints  # Points to volume
      - HF_ENDPOINT=https://hf-mirror.com      # Fast mirror

volumes:
  index-tts-models: # ← Persistent model storage
    driver: local
  index-tts-cache: # ← HuggingFace cache
    driver: local
```

---

## 💡 Smart Model Download Logic

The container is now intelligent about model downloads:

### During Build (Dockerfile)

```dockerfile
# Try to download models during build
RUN hf download IndexTeam/IndexTTS-2 --local-dir=/opt/index-tts/checkpoints --resume-download || \
    echo "Model download skipped - will use models from mounted volume or download at runtime"
```

- If build succeeds → Models in volume ✅
- If build interrupted → Will download at runtime ✅
- If volume has models → Reuses them ✅

### During Startup (server.py)

```python
# Check if models exist
if not os.path.exists(config_path) or not os.path.exists(gpt_model_path):
    logger.warning("Models not found, downloading from HuggingFace...")
    # Downloads using hf CLI with mirror
    # Saves to mounted volume
    # Only happens once!
else:
    logger.info("Models found in mounted volume, skipping download!")
```

---

## 📊 Comparison: Before vs After

| Scenario          | ❌ Before         | ✅ After                        |
|-------------------|------------------|--------------------------------|
| First build       | 15-25 min        | 15-25 min (one-time)           |
| Rebuild container | 15-25 min        | **2-3 min** 🚀                 |
| Delete container  | Models lost      | **Models kept** ✅              |
| Delete image      | Models lost      | **Models kept** ✅              |
| Disk usage        | In image (~10GB) | In volume (~5GB)               |
| Share models      | No               | **Yes** (volume can be shared) |

---

## 🎯 Common Workflows

### Workflow 1: Clean Start (First Time)

```cmd
# Build and download models
index-tts-helper.bat build

# Start container
index-tts-helper.bat start

# Check logs
index-tts-helper.bat logs
```

**Expected:** Models download during build, stored in volume.

---

### Workflow 2: Update Code (Rebuild)

```cmd
# Make changes to Dockerfile or requirements.txt
# Rebuild - fast because models already in volume!
index-tts-helper.bat build

# Restart
index-tts-helper.bat restart
```

**Time:** 2-3 minutes (models reused from volume!)

---

### Workflow 3: Edit Server Logic (No Rebuild!)

```cmd
# Edit server.py
notepad E:\github\project-hub\docker\index-tts\server.py

# Just restart (5 seconds)
index-tts-helper.bat restart
```

**Time:** 5 seconds (no rebuild, no model reload!)

---

### Workflow 4: Complete Reset

```cmd
# Stop container
index-tts-helper.bat stop

# Remove container and image
docker rm index-tts
docker rmi project-hub-index-tts

# Models still safe in volume!
docker volume ls | findstr index-tts-models

# Rebuild - will reuse models from volume
index-tts-helper.bat build
```

**Time:** 2-3 minutes (models reused!)

---

## 🗑️ Managing Model Storage

### Browse Models in Windows Explorer

Simply open:

```
E:\github\project-hub\docker\index-tts\checkpoints\
```

You'll see all model files directly! 📁

### Check Model Size

```cmd
# Windows Explorer: Right-click folder → Properties

# Or use PowerShell:
Get-ChildItem E:\github\project-hub\docker\index-tts\checkpoints -Recurse | Measure-Object -Property Length -Sum
```

### Backup Models

```cmd
# Simple copy!
xcopy E:\github\project-hub\docker\index-tts\checkpoints E:\backups\index-tts-models\ /E /I /Y

# Or use Windows Explorer - just copy the folder!
```

### Restore Models

```cmd
# Simple paste!
xcopy E:\backups\index-tts-models\* E:\github\project-hub\docker\index-tts\checkpoints\ /E /I /Y
```

### Delete Models (Clean Slate)

```cmd
# Stop container first
index-tts-helper.bat stop

# Delete models (Windows)
rmdir /s /q E:\github\project-hub\docker\index-tts\checkpoints
mkdir E:\github\project-hub\docker\index-tts\checkpoints

# Or use Windows Explorer - just delete files in the folder

# Next build will download fresh models
index-tts-helper.bat build
```

### Share Models Between Projects

Just copy the checkpoints folder or create a symbolic link:

```cmd
# Create symlink to share models
mklink /D E:\other-project\checkpoints E:\github\project-hub\docker\index-tts\checkpoints
```

---

## 🔍 Verify Models on Host System

### Check if models are present:

```cmd
# List files
dir /s E:\github\project-hub\docker\index-tts\checkpoints

# Or open in Windows Explorer
explorer E:\github\project-hub\docker\index-tts\checkpoints
```

**Expected files:**

```
checkpoints\
├── README.md
├── config.yaml (3 KB)
├── gpt.pth (1-2 GB)
├── s2mel.pth (500 MB - 1 GB)
├── qwen0.6bemo4-merge\ (directory)
├── bpe.model
├── pinyin.vocab
└── ... (more files)
```

### Check model size:

```cmd
# PowerShell
Get-ChildItem E:\github\project-hub\docker\index-tts\checkpoints -Recurse | Measure-Object -Property Length -Sum | Select-Object @{Name="Size(GB)";Expression={[math]::Round($_.Sum / 1GB, 2)}}
```

Should show ~3-5GB total.

---

## 🚨 Troubleshooting

### Issue: "Models not found" on every startup

**Cause:** Host directory empty or models failed to download.

**Solution:**

```cmd
# Check if directory exists and has files
dir E:\github\project-hub\docker\index-tts\checkpoints

# Should see .pth, .pt, .yaml files
# If empty, manually trigger download
index-tts-helper.bat start
index-tts-helper.bat logs
# Wait for "Models downloaded successfully!"
```

### Issue: Build still takes 15+ minutes

**Cause:** Models not in host directory yet, downloading during build.

**Solution:** This is normal for first build! Subsequent builds will be fast.

### Issue: Want to force re-download models

**Solution:**

```cmd
# Stop container
index-tts-helper.bat stop

# Delete old models from host directory
rmdir /s /q E:\github\project-hub\docker\index-tts\checkpoints
mkdir E:\github\project-hub\docker\index-tts\checkpoints

# Rebuild - will download fresh
index-tts-helper.bat build
```

### Issue: Permission errors accessing checkpoints

**Solution:**

Windows might have file permission issues. Make sure:

```cmd
# Check folder permissions
icacls E:\github\project-hub\docker\index-tts\checkpoints

# If needed, grant full control to your user
icacls E:\github\project-hub\docker\index-tts\checkpoints /grant %USERNAME%:F /T
```

---

## 📝 Summary

### What Changed

| Component       | Old Behavior           | New Behavior                           |
|-----------------|------------------------|----------------------------------------|
| **Models**      | In Docker image        | **On host directory** ✅                |
| **Rebuilds**    | Re-download every time | **Reuse from host folder** ✅           |
| **Persistence** | Lost on image delete   | **Always on your disk** ✅              |
| **Access**      | Docker commands only   | **Windows Explorer** ✅                 |
| **Startup**     | No check               | **Auto-download if missing** ✅         |
| **Build time**  | Always 15-25 min       | **First: 15-25 min, After: 2-3 min** ✅ |

### Benefits

- 🚀 **Much faster rebuilds** (2-3 min vs 15-25 min)
- 💾 **Models on your filesystem** - browse with Windows Explorer
- 🗂️ **Easy backup/restore** - just copy the folder
- 🔄 **Smart download** - only downloads if needed
- 📦 **Smaller images** - models not baked into image
- 🎯 **Flexible** - share across projects using symlinks
- 🖥️ **No Docker commands** - manage files directly

---

## 🎯 Quick Start

### First Time:

```cmd
cd E:\github\project-hub
index-tts-helper.bat build   # Downloads models (15-25 min)
index-tts-helper.bat start
index-tts-helper.bat logs
```

### After Updates:

```cmd
index-tts-helper.bat build   # Fast! Uses existing models (2-3 min)
index-tts-helper.bat restart
```

### Edit Server Code:

```cmd
notepad docker\index-tts\server.py
index-tts-helper.bat restart  # Super fast! (5 sec)
```

---

## ✅ Ready to Build!

Models will now be stored in `E:\github\project-hub\docker\index-tts\checkpoints\` on your host system. After the first
build, all rebuilds will be much faster! 🚀

You can browse the models anytime in Windows Explorer!

```cmd
index-tts-helper.bat build
```

