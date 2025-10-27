# ✅ Models Now on Host Directory!

## 🎯 What Changed

Models are now stored on your **host filesystem** instead of a Docker volume:

```
E:\github\project-hub\docker\index-tts\checkpoints\
```

## 📁 What You Get

### Before (Docker Volume):

- ❌ Models hidden in Docker volume
- ❌ Need Docker commands to access
- ❌ Hard to browse/backup

### After (Host Directory):

- ✅ **Direct access** in Windows Explorer
- ✅ **Easy backup** - just copy the folder
- ✅ **Simple cleanup** - delete files directly
- ✅ **Visible** - see exactly what's downloaded
- ✅ **Shareable** - use symlinks across projects

## 🗂️ Directory Structure

```
E:\github\project-hub\
└── docker\
    └── index-tts\
        ├── checkpoints\              ← Your models here!
        │   ├── README.md             ← Info about models
        │   ├── .gitignore            ← Excludes from Git
        │   ├── config.yaml           ← Downloaded on first build
        │   ├── gpt.pth (~1-2GB)      ← Downloaded on first build
        │   ├── s2mel.pth (~500MB)    ← Downloaded on first build
        │   ├── qwen0.6bemo4-merge\   ← Downloaded on first build
        │   └── ... (other models)
        ├── Dockerfile
        ├── server.py                 ← Also mounted (live editable)
        └── requirements.txt
```

## 🚀 How to Use

### Browse Models

```cmd
# Open in Windows Explorer
explorer E:\github\project-hub\docker\index-tts\checkpoints
```

### Check Size

```cmd
# Windows: Right-click folder → Properties

# PowerShell:
Get-ChildItem E:\github\project-hub\docker\index-tts\checkpoints -Recurse | Measure-Object -Property Length -Sum
```

### Backup Models

```cmd
# Simple copy!
xcopy E:\github\project-hub\docker\index-tts\checkpoints E:\backups\index-tts\ /E /I /Y
```

### Delete Models

```cmd
# Stop container first
index-tts-helper.bat stop

# Delete files
del /q E:\github\project-hub\docker\index-tts\checkpoints\*.*
rmdir /s /q E:\github\project-hub\docker\index-tts\checkpoints\qwen0.6bemo4-merge

# Next build will re-download
index-tts-helper.bat build
```

### Share Between Projects

```cmd
# Create symlink
mklink /D E:\other-project\checkpoints E:\github\project-hub\docker\index-tts\checkpoints
```

## 📋 Configuration

### docker-compose-index-tts.yml

```yaml
volumes:
  # Host directory mount (not Docker volume!)
  - ./docker/index-tts/checkpoints:/opt/index-tts/checkpoints
  - ./docker/index-tts/server.py:/app/server.py:rw
  - index-tts-cache:/cache
```

**Key point:** `./docker/index-tts/checkpoints` is a relative path to your host directory!

## ✅ Benefits

| Feature        | Docker Volume   | Host Directory     |
|----------------|-----------------|--------------------|
| **Access**     | Docker commands | Windows Explorer ✅ |
| **Backup**     | Complex command | Copy folder ✅      |
| **Browse**     | Not directly    | Direct ✅           |
| **Share**      | Hard            | Symlinks ✅         |
| **Visibility** | Hidden          | Visible ✅          |
| **Speed**      | Same            | Same               |

## 🎯 First Build

```cmd
cd E:\github\project-hub
index-tts-helper.bat build
```

**What happens:**

1. Builds container
2. Downloads models to `E:\github\project-hub\docker\index-tts\checkpoints\`
3. You can watch files appear in Windows Explorer!
4. Takes 15-25 minutes (one-time)

## 🚀 Subsequent Builds

```cmd
index-tts-helper.bat build
```

**What happens:**

1. Checks `E:\github\project-hub\docker\index-tts\checkpoints\`
2. Finds existing models ✅
3. Skips download
4. Takes 2-3 minutes!

## 🗑️ Git Ignore

The `.gitignore` file in checkpoints/ ensures large model files aren't committed to Git:

```gitignore
# Ignore all model files
*.pth
*.pt
*.bin
*.safetensors

# Ignore model directories
qwen*/
bigvgan*/

# Keep the README
!README.md
```

## 📚 Documentation

See **`PERSISTENT_MODELS_GUIDE.md`** for complete details on:

- Storage architecture
- Smart download logic
- Backup/restore procedures
- Troubleshooting
- All workflows

## ✨ Ready!

Your models will now be stored at:

```
E:\github\project-hub\docker\index-tts\checkpoints\
```

You can browse them anytime in Windows Explorer! 📁

```cmd
index-tts-helper.bat build
```

🎉 **Much easier to manage!**

