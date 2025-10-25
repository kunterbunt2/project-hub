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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Manages cache and chronological copies for generated TTS audio.
 * Responsibilities:
 * - Build deterministic canonical file name based on text + attributes
 * - Create and clean cache directory
 * - Persist/resolve canonical vs chronological files
 */
public class TtsCacheManager {
    private static final Logger logger = LoggerFactory.getLogger(TtsCacheManager.class);

    @Getter
    private final Path audioDir; // root for chronological files
    @Getter
    private final Path cacheDir; // subdir for canonical files
    private       int  nextId;

    public TtsCacheManager(Path audioDir) {
        this.audioDir = audioDir;
        this.cacheDir = audioDir.resolve("cache");
        try {
            Files.createDirectories(this.audioDir);
            Files.createDirectories(this.cacheDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create audio/cache directories under " + audioDir, e);
        }
        initNextId();
    }

    public String buildFileName(String text, float temperature, float exaggeration, float cfgWeight) {
        String prefix = sanitizePrefix(crop(text, 20));
        if (prefix.isBlank()) prefix = "tts";
        DecimalFormat df      = new DecimalFormat("0.########", DecimalFormatSymbols.getInstance(Locale.ROOT));
        String        tempStr = df.format(temperature);
        String        exStr   = df.format(exaggeration);
        String        cfgStr  = df.format(cfgWeight);
        String        toHash  = text + "|temp=" + tempStr + "|ex=" + exStr + "|cfg=" + cfgStr;
        String        hash    = shortSha256Hex(toHash, 12);
        return prefix + "_" + hash + ".wav";
    }

    public Path canonicalPath(String canonicalName) {
        return cacheDir.resolve(canonicalName);
    }

    public Path chronologicalPath(String canonicalName) {
        String idPrefix = nextIdPrefix();
        Path   idOut    = audioDir.resolve(idPrefix + canonicalName);
        while (Files.exists(idOut)) {
            idPrefix = nextIdPrefix();
            idOut    = audioDir.resolve(idPrefix + canonicalName);
        }
        return idOut;
    }

    public void cleanupCacheSiblings(String canonicalName) {
        int sep = canonicalName.lastIndexOf('_');
        int dot = canonicalName.lastIndexOf('.');
        if (sep <= 0 || dot <= sep) return;
        String prefix = canonicalName.substring(0, sep) + "_";
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(cacheDir, path -> {
            String n = path.getFileName().toString();
            return n.startsWith(prefix) && n.endsWith(".wav") && !n.equals(canonicalName);
        })) {
            for (Path stale : stream) {
                try {
                    Files.deleteIfExists(stale);
                    logger.debug("Deleted stale cache file {}", stale.getFileName());
                } catch (IOException e) {
                    logger.warn("Failed to delete stale cache file {}", stale.getFileName(), e);
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to list cache dir {} for cleanup", cacheDir, e);
        }
    }

    public Path copyToChronological(Path canonical, String canonicalName) {
        Path idOut = chronologicalPath(canonicalName);
        try {
            Files.copy(canonical, idOut, StandardCopyOption.REPLACE_EXISTING);
            logger.debug("Saved chronological file: {} -> {}", canonical.getFileName(), idOut.getFileName());
        } catch (IOException e) {
            logger.warn("Failed to create chronological file {} from {}", idOut.getFileName(), canonical.getFileName(), e);
        }
        return idOut;
    }

    public static String crop(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }

    private void initNextId() {
        int max = 0;
        try {
            if (Files.isDirectory(audioDir)) {
                try (var stream = Files.list(audioDir)) {
                    for (Path p : (Iterable<Path>) stream::iterator) {
                        String name = p.getFileName().toString();
                        // matches NNN- prefix
                        if (name.length() >= 4 && name.charAt(3) == '-') {
                            try {
                                int id = Integer.parseInt(name.substring(0, 3));
                                if (id > max) max = id;
                            } catch (NumberFormatException ignore) {
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to scan audioDir for id prefix: {}", audioDir, e);
        }
        this.nextId = max + 1;
        logger.debug("Initialized nextId={} for directory {}", this.nextId, audioDir);
    }

    private String nextIdPrefix() {
        int id = nextId++;
        return (id < 1000) ? String.format(Locale.ROOT, "%03d-", id) : id + "-";
    }

    public static String sanitizePrefix(String s) {
        String cleaned = s
                .replaceAll("\\s+", "_")
                .replaceAll("[^A-Za-z0-9._-]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");
        if (cleaned.isBlank() || cleaned.equals(".") || cleaned.equals("..")) return "tts";
        return cleaned;
    }

    private static String shortSha256Hex(String input, int hexLen) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[]        d  = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(d.length * 2);
            for (byte b : d) sb.append(String.format(Locale.ROOT, "%02x", b));
            String hex = sb.toString();
            return hexLen > 0 && hexLen < hex.length() ? hex.substring(0, hexLen) : hex;
        } catch (Exception e) {
            return Integer.toHexString(input.hashCode());
        }
    }

    public File toFile(Path p) {
        return p.toFile();
    }

    public Path writeCanonical(byte[] wavBytes, String canonicalName) throws IOException {
        Path out = canonicalPath(canonicalName);
        Files.write(out, wavBytes);
        return out;
    }
}

