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

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * handles vaadin ui tests using selenium
 */
@Component
@Getter
public class SeleniumHandler {
    private       WebDriver     driver;
    private final Duration      implicitWaitDuration;
    private final Logger        logger = LoggerFactory.getLogger(this.getClass());
    private final VideoRecorder videoRecorder;
    private       WebDriverWait wait;
    private       Duration      waitDuration;
    private       Dimension     windowSize;  // Added field to store custom window size

    public SeleniumHandler() {
        this(Duration.ofSeconds(10), Duration.ofSeconds(30));
    }

    public SeleniumHandler(Duration implicitWaitDuration, Duration waitDuration) {
        this.implicitWaitDuration = implicitWaitDuration;
        this.waitDuration         = waitDuration;
        this.videoRecorder        = new VideoRecorder();
    }

    public void click(String id) {
        waitUntil(ExpectedConditions.elementToBeClickable(By.id(id)));
        WebElement element = findElement(By.id(id));
        element.click();
    }

    @PreDestroy
    public void destroy() {
        if (videoRecorder.isRecording()) {
            stopRecording();
        }

        if (driver != null) {
//            ScreenShotCreator.takeScreenShot(driver, testInfo.getDisplayName(), testInfo.getTestMethod().get().getName());
            driver.close();//closing the browser
            driver.quit();//quit the driver
            driver = null;
        }
    }

    public void ensureIsInList(String id, String userName) {
        waitUntil(ExpectedConditions.elementToBeClickable(By.id(id + userName)));
    }

    public void ensureIsNotInList(String id, String name) {
        Duration implicitTime = getImplicitWaitDuration();
        setImplicit(Duration.ofSeconds(1));
        waitUntil(ExpectedConditions.not(ExpectedConditions.elementToBeClickable(By.id(id + name))));
        setImplicit(implicitTime);
    }

    public void ensureIsSelected(String id, String userName) {
        WebElement label = findElement(By.id(id));
        waitUntil(ExpectedConditions.textToBePresentInElement(label, userName));
    }

