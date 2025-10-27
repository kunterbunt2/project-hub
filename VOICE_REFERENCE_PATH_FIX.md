# Voice Reference Path Debugging Guide

## Issue

You're seeing this in the logs:

```
INFO:__main__:Generating speech: voice_reference=/opt/index-tts/examples/chatterbox.wav, speed=1.0, emotions=None, text_len=58
INFO:__main__:Using default voice reference: /opt/index-tts/examples/voice_01.wav
```

This means:

1. ✅ The Java code IS sending the voice_reference parameter
2. ❌ But the file `/opt/index-tts/examples/chatterbox.wav` doesn't exist on the server
3. ⚠️ Server falls back to default voice

## Solution

You have two options:

### Option 1: Upload the Voice Reference First (Recommended)

```java
// 1. Upload your voice file
IndexTTS.VoiceReference ref = IndexTTS.uploadVoiceReference("path/to/local/chatterbox.wav");
System.out.

println("Uploaded to: "+ref.path());  // Will be: /opt/index-tts/voices/chatterbox.wav

// 2. Use the uploaded path
NarratorAttribute attrs = new NarratorAttribute()
        .withVoiceReference(ref.path());  // Use the server path from upload response

narrator.

narrate(attrs, "Hello world");
```

### Option 2: Use Existing Voice References

```java
// 1. List available voice references
IndexTTS.VoiceReference[] refs = IndexTTS.listVoiceReferences();
for(
IndexTTS.VoiceReference ref :refs){
        System.out.

println("Available: "+ref.path());
        }

// 2. Use one of them
        if(refs.length >0){
NarratorAttribute attrs = new NarratorAttribute()
        .withVoiceReference(refs[0].path());
    narrator.

narrate(attrs, "Hello world");
}
```

### Option 3: Sync Local Files First

```java
// Sync all WAV files from local directory to server
SyncResult result = IndexTTSVoiceManagementExamples.syncVoiceReferences("docker\\index-tts\\voices");
System.out.

println("Uploaded: "+result.uploadedCount);

// Now use them
IndexTTS.VoiceReference[] refs = IndexTTS.listVoiceReferences();
// ... use refs[0].path()
```

## Why `/opt/index-tts/examples/chatterbox.wav` Doesn't Work

The path `/opt/index-tts/examples/` is for example files that might come with Index TTS. But:

- The `chatterbox.wav` file doesn't exist there by default
- You need to upload your own voice references to `/opt/index-tts/voices/`

## Correct Voice Reference Paths

Voice references should be in one of these directories:

### Uploaded Files (Recommended)

```
/opt/index-tts/voices/chatterbox.wav
/opt/index-tts/voices/narrator.wav
/opt/index-tts/voices/hero.wav
```

### Mounted Files

If you mounted `docker/index-tts/voices` in docker-compose:

```yaml
volumes:
  - ./docker/index-tts/voices:/opt/index-tts/voices
```

Then files in `E:\github\project-hub\docker\index-tts\voices\` will be available at `/opt/index-tts/voices/`

## Testing

Create a simple test:

```java

@Test
public void testVoiceReferencePath() throws Exception {
    // Upload a voice reference
    String                  localFile = "path/to/your/voice.wav";
    IndexTTS.VoiceReference ref       = IndexTTS.uploadVoiceReference(localFile);

    System.out.println("Uploaded to: " + ref.path());
    System.out.println("Filename: " + ref.filename());

    // Generate speech with it
    byte[] audio = IndexTTS.generateSpeech(
            "Testing voice reference",
            ref.path(),  // Use the server path
            null, null, null, null, null, null, null
    );

    IndexTTS.writeWav(audio, "test_output.wav");
    System.out.println("✅ Generated speech with custom voice");
}
```

## Check Server Logs

After the fix, you should see:

```
# When voice exists:
INFO:__main__:Generating speech: voice_reference=/opt/index-tts/voices/chatterbox.wav, ...
INFO:__main__:Using provided voice reference: /opt/index-tts/voices/chatterbox.wav

# When voice doesn't exist:
INFO:__main__:Generating speech: voice_reference=/opt/index-tts/examples/chatterbox.wav, ...
WARNING:__main__:Provided voice reference not found: /opt/index-tts/examples/chatterbox.wav
WARNING:__main__:Falling back to default voice reference
INFO:__main__:No voice reference provided, using default
```

The new logging makes it clear WHY the default is being used!

## Summary

✅ **Server fix applied** - Better logging to show why default is used  
✅ **Java fix applied** - Narrator passes voice reference from attributes  
🔧 **Action needed** - Upload voice reference or use correct path

**Next steps:**

1. Upload your `chatterbox.wav` file via the API
2. Use the returned path from the upload response
3. Or sync all files from `docker/index-tts/voices` directory

