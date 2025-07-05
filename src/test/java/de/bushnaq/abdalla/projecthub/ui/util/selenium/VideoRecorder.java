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
import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
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
    private       Rectangle      captureArea;  // Added field to store the capture area
    private       String         currentTestName;
    @Getter
    private       boolean        isRecording = false;
    private final Logger         logger      = LoggerFactory.getLogger(this.getClass());
    private       File           outputDirectory;
    private final File           rootDirectory;
    private       ScreenRecorder screenRecorder;
    private       WebDriver      webDriver;    // Added field to access browser content

    public VideoRecorder() {
        this(new File("test-recordings"));
    }

    public VideoRecorder(File rootDirectory) {
        this.rootDirectory = rootDirectory;
        // Create directory if it doesn't exist
        rootDirectory.mkdirs();
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
     * @param testName Name to use for the video file
     * @return true if recording started successfully, false if in headless mode
     * @throws IOException  If there's an IO error
     * @throws AWTException If there's an issue with AWT
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
     * @throws IOException  If there's an IO error
     * @throws AWTException If there's an issue with AWT
     */
    public boolean startRecording(String subFolderName, String testName, boolean contentOnly) throws IOException, AWTException {
        this.outputDirectory = new File(rootDirectory, subFolderName);
        outputDirectory.mkdirs();

        if (SeleniumHandler.isSeleniumHeadless()) {
            logger.warn("WARNING: Running in headless mode. Video recording disabled.");
            return false; // Skip recording in headless environments
        }

        isRecording          = true;
        this.currentTestName = testName;
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration();

        // Determine the area to record
        Rectangle recordingArea;

        if (contentOnly && webDriver != null) {
            try {
                // Try to get the dimensions of the content area only
                JavascriptExecutor jsExecutor = (JavascriptExecutor) webDriver;

                // Calculate viewport position and size
                Point browserPosition = webDriver.manage().window().getPosition();
//                Long  outerWidth      = (Long) jsExecutor.executeScript("return window.outerWidth;");
//                Long  innerWidth      = (Long) jsExecutor.executeScript("return window.innerWidth;");
//                Long  outerHeight     = (Long) jsExecutor.executeScript("return window.outerHeight;");
//                Long  innerHeight     = (Long) jsExecutor.executeScript("return window.innerHeight;");

                Long viewportX      = (Long) jsExecutor.executeScript("return window.pageXOffset + (window.outerWidth - window.innerWidth);");
                Long viewportY      = (Long) jsExecutor.executeScript("return window.pageYOffset + (window.outerHeight - window.innerHeight);");
                Long viewportWidth  = (Long) jsExecutor.executeScript("return window.innerWidth;");
                Long viewportHeight = (Long) jsExecutor.executeScript("return window.innerHeight;");

//                Long screenX = (Long) jsExecutor.executeScript("return window.screenX;");
//                Long screenY = (Long) jsExecutor.executeScript("return window.screenY;");


                // Calculate absolute screen coordinates of the content area
                int contentX = browserPosition.getX() + viewportX.intValue() - 8;
                int contentY = browserPosition.getY() + viewportY.intValue() - 8;

                recordingArea = new Rectangle(contentX, contentY, viewportWidth.intValue(), viewportHeight.intValue());
                logger.info("Recording browser content area: x={}, y={}, width={}, height={}", contentX, contentY, viewportWidth.intValue(), viewportHeight.intValue());

            } catch (Exception e) {
                logger.warn("Failed to determine browser content dimensions, falling back to browser window", e);
                recordingArea = captureArea != null ? captureArea : new Rectangle(gc.getBounds());
            }
        } else {
            // Use the specified capture area if set, otherwise capture the entire screen
            recordingArea = captureArea != null ? captureArea : new Rectangle(gc.getBounds());
        }

        this.screenRecorder = new NamedScreenRecorder(
                gc,
                recordingArea,
                new Format(FormatKeys.MediaTypeKey, FormatKeys.MediaType.FILE,
                        MimeTypeKey, MIME_AVI),
                new Format(MediaTypeKey, MediaType.VIDEO,
                        EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                        CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                        DepthKey, 24,
                        FrameRateKey, Rational.valueOf(60),// 60 FPS
                        QualityKey, 0.9f,
                        KeyFrameIntervalKey, 10),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, ScreenRecorder.ENCODING_BLACK_CURSOR, FrameRateKey, Rational.valueOf(60)),//- Mouse recording is disabled
                null,//- Audio recording is disabled
                rootDirectory);
        this.screenRecorder.start();
        return true;
    }

    /**
     * Stop recording and return the recorded video file
     *
     * @return File object pointing to the recorded video
     * @throws IOException If there's an IO error
     */
    public File stopRecording() throws IOException {
        if (this.screenRecorder != null && isRecording) {
            this.screenRecorder.stop();
            isRecording = false;
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
