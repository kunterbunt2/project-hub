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

package de.bushnaq.abdalla.projecthub.ai.chatterbox;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NarratorAttribute {
    // Nullable fields: when null, fall back to stack or instance defaults
    Float cfg_weight;
    Float exaggeration;
    Float temperature;

    public NarratorAttribute() {
    }

    public NarratorAttribute(Float exaggeration, Float cfg_weight, Float temperature) {
        this.exaggeration = exaggeration;
        this.cfg_weight   = cfg_weight;
        this.temperature  = temperature;
    }
}