    /**
     * Executes JavaScript in the current browser window.
     * <p>
     * This method allows executing arbitrary JavaScript code in the browser, which is useful for
     * interacting with complex components that may be difficult to access through standard Selenium methods.
     * It's particularly useful for setting values on custom components like color pickers and date pickers.
     *
     * @param script the JavaScript to execute
     * @param args   the arguments to pass to the script (will be available in the script as arguments[0], arguments[1], etc.)
     * @return the value returned by the script, which may be null
     */
    public Object executeJavaScript(String script, Object... args) {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) getDriver();
        return jsExecutor.executeScript(script, args);
    }

    public WebElement expandRootElementAndFindElement(WebElement element, String elementName) {
        String     script = String.format("return arguments[0].shadowRoot.querySelector('%s')", elementName);
        WebElement ele    = (WebElement) ((JavascriptExecutor) getDriver()).executeScript(script, element);
        return ele;
    }

    public WebElement findDialogOverlayElement(String dialogId) {
        WebElement    rootHost   = getDriver().findElement(By.tagName("vaadin-dialog-overlay"));
        SearchContext shadowRoot = rootHost.getShadowRoot();
        return shadowRoot.findElement(By.cssSelector("div[part=\"overlay\"]"));
    }

    public WebElement findElement(By by) {
        return getDriver().findElement(by);
    }

    public List<WebElement> findElements(By by) {
        return getDriver().findElements(by);
    }

    public WebElement findLoginOverlayElement(String dialogId) {
        WebElement    rootHost   = getDriver().findElement(By.tagName("vaadin-login-form-wrapper"));
        SearchContext shadowRoot = rootHost.getShadowRoot();
        return shadowRoot.findElement(By.cssSelector("section[part=\"form\"]"));
    }

    public void get(String url) {
        getDriver().get(url);
    }

    public void getAndCheck(String url) {
        get(url);
        waitUntil(ExpectedConditions.urlContains(url));
        testForAnyError();
    }

    public boolean getCheckbox(String id) {
        WebElement element = findElement(By.id(id));
        return element.getAttribute("checked") != null;
    }

    /**
     * Gets the current value from a color picker component.
     * <p>
     * This method retrieves the color value from a Vaadin ColorPicker component
     * using JavaScript. It accesses the component's shadow DOM to get the color
     * input element's value. Returns the hex color code with # prefix.
     *
     * @param colorPickerId the ID of the color picker component
     * @return the current color value as a hex string (e.g. "#FF5733"), or null if not available
     */
    public String getColorPickerValue(String colorPickerId) {
        // Wait for the color picker element to be present
        waitUntil(ExpectedConditions.presenceOfElementLocated(By.id(colorPickerId)));

        // Access the color input element through the shadow DOM, similar to the set method
        String colorScript =
                "var picker = document.getElementById('" + colorPickerId + "');" +
                        "if (picker) {" +
                        "  var inputElement = picker.shadowRoot.querySelector('input[type=\"color\"]');" +
                        "  if (inputElement) {" +
                        "    return inputElement.value;" +
                        "  }" +
                        "  return picker.value;" + // Fallback to component's value if input not found
                        "}" +
                        "return null;";

        return (String) executeJavaScript(colorScript);
    }

    public String getCurrentUrl() {
        return getDriver().getCurrentUrl();
    }

    /**
     * Gets the current browser window size.
     *
     * @return the current window size as a Dimension object, or null if the browser is not yet initialized
     */
    public Dimension getCurrentWindowSize() {
        if (driver != null) {
            return driver.manage().window().getSize();
        }
        return windowSize;
    }

    /**
     * Gets the current value from a date picker component.
     * <p>
     * This method retrieves the date value from a Vaadin DatePicker component
     * using JavaScript and converts it to a LocalDate object.
     *
     * @param datePickerId the ID of the date picker component
     * @return the current date value as LocalDate, or null if no date is set or component not found
     */
    public LocalDate getDatePickerValue(String datePickerId) {
        // Wait for the date picker element to be present
        waitUntil(ExpectedConditions.presenceOfElementLocated(By.id(datePickerId)));

        String dateScript =
                "var datePicker = document.getElementById('" + datePickerId + "');" +
                        "if (datePicker && datePicker.value) {" +
                        "  return datePicker.value;" +
                        "}" +
                        "return null;";

        String dateStr = (String) executeJavaScript(dateScript);

        if (dateStr != null && !dateStr.isEmpty()) {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return null;
    }

    public WebDriver getDriver() {
        if (driver == null) {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
            options.addArguments("--remote-allow-origins=*");
            driver = new ChromeDriver(options);
            wait   = new WebDriverWait(getDriver(), waitDuration);
            //Applied wait time
            setImplicit(implicitWaitDuration);

            if (windowSize != null) {
                getDriver().manage().window().setSize(windowSize);
                getDriver().manage().window().setPosition(new Point(33, 22));
            } else {
                //maximize window by default
                getDriver().manage().window().maximize();
            }
            //firefox
            {
                //            WebDriverManager.firefoxdriver().setup();
                //            FirefoxOptions options = new FirefoxOptions();
                //            options.setCapability("moz:webdriverClick", false);//https://stackoverflow.com/questions/49864965/org-openqa-selenium-elementnotinteractableexception-element-is-not-reachable-by
                //            options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
                //            driver = new FirefoxDriver(options);
            }
        }
        return driver;
    }

    public Duration getImplicitWaitDuration() {
        return implicitWaitDuration;
    }

    public String getIntegerField(String id) {
        WebElement e     = findElement(By.id(id));
        String     value = e.getAttribute("value");
        return value;
    }

    public String getRouteValue(Class<?> viewClass) {
        if (viewClass.isAnnotationPresent(com.vaadin.flow.router.Route.class)) {
            com.vaadin.flow.router.Route route = viewClass.getAnnotation(com.vaadin.flow.router.Route.class);
            if (route != null)
                return route.value();
        }
        return null;
    }

    public String getTextField(String id) {
        WebElement e     = findElement(By.id(id));
        String     value = e.getAttribute("value");
        return value;
    }

    public String getTitle() {
        return getDriver().getTitle();
    }

    public Duration getWaitDuration() {
        return waitDuration;
    }

    /**
     * Checks if an element is present on the page
     *
     * @param locator the By locator for the element to check
     * @return true if the element is present, false otherwise
     */
    public boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Checks if an element with the specified ID is present on the page
     *
     * @param id the ID of the element to check
     * @return true if the element is present, false otherwise
     */
    public boolean isElementPresent(String id) {
        return isElementPresent(By.id(id));
    }

    public boolean isRecording() {
        return videoRecorder.isRecording();
    }

    public void loginSubmit() {
        // Find the login button using a more specific XPath selector that matches the attributes
        WebElement button = findElement(By.xpath("//vaadin-button[@slot='submit' and contains(@theme, 'submit')]"));
        button.click();
        waitForPageLoaded();
    }

    /**
     * Resets the window size setting to use default maximized window.
     * If the browser is already running, it will maximize the current window.
     */
    public void resetWindowSize() {
        windowSize = null;

        // If driver is already initialized, maximize the current window
        if (driver != null) {
            driver.manage().window().maximize();
        }
    }

    public void selectGridRow(String gridRowBaseId, Class<?> viewClass, String rowName) {
        String  url             = getRouteValue(viewClass);
        boolean outerTesting    = true;
        int     outerIterations = 12;//5 seconds
        //try several times
        do {
            WebElement row = findElement(By.id(gridRowBaseId + rowName));
            try {
                row.click();
            } catch (StaleElementReferenceException e) {
                //ignore and retry
            }
            //check several times
            int     innerIterations = 10;//5 seconds
            boolean innerTesting    = true;
            do {
                if (ExpectedConditions.urlContains(url).apply(getDriver())) {
                    outerTesting = false;
                }
                if (--innerIterations == 0) {
                    innerTesting = false;
                }
                if (outerTesting) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        //ignore
                    }
                }
            } while (innerTesting && outerTesting);
            if (--outerIterations == 0) {
                outerTesting = false;
            }
            if (outerTesting) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        } while (outerTesting);
    }

    public void selectGridRow(String gridRowBaseId, String labelId, String rowName) {
        boolean    outerTesting    = true;
        int        outerIterations = 12;//5 seconds
        WebElement label           = findElement(By.id(labelId));
        //try several times
        do {
            WebElement row = findElement(By.id(gridRowBaseId + rowName));
            try {
                row.click();
            } catch (StaleElementReferenceException e) {
                //ignore and retry
            }
            //check several times
            int     innerIterations = 10;//5 seconds
            boolean innerTesting    = true;
            do {
                if (ExpectedConditions.textToBePresentInElement(label, rowName).apply(getDriver())) {
                    outerTesting = false;
                }
                if (--innerIterations == 0) {
                    innerTesting = false;
                }
                if (outerTesting) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        //ignore
                    }
                }
            } while (innerTesting && outerTesting);
            if (--outerIterations == 0) {
                outerTesting = false;
            }
            if (outerTesting) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        } while (outerTesting);
        assertEquals(rowName, label.getText());
    }

    public void setCheckCheckbox(String id, boolean value) {
        boolean old = getCheckbox(id);
        if (old != value) {
            WebElement e = findElement(By.id(id));
            WebElement i = e.findElement(By.tagName("input"));
            if (value) {
                if (!i.isSelected()) {
                    i.click();
                }
            } else {
                if (i.isSelected()) {
                    i.click();
                }
            }
        }
    }

    /**
     * Sets a value to a color picker component.
     * <p>
     * This method provides a reliable way to set color values on Vaadin ColorPicker
     * components. It uses JavaScript to set the value directly and ensures the component
     * properly registers the color change by triggering necessary events.
     *
     * @param colorPickerId the ID of the color picker component
     * @param colorValue    the color value to set (hex format with # prefix, e.g., "#FF5733")
     * @return true if the color was successfully set, false otherwise
     */
    public boolean setColorPickerValue(String colorPickerId, String colorValue) {
        // Wait for the color picker element to be present
        waitUntil(ExpectedConditions.presenceOfElementLocated(By.id(colorPickerId)));

        // First attempt: set the value and trigger necessary events
        String colorScript =
                "var picker = document.getElementById('" + colorPickerId + "');" +
                        "if (picker) {" +
                        "  picker.value = '" + colorValue + "';" +
                        "  if (typeof picker._valueChanged === 'function') {" +
                        "    picker._valueChanged('" + colorValue + "');" +
                        "  }" +
                        "  var inputElement = picker.shadowRoot.querySelector('input[type=\"color\"]');" +
                        "  if (inputElement) {" +
                        "    inputElement.value = '" + colorValue + "';" +
                        "    inputElement.dispatchEvent(new Event('input', {bubbles: true}));" +
                        "    inputElement.dispatchEvent(new Event('change', {bubbles: true}));" +
                        "  }" +
                        "  return picker.value === '" + colorValue + "';" +
                        "}" +
                        "return false;";

        Boolean result = (Boolean) executeJavaScript(colorScript);

        // If first attempt failed, try a simpler direct approach
        //TODO do we ned this?
        if (result == null || !result) {
            // Direct method as fallback: use the component's public API
            executeJavaScript(
                    "document.getElementById('" + colorPickerId + "').value = '" + colorValue + "';" +
                            "document.getElementById('" + colorPickerId + "').dispatchEvent(new Event('change'));"
            );

            // Verify the color was set correctly
            String verifyScript = "return document.getElementById('" + colorPickerId + "').value;";
            String verifyResult = (String) executeJavaScript(verifyScript);

            return verifyResult != null && verifyResult.equalsIgnoreCase(colorValue);
        }

        return result;
    }

    /**
     * Sets a value to a date picker component.
     * <p>
     * This method provides a reliable way to set date values on Vaadin DatePicker
     * components. It uses JavaScript to set the value directly in ISO date format
     * and ensures the component properly registers the date change by triggering
     * necessary events.
     *
     * @param datePickerId the ID of the date picker component
     * @param date         the LocalDate value to set, can be null to clear the date
     * @return true if the date was successfully set, false otherwise
     */
    public boolean setDatePickerValue(String datePickerId, LocalDate date) {
        // Wait for the date picker element to be present
        waitUntil(ExpectedConditions.presenceOfElementLocated(By.id(datePickerId)));

        if (date == null) {
            // Clear the date picker
            String clearScript =
                    "var datePicker = document.getElementById('" + datePickerId + "');" +
                            "if (datePicker) {" +
                            "  datePicker.value = '';" +
                            "  datePicker.dispatchEvent(new CustomEvent('change', { bubbles: true }));" +
                            "  return true;" +
                            "}" +
                            "return false;";

            return Boolean.TRUE.equals(executeJavaScript(clearScript));
        } else {
            // Format the date in ISO format (YYYY-MM-DD) as expected by the date picker
            String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);

            String dateScript =
                    "var datePicker = document.getElementById('" + datePickerId + "');" +
                            "if (datePicker) {" +
                            "  datePicker.value = '" + dateStr + "';" +
                            "  datePicker.dispatchEvent(new CustomEvent('change', { bubbles: true }));" +
                            "  return true;" +
                            "}" +
                            "return false;";

            return Boolean.TRUE.equals(executeJavaScript(dateScript));
        }
    }

    public void setImplicit(Duration duration) {
        getDriver().manage().timeouts().implicitlyWait(duration);
    }

    public void setIntegerField(String id, String userName) {
        WebElement e     = findElement(By.id(id));
        WebElement i     = e.findElement(By.tagName("input"));
        String     value = i.getAttribute("value");
        if (value.isEmpty()) {
        } else {
            i.sendKeys(Keys.CONTROL + "a");
            i.sendKeys(Keys.DELETE);
        }
        i.sendKeys(userName);
    }

    public void setLoginPassword(String loginPassword) {
        WebElement passwordElement = findElement(By.name("password"));
        passwordElement.sendKeys(loginPassword);
    }

    public void setLoginUser(String loginUser) {
        WebElement usernameElement = findElement(By.id("vaadinLoginUsername"));
        usernameElement.sendKeys(loginUser);
    }

    public void setTextArea(String id, String userName) {
        WebElement e     = findElement(By.id(id));
        WebElement i     = e.findElement(By.tagName("textarea"));
        String     value = i.getAttribute("value");
        if (value.isEmpty()) {
        } else {
            i.sendKeys(Keys.CONTROL + "a");
            i.sendKeys(Keys.DELETE);
        }
        i.sendKeys(userName);
    }

    public void setTextField(String id, String userName) {
        waitUntil(ExpectedConditions.elementToBeClickable(By.id(id)));
        WebElement e     = findElement(By.id(id));
        WebElement i     = e.findElement(By.tagName("input"));
        String     value = i.getAttribute("value");
        if (value.isEmpty()) {
        } else {
            i.sendKeys(Keys.CONTROL + "a");
            i.sendKeys(Keys.DELETE);
        }
        i.sendKeys(userName);
    }

    public void setWaitDuration(Duration waitDuration) {
        this.waitDuration = waitDuration;
        wait.withTimeout(waitDuration);
    }

    /**
     * Sets the browser window to a specific size.
     * This must be called before the browser is initialized.
     * If the browser is already running, it will resize the current window.
     *
     * @param width  the width of the browser window in pixels
     * @param height the height of the browser window in pixels
     */
    public void setWindowSize(int width, int height) {
        windowSize = new Dimension(width, height);

        // If driver is already initialized, resize the current window
        if (driver != null) {
            driver.manage().window().setSize(windowSize);
        }
    }

    /**
     * Start recording screen activities for the current test
     *
     * @param folderName Subfolder name for storing the video
     * @param testName   Name of the test for the video file name
     */
    public void startRecording(String folderName, String testName) {
        if (!videoRecorder.isRecording()) {
            try {
                getDriver(); // Ensure the driver is initialized and browser is open

                // Pass the WebDriver to VideoRecorder to access browser content
                videoRecorder.setWebDriver(driver);

                // Option 1: Record the entire browser window
                Point     position = driver.manage().window().getPosition();
                Dimension size     = driver.manage().window().getSize();
                videoRecorder.setCaptureArea(position.getX(), position.getY(), size.getWidth(), size.getHeight());

                // Start the recording with content-only mode, which will use JavaScript to determine
                // the exact content area within the browser window
                if (videoRecorder.startRecording(folderName, testName, true)) {
                    logger.info("Started video recording for test: {}", testName);
                }
            } catch (IOException | AWTException e) {
                logger.error("Failed to start video recording: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Stop the active recording and save the video file
     *
     * @return File object pointing to the recorded video, or null if recording failed
     */
    public File stopRecording() {
        if (videoRecorder.isRecording()) {
            try {
                File videoFile = videoRecorder.stopRecording();
                if (videoFile != null) {
                    logger.info("Video recording saved to: {}", videoFile.getAbsolutePath());
                }
                return videoFile;
            } catch (IOException e) {
                logger.error("Failed to stop video recording: {}", e.getMessage(), e);
            }
        }
        return null;
    }

    public void takeElementScreenShot(WebElement overlayElement, String dialogId, String fileName) {
        ScreenShotCreator.takeElementScreenshot(getDriver(), overlayElement, dialogId, fileName);
    }

    public void takeScreenShot(String fileName) {
        ScreenShotCreator.takeScreenShot(getDriver(), fileName);
    }

    private void testForAnyError() {
        assertEquals(-1, getTitle().indexOf("HTTP Status"), String.format("HTTP error detected '%s'.", getTitle()));
    }

    private void testForError(String errorTitle) {
        //        String errorMessage = String.format("HTTP Status %d – Not Found", errorCode);
        assertTrue(getTitle().indexOf(errorTitle) != -1, String.format("Expected '%s', but got '%s'.", errorTitle, getTitle()));
    }

    public void testUrl(String expectedUrl) {
        String currentUrl = getCurrentUrl();
        waitUntil(ExpectedConditions.urlContains(expectedUrl));
        logger.trace(String.format("expectedUrl=%s, actualUrl=%s", expectedUrl, currentUrl));
        assertTrue(currentUrl.contains(expectedUrl));
    }

    public void toggleCheckbox(String id) {
        WebElement e = findElement(By.id(id));
        WebElement i = e.findElement(By.tagName("input"));
        i.click();
    }

    public void wait(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread interrupted while waiting", e);
        }
    }

    public void waitForElementToBeClickable(String id) {
        waitUntil(ExpectedConditions.elementToBeClickable(By.id(id)));
    }

    public void waitForElementToBeLocated(String id) {
        waitUntil(ExpectedConditions.presenceOfElementLocated(By.id(id)));
    }

    public void waitForPageLoaded() {
        waitForPageLoaded(30);
    }

    public void waitForPageLoaded(long seconds) {
        ExpectedCondition<Boolean> expectation = new ExpectedCondition<>() {
            @Override
            public Boolean apply(WebDriver driver) {
                return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
            }
        };
        Wait<WebDriver> wait = new WebDriverWait(getDriver(), Duration.ofSeconds(seconds));
        try {
            wait.until(expectation);
        } catch (Throwable error) {
            fail("Timeout waiting for Page Load Request to complete.");
        }
    }

    public void waitForUrl(String url) {
        waitUntil(ExpectedConditions.urlContains(url));
        testForAnyError();
    }

    public void waitForUrl(String url, String errortitle) {
        waitUntil(ExpectedConditions.urlContains(url));
        testForError(errortitle);
    }

    public void waitForUrlNot(String url) {
        waitUntil(ExpectedConditions.not(ExpectedConditions.urlToBe(url)));
        //        waitUntil(ExpectedConditions.urlToBe(url));
        testForAnyError();
    }

    public void waitUntil(ExpectedCondition<?> condition) {
        wait.until(condition);
    }

    /**
     * Waits until the browser window is closed either manually by the user
     * or by another process.
     *
     * @param timeoutMillis Maximum time to wait in milliseconds, or 0 for infinite wait
     * @return true if browser was closed, false if timeout occurred
     */
    public boolean waitUntilBrowserClosed(long timeoutMillis) {
        long startTime = System.currentTimeMillis();

        while (true) {
            try {
                // Try to execute a simple script or get window handles to verify browser is open
                getDriver().getWindowHandles();
                // Short sleep to prevent high CPU usage
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    if (videoRecorder.isRecording()) {
                        stopRecording();
                    }
                    driver.quit();//quit the driver
                    driver = null;
                    return false;
                }

                // Check for timeout if specified
                if (timeoutMillis > 0 && System.currentTimeMillis() - startTime > timeoutMillis) {
                    if (videoRecorder.isRecording()) {
                        stopRecording();
                    }
                    driver.quit();//quit the driver
                    driver = null;
                    return false;
                }
            } catch (WebDriverException e) {
                if (e instanceof NoSuchSessionException) {
                    // Browser has been closed if we get a NoSuchSessionException
                } else {
                    throw e;
                }
                if (videoRecorder.isRecording()) {
                    stopRecording();
                }
                driver.quit();//quit the driver
                driver = null;
                return true;
            }
        }
    }
}
