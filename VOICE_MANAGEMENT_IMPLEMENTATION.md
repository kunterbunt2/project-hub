# Index TTS Voice Management - Complete Implementation Summary

## Problem

User asked: "If the voices are fake, we should remove that API. Also, how should I get reference wav file uploaded to
the container?"

## Solution Implemented

### ✅ Changes Made

#### 1. **Removed Fake Voices API**

- **Server (`docker/index-tts/server.py`)**:
    - ❌ Removed `@app.get("/voices")` endpoint that returned hardcoded fake names
    - ✅ Added `@app.get("/v1/voice-references")` - List uploaded voice references
    - ✅ Added `@app.post("/v1/voice-references")` - Upload voice reference files
    - ✅ Added `@app.delete("/v1/voice-references/{filename}")` - Delete voice references

- **Java Client (`IndexTTS.java`)**:
    - ❌ Removed `getVoices()` method
    - ✅ Added `listVoiceReferences()` - List available voice references
    - ✅ Added `uploadVoiceReference(localFilePath)` - Upload WAV files from local system
    - ✅ Added `deleteVoiceReference(filename)` - Delete voice references
    - ✅ Added `VoiceReference` inner class with metadata (filename, path, size, timestamp)

#### 2. **Fixed Existing Tests**

- **TestIndexTTS.java**:
    - Fixed `listVoices()` test to use `listVoiceReferences()`
    - Fixed `testDifferentVoices()` to iterate over voice references
    - Fixed `testSpeech_09_WithVoice()` to use voice references

#### 3. **Added New Examples**

- **IndexTTSVoiceManagementExamples.java**:
    - Example 1: List voice references
    - Example 2: Upload voice reference
    - Example 3: Use voice reference
    - Example 4: Delete voice reference
    - Example 5: Complete workflow
    - Example 6: Voice cloning with emotions
    - Example 7: Setup guide

#### 4. **Updated Documentation**

- **INDEX_TTS_VOICE_SELECTION_ANSWER.md** - Complete updated guide
- **docs/INDEX_TTS_VOICE_CLONING.md** - Original detailed guide (still valid)

## API Reference

### Java Client API

```java
// List voice references on server
IndexTTS.VoiceReference[] refs = IndexTTS.listVoiceReferences();
// Returns: Array of VoiceReference objects

// Upload a voice reference from local file
IndexTTS.VoiceReference ref = IndexTTS.uploadVoiceReference("E:\\my_voice.wav");
// Returns: VoiceReference with server path

// Delete a voice reference
IndexTTS.

deleteVoiceReference("my_voice.wav");
// Returns: void

// Use voice reference in speech generation
byte[] audio = IndexTTS.generateSpeech(
        "Hello world",
        ref.getPath(),  // Use uploaded voice reference
        null, null, null, null, null, null, null
);
```

### VoiceReference Class

```java
/**
 * @param filename  e.g., "my_voice.wav"
 * @param path  e.g., "/opt/index-tts/voices/my_voice.wav"
 * @param sizeBytes  File size
 * @param modifiedTimestamp  Last modified time */
public record VoiceReference(String filename, String path, long sizeBytes, double modifiedTimestamp) {
    @Override
    public String filename() { ...}

    @Override
    public String path() { ...}

    @Override
    public long sizeBytes() { ...}

    @Override
    public double modifiedTimestamp() { ...}
}
```

### REST API Endpoints

#### GET /v1/voice-references

List all voice reference files on server.

**Response:**

```json
{
  "voice_references": [
    {
      "filename": "my_voice.wav",
      "path": "/opt/index-tts/voices/my_voice.wav",
      "size_bytes": 524288,
      "modified_timestamp": 1730000000.0
    }
  ],
  "count": 1,
  "directory": "/opt/index-tts/voices"
}
```

#### POST /v1/voice-references

Upload a voice reference WAV file.

**Request:** `multipart/form-data` with file field
**Response:**

```json
{
  "filename": "my_voice.wav",
  "path": "/opt/index-tts/voices/my_voice.wav",
  "size_bytes": 524288,
  "message": "Voice reference uploaded successfully"
}
```

#### DELETE /v1/voice-references/{filename}

Delete a voice reference file.

**Response:**

```json
{
  "filename": "my_voice.wav",
  "message": "Voice reference deleted successfully"
}
```

## Usage Examples

### Example 1: Upload and Use (Recommended)

```java
// 1. Upload your voice sample
IndexTTS.VoiceReference myVoice =
        IndexTTS.uploadVoiceReference("E:\\recordings\\my_voice.wav");

System.out.

println("Uploaded: "+myVoice.getFilename());
        System.out.

println("Server path: "+myVoice.getPath());

// 2. Generate speech with your voice
byte[] audio = IndexTTS.generateSpeech(
        "This is my cloned voice speaking!",
        myVoice.getPath(),
        null, null, null, null, null, null, null
);

// 3. Save the result
IndexTTS.

writeWav(audio, "cloned_speech.wav");

// 4. Clean up when done (optional)
IndexTTS.

deleteVoiceReference(myVoice.getFilename());
```

