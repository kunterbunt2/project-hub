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

package de.bushnaq.abdalla.projecthub.ui.util;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;

public class ScreenShotCreator {

    static String fileNameGenerator(String displayName, String testmethod) {
        displayName = displayName.replace("'", "");
        String path = getScreenshotFolder();
        return path + testmethod + "(" + displayName + ")" + ".png";
    }

    private static String getScreenshotFolder() {
        return "target" + File.separator + "externalFiles" + File.separator + "screenshots" + File.separator;
    }

    public static void takeScreenShot(WebDriver driver, String displayName, String testmethod) {
        try {
            File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(screenshotFile, new File(fileNameGenerator(displayName, testmethod)));
        } catch (IOException e) {
            throw new RuntimeException("Could not make a screenshot", e);
        }
    }
}
