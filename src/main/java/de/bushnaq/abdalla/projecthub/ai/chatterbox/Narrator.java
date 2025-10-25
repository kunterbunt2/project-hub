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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Narrator provides text-to-speech generation with on-disk caching and queued audio playback.
 * <p>
 * Responsibilities are split into collaborators:
 * - TtsCacheManager: build canonical name, manage cache/chronological files
 * - TtsEngine: synthesize WAV bytes for text
 * - AudioPlayer: serialized playback with {@link Playback}
 */
public class Narrator {

    private static final Logger logger = LoggerFactory.getLogger(Narrator.class);
    private final AudioPlayer     audioPlayer;  // playback queue/handles
    private final TtsCacheManager cacheManager; // file/cache coordinator
    @Getter
    @Setter
    private float cfgWeight;
    @Getter
    @Setter
    private float exaggeration;
    @Getter
    private volatile Playback playback; // most recently scheduled playback for external access
    @Getter
    @Setter
    private float temperature;
    private final TtsEngine       ttsEngine;    // synthesis strategy

    /**
     * Creates a Narrator storing generated audio under the given folder using default TTS engine.
     */
    public Narrator(String relativeFolder) {
        this(relativeFolder, defaultEngine());
    }

    /**
     * Creates a Narrator with an explicit {@link TtsEngine}.
     */
    public Narrator(String relativeFolder, TtsEngine engine) {
        Path audioDir = Path.of(relativeFolder);
        this.cacheManager = new TtsCacheManager(audioDir);
        this.audioPlayer  = new AudioPlayer();
        this.ttsEngine    = engine;
        // defaults
        this.temperature  = 0.5f;
        this.exaggeration = 0.5f;
        this.cfgWeight    = 1.0f;
    }

    private static TtsEngine defaultEngine() {
        return (text, temp, ex, cfg) -> {
            // Delegate to existing implementation to preserve behavior
            return ChatterboxTTS.generateSpeech(text, temp, ex, cfg);
            // return CoquiTTS.generateSpeech(text);
        };
    }

    /**
     * Synchronously speak the provided text using the current instance defaults.
     */
    public Narrator narrate(String text) throws Exception {
        narrateAsync(text);
        getPlayback().await();
        return this;
    }

    /**
     * Synchronously speak with per-call attributes.
     */
    public Narrator narrate(NarratorAttribute attrs, String text) throws Exception {
        narrateAsync(attrs, text);
        getPlayback().await();
        return this;
    }

    /**
     * Asynchronously speak using instance defaults.
     */
    public Narrator narrateAsync(String text) throws Exception {
        float eTemp = this.temperature;
        float eEx   = this.exaggeration;
        float eCfg  = this.cfgWeight;
        return narrateResolved(eTemp, eEx, eCfg, text);
    }

    /**
     * Asynchronously speak using per-call attributes.
     */
    public Narrator narrateAsync(NarratorAttribute attrs, String text) throws Exception {
        float eTemp = attrs != null && attrs.getTemperature() != null ? attrs.getTemperature() : this.temperature;
        float eEx   = attrs != null && attrs.getExaggeration() != null ? attrs.getExaggeration() : this.exaggeration;
        float eCfg  = attrs != null && attrs.getCfg_weight() != null ? attrs.getCfg_weight() : this.cfgWeight;
        return narrateResolved(eTemp, eEx, eCfg, text);
    }

    // Core orchestration: resolve file from cache, synthesize if missing, copy chronological, queue playback
    private Narrator narrateResolved(float eTemp, float eEx, float eCfg, String text) throws Exception {
        String canonicalName = cacheManager.buildFileName(text, eTemp, eEx, eCfg);
        Path   canonicalPath = cacheManager.canonicalPath(canonicalName);

        cacheManager.cleanupCacheSiblings(canonicalName);

        if (!Files.exists(canonicalPath)) {
            long t0 = System.nanoTime();
            logger.info("TTS generate start: temp={}, ex={}, cfg={}, file={}, text=\"{}\"", eTemp, eEx, eCfg, canonicalName, text);
            byte[] audio = ttsEngine.synthesize(text, eTemp, eEx, eCfg);
            cacheManager.writeCanonical(audio, canonicalName);
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
            logger.info("TTS generate done:   temp={}, ex={}, cfg={}, file={}, bytes={}, took={} ms", eTemp, eEx, eCfg, canonicalName, audio.length, tookMs);
        } else {
            logger.debug("TTS cache hit: canonical file={}", canonicalName);
        }

        Path chronological = cacheManager.copyToChronological(canonicalPath, canonicalName);
        File fileToPlay    = cacheManager.toFile(chronological);

        Playback current = audioPlayer.play(fileToPlay);
        this.playback = current;
        return this;
    }

    /**
     * Sleeps the current thread for 1s.
     */
    public void pause() throws InterruptedException {
        Thread.sleep(1000);
    }

    /**
     * Sleeps the current thread for 0.5s.
     */
    public void shortPause() throws InterruptedException {
        Thread.sleep(500);
    }
}
