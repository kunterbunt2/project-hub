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

import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.monte.media.FormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

/**
 * Utility class for recording screen during UI tests
 */
public class VideoRecorder {
    private       String         currentTestName;
    private final Logger         logger = LoggerFactory.getLogger(this.getClass());
    private       File           outputDirectory;
    private final File           rootDirectory;
    private       ScreenRecorder screenRecorder;

    public VideoRecorder() {
        this(new File("test-recordings"));
    }

    public VideoRecorder(File rootDirectory) {
        this.rootDirectory = rootDirectory;
        // Create directory if it doesn't exist
        rootDirectory.mkdirs();
    }

    /**
     * Start recording with the given test name
     *
     * @param testName Name to use for the video file
     * @throws IOException  If there's an IO error
     * @throws AWTException If there's an issue with AWT
     */
    public void startRecording(String subFolderName, String testName) throws IOException, AWTException {
        this.outputDirectory = new File(rootDirectory, subFolderName);
        outputDirectory.mkdirs();
        if (GraphicsEnvironment.isHeadless()) {
            logger.warn("WARNING: Running in headless mode despite IDE environment. Video recording disabled.");
            logger.warn("Check your IntelliJ run configuration for -Djava.awt.headless=true flag");
            return; // Skip recording in headless environments
        }
        this.currentTestName = testName;
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration();
        this.screenRecorder = new NamedScreenRecorder(
                gc,
                new Rectangle(gc.getBounds()),
                new Format(FormatKeys.MediaTypeKey, FormatKeys.MediaType.FILE,
                        MimeTypeKey, MIME_AVI),
                new Format(MediaTypeKey, MediaType.VIDEO,
                        EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                        CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                        DepthKey, 24,
                        FrameRateKey, Rational.valueOf(60),// 60 FPS
                        QualityKey, 0.9f,
                        KeyFrameIntervalKey, 10),
                null,//- Mouse recording is disabled
                null,//- Audio recording is disabled
                rootDirectory);
        this.screenRecorder.start();
    }

    /**
     * Stop recording and return the recorded video file
     *
     * @return File object pointing to the recorded video
     * @throws IOException If there's an IO error
     */
    public File stopRecording() throws IOException {
        if (this.screenRecorder != null) {
            this.screenRecorder.stop();
            return this.screenRecorder.getCreatedMovieFiles().get(0);
        }
        return null;
    }

    private class NamedScreenRecorder extends ScreenRecorder {
        public NamedScreenRecorder(GraphicsConfiguration cfg, Rectangle captureArea, Format fileFormat, Format screenFormat, Format mouseFormat, Format audioFormat, File movieFolder) throws IOException, AWTException {
            super(cfg, captureArea, fileFormat, screenFormat, mouseFormat, audioFormat, movieFolder);
        }

        @Override
        protected File createMovieFile(Format fileFormat) throws IOException {
            File file = Paths.get(rootDirectory.getPath(), outputDirectory.getName(), currentTestName + ".avi").toFile();
            if (file.exists()) {
                if (file.isDirectory()) {
                    deleteDirectoryRecursively(file);
                } else if (!file.delete()) {
                    logger.warn("Could not delete existing video file: {}", file.getAbsolutePath());
                }
            }
            return file;
        }

        private void deleteDirectoryRecursively(File dir) throws IOException {
            File[] allContents = dir.listFiles();
            if (allContents != null) {
                for (File f : allContents) {
                    if (f.isDirectory()) {
                        deleteDirectoryRecursively(f);
                    } else {
                        if (!f.delete()) {
                            logger.warn("Could not delete file: {}", f.getAbsolutePath());
                        }
                    }
                }
            }
            if (!dir.delete()) {
                logger.warn("Could not delete directory: {}", dir.getAbsolutePath());
            }
        }
    }
}
