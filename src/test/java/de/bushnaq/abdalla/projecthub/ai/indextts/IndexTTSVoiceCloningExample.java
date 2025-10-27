/*
 *
 * Copyright (C) 2025-2025 Abdalla Bushnaq
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package de.bushnaq.abdalla.projecthub.ai.indextts;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Example tests demonstrating Index TTS voice cloning
 * <p>
 * NOTE: For complete voice reference management examples (upload/list/delete),
 * see IndexTTSVoiceManagementExamples.java
 */
public class IndexTTSVoiceCloningExample {

    /**
     * Example 1: Setting up your first voice reference
     */
    @Test
    public void setupGuide() {
        System.out.println("\n=== Example 1: How to Set Up Voice References ===");

        System.out.println("\n📋 Two Methods Available:");
        System.out.println("  Method 1: Upload via API (see IndexTTSVoiceManagementExamples)");
        System.out.println("  Method 2: Manual Directory Mount (shown below)");

        System.out.println("\n1. Create a voices directory:");
        System.out.println("   mkdir E:\\github\\project-hub\\docker\\index-tts\\voices");

        System.out.println("\n2. Record or obtain voice samples (WAV format):");
        System.out.println("   - Duration: 10-30 seconds");
        System.out.println("   - Format: WAV, PCM 16-bit");
        System.out.println("   - Sample Rate: 22050 Hz recommended");
        System.out.println("   - Content: Natural speech with varied intonation");

        System.out.println("\n3. Place your WAV files in the voices directory:");
        System.out.println("   E:\\github\\project-hub\\docker\\index-tts\\voices\\");
        System.out.println("     ├── narrator.wav");
        System.out.println("     ├── hero.wav");
        System.out.println("     └── villain.wav");

        System.out.println("\n4. Update docker-compose-index-tts.yml:");
        System.out.println("   volumes:");
        System.out.println("     - ./docker/index-tts/voices:/opt/index-tts/voices");

        System.out.println("\n5. Restart the container:");
        System.out.println("   index-tts-helper.bat restart");

        System.out.println("\n6. Use in your code:");
        System.out.println("   byte[] audio = IndexTTS.generateSpeech(");
        System.out.println("       \"Hello world\",");
        System.out.println("       \"/opt/index-tts/voices/narrator.wav\",");
        System.out.println("       null, null, null, null, null, null, null");
        System.out.println("   );");

        System.out.println("\n📚 See Also:");
        System.out.println("  • IndexTTSVoiceManagementExamples.java - Upload/list/delete API");
        System.out.println("  • docs/INDEX_TTS_VOICE_CLONING.md - Complete guide");
    }

    /**
     * Example 2: Using custom voice reference (voice cloning)
     * NOTE: You need to have a voice reference file in the mounted directory!
     */
    @Test
    public void testCustomVoiceCloning() throws Exception {
        System.out.println("\n=== Example 2: Custom Voice Cloning ===");

        // This path is the SERVER-SIDE path (inside the container)
        // Make sure you have mounted ./docker/index-tts/voices:/opt/index-tts/voices
        // and placed a WAV file there
        String voiceReference = "/opt/index-tts/voices/my_voice.wav";

        byte[] audio = IndexTTS.generateSpeech(
                "Hello world! This voice is cloned from a reference audio.",
                voiceReference,  // Path to voice reference WAV file
                null,            // speed (null = default)
                null,            // emotionAngry
                null,            // emotionHappy
                null,            // emotionSad
                null,            // emotionSurprise
                null,            // emotionNeutral
                null             // temperature
        );

        assertNotNull(audio);
        assertTrue(audio.length > 0);

        IndexTTS.writeWav(audio, "test_cloned_voice.wav");
        System.out.println("✅ Generated with cloned voice -> test_cloned_voice.wav");
    }

    /**
     * Example 1: Using default voice (no voice reference)
     */
    @Test
    public void testDefaultVoice() throws Exception {
        System.out.println("=== Example 1: Default Voice ===");

        byte[] audio = IndexTTS.generateSpeech("Hello world! This is the default voice.");

        assertNotNull(audio);
        assertTrue(audio.length > 0);

        // Save to file to listen
        IndexTTS.writeWav(audio, "test_default_voice.wav");
        System.out.println("✅ Generated with default voice -> test_default_voice.wav");
    }

    /**
     * Example 4: Multiple voices for different characters
     */
    @Test
    public void testMultipleVoices() throws Exception {
        System.out.println("\n=== Example 4: Multiple Character Voices ===");

        // Narrator voice
        String narratorVoice = "/opt/index-tts/voices/narrator.wav";
        byte[] narration = IndexTTS.generateSpeech(
                "Once upon a time, in a land far away...",
                narratorVoice,
                0.9f,    // slightly slower for narration
                null, null, null, null, null, null
        );
        IndexTTS.writeWav(narration, "test_narrator.wav");
        System.out.println("✅ Narrator voice -> test_narrator.wav");

        // Hero voice
        String heroVoice = "/opt/index-tts/voices/hero.wav";
        byte[] heroLine = IndexTTS.generateSpeech(
                "I will save the day!",
                heroVoice,
                1.1f,    // slightly faster, more energetic
                null, 0.6f, null, null, 0.4f, null  // happy
        );
        IndexTTS.writeWav(heroLine, "test_hero.wav");
        System.out.println("✅ Hero voice -> test_hero.wav");

        // Villain voice
        String villainVoice = "/opt/index-tts/voices/villain.wav";
        byte[] villainLine = IndexTTS.generateSpeech(
                "You will never defeat me!",
                villainVoice,
                0.85f,   // slower, more menacing
                0.7f, null, null, null, 0.3f, null  // angry
        );
        IndexTTS.writeWav(villainLine, "test_villain.wav");
        System.out.println("✅ Villain voice -> test_villain.wav");
    }

    /**
     * Example 3: Voice cloning with emotions
     */
    @Test
    public void testVoiceCloningWithEmotions() throws Exception {
        System.out.println("\n=== Example 3: Voice Cloning + Emotions ===");

        String voiceReference = "/opt/index-tts/voices/my_voice.wav";

        // Generate happy speech
        byte[] happyAudio = IndexTTS.generateSpeech(
                "I am so excited! This is wonderful news!",
                voiceReference,
                null,    // speed
                null,    // angry
                0.8f,    // happy (80%)
                null,    // sad
                null,    // surprise
                0.2f,    // neutral (20%)
                null     // temperature
        );
        IndexTTS.writeWav(happyAudio, "test_happy_voice.wav");
        System.out.println("✅ Generated happy voice -> test_happy_voice.wav");

        // Generate sad speech
        byte[] sadAudio = IndexTTS.generateSpeech(
                "I am disappointed. This is unfortunate.",
                voiceReference,
                null,    // speed
                null,    // angry
                null,    // happy
                0.7f,    // sad (70%)
                null,    // surprise
                0.3f,    // neutral (30%)
                null     // temperature
        );
        IndexTTS.writeWav(sadAudio, "test_sad_voice.wav");
        System.out.println("✅ Generated sad voice -> test_sad_voice.wav");

        // Generate angry speech
        byte[] angryAudio = IndexTTS.generateSpeech(
                "This is unacceptable! I demand an explanation!",
                voiceReference,
                null,    // speed
                0.8f,    // angry (80%)
                null,    // happy
                null,    // sad
                null,    // surprise
                0.2f,    // neutral (20%)
                null     // temperature
        );
        IndexTTS.writeWav(angryAudio, "test_angry_voice.wav");
        System.out.println("✅ Generated angry voice -> test_angry_voice.wav");
    }
}

