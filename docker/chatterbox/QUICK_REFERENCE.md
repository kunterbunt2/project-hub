# Chatterbox TTS - Quick Reference

## 🔥 Hot-Reload Feature

**server.py is mounted as a volume - edit without rebuild!**

```bash
# 1. Edit server.py locally in your IDE
# 2. Restart container (NO rebuild needed!)
chatterbox-helper.bat restart    # ~30 seconds
# 3. Test changes
chatterbox-helper.bat test
```

See `HOT_RELOAD_GUIDE.md` for details.

---

## Quick Commands

```bash
# Navigate to chatterbox directory
cd E:\github\project-hub\docker\chatterbox

# Build container (first time only, ~10-15 min)
chatterbox-helper.bat build

# Start service (~1-2 min startup)
chatterbox-helper.bat start

# Test all endpoints
chatterbox-helper.bat test

# View logs
chatterbox-helper.bat logs

# Check status
chatterbox-helper.bat status

# Restart service
chatterbox-helper.bat restart

# Stop service
chatterbox-helper.bat stop

# Access container shell
chatterbox-helper.bat shell
```

## API Testing

```bash
# Health check
curl http://localhost:4123/health

# Get languages
curl http://localhost:4123/languages

# Generate speech
curl -X POST http://localhost:4123/v1/audio/speech \
  -H "Content-Type: application/json" \
  -d "{\"input\":\"Hello world\",\"temperature\":0.7,\"exaggeration\":1.0,\"cfg_weight\":3.0}" \
  --output speech.wav

# API documentation (browser)
start http://localhost:4123/docs
```

## Troubleshooting

```bash
# View recent logs
chatterbox-helper.bat status

# Follow logs in real-time
chatterbox-helper.bat logs

# Rebuild from scratch
chatterbox-helper.bat build

# Check GPU status
nvidia-smi

# Test Docker GPU access
docker run --rm --gpus all nvidia/cuda:11.8.0-base-ubuntu22.04 nvidia-smi

# Access Python shell in container
chatterbox-helper.bat shell
python
>>> import chatterbox
>>> help(chatterbox)
```

## File Locations

- **Docker files:** `E:\github\project-hub\docker\chatterbox\`
- **Java client:** `src\main\java\de\bushnaq\abdalla\projecthub\ai\chatterbox\ChatterboxTTS.java`
- **Test audio:** `docker\chatterbox\test-output.wav` (after running test)
- **Logs:** `docker-compose -f docker-compose-chatterbox.yml logs`

## Port & URL

- **Port:** 4123
- **URL:** http://localhost:4123
- **Health:** http://localhost:4123/health
- **Docs:** http://localhost:4123/docs

## Common Fixes

**Port in use:** Change port in `docker-compose-chatterbox.yml`
**GPU issues:** Switch to CPU mode (edit compose file, set `DEVICE=cpu`)
**Out of memory:** Restart container
**Import errors:** Check logs, update `server.py` imports

