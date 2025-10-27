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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ChatterboxTTS {
    private static final ObjectMapper objectMapper    = new ObjectMapper();
    private static       String       TTS_SERVICE_URL = "http://localhost:4123";

    public static byte[] generateSpeech(String text, float temperature, float exaggeration, float cfgWeight) throws Exception {
        URL               url  = new URL(TTS_SERVICE_URL + "/v1/audio/speech");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        java.util.Map<String, Object> jsonMap = new java.util.HashMap<>();
        jsonMap.put("input", text); // Chatterbox API expects 'input' not 'text'
        jsonMap.put("temperature", temperature);
        jsonMap.put("exaggeration", exaggeration);
        jsonMap.put("cfg_weight", cfgWeight);
        String jsonPayload = objectMapper.writeValueAsString(jsonMap);
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            String error = readProcessOutput(conn.getErrorStream());
            throw new RuntimeException("Chatterbox TTS service returned error " + responseCode + ": " + error);
        }
        try (InputStream is = conn.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int    bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }

    public static String[] getLanguages() throws Exception {
        URL               url  = new URL(TTS_SERVICE_URL + "/languages");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            String error = readProcessOutput(conn.getErrorStream());
            throw new RuntimeException("Failed to get languages " + responseCode + ": " + error);
        }
        String response = readProcessOutput(conn.getInputStream());
        // Parse as Map and extract 'languages' array
        java.util.Map<?, ?> map          = objectMapper.readValue(response, java.util.Map.class);
        Object              languagesObj = map.get("languages");
        if (languagesObj instanceof List<?> languagesList) {
            List<String> codes = new java.util.ArrayList<>();
            for (Object langObj : languagesList) {
                if (langObj instanceof java.util.Map<?, ?> langMap) {
                    Object code = langMap.get("code");
                    if (code != null) codes.add(code.toString());
                }
            }
            return codes.toArray(new String[0]);
        } else {
            throw new RuntimeException("Unexpected response format: 'languages' field missing or not a list");
        }
    }

    private static String readProcessOutput(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().reduce("", (a, b) -> a + "\n" + b);
        }
    }

    public static void setServiceUrl(String url) {
        TTS_SERVICE_URL = url;
    }

    public static void writeWav(byte[] audioData, String fileName) throws IOException {
        Path outputPath = Paths.get(fileName);
        Files.write(outputPath, audioData);
        System.out.printf("Audio saved to '%s'.\n", outputPath.toAbsolutePath());
    }
}
