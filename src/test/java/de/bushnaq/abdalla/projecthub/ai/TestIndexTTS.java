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

package de.bushnaq.abdalla.projecthub.ai;

import de.bushnaq.abdalla.projecthub.ai.chatterbox.IndexTTS;
import de.bushnaq.abdalla.projecthub.ai.chatterbox.Narrator;
import de.bushnaq.abdalla.projecthub.ai.chatterbox.NarratorAttribute;
import de.bushnaq.abdalla.projecthub.ui.util.selenium.SeleniumHandler;
import org.junit.jupiter.api.Test;

public class TestIndexTTS {
    Narrator        narrator        = Narrator.withIndexTTS("tts/index-tts");
    SeleniumHandler seleniumHandler = new SeleniumHandler();

    @Test
    public void compareWithChatterbox() throws Exception {
        String text = "The quick brown fox jumps over the lazy dog.";

        System.out.println("Playing with Chatterbox TTS...");
        Narrator chatterbox = new Narrator("tts/comparison");
        chatterbox.narrate(text);

        Thread.sleep(2000);

        System.out.println("Playing with Index TTS (neutral)...");
        Narrator indexTts = Narrator.withIndexTTS("tts/comparison");
        indexTts.narrate(text);

        Thread.sleep(2000);

        System.out.println("Playing with Index TTS (happy)...");
        indexTts.narrate(new NarratorAttribute().withHappy(0.8f), text);
    }

    @Test
    public void listEmotions() throws Exception {
        System.out.println("=== Available Index TTS Emotions ===");
        for (String emotion : IndexTTS.getEmotions()) {
            System.out.println(emotion);
        }
    }

    @Test
    public void listModels() throws Exception {
        System.out.println("=== Available Index TTS Models ===");
        for (String model : IndexTTS.getModels()) {
            System.out.println(model);
        }
    }

    @Test
    public void listVoices() throws Exception {
        System.out.println("=== Available Index TTS Voice References ===");
        System.out.println("Note: Index TTS uses voice cloning, not predefined voice names.");
        System.out.println("Voice references are WAV files that can be used for cloning.\n");

        IndexTTS.VoiceReference[] refs = IndexTTS.listVoiceReferences();
        if (refs.length == 0) {
            System.out.println("No voice references found on server.");
            System.out.println("Upload one using: IndexTTS.uploadVoiceReference(\"path/to/file.wav\")");
        } else {
            for (IndexTTS.VoiceReference ref : refs) {
                System.out.println(ref);
            }
        }
    }

    @Test
    public void testDifferentVoices() throws Exception {
        String text = "Good morning, my name is Jennifer Holleman. I am the product manager of Kassandra and I will be demonstrating the latest alpha version of the Kassandra project server to you today.";

        IndexTTS.VoiceReference[] refs = IndexTTS.listVoiceReferences();
        if (refs.length == 0) {
            System.out.println("No voice references available. Using default voice.");
            narrator.narrate(text);
        } else {
            for (IndexTTS.VoiceReference ref : refs) {
                System.out.println("Testing voice reference: " + ref.filename());
                Narrator          narrator = Narrator.withIndexTTS("tts/TestIndexTTS");
                NarratorAttribute na       = new NarratorAttribute();
                na.withVoiceReference(ref.filename());
                narrator.narrate(na, text);
                Thread.sleep(1000);
            }
        }
    }

    @Test
    public void testEmotionalRange() throws Exception {
        System.out.println("=== Testing Emotional Range ===");

        String text = "The quarterly results show significant progress.";

        // Neutral
        System.out.println("Neutral:");
        narrator.narrate(new NarratorAttribute().withNeutral(1.0f), text);
        Thread.sleep(500);

        // Happy
        System.out.println("Happy:");
        narrator.narrate(new NarratorAttribute().withHappy(0.9f), text);
        Thread.sleep(500);

        // Sad
        System.out.println("Sad:");
        narrator.narrate(new NarratorAttribute().withSad(0.8f), text);
        Thread.sleep(500);

        // Angry
        System.out.println("Angry:");
        narrator.narrate(new NarratorAttribute().withAngry(0.7f), text);
        Thread.sleep(500);

        // Surprised
        System.out.println("Surprised:");
        narrator.narrate(new NarratorAttribute().withSurprise(0.9f), text);
    }

