# Volume Mount Architecture

## server.py Hot-Reload Setup

```
┌─────────────────────────────────────────────────────────────────┐
│                         WINDOWS HOST                             │
│                                                                   │
│  E:\github\project-hub\docker\chatterbox\                        │
│  ├── Dockerfile                                                  │
│  ├── docker-compose-chatterbox.yml  ← Defines volume mount      │
│  ├── requirements.txt                                            │
│  └── server.py  ◄──────────────────────┐                        │
│       ↑                                  │                        │
│       │ You edit here                   │                        │
│       │ with your IDE                   │                        │
│       │                                  │                        │
└───────┼──────────────────────────────────┼────────────────────────┘
        │                                  │
        │ Real-time sync                   │ Volume mount
        │ (bind mount)                     │ (defined in compose)
        │                                  │
┌───────┼──────────────────────────────────┼────────────────────────┐
│       ↓                                  │                        │
│  ┌─────────────────────────────────┐    │                        │
│  │  DOCKER CONTAINER               │    │                        │
│  │  chatterbox-tts                 │    │                        │
│  │                                 │    │                        │
│  │  /app/                          │    │                        │
│  │  ├── requirements.txt (copied)  │    │                        │
│  │  └── server.py  ◄────────────────────┘                        │
│  │       ↓                         │                             │
│  │       Python process reads      │                             │
│  │       this file                 │                             │
│  │                                 │                             │
│  │  /cache/                        │                             │
│  │  └── models/  ◄─────────────────┼──── Persistent volume      │
│  │       (model files cached)      │     (survives restarts)    │
│  └─────────────────────────────────┘                             │
│                                                                   │
│  FastAPI server running on port 4123                             │
│  ├── Reads /app/server.py                                        │
│  ├── Loads models from /cache/models/                            │
│  └── Serves API endpoints                                        │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
        │
        ↓
    Port 4123 exposed to host
        │
        ↓
┌───────┴───────────────────────────────────────────────────────────┐
│  ACCESSIBLE FROM HOST                                             │
│                                                                   │
│  http://localhost:4123/health                                    │
│  http://localhost:4123/v1/audio/speech                           │
│  http://localhost:4123/languages                                 │
│                                                                   │
│  Your Java Client:                                               │
│  ChatterboxTTS.generateSpeech(...)                               │
└───────────────────────────────────────────────────────────────────┘
```

## Volume Mount Configuration

In `docker-compose-chatterbox.yml`:

```yaml
volumes:
  # Bind mount - syncs host file to container
  - ./server.py:/app/server.py:rw

  # Named volume - persists between restarts
  - chatterbox-cache:/cache
```

## How It Works

### 1. Build Time

```
Dockerfile:
  COPY server.py .     ← Initial copy during build
```

### 2. Runtime

```
docker-compose.yml:
  - ./server.py:/app/server.py:rw   ← Overrides with live mount
```

The volume mount **overlays** the copied file with the host file.

## File Lifecycle

### During Build

```bash
chatterbox-helper.bat build
```

