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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Narrator {

    private static final Pattern                  ID_PREFIX    = Pattern.compile("^(\\d{3,})-.*");
    private static final Logger                   logger       = LoggerFactory.getLogger(Narrator.class);
    // Single composite attribute stack: top frame holds latest overrides; search top-down
    private final        Deque<NarratorAttribute> attrStack    = new ArrayDeque<>();
    private final        Path                     audioDir;// Directory to store generated audio files
    private final        Path                     cacheDir;// Subfolder for canonical cached files
    @Getter
    @Setter
    private              float                    cfgWeight;
    @Getter
    @Setter
    private              float                    exaggeration;
    private              Playback                 lastPlayback = null;
    private              int                      nextId; // incrementing file id per audioDir
    // Serialize all playback: each request waits for the previous to finish before starting
    private final        Object                   queueLock    = new Object();
    @Getter
    @Setter
    private              float                    temperature;

    public Narrator(String relativeFolder, float temperature, float exaggeration, float cfgWeight) {
        this.audioDir     = Path.of(relativeFolder);
        this.cacheDir     = this.audioDir.resolve("cache");
        this.temperature  = temperature;
        this.exaggeration = exaggeration;
        this.cfgWeight    = cfgWeight;
        try {
            Files.createDirectories(this.audioDir);
            Files.createDirectories(this.cacheDir);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create audio output directory: " + this.audioDir, e);
        }
        initNextId();
        // Log available playback mixers to help choose a device
        logAvailablePlaybackMixers();
    }

    private String buildFileName(String text, float temperature, float exaggeration, float cfgWeight) {
        String prefix = sanitizePrefix(crop(text, 20));
        if (prefix.isBlank()) prefix = "tts";

        // Locale-stable formatting for floats
        DecimalFormat df      = new DecimalFormat("0.########", DecimalFormatSymbols.getInstance(Locale.ROOT));
        String        tempStr = df.format(temperature);
        String        exStr   = df.format(exaggeration);
        String        cfgStr  = df.format(cfgWeight);

        String toHash = text + "|temp=" + tempStr + "|ex=" + exStr + "|cfg=" + cfgStr;
        String hash   = shortSha256Hex(toHash, 12);
        return prefix + "_" + hash + ".wav";
    }

    private void cleanupCacheSiblings(String canonicalName) {
        // canonicalName format: <prefix>_<hash>.wav ; delete other files with same <prefix>_*.wav
        int sep = canonicalName.lastIndexOf('_');
        int dot = canonicalName.lastIndexOf('.');
        if (sep <= 0 || dot <= sep) return;
        String prefix = canonicalName.substring(0, sep) + "_"; // include underscore separator
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

    private static String crop(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }

    // Resolve effective values: look from top-most frame down; fall back to defaults
    private float effectiveCfgWeight() {
        for (NarratorAttribute f : attrStack) {
            if (f.cfg != null) return f.cfg;
        }
        return cfgWeight;
    }

    private float effectiveExaggeration() {
        for (NarratorAttribute f : attrStack) {
            if (f.ex != null) return f.ex;
        }
        return exaggeration;
    }

    private float effectiveTemperature() {
        for (NarratorAttribute f : attrStack) {
            if (f.temp != null) return f.temp;
        }
        return temperature;
    }

    /**
     * Find a playback mixer by name from system property or environment variable, else return null.
     * Matching is case-insensitive and checks both name and description contains the token.
     * <p>
     * System property: projecthub.audio.playback.mixer
     * Env var: PROJECTHUB_AUDIO_PLAYBACK_MIXER
     */
    private static Mixer.Info findConfiguredPlaybackMixer() {
        String token = "direct audio device: directsound playback"; // Replace with actual retrieval logic
//        String token = System.getProperty("projecthub.audio.playback.mixer");
//        if (token == null || token.isBlank()) {
//            token = System.getenv("PROJECTHUB_AUDIO_PLAYBACK_MIXER");
//        }
//        if (token == null || token.isBlank()) return null;
        String t = token.toLowerCase(Locale.ROOT);
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            String name = info.getName() == null ? "" : info.getName().toLowerCase(Locale.ROOT);
            String desc = info.getDescription() == null ? "" : info.getDescription().toLowerCase(Locale.ROOT);
            if (name.contains(t) || desc.contains(t)) {
                logger.warn("using playback mixer '{}'.", token);
                return info;
            }
        }
        logger.warn("Requested playback mixer '{}' not found. Falling back to default.", token);
        return null;
    }

    private void initNextId() {
        int max = 0;
        try {
            if (Files.isDirectory(audioDir)) {
                try (var stream = Files.list(audioDir)) {
                    for (Path p : (Iterable<Path>) stream::iterator) {
                        String  name = p.getFileName().toString();
                        Matcher m    = ID_PREFIX.matcher(name);
                        if (m.matches()) {
                            try {
                                int id = Integer.parseInt(m.group(1));
                                if (id > max) max = id;
                            } catch (NumberFormatException ignore) {
                                // skip
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to scan audioDir for id prefix: {}", audioDir, e);
        }
        this.nextId = max + 1; // reset per directory
        logger.debug("Initialized nextId={} for directory {}", this.nextId, audioDir);
    }

    /**
     * Enumerate and log all playback-capable mixers (supporting Clip or SourceDataLine).
     */
    private static void logAvailablePlaybackMixers() {
        try {
            for (Mixer.Info info : AudioSystem.getMixerInfo()) {
                Mixer   mixer          = AudioSystem.getMixer(info);
                boolean supportsClip   = mixer.isLineSupported(new DataLine.Info(Clip.class, null));
                boolean supportsSource = mixer.isLineSupported(new DataLine.Info(javax.sound.sampled.SourceDataLine.class, null));
                if (supportsClip || supportsSource) {
                    logger.info("Playback device: name='{}', desc='{}', vendor='{}', version='{}' (Clip={}, SourceDataLine={})",
                            info.getName(), info.getDescription(), info.getVendor(), info.getVersion(), supportsClip, supportsSource);
                }
            }
        } catch (Throwable t) {
            logger.warn("Failed to enumerate playback mixers", t);
        }
    }

    // Blocking convenience method preserving existing behavior
    public void narrate(String text) throws Exception {
        narrateAsync(text).await();
    }

    // New: Blocking narration with per-call attributes override
    public void narrate(NarratorAttribute attrs, String text) throws Exception {
        narrateAsync(attrs, text).await();
    }

    // Non-blocking narration that returns a handle to await or stop playback
    public Playback narrateAsync(String text) throws Exception {
        // Resolve effective attributes from frames or defaults
        float eTemp = effectiveTemperature();
        float eEx   = effectiveExaggeration();
        float eCfg  = effectiveCfgWeight();
        return narrateResolved(eTemp, eEx, eCfg, text);
    }

    // New: Non-blocking narration with per-call attributes override
    public Playback narrateAsync(NarratorAttribute attrs, String text) throws Exception {
        // If attrs provided, take precedence; else fall back to stack/defaults
        float eTemp = attrs != null && attrs.getTemp() != null ? attrs.getTemp() : effectiveTemperature();
        float eEx   = attrs != null && attrs.getEx() != null ? attrs.getEx() : effectiveExaggeration();
        float eCfg  = attrs != null && attrs.getCfg() != null ? attrs.getCfg() : effectiveCfgWeight();
        return narrateResolved(eTemp, eEx, eCfg, text);
    }

    // Core implementation shared by both overloads
    private Playback narrateResolved(float eTemp, float eEx, float eCfg, String text) throws Exception {
        String canonicalName = buildFileName(text, eTemp, eEx, eCfg);
        Path   out           = cacheDir.resolve(canonicalName);

        // Remove stale cache entries with same prefix but different hash
        cleanupCacheSiblings(canonicalName);

        // Determine chronological filename with incrementing id
        String idPrefix = nextIdPrefix();
        Path   idOut    = audioDir.resolve(idPrefix + canonicalName);
        while (Files.exists(idOut)) { // extremely rare, but avoid collisions
            idPrefix = nextIdPrefix();
            idOut    = audioDir.resolve(idPrefix + canonicalName);
        }

        if (!Files.exists(out)) {
            long t0 = System.nanoTime();
            logger.info("TTS generate start: temp={}, ex={}, cfg={}, file={}, text=\"{}\"", eTemp, eEx, eCfg, out.getFileName(), text);
            byte[] audio = ChatterboxTTS.generateSpeech(text, eTemp, eEx, eCfg);
//            byte[] audio = CoquiTTS.generateSpeech(text);
            Files.write(out, audio);
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
            logger.info("TTS generate done:   temp={}, ex={}, cfg={}, file={}, bytes={}, took={} ms", eTemp, eEx, eCfg, out.getFileName(), audio.length, tookMs);
        } else {
            logger.debug("TTS cache hit: canonical file={}", out.getFileName());
        }

        // Create chronological file as a copy of canonical for sortable history
        try {
            Files.copy(out, idOut, StandardCopyOption.REPLACE_EXISTING);
            logger.debug("Saved chronological file: {} -> {}", out.getFileName(), idOut.getFileName());
        } catch (IOException e) {
            logger.warn("Failed to create chronological file {} from {}", idOut.getFileName(), out.getFileName(), e);
        }

        final Playback thisPlayback = new Playback();
        final Playback prevPlayback;
        synchronized (queueLock) {
            prevPlayback = lastPlayback;
            lastPlayback = thisPlayback;
        }

        final File file = idOut.toFile(); // play the chronological file
        Thread t = new Thread(() -> {
            try {
                // Wait for any previous playback to finish
                if (prevPlayback != null) prevPlayback.await();

                // If stopped before starting, complete and return
                if (thisPlayback.isCanceled()) {
                    thisPlayback.finishEarly();
                    return;
                }

                Clip clip = null;
//                Mixer.Info chosen = findConfiguredPlaybackMixer();
//                try {
//                    if (chosen != null) {
//                        Mixer mixer = AudioSystem.getMixer(chosen);
//                        if (mixer.isLineSupported(new DataLine.Info(Clip.class, null))) {
//                            clip = (Clip) mixer.getLine(new DataLine.Info(Clip.class, null));
//                            logger.info("Using configured playback mixer: {}", chosen.getName());
//                        }
//                    }
//                } catch (Exception e) {
//                    logger.warn("Failed to get Clip from configured mixer, falling back to default: {}", e.toString());
//                    clip = null;
//                }
                if (clip == null) {
                    clip = AudioSystem.getClip();
                }
                thisPlayback.setClip(clip);

                clip.addLineListener(ev -> {
                    if (ev.getType() == LineEvent.Type.STOP) {
                        thisPlayback.countDown();
                    }
                });

                clip.open(AudioSystem.getAudioInputStream(file));

                if (thisPlayback.isCanceled()) {
                    thisPlayback.finishEarly();
                    return;
                }

                clip.start();
                thisPlayback.await(); // wait until STOP event or external stop
            } catch (Exception e) {
                thisPlayback.countDown(); // ensure latch is released on failure
            } finally {
                thisPlayback.closeQuietly();
            }
        }, "Narrator-Playback");
        t.setDaemon(true);
        t.start();

        return thisPlayback;
    }

    private String nextIdPrefix() {
        int id = nextId++;
        return (id < 1000)
                ? String.format(Locale.ROOT, "%03d-", id)
                : id + "-";
    }

    public void pause(float seconds) throws InterruptedException {
        Thread.sleep((long) (seconds * 1000));
    }

    // Pop entire frame: removes all attributes pushed into the current frame
    @Deprecated
    public void pop() {
        if (!attrStack.isEmpty()) attrStack.pop();
    }

    @Deprecated
    public Narrator pushCfgWeight(float value) {
        topOrNew().cfg = value;
        return this;
    }

    @Deprecated
    public Narrator pushExaggeration(float value) {
        topOrNew().ex = value;
        return this;
    }

    @Deprecated
    public Narrator pushTemperature(float value) {
        topOrNew().temp = value;
        return this;
    }

    private static String sanitizePrefix(String s) {
        // Replace non-filename-safe characters with '_', collapse repeats, trim underscores
        String cleaned = s
                .replaceAll("\\s+", "_")
                .replaceAll("[^A-Za-z0-9._-]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");
        // prevent empty or dot-only names
        if (cleaned.isBlank() || cleaned.equals(".") || cleaned.equals("..")) {
            return "tts";
        }
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
            // Fallback to simple hashCode if SHA-256 not available
            return Integer.toHexString(input.hashCode());
        }
    }

    // Helper: ensure there's a top frame to write into
    private NarratorAttribute topOrNew() {
        NarratorAttribute top = attrStack.peek();
        if (top == null) {
            top = new NarratorAttribute();
            attrStack.push(top);
        }
        return top;
    }

    // Playback handle for non-blocking control with queue support
    public static class Playback {
        private volatile boolean        canceled = false;
        private volatile Clip           clip;
        private volatile boolean        closed   = false;
        private final    CountDownLatch done     = new CountDownLatch(1);

        private Playback() {
        }

        public void await() throws InterruptedException {
            done.await();
            closeQuietly();
        }

        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            boolean finished = done.await(timeout, unit);
            if (finished) closeQuietly();
            return finished;
        }

        void closeQuietly() {
            if (!closed) {
                closed = true;
                Clip c = this.clip;
                if (c != null) {
                    try {
                        c.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        void countDown() {
            if (done.getCount() > 0) done.countDown();
        }

        void finishEarly() {
            countDown();
            closeQuietly();
        }

        boolean isCanceled() {
            return canceled;
        }

        public boolean isDone() {
            return done.getCount() == 0;
        }

        void setClip(Clip clip) {
            this.clip = clip;
        }

        public void stop() {
            canceled = true;
            Clip c = this.clip;
            if (c != null) {
                try {
                    c.stop();
                } catch (Exception ignored) {
                }
            } else {
                countDown();
                closeQuietly();
            }
        }
    }

}
