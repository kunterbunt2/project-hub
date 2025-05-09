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

package de.bushnaq.abdalla.projecthub.rest;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.awt.*;
import java.io.IOException;

public class ColorDeserializer extends JsonDeserializer<Color> {
    @Override
    public Color deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        String json = p.getText();
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            // If color string has # prefix, use Color.decode
            if (json.startsWith("#")) {
                return new Color((int) Long.parseLong(json.substring(1), 16), true);
            } else {
                // Parse integer RGB value
                return new Color((int) Long.parseLong(json, 16), true);
            }
        } catch (NumberFormatException e) {
            throw new IOException("Invalid color format: " + json, e);
        }
    }
}
