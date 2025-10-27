# Index TTS Voice Selection - Updated Answer

## Your Question

> The index-tts client (IndexTTS) allows me to list voices, but all it does is return some default list that is actually
> not supported. The server itself is showing:
> ```
> WARNING:__main__:Model doesn't have get_available_voices method, using defaults
> ```
> How can I select a voice? Is that even supported by index-tts?

## Short Answer

**Index TTS doesn't have predefined voice names.** It uses **voice cloning** instead. You provide a WAV file with a
voice sample, and Index TTS clones that voice.

## ✅ What's Fixed

### 1. **Removed Fake Voices API**

- ❌ Removed `/voices` endpoint that returned fake voice names
- ❌ Removed `getVoices()` method from Java client

### 2. **Added Voice Reference Management API**

- ✅ `GET /v1/voice-references` - List uploaded voice references
- ✅ `POST /v1/voice-references` - Upload a voice reference WAV file
- ✅ `DELETE /v1/voice-references/{filename}` - Delete a voice reference

### 3. **Updated Java Client**

- ✅ `listVoiceReferences()` - List available voice references
- ✅ `uploadVoiceReference(localFilePath)` - Upload a WAV file
- ✅ `deleteVoiceReference(filename)` - Delete a voice reference
- ✅ Added `VoiceReference` class with file metadata

### 4. **Added Examples**

- ✅ `IndexTTSVoiceManagementExamples.java` - Complete examples of new API

## How to Use Voice References

### Method 1: Upload via API (Recommended) 🚀

```java
// 1. Upload a voice reference
IndexTTS.VoiceReference ref = IndexTTS.uploadVoiceReference("E:\\my_voice.wav");
System.out.

println("Uploaded: "+ref.getFilename());
        System.out.

println("Server path: "+ref.getPath());

// 2. Use the uploaded voice
byte[] audio = IndexTTS.generateSpeech(
        "Hello world!",
        ref.getPath(),  // Use the server path from the uploaded reference
        null, null, null, null, null, null, null
);
IndexTTS.

writeWav(audio, "output.wav");

// 3. List all voice references
IndexTTS.VoiceReference[] refs = IndexTTS.listVoiceReferences();
for(
IndexTTS.VoiceReference r :refs){
        System.out.

println(r);
}

// 4. Delete when done
        IndexTTS.

deleteVoiceReference(ref.getFilename());
```

### Method 2: Manual Directory Mount

```cmd
# 1. Create voices directory
mkdir E:\github\project-hub\docker\index-tts\voices

# 2. Place WAV files there
copy my_voice.wav E:\github\project-hub\docker\index-tts\voices\
```

Update `docker-compose-index-tts.yml`:

```yaml
volumes:
  - ./docker/index-tts/checkpoints:/opt/index-tts/checkpoints
  - ./docker/index-tts/voices:/opt/index-tts/voices  # Add this
  - index-tts-cache:/cache
```

```cmd
# 3. Restart container
index-tts-helper.bat restart
```

```java
// 4. List and use
IndexTTS.VoiceReference[] refs = IndexTTS.listVoiceReferences();
byte[] audio = IndexTTS.generateSpeech(
        "Hello world!",
        refs[0].getPath(),
        null, null, null, null, null, null, null
);
```

### Method 3: Default Voice (Easiest)

```java
// No voice reference needed - uses default
byte[] audio = IndexTTS.generateSpeech("Hello world");
```

## Quick Start Example

```java
import de.bushnaq.abdalla.projecthub.ai.chatterbox.IndexTTS;

public class VoiceCloneDemo {
    static void main(String[] args) throws Exception {
        // Upload your voice
        IndexTTS.VoiceReference myVoice =
                IndexTTS.uploadVoiceReference("E:\\recordings\\my_voice.wav");

        // Generate speech with your cloned voice
        byte[] audio = IndexTTS.generateSpeech(
                "This is my cloned voice speaking!",
                myVoice.getPath(),
                null, null, null, null, null, null, null
        );

        // Save to file
        IndexTTS.writeWav(audio, "cloned_speech.wav");

        System.out.println("✅ Speech generated with your cloned voice!");
    }
}
```

## Voice File Requirements

| Property    | Requirement                           |
|-------------|---------------------------------------|
| Format      | WAV (PCM 16-bit)                      |
| Duration    | 10-30 seconds (optimal)               |
| Sample Rate | 22050 Hz recommended                  |
| Quality     | Clear, minimal background noise       |
| Content     | Natural speech with varied intonation |

## API Reference

### List Voice References

```java
IndexTTS.VoiceReference[] refs = IndexTTS.listVoiceReferences();
// Returns: Array of VoiceReference objects with filename, path, size, timestamp
```

### Upload Voice Reference

```java
IndexTTS.VoiceReference ref = IndexTTS.uploadVoiceReference("local/path/to/file.wav");
// Returns: VoiceReference with server path
```

### Delete Voice Reference

```java
IndexTTS.deleteVoiceReference("filename.wav");
// Removes the file from server
```

### Generate Speech with Voice

```java
byte[] audio = IndexTTS.generateSpeech(
        "Text to speak",           // text
        "/opt/index-tts/voices/my_voice.wav",  // voice reference path
        1.0f,                      // speed (0.5-2.0)
        0.0f,                      // emotion: angry (0.0-1.0)
        0.7f,                      // emotion: happy (0.0-1.0)
        0.0f,                      // emotion: sad (0.0-1.0)
        0.0f,                      // emotion: surprise (0.0-1.0)
        0.3f,                      // emotion: neutral (0.0-1.0)
        0.7f                       // temperature (0.0-2.0)
);
```

## Files Changed

- ✅ `docker/index-tts/server.py` - Removed `/voices`, added voice reference management
- ✅ `src/test/java/.../IndexTTS.java` - Removed `getVoices()`, added new methods
- ✅ `src/test/java/.../IndexTTSVoiceManagementExamples.java` - New examples
- ✅ `docs/INDEX_TTS_VOICE_CLONING.md` - Updated documentation

## Testing the New API

Run the example tests:

```java
// List voice references
IndexTTSVoiceManagementExamples.testListVoiceReferences()

// Upload and use
IndexTTSVoiceManagementExamples.

testUploadVoiceReference()
IndexTTSVoiceManagementExamples.

testUseVoiceReference()

// Complete workflow
IndexTTSVoiceManagementExamples.

testCompleteWorkflow()

// With emotions
IndexTTSVoiceManagementExamples.

testVoiceCloningWithEmotions()
```

## Next Steps

1. ✅ Restart Index TTS container to get the updated server
2. 🎤 Record or obtain a voice sample (10-30 seconds WAV file)
3. 📤 Upload it using `uploadVoiceReference()`
4. 🎯 Use it in `generateSpeech()` with the returned path
5. 🧪 Run the examples in `IndexTTSVoiceManagementExamples.java`

## Summary

| Feature          | Before                             | After                                      |
|------------------|------------------------------------|--------------------------------------------|
| Fake voices API  | ❌ `/voices` returned fake names    | ✅ Removed                                  |
| Voice management | ❌ Manual file mounting only        | ✅ Upload/list/delete via API               |
| Java client      | ❌ `getVoices()` returned fake list | ✅ `listVoiceReferences()` shows real files |
| Upload           | ❌ Not supported                    | ✅ `uploadVoiceReference()`                 |
| Delete           | ❌ Not supported                    | ✅ `deleteVoiceReference()`                 |

**Now you can manage voice references programmatically!** 🎉

