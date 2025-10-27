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

package de.bushnaq.abdalla.projecthub.ai.narrator;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NarratorAttribute {
    // Nullable fields: when null, fall back to stack or instance defaults

    // === Chatterbox TTS parameters ===
    Float  cfg_weight;
    // Index TTS Emotional parameters
    Float  emotion_angry;    // Anger emotion level (0.0 to 1.0)
    Float  emotion_happy;    // Happiness emotion level (0.0 to 1.0)
    Float  emotion_neutral;  // Neutral emotion level (0.0 to 1.0)
    Float  emotion_sad;      // Sadness emotion level (0.0 to 1.0)
    Float  emotion_surprise; // Surprise emotion level (0.0 to 1.0)
    Float  exaggeration;
    Float  speed;            // Speech speed multiplier (0.5 to 2.0, default 1.0)
    Float  temperature;
    // === Index TTS parameters ===
    String voiceReference;  // Voice reference path for cloning (e.g., "/opt/index-tts/voices/my_voice.wav")

    public NarratorAttribute() {
    }

    public NarratorAttribute(Float exaggeration, Float cfg_weight, Float temperature) {
        this.exaggeration = exaggeration;
        this.cfg_weight   = cfg_weight;
        this.temperature  = temperature;
    }

    public NarratorAttribute withAngry(Float level) {
        this.emotion_angry = level;
        return this;
    }

    public NarratorAttribute withHappy(Float level) {
        this.emotion_happy = level;
        return this;
    }

    public NarratorAttribute withNeutral(Float level) {
        this.emotion_neutral = level;
        return this;
    }

    public NarratorAttribute withSad(Float level) {
        this.emotion_sad = level;
        return this;
    }

    public NarratorAttribute withSpeed(Float speed) {
        this.speed = speed;
        return this;
    }

    public NarratorAttribute withSurprise(Float level) {
        this.emotion_surprise = level;
        return this;
    }

    @Deprecated
    public NarratorAttribute withVoice(String voice) {
        // Keep for backward compatibility
        this.voiceReference = voice;
        return this;
    }

    // Convenience builder methods for Index TTS emotions
    public NarratorAttribute withVoiceReference(String voiceReference) {
        this.voiceReference = voiceReference;
        return this;
    }
}
