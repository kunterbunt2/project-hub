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

import de.bushnaq.abdalla.projecthub.ai.chatterbox.ChatterboxTTS;
import de.bushnaq.abdalla.projecthub.ai.indextts.IndexTTS;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Narrator orchestrates text-to-speech synthesis and audio playback.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Builds a canonical name for the text + parameters via {@link TtsCacheManager#buildFileName(String, float, float, float)}</li>
 *   <li>Requests a chronological target path from {@link TtsCacheManager#prepareChronological(String)}</li>
 *   <li>Synthesizes audio only when the current id is not up-to-date, then writes it via {@link TtsCacheManager#writeChronological(byte[], Path)}</li>
 *   <li>Queues playback with {@link AudioPlayer}</li>
 * </ul>
 */
public class Narrator {

    private static final Logger            logger = LoggerFactory.getLogger(Narrator.class);
    private final        AudioPlayer       audioPlayer;  // playback queue/handles
    private final        TtsCacheManager   cacheManager; // chronological file coordinator
    @Getter
    @Setter
    private              NarratorAttribute defaultAttributes; // default TTS attributes for this narrator
    @Getter
    private volatile     Playback          playback; // most recently scheduled playback for external access
    @Getter
    @Setter
    private static       long              startTime;//used by VideoRecorder to sync time with audio playing (only used to log the time)
    private final        TtsEngine         ttsEngine;    // synthesis strategy

    /**
     * Creates a Narrator storing audio under {@code relativeFolder} and using the default TTS engine.
     */
    public Narrator(String relativeFolder) {
        this(relativeFolder, chatterboxTtsEngine());
    }


    /**
     * Creates a Narrator storing audio under {@code relativeFolder} and using a provided {@link TtsEngine}.
     *
     * @param relativeFolder output directory for chronological WAV files
     * @param engine         TTS engine implementation used to synthesize audio
     */
    public Narrator(String relativeFolder, TtsEngine engine) {
        Path audioDir = Path.of(relativeFolder);
        this.cacheManager      = new TtsCacheManager(audioDir);
        this.audioPlayer       = new AudioPlayer();
        this.ttsEngine         = engine;
        this.defaultAttributes = new NarratorAttribute(0.5f, 1.0f, 0.5f);
    }

    private static TtsEngine chatterboxTtsEngine() {
        return (text, attrs) -> ChatterboxTTS.generateSpeech(
                text,
                attrs.getTemperature() != null ? attrs.getTemperature() : 0.5f,
                attrs.getExaggeration() != null ? attrs.getExaggeration() : 0.5f,
                attrs.getCfg_weight() != null ? attrs.getCfg_weight() : 1.0f
        );
    }

    public static String getElapsedNarrationTime() {
        long   now          = System.currentTimeMillis();
        long   elapsedMs    = now - Narrator.getStartTime();
        long   secondsTotal = elapsedMs / 1000;
        long   hours        = secondsTotal / 3600;
        long   minutes      = (secondsTotal % 3600) / 60;
        long   seconds      = secondsTotal % 60;
        String timeString   = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return timeString;
    }

    /**
     * Creates TTS engine for Index TTS with specific voice
     */
    private static TtsEngine indexTtsEngine() {
        return (text, attrs) -> {
            String voiceReference  = attrs.getVoiceReference();
            Float  speed           = attrs.getSpeed();
            Float  emotionAngry    = attrs.getEmotion_angry();
            Float  emotionHappy    = attrs.getEmotion_happy();
            Float  emotionSad      = attrs.getEmotion_sad();
            Float  emotionSurprise = attrs.getEmotion_surprise();
            Float  emotionNeutral  = attrs.getEmotion_neutral();
            Float  temperature     = attrs.getTemperature();

            return IndexTTS.generateSpeech(text, voiceReference, speed,
                    emotionAngry, emotionHappy, emotionSad,
                    emotionSurprise, emotionNeutral, temperature);
        };
    }

    /**
     * Sleeps the current thread for roughly half a second.
     */
    public void longPause() throws InterruptedException {
        Thread.sleep(1000);
    }

    /**
     * Synchronously synthesize and play using per-call attributes. Blocks until playback finishes.
     */
    public Narrator narrate(NarratorAttribute attrs, String text) throws Exception {
        narrateAsync(attrs, text);
        getPlayback().await();
        return this;
    }

    /**
     * Synchronously synthesize and play the given text using current instance defaults.
     * Blocks until playback finishes.
     */
    public Narrator narrate(String text) throws Exception {
        narrateAsync(text);
        getPlayback().await();
        return this;
    }

    /**
     * Asynchronously synthesize and queue playback using per-call attributes.
     * Nullable fields in {@code attrs} fall back to instance defaults.
     */
    public Narrator narrateAsync(NarratorAttribute attrs, String text) throws Exception {
        float eTemp = attrs != null && attrs.getTemperature() != null ? attrs.getTemperature() : defaultAttributes.getTemperature();
        float eEx   = attrs != null && attrs.getExaggeration() != null ? attrs.getExaggeration() : defaultAttributes.getExaggeration();
        float eCfg  = attrs != null && attrs.getCfg_weight() != null ? attrs.getCfg_weight() : defaultAttributes.getCfg_weight();
        return narrateResolved(eTemp, eEx, eCfg, text);
    }

    /**
     * Asynchronously synthesize and queue playback using instance defaults.
     * Returns immediately with a {@link Playback} handle available via {@link #getPlayback()}.
     */
    public Narrator narrateAsync(String text) throws Exception {
        float eTemp = defaultAttributes.getTemperature();
        float eEx   = defaultAttributes.getExaggeration();
        float eCfg  = defaultAttributes.getCfg_weight();
        return narrateResolved(eTemp, eEx, eCfg, text);
    }

    /**
     * Core flow:
     * <ol>
     *   <li>Build canonical name containing sanitized text + hash.</li>
     *   <li>Ask {@link TtsCacheManager} for the next id's plan.</li>
     *   <li>If up-to-date, reuse the file; otherwise synthesize and write to the plan path.</li>
     *   <li>Queue playback and expose the {@link Playback} handle.</li>
     * </ol>
     */
    private Narrator narrateResolved(float eTemp, float eEx, float eCfg, String text) throws Exception {
        String                     canonicalName = cacheManager.buildFileName(text, eTemp, eEx, eCfg);
        TtsCacheManager.ChronoPlan plan          = cacheManager.prepareChronological(canonicalName);

        Path pathToPlay;
        if (plan.upToDate()) {
            // Already matches; reuse
            pathToPlay = plan.path();
            logger.debug("Narration up-to-date at {}", pathToPlay.getFileName());
        } else {
            long t0 = System.nanoTime();
            logger.info("TTS generate start: temp={}, ex={}, cfg={}, file={}, text=\"{}\"", eTemp, eEx, eCfg, plan.path().getFileName(), text);

            // Create NarratorAttribute with resolved values
            NarratorAttribute resolvedAttrs = new NarratorAttribute(eEx, eCfg, eTemp);
            resolvedAttrs.setVoiceReference("/opt/index-tts/voices/chatterbox.wav");
            byte[] audio = ttsEngine.synthesize(text, resolvedAttrs);

            cacheManager.writeChronological(audio, plan.path());
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
            logger.info("TTS generate done:   temp={}, ex={}, cfg={}, file={}, bytes={}, took={} ms", eTemp, eEx, eCfg, plan.path().getFileName(), audio.length, tookMs);
            pathToPlay = plan.path();
        }

        File fileToPlay = cacheManager.toFile(pathToPlay);
        this.playback = audioPlayer.play(fileToPlay);
        return this;
    }

    /**
     * Sleeps the current thread for roughly one second.
     */
    public void pause() throws InterruptedException {
        Thread.sleep(500);
    }

    public static Narrator withChatterboxTTS(String relativeFolder) {
        return new Narrator(relativeFolder, chatterboxTtsEngine());
    }

    /**
     * Creates a Narrator using Index TTS engine with default voice
     */
    public static Narrator withIndexTTS(String relativeFolder) {
        return new Narrator(relativeFolder, indexTtsEngine());
    }

}
