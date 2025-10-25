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

package de.bushnaq.abdalla.projecthub.ui.util.selenium;

import lombok.Getter;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility class for recording screen during UI tests using JavaCV (FFmpeg)
 */
public class VideoRecorder {
    private       String              audioDevice  = System.getProperty("videoRecorder.audioDevice", "auto");
    private       FFmpegFrameGrabber  audioGrabber;
    private       Thread              audioThread;
    private       Rectangle           captureArea;  // Optional explicit capture area
    private       String              currentTestName;
    private final int                 frameRate    = Integer.getInteger("videoRecorder.fps", 60);
    // Config
    private       boolean             includeAudio = Boolean.parseBoolean(System.getProperty("videoRecorder.audioEnabled", "true"));
    @Getter
    private       boolean             isRecording  = false;
    private final Logger              logger       = LoggerFactory.getLogger(this.getClass());
    private       File                outputDirectory;
    private       FFmpegFrameRecorder recorder;
    private final File                rootDirectory;
    private final AtomicBoolean       running      = new AtomicBoolean(false);
    // JavaCV/FFmpeg members
    private       FFmpegFrameGrabber  screenGrabber;
    private       Thread              videoThread;
    private       WebDriver           webDriver;    // Used to compute content-only area

    public VideoRecorder() {
        this(new File("test-recordings"));
    }

    public VideoRecorder(File rootDirectory) {
        this.rootDirectory = rootDirectory;
        // Create directory if it doesn't exist
        rootDirectory.mkdirs();
    }

    private void closeQuietly(FFmpegFrameGrabber grabber) {
        if (grabber == null) return;
        try {
            grabber.stop();
        } catch (Exception ignored) {
        }
        try {
            grabber.release();
        } catch (Exception ignored) {
        }
    }

    private Rectangle computeRecordingArea(boolean contentOnly) {
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration();

        if (contentOnly && webDriver != null) {
            try {
                JavascriptExecutor jsExecutor      = (JavascriptExecutor) webDriver;
                Point              browserPosition = webDriver.manage().window().getPosition();

                Long   viewportX      = (Long) jsExecutor.executeScript("return window.pageXOffset + (window.outerWidth - window.innerWidth);");
                Long   viewportY      = (Long) jsExecutor.executeScript("return window.pageYOffset + (window.outerHeight - window.innerHeight);");
                Long   viewportWidth  = (Long) jsExecutor.executeScript("return window.innerWidth;");
                Long   viewportHeight = (Long) jsExecutor.executeScript("return window.innerHeight;");
                Double dpr            = (Double) jsExecutor.executeScript("return window.devicePixelRatio || 1.0;");

                int contentX = (int) Math.round(browserPosition.getX() + viewportX.doubleValue() * dpr - 8);
                int contentY = (int) Math.round(browserPosition.getY() + viewportY.doubleValue() * dpr - 8);
                int width    = (int) Math.round(viewportWidth.doubleValue() * dpr);
                int height   = (int) Math.round(viewportHeight.doubleValue() * dpr);

                Rectangle r = new Rectangle(contentX, contentY, width, height);
                logger.info("Recording browser content area: x={}, y={}, width={}, height={} (dpr={})", contentX, contentY, width, height, dpr);
                return r;
            } catch (Exception e) {
                logger.warn("Failed to determine browser content dimensions, falling back to browser window", e);
                return captureArea != null ? captureArea : new Rectangle(gc.getBounds());
            }
        } else {
            return captureArea != null ? captureArea : new Rectangle(gc.getBounds());
        }
    }

