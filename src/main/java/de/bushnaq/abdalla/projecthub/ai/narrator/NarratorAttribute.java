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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Getter
@Setter
public class NarratorAttribute {
    Float  cfg_weight;
    Float  emotion_angry;    // Anger emotion level (0.0 to 1.0)
    Float  emotion_happy;    // Happiness emotion level (0.0 to 1.0)
    Float  emotion_neutral;  // Neutral emotion level (0.0 to 1.0)
    Float  emotion_sad;      // Sadness emotion level (0.0 to 1.0)
    Float  emotion_surprise; // Surprise emotion level (0.0 to 1.0)
    Float  exaggeration;
    Float  speed;            // Speech speed multiplier (0.5 to 2.0, default 1.0)
    Float  temperature;
    String voiceReference;  // Voice reference path for cloning (e.g., "/opt/index-tts/voices/my_voice.wav")

    public NarratorAttribute() {
    }


    /**
     * Generates a string representation of all non-null attributes suitable for hashing.
     * Uses consistent formatting for floating-point values.
     *
     * @return formatted string with all non-null attributes for hash computation
     */
    public String toHashString() {
        DecimalFormat df = new DecimalFormat("0.########", DecimalFormatSymbols.getInstance(Locale.ROOT));
        StringBuilder sb = new StringBuilder();

        if (temperature != null) {
            sb.append("|temp=").append(df.format(temperature));
        }
        if (exaggeration != null) {
            sb.append("|ex=").append(df.format(exaggeration));
        }
        if (cfg_weight != null) {
            sb.append("|cfg=").append(df.format(cfg_weight));
        }
        if (speed != null) {
            sb.append("|speed=").append(df.format(speed));
        }
        if (emotion_angry != null) {
            sb.append("|angry=").append(df.format(emotion_angry));
        }
        if (emotion_happy != null) {
            sb.append("|happy=").append(df.format(emotion_happy));
        }
        if (emotion_sad != null) {
            sb.append("|sad=").append(df.format(emotion_sad));
        }
        if (emotion_surprise != null) {
            sb.append("|surprise=").append(df.format(emotion_surprise));
        }
        if (emotion_neutral != null) {
            sb.append("|neutral=").append(df.format(emotion_neutral));
        }
        if (voiceReference != null) {
            sb.append("|voice=").append(voiceReference);
        }

        return sb.toString();
    }

    /**
     * Formats attributes for logging or display, showing only non-null values.
     *
     * @return formatted string with all non-null attributes
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (temperature != null) sb.append("temp=").append(temperature).append(" ");
        if (exaggeration != null) sb.append("ex=").append(exaggeration).append(" ");
        if (cfg_weight != null) sb.append("cfg=").append(cfg_weight).append(" ");
        if (speed != null) sb.append("speed=").append(speed).append(" ");
        if (emotion_angry != null) sb.append("angry=").append(emotion_angry).append(" ");
        if (emotion_happy != null) sb.append("happy=").append(emotion_happy).append(" ");
        if (emotion_sad != null) sb.append("sad=").append(emotion_sad).append(" ");
        if (emotion_surprise != null) sb.append("surprise=").append(emotion_surprise).append(" ");
        if (emotion_neutral != null) sb.append("neutral=").append(emotion_neutral).append(" ");
        if (voiceReference != null) sb.append("voice=").append(voiceReference).append(" ");
        return sb.toString().trim();
    }

    public NarratorAttribute withAngry(Float level) {
        this.emotion_angry = level;
        return this;
    }

    public NarratorAttribute withCfgWeight(Float cfgWeight) {
        this.cfg_weight = cfgWeight;
        return this;
    }

    public NarratorAttribute withExaggeration(Float exaggeration) {
        this.exaggeration = exaggeration;
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

    public NarratorAttribute withTemperature(Float temperature) {
        this.temperature = temperature;
        return this;
    }

    public NarratorAttribute withVoiceReference(String voiceReference) {
        this.voiceReference = voiceReference;
        return this;
    }
}
