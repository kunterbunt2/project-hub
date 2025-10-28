# Server.py Hot-Reload Configuration

## ✅ Configuration Status: READY

The `server.py` file is already configured for **live editing** without container rebuild!

## How It Works

### Volume Mount (Already Configured)

In `docker-compose-chatterbox.yml`:

```yaml
volumes:
  - chatterbox-cache:/cache
  - ./server.py:/app/server.py:rw  # ← This line enables live editing
```

**What this means:**

- `./server.py` - Your local file on Windows host
- `/app/server.py` - The file inside the container
- `:rw` - Read-write permissions (container can read, you can edit)

### File Sync Behavior

```
┌─────────────────────────┐
│  Windows Host           │
│  E:\github\             │
│  project-hub\docker\    │
│  chatterbox\server.py   │ ← Edit here
└────────────┬────────────┘
             │ Synced in real-time
             ↓
┌─────────────────────────┐
│  Docker Container       │
│  /app/server.py         │ ← Changes reflected here
└─────────────────────────┘
```

## Usage Workflow

### 1. Start the Container

```bash
cd E:\github\project-hub\docker\chatterbox
chatterbox-helper.bat start
```

### 2. Edit server.py Locally

Open in your IDE:

- File: `E:\github\project-hub\docker\chatterbox\server.py`
- Make your changes
- Save the file

### 3. Restart the Container (NOT Rebuild)

```bash
chatterbox-helper.bat restart
```

⏱️ Takes ~30 seconds (vs ~15 minutes for full rebuild)

### 4. Verify Changes

```bash
chatterbox-helper.bat test
```

## Example: Fixing Import Issues

Let's say you discover the Chatterbox import needs adjustment:

### Step 1: Check the Error

```bash
chatterbox-helper.bat logs
```

You see:

```
ModuleNotFoundError: No module named 'chatterbox'
```

### Step 2: Access Container to Investigate

```bash
chatterbox-helper.bat shell

# Inside container:
python
>>> import chatterbox_tts
>>> help(chatterbox_tts)
```

You discover it should be `chatterbox_tts` not `chatterbox`.

### Step 3: Edit Locally

Open `E:\github\project-hub\docker\chatterbox\server.py` in your IDE.

Change line 44:

```python
# OLD:
from chatterbox import ChatterboxTTS

# NEW:
from chatterbox_tts import ChatterboxTTS
```

Save the file.

### Step 4: Restart (No Rebuild Needed!)

```bash
chatterbox-helper.bat restart
```

### Step 5: Verify Fix

```bash
chatterbox-helper.bat logs
# Should show successful initialization

chatterbox-helper.bat test
# Should generate audio
```

## What Requires Rebuild vs Restart

### ❌ Requires Full Rebuild (chatterbox-helper.bat build)

- Changes to `Dockerfile`
- Changes to `requirements.txt`
- Changes to system packages
- Changes to docker-compose (ports, volumes, environment)

⏱️ Time: ~10-15 minutes

### ✅ Only Requires Restart (chatterbox-helper.bat restart)

- Changes to `server.py`
- Python code logic changes
- API endpoint changes
- Error handling changes

⏱️ Time: ~30 seconds

## Advantages

1. **Fast Iteration** - Test fixes in seconds, not minutes
2. **No Data Loss** - Model cache persists in volume
3. **Easy Debugging** - Edit → Restart → Test cycle
4. **Version Control** - Changes on host are tracked by Git
5. **IDE Support** - Use your favorite editor with full features

## Verification

To confirm the volume mount is active:

```bash
# On host, check file
dir E:\github\project-hub\docker\chatterbox\server.py

# In container, verify same file
chatterbox-helper.bat shell
ls -la /app/server.py
cat /app/server.py | head -5

# Should show same content!
```

## Testing the Setup

### Test 1: Add a Comment

1. Edit `server.py` locally, add a comment at top:
   ```python
   # TEST EDIT - If you see this in container, volume mount works!
   ```

2. Restart:
   ```bash
   chatterbox-helper.bat restart
   ```

3. Check in container:
   ```bash
   chatterbox-helper.bat shell
   head -5 /app/server.py
   ```

   Should show your comment!

### Test 2: Change Log Message