    private boolean initAudio() {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                String device = resolveWindowsAudioDevice();
                if (device == null) return false;
                audioGrabber = new FFmpegFrameGrabber(device);
                audioGrabber.setFormat("dshow");
                audioGrabber.setSampleRate(44100);
                audioGrabber.setAudioChannels(2);
                audioGrabber.start();
                return true;
            } else if (os.contains("mac")) {
                // macOS: requires loopback device (e.g., BlackHole) configured
                String device = resolveGenericAudioDevice("avfoundation");
                if (device == null) return false;
                audioGrabber = new FFmpegFrameGrabber(device);
                audioGrabber.setFormat("avfoundation");
                audioGrabber.setSampleRate(44100);
                audioGrabber.setAudioChannels(2);
                audioGrabber.start();
                return true;
            } else {
                // Linux: pulse (recommended) or alsa
                String device = resolveGenericAudioDevice("pulse");
                if (device == null) return false;
                audioGrabber = new FFmpegFrameGrabber(device);
                audioGrabber.setFormat("pulse");
                audioGrabber.setSampleRate(44100);
                audioGrabber.setAudioChannels(2);
                audioGrabber.start();
                return true;
            }
        } catch (Exception e) {
            logger.warn("Failed to initialize audio grabber: {}", e.toString());
            closeQuietly(audioGrabber);
            audioGrabber = null;
            return false;
        }
    }

    private void initVideo(Rectangle area) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                // Windows: use gdigrab
                screenGrabber = new FFmpegFrameGrabber("desktop");
                screenGrabber.setFormat("gdigrab");
                screenGrabber.setFrameRate(frameRate);
                screenGrabber.setOption("draw_mouse", "1");
                screenGrabber.setOption("video_size", area.width + "x" + area.height);
                screenGrabber.setOption("offset_x", Integer.toString(area.x));
                screenGrabber.setOption("offset_y", Integer.toString(area.y));
            } else if (os.contains("mac")) {
                // macOS: use avfoundation; region cropping via filter later if needed
                // Default device 1 for screen; JavaCV expects device index like ":1"
                screenGrabber = new FFmpegFrameGrabber("1");
                screenGrabber.setFormat("avfoundation");
                screenGrabber.setFrameRate(frameRate);
                screenGrabber.setImageWidth(area.width);
                screenGrabber.setImageHeight(area.height);
                // avfoundation lacks direct offset; full-screen then crop is typical (omitted for now)
            } else {
                // Linux: use x11grab
                String display = System.getenv().getOrDefault("DISPLAY", ":0.0");
                screenGrabber = new FFmpegFrameGrabber(display);
                screenGrabber.setFormat("x11grab");
                screenGrabber.setFrameRate(frameRate);
                screenGrabber.setOption("video_size", area.width + "x" + area.height);
                screenGrabber.setOption("grab_x", Integer.toString(area.x));
                screenGrabber.setOption("grab_y", Integer.toString(area.y));
            }
            screenGrabber.start();
        } catch (Exception e) {
            closeQuietly(screenGrabber);
            throw new IOException("Failed to initialize screen grabber", e);
        }
    }

    private void pumpAudio() {
        while (running.get()) {
            try {
                Frame af = audioGrabber.grabSamples();
                if (af != null) {
                    synchronized (this) {
                        recorder.record(af);
                    }
                }
            } catch (Exception e) {
                if (running.get()) {
                    logger.warn("Audio capture error: {}", e.toString());
                }
                break;
            }
        }
    }

    private void pumpVideo() {
        long frameIntervalMillis = Math.max(1, Math.round(1000.0 / Math.max(1, frameRate)));
        while (running.get()) {
            try {
                Frame vf = screenGrabber.grab();
                if (vf != null) {
                    synchronized (this) {
                        recorder.record(vf);
                    }
                }
                Thread.sleep(frameIntervalMillis);
            } catch (Exception e) {
                if (running.get()) {
                    logger.warn("Video capture error: {}", e.toString());
                }
                break;
            }
        }
    }

    private String resolveGenericAudioDevice(String format) {
        if (audioDevice != null && !audioDevice.equalsIgnoreCase("auto")) {
            return audioDevice;
        }
        // We cannot enumerate devices reliably here without spawning ffmpeg; require configuration
        logger.warn("Audio device not specified for format '{}'. Set -DvideoRecorder.audioDevice=<device> to enable.", format);
        return null;
    }

    // --- Internal helpers ---

    private String resolveWindowsAudioDevice() {
        if (audioDevice != null && !audioDevice.equalsIgnoreCase("auto")) {
            return audioDevice.startsWith("audio=") ? audioDevice : ("audio=" + audioDevice);
        }
        // Try common loopback devices in order
        String[] candidates = new String[]{
                "audio=virtual-audio-capturer", // screen-capture-recorder
                "audio=Stereo Mix (Realtek(R) Audio)",
                "audio=CABLE Output (VB-Audio Virtual Cable)",
                "audio=VoiceMeeter Input (VB-Audio VoiceMeeter VAIO)"
        };
        for (String c : candidates) {
            try (FFmpegFrameGrabber probe = new FFmpegFrameGrabber(c)) {
                probe.setFormat("dshow");
                probe.setAudioChannels(2);
                probe.setSampleRate(44100);
                probe.start();
                probe.stop();
                logger.info("Using audio device: {}", c);
                return c;
            } catch (Exception ignore) {
                // try next
            }
        }
        logger.warn("No suitable Windows loopback audio device found. Set -DvideoRecorder.audioDevice=audio=<device name> to enable.");
        return null;
    }

    /**
     * Sets the preferred audio loopback device name (Windows dshow, macOS avfoundation, Linux pulse/alsa).
     */
    public void setAudioDevice(String audioDevice) {
        this.audioDevice = audioDevice;
    }

    /**
     * Sets the area to be captured during recording
     *
     * @param x      X coordinate of the top-left corner
     * @param y      Y coordinate of the top-left corner
     * @param width  Width of the area to capture
     * @param height Height of the area to capture
     */
    public void setCaptureArea(int x, int y, int width, int height) {
        this.captureArea = new Rectangle(x, y, width, height);
    }

    /**
     * Sets whether to include system audio in the recording.
     */
    public void setIncludeAudio(boolean includeAudio) {
        this.includeAudio = includeAudio;
    }

    /**
     * Sets the WebDriver to use for content capture
     *
     * @param driver The Selenium WebDriver instance
     */
    public void setWebDriver(WebDriver driver) {
        this.webDriver = driver;
    }

    /**
     * Start recording with the given test name
     *
     * @return true if recording started successfully, false if in headless mode
     */
    public boolean startRecording(String subFolderName, String testName) throws IOException, AWTException {
        return startRecording(subFolderName, testName, false);
    }

    /**
     * Start recording with the given test name
     *
     * @param subFolderName Name of the subfolder to store the video
     * @param testName      Name to use for the video file
     * @param contentOnly   If true, will attempt to capture only the webpage content
     * @return true if recording started successfully, false if in headless mode
     */
    public boolean startRecording(String subFolderName, String testName, boolean contentOnly) throws IOException, AWTException {
        this.outputDirectory = new File(rootDirectory, subFolderName);
        outputDirectory.mkdirs();

        if (SeleniumHandler.isSeleniumHeadless()) {
            logger.warn("WARNING: Running in headless mode. Video recording disabled.");
            return false; // Skip recording in headless environments
        }

        this.currentTestName = testName;

        // Determine the area to record
        Rectangle recordingArea = computeRecordingArea(contentOnly);

        // Prepare output file
        Path output = Paths.get(rootDirectory.getPath(), outputDirectory.getName(), currentTestName + ".mp4");
        Files.createDirectories(output.getParent());
        if (Files.exists(output)) {
            try {
                Files.delete(output);
            } catch (IOException e) {
                logger.warn("Could not delete existing video file: {}", output.toAbsolutePath());
            }
        }

        // Initialize recorder and grabbers
        initVideo(recordingArea);
        boolean audioInitialized = false;
        if (includeAudio) {
            audioInitialized = initAudio();
            if (!audioInitialized) {
                logger.warn("Audio capture not initialized. Proceeding with video-only recording.");
            }
        }

        try {
            int audioChannels = (audioInitialized ? audioGrabber.getAudioChannels() : 0);
            recorder = new FFmpegFrameRecorder(output.toFile(), recordingArea.width, recordingArea.height, audioChannels);
            recorder.setFormat("mp4");
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFrameRate(frameRate);
            recorder.setVideoOption("preset", "veryfast");
            recorder.setVideoOption("tune", "zerolatency");
            recorder.setVideoOption("crf", "23");
            recorder.setPixelFormat(0); // yuv420p
            if (audioInitialized) {
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                recorder.setAudioBitrate(192_000);
                recorder.setSampleRate(audioGrabber.getSampleRate());
                recorder.setAudioChannels(audioGrabber.getAudioChannels());
            }
            recorder.start();
        } catch (Exception e) {
            // Cleanup
            closeQuietly(screenGrabber);
            closeQuietly(audioGrabber);
            throw new IOException("Failed to start recorder", e);
        }

        running.set(true);
        isRecording = true;

        // Start threads
        videoThread = new Thread(this::pumpVideo, "javacv-video");
        videoThread.setDaemon(true);
        videoThread.start();
        if (includeAudio && audioGrabber != null) {
            audioThread = new Thread(this::pumpAudio, "javacv-audio");
            audioThread.setDaemon(true);
            audioThread.start();
        }

        logger.info("Started recording {}x{} at {} fps to {} (audio: {})", recordingArea.width, recordingArea.height, frameRate, output.toAbsolutePath(), includeAudio && audioGrabber != null);
        return true;
    }

    /**
     * Stop recording and return the recorded video file
     */
    public File stopRecording() throws IOException {
        if (!isRecording) return null;

        running.set(false);
        // Join threads
        try {
            if (videoThread != null) videoThread.join(3000);
            if (audioThread != null) audioThread.join(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Stop recorder and grabbers
        try {
            if (recorder != null) recorder.stop();
        } catch (Exception e) {
            logger.warn("Error stopping recorder", e);
        }
        try {
            if (recorder != null) recorder.release();
        } catch (Exception ignored) {
        }
        closeQuietly(screenGrabber);
        closeQuietly(audioGrabber);

        isRecording = false;
        Path output = Paths.get(rootDirectory.getPath(), outputDirectory.getName(), currentTestName + ".mp4");
        return output.toFile();
    }
}
