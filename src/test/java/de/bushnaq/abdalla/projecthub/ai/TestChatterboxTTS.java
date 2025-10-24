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

import de.bushnaq.abdalla.projecthub.ai.chatterbox.ChatterboxTTS;
import de.bushnaq.abdalla.projecthub.ai.chatterbox.Narrator;
import org.junit.jupiter.api.Test;

public class TestChatterboxTTS {
    Narrator narrator = new Narrator("tts", 1.0f, 0.5f, .3f);

    @Test
    public void listLanguages() throws Exception {

        for (String language : ChatterboxTTS.getLanguages()) {
            System.out.println(language);
        }
    }

    @Test
    public void listVoices() throws Exception {

        for (String voice : ChatterboxTTS.getVoices()) {
            System.out.println(voice);
        }
    }

    @Test
    public void testSpeech_01() throws Exception {

        narrator.narrate("Welcome to the Kassandra demonstration. My name is Abby, and I will be your guide today.");

    }

    @Test
    public void testSpeech_02() throws Exception {
        narrator.narrate("Kassandra is a project planning and progress tracking server.");

    }
}