### Example 2: List and Select

```java
// List all available voice references
IndexTTS.VoiceReference[] refs = IndexTTS.listVoiceReferences();

if(refs.length >0){
        System.out.

println("Available voice references:");
    for(
int i = 0;
i<refs.length;i++){
        System.out.

println("["+i +"] "+refs[i]);
    }

// Use the first one
byte[] audio = IndexTTS.generateSpeech(
        "Using an existing voice reference",
        refs[0].getPath(),
        null, null, null, null, null, null, null
);
}else{
        System.out.

println("No voice references available. Upload one first!");
}
```

### Example 3: With Narrator Integration

```java
// Using with Narrator class (from TestIndexTTS)
IndexTTS.VoiceReference[] refs = IndexTTS.listVoiceReferences();
if(refs.length >0){
Narrator narrator = Narrator.withIndexTTS("tts/output");
NarratorAttribute attrs = new NarratorAttribute()
        .withVoiceReference(refs[0].getPath())
        .withHappy(0.8f);
    
    narrator.

narrate(attrs, "Hello with emotion and custom voice!");
}
```

## Voice File Requirements

| Property        | Requirement                           |
|-----------------|---------------------------------------|
| **Format**      | WAV (PCM 16-bit)                      |
| **Duration**    | 10-30 seconds (optimal)               |
| **Sample Rate** | 22050 Hz (or 16kHz, 44.1kHz)          |
| **Quality**     | Clear audio, minimal background noise |
| **Content**     | Natural speech with varied intonation |

## Recording Tips

1. **Use good quality microphone** - Built-in laptop mics work, but external USB mics are better
2. **Quiet environment** - Minimize background noise
3. **Natural speech** - Speak with emotion and varied intonation
4. **Varied content** - Include questions, statements, different emotions
5. **Example script:**
   > "Hello! How are you today? I'm excited to help you. Technology is amazing, isn't it? Let me tell you something
   interesting. The weather is lovely, and I hope you're having a great day!"

## Testing

### Run Individual Tests

```bash
# Test voice reference listing
mvn test -Dtest=TestIndexTTS#listVoices

# Test different voice references
mvn test -Dtest=TestIndexTTS#testDifferentVoices

# Test with voice reference
mvn test -Dtest=TestIndexTTS#testSpeech_09_WithVoice

# Run all voice management examples
mvn test -Dtest=IndexTTSVoiceManagementExamples
```

## Deployment Steps

### 1. Restart Index TTS Container

```cmd
index-tts-helper.bat restart
```

### 2. Verify Server is Running

```cmd
curl http://localhost:5124/
```

Should return:

```json
{
  "name": "Index TTS API",
  "version": "1.0.0",
  "endpoints": {
    "list_voice_references": "/v1/voice-references (GET)",
    "upload_voice_reference": "/v1/voice-references (POST)",
    "delete_voice_reference": "/v1/voice-references/{filename} (DELETE)",
    ...
  }
}
```

### 3. Test Upload via Java

```java
IndexTTS.VoiceReference ref =
        IndexTTS.uploadVoiceReference("E:\\test_voice.wav");
System.out.

println("Success: "+ref);
```

## Files Modified

| File                                                     | Changes                                                                                                  |
|----------------------------------------------------------|----------------------------------------------------------------------------------------------------------|
| `docker/index-tts/server.py`                             | Removed `/voices`, added voice reference management endpoints                                            |
| `src/test/java/.../IndexTTS.java`                        | Removed `getVoices()`, added `listVoiceReferences()`, `uploadVoiceReference()`, `deleteVoiceReference()` |
| `src/test/java/.../TestIndexTTS.java`                    | Fixed compilation errors, updated to use new API                                                         |
| `src/test/java/.../IndexTTSVoiceManagementExamples.java` | New file with complete examples                                                                          |
| `INDEX_TTS_VOICE_SELECTION_ANSWER.md`                    | Updated with new API documentation                                                                       |

## Summary

✅ **Problem Solved:**

- Removed fake voices API that returned unusable names
- Added proper voice reference management (upload/list/delete)
- Fixed all compilation errors in existing tests
- Provided complete examples and documentation

✅ **New Capabilities:**

- Upload voice references programmatically from Java
- List all available voice references with metadata
- Delete voice references when no longer needed
- Use uploaded voices in speech generation

✅ **Developer Experience:**

- No more confusion about fake voice names
- Clear API for voice management
- Comprehensive examples and documentation
- Easy integration with existing Narrator system

**Everything is ready to use!** 🎉