    @Test
    public void testSpeech_01_Basic() throws Exception {
//        seleniumHandler.startRecording("TestIndexTTS", "testSpeech_01_Basic");
        narrator.narrate("Welcome to the Kassandra demonstration. My name is powered by Index TTS.");
//        seleniumHandler.destroy();
    }

    @Test
    public void testSpeech_02_Simple() throws Exception {
        narrator.narrate("Kassandra is a project planning and progress tracking server.");
    }

    @Test
    public void testSpeech_03_WithSpeed() throws Exception {
        NarratorAttribute attrs = new NarratorAttribute();
        attrs.setSpeed(1.3f);  // Faster speech

        narrator.narrate(attrs, "This text is spoken faster than normal speed.");
    }

    @Test
    public void testSpeech_04_Happy() throws Exception {
        NarratorAttribute attrs = new NarratorAttribute()
                .withHappy(0.8f);

        narrator.narrate(attrs, "I'm so excited to announce that the project is complete!");
    }

    @Test
    public void testSpeech_05_Sad() throws Exception {
        NarratorAttribute attrs = new NarratorAttribute()
                .withSad(0.7f);

        narrator.narrate(attrs, "Unfortunately, we encountered some setbacks in the development.");
    }

    @Test
    public void testSpeech_06_Angry() throws Exception {
        NarratorAttribute attrs = new NarratorAttribute()
                .withAngry(0.6f);

        narrator.narrate(attrs, "This is completely unacceptable! The deadline was yesterday!");
    }

    @Test
    public void testSpeech_07_Surprise() throws Exception {
        NarratorAttribute attrs = new NarratorAttribute()
                .withSurprise(0.9f);

        narrator.narrate(attrs, "Wow! I can't believe we finished ahead of schedule!");
    }

    @Test
    public void testSpeech_08_MixedEmotions() throws Exception {
        NarratorAttribute attrs = new NarratorAttribute()
                .withHappy(0.5f)
                .withSurprise(0.3f);

        narrator.narrate(attrs, "The results are in, and they're better than expected!");
    }

    @Test
    public void testSpeech_09_WithVoice() throws Exception {
        IndexTTS.VoiceReference[] refs = IndexTTS.listVoiceReferences();
        if (refs.length > 0) {
            System.out.println("Using voice reference: " + refs[0].filename());
            NarratorAttribute attrs = new NarratorAttribute()
                    .withVoiceReference(refs[0].path());

            narrator.narrate(attrs, "Testing with a specific voice reference.");
        } else {
            System.out.println("No voice references available. Using default voice.");
            narrator.narrate("Testing with default voice.");
        }
    }

    @Test
    public void testSpeech_10_ComplexAttributes() throws Exception {
        NarratorAttribute attrs = new NarratorAttribute()
                .withSpeed(0.9f)        // Slightly slower
                .withHappy(0.4f)        // Moderately happy
                .withSurprise(0.2f);    // Slightly surprised

        attrs.setTemperature(0.8f);     // More variation

        narrator.narrate(attrs, "This is a complex test with multiple emotional parameters and custom speed.");
    }

    @Test
    public void testSpeedVariations() throws Exception {
        String text = "This sentence tests different speed variations.";

        System.out.println("=== Testing Speed Variations ===");

        System.out.println("Slow (0.7x):");
        narrator.narrate(new NarratorAttribute().withSpeed(0.7f), text);
        Thread.sleep(500);

        System.out.println("Normal (1.0x):");
        narrator.narrate(new NarratorAttribute().withSpeed(1.0f), text);
        Thread.sleep(500);

        System.out.println("Fast (1.5x):");
        narrator.narrate(new NarratorAttribute().withSpeed(1.5f), text);
    }
}