1. Edit line ~53 in `server.py`:
   ```python
   logger.info("Chatterbox TTS engine initialized successfully - CUSTOM MESSAGE")
   ```

2. Restart and check logs:
   ```bash
   chatterbox-helper.bat restart
   chatterbox-helper.bat logs
   ```

   Should show your custom message!

## Important Notes

### ⚠️ Windows Line Endings

If you see `^M` characters in container:

```bash
# Convert to Unix line endings
dos2unix /app/server.py
# Or in PowerShell on host:
(Get-Content server.py) | Set-Content -NoNewline server.py
```

### ⚠️ File Permissions

Volume mount creates files with host user permissions, which is fine for read operations.

### ⚠️ Syntax Errors

If you introduce a Python syntax error:

- Container will fail to start
- Check logs: `chatterbox-helper.bat logs`
- Fix the error locally
- Restart: `chatterbox-helper.bat restart`

### ⚠️ Import Errors

If you change imports that don't exist:

- Service will start but fail on first request
- Check health endpoint: `curl http://localhost:4123/health`
- Fix imports locally
- Restart

## Common Scenarios

### Scenario 1: Adjusting Chatterbox API Calls

```python
# In server.py, line ~145
# Edit the synthesize call based on actual API
audio_data = tts_engine.synthesize(...)  # Adjust parameters
```

→ **Action:** Edit → Restart

### Scenario 2: Changing Audio Output Format

```python
# In server.py, line ~160
# Change sample rate or format
sf.write(audio_buffer, audio_np, 24000, format='WAV')  # Changed from 22050
```

→ **Action:** Edit → Restart

### Scenario 3: Adding New Endpoint

```python
# In server.py, add new endpoint
@app.get("/v1/models")
async def list_models():
    return {"models": ["chatterbox-default"]}
```

→ **Action:** Edit → Restart

### Scenario 4: Changing Error Handling

```python
# In server.py, improve error messages
except Exception as e:
    logger.error(f"Detailed error: {e}", exc_info=True)
    raise HTTPException(status_code=500, detail=f"TTS failed: {str(e)}")
```

→ **Action:** Edit → Restart

## Troubleshooting

### Problem: Changes Not Reflected

**Check 1:** Is the file saved?

```bash
# Show last modified time
dir E:\github\project-hub\docker\chatterbox\server.py
```

**Check 2:** Did you restart the container?

```bash
chatterbox-helper.bat restart
```

**Check 3:** Is the volume mount correct?

```bash
docker inspect chatterbox-tts | grep -A 5 Mounts
```

### Problem: Container Won't Start After Edit

**Check logs:**

```bash
chatterbox-helper.bat logs
```

**Look for:**

- Syntax errors: `SyntaxError: invalid syntax`
- Import errors: `ModuleNotFoundError`
- Indentation errors: `IndentationError`

**Fix:** Correct the error in local file, then restart.

### Problem: Old Code Still Running

**Solution:** Full restart

```bash
chatterbox-helper.bat stop
chatterbox-helper.bat start
```

If still not working, rebuild:

```bash
chatterbox-helper.bat build
chatterbox-helper.bat start
```

## Best Practices

1. **Test Small Changes** - Make one change at a time
2. **Check Logs** - Always verify the change worked
3. **Keep Backups** - Copy working version before major changes
4. **Use Git** - Commit working versions
5. **Comment Changes** - Add comments explaining fixes
6. **Validate Syntax** - Use IDE linting before saving

## Integration with Development Workflow

```bash
# Development cycle:
1. chatterbox-helper.bat start        # Start once
2. Edit server.py                      # Make changes
3. chatterbox-helper.bat restart      # Apply changes
4. chatterbox-helper.bat test         # Verify
5. Repeat steps 2-4 until satisfied
6. Git commit                          # Save working version
```

## Summary

✅ **Current Status:** Volume mount is configured and ready
✅ **Edit Location:** `E:\github\project-hub\docker\chatterbox\server.py`
✅ **Apply Changes:** `chatterbox-helper.bat restart` (30 seconds)
✅ **No Rebuild Needed:** For Python code changes only
✅ **Fast Iteration:** Edit → Restart → Test in under 1 minute

---

**You're all set!** Edit `server.py` anytime and just restart the container to apply changes. No rebuild required! 🚀