1. Dockerfile copies `server.py` into image at `/app/server.py`
2. Image is created with this snapshot
3. Image is immutable (doesn't change)

### During Start

```bash
chatterbox-helper.bat start
```

1. Docker creates container from image
2. Volume mount overlays `/app/server.py` with host file
3. Container sees live version from host
4. Any edits on host immediately visible in container

### During Restart

```bash
chatterbox-helper.bat restart
```

1. Container stops (Python process ends)
2. Container starts (Python process starts)
3. Reads `/app/server.py` (which is mounted from host)
4. New code is now running

### During Stop

```bash
chatterbox-helper.bat stop
```

1. Container stops
2. Volume mount unmounted
3. Host file remains unchanged
4. Cache volume persists

## What Changes When

| Action            | server.py in Container | Model Cache | Requires |
|-------------------|------------------------|-------------|----------|
| Edit host file    | ✅ Updates immediately  | Unchanged   | Nothing  |
| Restart container | ✅ Reloads changes      | Unchanged   | ~30 sec  |
| Stop/Start        | ✅ Uses latest          | Persists    | ~30 sec  |
| Rebuild           | ✅ Fresh copy           | Persists    | ~15 min  |
| Remove volumes    | ✅ Uses host            | ❌ Deleted   | Rebuild  |

## Volume Types

### Bind Mount (server.py)

```yaml
- ./server.py:/app/server.py:rw
```

- **Type:** Bind mount
- **Source:** Host filesystem
- **Purpose:** Live editing
- **Persistence:** As long as host file exists
- **Performance:** Direct file access

### Named Volume (cache)

```yaml
volumes:
  - chatterbox-cache:/cache
```

- **Type:** Named volume
- **Source:** Docker-managed storage
- **Purpose:** Model caching
- **Persistence:** Until explicitly deleted
- **Performance:** Optimized by Docker

## Comparison: With vs Without Hot-Reload

### Without Hot-Reload (Traditional)

```
Edit code → Rebuild image → Restart container → Test
   ↓            ↓               ↓                ↓
  1 min      15 min          30 sec           1 min
  
  Total: ~17 minutes per iteration
```

### With Hot-Reload (Current Setup)

```
Edit code → Restart container → Test
   ↓              ↓               ↓
  1 min        30 sec          1 min
  
  Total: ~2.5 minutes per iteration
```

**Speedup: ~7x faster! 🚀**

## Security Considerations

### Read-Write Mount (`:rw`)

- Container can read the file ✅
- Container can write to the file ⚠️
- Host edits are immediate ✅

**Safe because:**

- Container runs as non-root (default)
- Server code doesn't write to itself
- Only Python process reads the file

### Alternative: Read-Only Mount (`:ro`)

```yaml
- ./server.py:/app/server.py:ro
```

- Container can only read
- Extra safety layer
- Works fine for our use case

To make it read-only, change in `docker-compose-chatterbox.yml`:

```yaml
- ./server.py:/app/server.py:ro  # Changed :rw to :ro
```

## Troubleshooting Volume Mounts

### Verify Mount is Active

```bash
# Check container mounts
docker inspect chatterbox-tts | grep -A 10 Mounts

# Should show:
# "Source": "E:\\github\\project-hub\\docker\\chatterbox\\server.py"
# "Destination": "/app/server.py"
```

### Check File Sync

```bash
# On host, get file hash
certutil -hashfile E:\github\project-hub\docker\chatterbox\server.py MD5

# In container, get same hash
chatterbox-helper.bat shell
md5sum /app/server.py

# Hashes should match!
```

### Test Live Sync

```bash
# Terminal 1: Watch container file
chatterbox-helper.bat shell
watch -n 1 'stat /app/server.py'

# Terminal 2: Edit host file
echo "# test comment" >> E:\github\project-hub\docker\chatterbox\server.py

# Terminal 1 should show updated timestamp
```

## Advanced: Multiple File Mounts

If you want to mount more files (future expansion):

```yaml
volumes:
  - chatterbox-cache:/cache
  - ./server.py:/app/server.py:rw
  - ./config.yaml:/app/config.yaml:rw     # Add config
  - ./voices:/app/voices:rw                # Add voice directory
```

## Performance Notes

### Bind Mount Performance (Windows)

- Uses WSL2 filesystem (fast)
- Direct access to host files
- Minimal overhead for reads
- Slightly slower than Linux native mounts

### Best Practices

- ✅ Mount only files you need to edit
- ✅ Use named volumes for large datasets
- ✅ Keep mounted files small (server.py is ~7KB)
- ❌ Don't mount entire project directory
- ❌ Don't mount frequently-accessed logs

## Summary

✅ **server.py is mounted** - Changes sync instantly
✅ **Models are cached** - Don't redownload on restart
✅ **Fast iteration** - Edit → Restart → Test in 2.5 minutes
✅ **No rebuild needed** - For Python code changes
✅ **Safe and secure** - Proper permissions and isolation

---

**You have the best development setup! 🎉**

