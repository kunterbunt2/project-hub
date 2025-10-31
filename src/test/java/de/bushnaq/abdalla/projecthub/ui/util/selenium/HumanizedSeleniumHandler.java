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
import lombok.Setter;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Component
public class HumanizedSeleniumHandler extends SeleniumHandler {
    @Getter
    @Setter
    private       boolean humanize          = true;
    private final Random  random            = new Random(); // For human-like randomness
    private       Robot   robot             = null; // Lazily initialized
    private final int     typingDelayMillis = 50;

    /**
     * Centers the mouse cursor on the browser window.
     * This is useful before starting recordings to ensure the mouse starts
     * in a predictable position rather than wherever it was previously.
     * Only works if mouse movement is enabled and not in headless mode.
     */
    protected void centerMouseOnBrowser() {
        // Skip if we're in headless mode
        if (isSeleniumHeadless()) {
            return;
        }

        Robot robotInstance = getRobot();
        if (robotInstance == null) {
            logger.debug("Robot not available, cannot center mouse");
            return;
        }

        try {
            // Get browser window position and size
            Point     windowPosition = getDriver().manage().window().getPosition();
            Dimension windowSize     = getDriver().manage().window().getSize();

            // Calculate center of browser window
            int centerX = windowPosition.getX() + (windowSize.getWidth() / 2);
            int centerY = windowPosition.getY() + (windowSize.getHeight() / 2);

            // Move mouse to center (instantly, no smooth movement)
            robotInstance.mouseMove(centerX, centerY);
            logger.debug("Centered mouse on browser at ({}, {})", centerX, centerY);

        } catch (Exception e) {
            logger.warn("Failed to center mouse on browser: {}", e.getMessage());
        }
    }

    /**
     * Gets or initializes the Robot instance for mouse movement.
     * Returns null if Robot cannot be initialized (e.g., in headless mode or due to security restrictions).
     *
     * @return the Robot instance, or null if unavailable
     */
    private Robot getRobot() {
        if (robot != null) {
            return robot;
        }

        // Don't even try to create Robot in headless mode
        if (isSeleniumHeadless()) {
            logger.debug("Headless mode detected, Robot not available");
            return null;
        }

        try {
            robot = new Robot();
            robot.setAutoDelay(0); // We'll control delays manually
            logger.info("Robot initialized successfully for mouse movement");
        } catch (AWTException e) {
            logger.warn("Failed to initialize Robot for mouse movement: {}", e.getMessage());
            robot = null;
        }

        return robot;
    }

    /**
     * Highlights one or more elements by their IDs for a short period of time (default 2 seconds).
     * This is a convenience method that finds the elements by ID and then highlights them.
     * <p>
     * The highlight style is automatically determined based on the element type:
     * - Buttons and interactive elements: Red border (3px solid)
     * - Text elements (titles, labels, spans): 50% transparent red overlay
     * - Other elements: Red border by default
     * <p>
     * The highlights are applied simultaneously to all elements and removed after the duration.
     *
     * @param ids One or more element IDs to highlight
     */
    public void highlight(String... ids) {
        highlight(2000, ids);
    }

    /**
     * Highlights one or more elements by their IDs for a specified duration.
     * This is a convenience method that finds the elements by ID and then highlights them.
     * <p>
     * The highlight style is automatically determined based on the element type:
     * - Buttons and interactive elements: Red border (3px solid)
     * - Text elements (titles, labels, spans): 50% transparent red overlay
     * - Other elements: Red border by default
     * <p>
     * The highlights are applied simultaneously to all elements and removed after the duration.
     *
     * @param durationMillis Duration in milliseconds to show the highlight (e.g., 2000 for 2 seconds)
     * @param ids            One or more element IDs to highlight
     */
    public void highlight(int durationMillis, String... ids) {
        if (ids == null || ids.length == 0) {
            logger.warn("No element IDs provided to highlight");
            return;
        }

        // Find all elements by their IDs
        WebElement[] elements = new WebElement[ids.length];
        for (int i = 0; i < ids.length; i++) {
            waitUntil(ExpectedConditions.elementToBeClickable(By.id(ids[i])));
            elements[i] = findElement(By.id(ids[i]));
        }

        // Delegate to the WebElement version
        highlight(durationMillis, elements);
    }

    /**
     * Highlights one or more WebElements on the page for a short period of time (default 2 seconds).
     * This is useful for creating instruction videos where you want to draw the viewer's attention
     * to specific elements without needing to describe their exact location.
     * <p>
     * The highlight style is automatically determined based on the element type:
     * - Buttons and interactive elements: Red border (3px solid)
     * - Text elements (titles, labels, spans): 50% transparent red overlay
     * - Other elements: Red border by default
     * <p>
     * The highlights are applied simultaneously to all elements and removed after the duration.
     * The original element styles are preserved and restored after highlighting.
     *
     * @param elements One or more WebElements to highlight
     */
    public void highlight(WebElement... elements) {
        highlight(2000, elements);
    }

    /**
     * Highlights one or more WebElements on the page for a specified duration.
     * This is useful for creating instruction videos where you want to draw the viewer's attention
     * to specific elements without needing to describe their exact location.
     * <p>
     * The highlight style is automatically determined based on the element type:
     * - Buttons and interactive elements: Red border (3px solid)
     * - Text elements (titles, labels, spans): 50% transparent red overlay
     * - Other elements: Red border by default
     * <p>
     * The highlights are applied simultaneously to all elements and removed after the duration.
     * The original element styles are preserved and restored after highlighting.
     *
     * @param durationMillis Duration in milliseconds to show the highlight (e.g., 2000 for 2 seconds)
     * @param elements       One or more WebElements to highlight
     */
    public void highlight(int durationMillis, WebElement... elements) {
        if (elements == null || elements.length == 0) {
            logger.warn("No elements provided to highlight");
            return;
        }

        try {
            // Build JavaScript to highlight all elements

            String script = "var elements = arguments;\n" +
                    "var originals = [];\n" +
                    "var overlays = [];\n" +
                    "\n" +

                    // Add highlights to all elements
                    "for (var i = 0; i < elements.length; i++) {\n" +
                    "  var element = elements[i];\n" +
                    "  if (!element) continue;\n" +
                    "\n" +
                    "  var tagName = element.tagName.toLowerCase();\n" +
                    "  var isTextElement = (tagName === 'h1' || tagName === 'h2' || tagName === 'h3' || \n" +
                    "                        tagName === 'h4' || tagName === 'h5' || tagName === 'h6' || \n" +
                    "                        tagName === 'p' || tagName === 'span' || tagName === 'label' || \n" +
                    "                        tagName === 'div' && element.classList.contains('title'));\n" +
                    "\n" +
                    "  if (isTextElement) {\n" +
                    "    // For text elements, create a semi-transparent red overlay\n" +
                    "    var overlay = document.createElement('div');\n" +
                    "    overlay.style.cssText = 'position: absolute; background-color: rgba(255, 0, 0, 0.3); pointer-events: none; z-index: 999998; border-radius: 4px; transition: opacity 0.3s ease-in-out;';\n" +
                    "    \n" +
                    "    // Get element position and size\n" +
                    "    var rect = element.getBoundingClientRect();\n" +
                    "    var scrollTop = window.pageYOffset || document.documentElement.scrollTop;\n" +
                    "    var scrollLeft = window.pageXOffset || document.documentElement.scrollLeft;\n" +
                    "    \n" +
                    "    overlay.style.top = (rect.top + scrollTop) + 'px';\n" +
                    "    overlay.style.left = (rect.left + scrollLeft) + 'px';\n" +
                    "    overlay.style.width = rect.width + 'px';\n" +
                    "    overlay.style.height = rect.height + 'px';\n" +
                    "    overlay.style.opacity = '0';\n" +
                    "    \n" +
                    "    document.body.appendChild(overlay);\n" +
                    "    overlays.push(overlay);\n" +
                    "    \n" +
                    "    // Trigger fade-in\n" +
                    "    setTimeout(function() { overlay.style.opacity = '1'; }, 10);\n" +
                    "  } else {\n" +
                    "    // For buttons and other elements, add a simple red border\n" +
                    "    originals.push({\n" +
                    "      element: element,\n" +
                    "      border: element.style.border,\n" +
                    "      transition: element.style.transition\n" +
                    "    });\n" +
                    "    \n" +
                    "    element.style.transition = 'all 0.3s ease-in-out';\n" +
                    "    element.style.border = '3px solid #ff0000';\n" +
                    "  }\n" +
                    "}\n" +
                    "\n" +
                    "// Store the cleanup data for later removal\n" +
                    "return { originals: originals, overlays: overlays };\n";

            // Execute the highlight script
            Object result = executeJavaScript(script, elements);

            logger.info("Highlighted {} element(s) for {}ms", elements.length, durationMillis);

            // Wait for the specified duration
            try {
                Thread.sleep(durationMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while waiting for highlight duration");
            }

            // Remove highlights
            String cleanupScript =
                    "var cleanup = arguments[0];\n" +
                            "if (!cleanup) return;\n" +
                            "\n" +
                            "// Restore original styles for border highlights\n" +
                            "if (cleanup.originals) {\n" +
                            "  cleanup.originals.forEach(function(original) {\n" +
                            "    if (original.element) {\n" +
                            "      original.element.style.transition = original.transition || '';\n" +
                            "      original.element.style.border = original.border || '';\n" +
                            "    }\n" +
                            "  });\n" +
                            "}\n" +
                            "\n" +
                            "// Remove overlay elements with fade-out\n" +
                            "if (cleanup.overlays) {\n" +
                            "  cleanup.overlays.forEach(function(overlay) {\n" +
                            "    if (overlay) {\n" +
                            "      overlay.style.opacity = '0';\n" +
                            "      setTimeout(function() {\n" +
                            "        if (overlay.parentNode) {\n" +
                            "          overlay.parentNode.removeChild(overlay);\n" +
                            "        }\n" +
                            "      }, 300);\n" +
                            "    }\n" +
                            "  });\n" +
                            "}\n";

            executeJavaScript(cleanupScript, result);

            logger.debug("Removed highlights from {} element(s)", elements.length);

        } catch (Exception e) {
            logger.error("Failed to highlight elements: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to highlight elements: " + e.getMessage(), e);
        }
    }

    /**
     * Performs a human-like mouse movement from one point to another.
     * Uses a B-spline curve for natural arc-like path and variable speed with easing
     * (starts fast, ends slow) to simulate real human mouse movement.
     *
     * @param fromX starting X coordinate
     * @param fromY starting Y coordinate
     * @param toX   target X coordinate
     * @param toY   target Y coordinate
     */
    private void humanLikeMouseMove(int fromX, int fromY, int toX, int toY) {
        Robot robotInstance = getRobot();
        if (robotInstance == null) {
            return;
        }

        int    deltaX   = toX - fromX;
        int    deltaY   = toY - fromY;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // If the distance is very small, just move directly
        if (distance < 5) {
            robotInstance.mouseMove(toX, toY);
            return;
        }

        // Generate control points for B-spline to create a shallow arc
        // Add a perpendicular offset to create a curved path
        double midX = (fromX + toX) / 2.0;
        double midY = (fromY + toY) / 2.0;

        // Calculate perpendicular direction for the curve
        double perpX      = -deltaY;
        double perpY      = deltaX;
        double perpLength = Math.sqrt(perpX * perpX + perpY * perpY);

        // Normalize and scale the perpendicular offset (shallow arc, about 5-10% of distance)
        double curveFactor = 0.05 + random.nextDouble() * 0.05; // 5-10% arc
        double offsetX     = (perpX / perpLength) * distance * curveFactor;
        double offsetY     = (perpY / perpLength) * distance * curveFactor;

        // Randomly choose arc direction
        if (random.nextBoolean()) {
            offsetX = -offsetX;
            offsetY = -offsetY;
        }

        // Control point for the curve (offset from midpoint)
        double ctrlX = midX + offsetX;
        double ctrlY = midY + offsetY;

        // Calculate number of steps (more steps for smoother movement)
        int steps = (int) (distance / 3); // Approximately 3 pixels per step
        steps = Math.max(steps, 20); // Minimum steps for smoothness
        steps = Math.min(steps, 200); // Maximum to prevent overly slow movements

        // Move the mouse along a quadratic B-spline curve with variable speed
        for (int i = 1; i <= steps; i++) {
            double t = i / (double) steps;

            // Apply easing function: starts fast (large steps), ends slow (small steps)
            // Using a cubic ease-out function: 1 - (1-t)^3
            double eased = 1 - Math.pow(1 - t, 3);

            // Quadratic Bezier curve formula
            double x = Math.pow(1 - eased, 2) * fromX +
                    2 * (1 - eased) * eased * ctrlX +
                    Math.pow(eased, 2) * toX;
            double y = Math.pow(1 - eased, 2) * fromY +
                    2 * (1 - eased) * eased * ctrlY +
                    Math.pow(eased, 2) * toY;

            robotInstance.mouseMove((int) x, (int) y);

            // Variable delay: faster at start, slower at end
            // Start with 1-3ms, end with 3-8ms
            int delay;
            if (t < 0.3) {
                // Fast start
                delay = 1 + random.nextInt(3);
            } else if (t < 0.7) {
                // Medium speed
                delay = 2 + random.nextInt(4);
            } else {
                // Slow end for precision
                delay = 3 + random.nextInt(6);
            }

            if (delay > 0) {
                robotInstance.delay(delay);
            }
        }

        // Ensure we end exactly at the target position
        robotInstance.mouseMove(toX, toY);

        // Small pause at the end (human-like settling)
        robotInstance.delay(20 + random.nextInt(30));
    }

    /**
     * Moves the mouse cursor smoothly to the center of the specified element.
     * This method only performs the movement if humanize is enabled and not in headless mode.
     * The actual click should still be performed by Selenium for reliability.
     *
     * @param element the WebElement to move the mouse to
     */
    protected void moveMouseToElement(WebElement element) {
        // Skip if humanize is disabled, or we're in headless mode
        if (!humanize || isSeleniumHeadless()) {
            return;
        }

        // Initialize Robot if needed
        Robot robotInstance = getRobot();
        if (robotInstance == null) {
            logger.debug("Robot not available, skipping mouse movement");
            return;
        }

        try {
            // Get element location and size relative to the page
            org.openqa.selenium.Point     elementLocation = element.getLocation();
            org.openqa.selenium.Dimension elementSize     = element.getSize();

            // Get browser window position on screen
            Point windowPosition = getDriver().manage().window().getPosition();

            // Calculate browser chrome height (cached after first calculation)
            int chromeHeight = getBrowserChromeHeight();

            // Calculate target screen coordinates (center of element)
            int targetX = windowPosition.getX() + elementLocation.getX() + (elementSize.getWidth() / 2);
            int targetY = windowPosition.getY() + elementLocation.getY() + (elementSize.getHeight() / 2) + chromeHeight;

            // Get current mouse position
            java.awt.Point currentMouse = MouseInfo.getPointerInfo().getLocation();

            // Perform smooth mouse movement with human-like characteristics
            humanLikeMouseMove(currentMouse.x, currentMouse.y, targetX, targetY);

            logger.debug("Moved mouse to element '{}' at ({}, {})", element.getText(), targetX, targetY);
            wait(300);

        } catch (Exception e) {
            logger.warn("Failed to move mouse to element: {}", e.getMessage());
            // Continue with normal Selenium click even if mouse movement fails
        }
    }

    /**
     * Sets a combobox value in a humanized way by clicking on the dropdown toggle,
     * waiting for the overlay to appear, and clicking on the matching item.
     * This simulates how a real human would interact with a combobox using mouse clicks.
     *
     * @param text the text of the item to select
     */
    public void setComboBoxValue(String id, String text) {
        if (humanize) {
            super.setComboBoxValue(id, text);
            return;
        }
        waitUntil(ExpectedConditions.elementToBeClickable(By.id(id)));
        WebElement comboBoxElement = findElement(By.id(id));
        WebElement inputElement    = comboBoxElement.findElement(By.tagName("input"));
        waitForElementToBeInteractable(inputElement.getAttribute("id"));
        try {
            // Find and click the toggle button to open the dropdown
            // A human would just click the toggle button directly (no need to click input first)
            // Vaadin combobox uses shadow DOM, so we use expandRootElementAndFindElement to access it
            try {
                // Try to find toggle button by ID first
                WebElement toggleButton = expandRootElementAndFindElement(comboBoxElement, "#toggleButton");

                if (toggleButton == null) {
                    // Fallback: try to find by part attribute
                    toggleButton = expandRootElementAndFindElement(comboBoxElement, "[part='toggle-button']");
                }

                if (toggleButton != null) {
                    moveMouseToElement(toggleButton);
                    toggleButton.click();
                    logger.debug("Clicked combobox toggle button via shadow DOM");
                } else {
                    // Fallback: click on the input field if toggle button not found
                    logger.debug("Toggle button not found in shadow DOM, clicking input field to open dropdown");
                    inputElement.click();
                }
            } catch (Exception ex) {
                // Fallback: click on the input field if shadow DOM access fails
                logger.debug("Failed to access toggle button via shadow DOM: {}, clicking input field to open dropdown", ex.getMessage());
                inputElement.click();
            }

            // Wait for the dropdown overlay to become visible
            // The overlay element exists in the DOM even when closed
            // When opened, the 'opened' attribute is set to "true" (not an empty string)
            waitUntil(ExpectedConditions.or(
                    ExpectedConditions.attributeToBe(By.cssSelector("vaadin-combo-box-overlay"), "opened", "true"),
                    ExpectedConditions.attributeToBe(By.cssSelector("vaadin-combo-box-dropdown-wrapper"), "opened", "true")
            ));

            // Find dropdown items
            // Items are in the light DOM as children of vaadin-combo-box-scroller
            // We can query them directly without accessing shadow DOM
            List<WebElement> dropdownItems = getDriver().findElements(By.cssSelector("vaadin-combo-box-item"));

            logger.debug("Found {} dropdown items", dropdownItems.size());

            // Find the item that matches the text
            WebElement matchingItem = null;
            for (WebElement item : dropdownItems) {
                String itemText = item.getText();
                if (itemText != null && itemText.trim().equals(text.trim())) {
                    matchingItem = item;
                    break;
                }
            }

            if (matchingItem != null) {
                // Move mouse to the item and click it
                moveMouseToElement(matchingItem);
                wait(100);
                matchingItem.click();
                logger.debug("Clicked on dropdown item: {}", text);
            } else {
                logger.warn("Could not find dropdown item with text: {}. Falling back to keyboard method.", text);
                // Fallback to typing method if item not found
                setComboBoxValueByTyping(inputElement, text);
            }

            // Wait for dropdown to close
            wait(200);

        } catch (Exception ex) {
            logger.warn("Error during humanized combobox selection: {}. Falling back to keyboard method.", ex.getMessage());
            // Fallback to typing method on any error
            setComboBoxValueByTyping(inputElement, text);
        }
    }

//    /**
//     * Adjust per-character typing delay in milliseconds for humanized mode.
//     */
//    public void setTypingDelayMillis(int typingDelayMillis) {
//        this.typingDelayMillis = Math.max(0, typingDelayMillis);
//    }

    /**
     * Fallback method to set combobox value by typing.
     * Used when humanized click selection fails or item cannot be found.
     *
     * @param inputElement the input element within the combobox
     * @param text         the text to type
     */
    private void setComboBoxValueByTyping(WebElement inputElement, String text) {
        String value = inputElement.getAttribute("value");
        if (!value.isEmpty()) {
            inputElement.sendKeys(Keys.CONTROL + "a");
            inputElement.sendKeys(Keys.DELETE);
        }
        typeText(inputElement, text);
        inputElement.sendKeys(Keys.RETURN);
        wait(500);
        inputElement.sendKeys(Keys.ARROW_DOWN, Keys.TAB);
    }

    /**
     * Sets a date picker value in a humanized way by clicking the toggle button,
     * then moving to the input field and typing the date.
     * This simulates how a human would interact with a date picker.
     *
     * @param datePickerId the ID of the date picker component
     * @param date         the LocalDate value to set
     */
    public void setDatePickerValue(String datePickerId, LocalDate date) {
        // Find the date picker element
        WebElement datePickerElement = findElement(By.id(datePickerId));
        // Find the toggle button in the shadow DOM to give visual feedback
        WebElement toggleButton = expandRootElementAndFindElement(datePickerElement, "[part='toggle-button']");

        if (toggleButton != null) {
            // Click the toggle button to show the calendar (gives visual feedback)
            moveMouseToElement(toggleButton);
            wait(100);
            toggleButton.click();
            logger.debug("Clicked date picker toggle button");
            wait(300); // Wait for calendar to appear
        }

        // Find the input field - it's NOT in shadow DOM, it's a direct child with slot='input'
        // The actual input has an id like "search-input-vaadin-date-picker-20"
        WebElement inputField = datePickerElement.findElement(By.cssSelector("input[slot='input']"));

        logger.debug("Found input field, typing date");

        moveMouseToElement(inputField);
        wait(100);
        inputField.click();// Click the input field to focus it
        wait(100);
        inputField.clear();// Clear any existing value
        wait(100);

        // Format the date in US format (M/d/yyyy)
        // Note: This matches the browser's default US locale
        String dateStr = date.format(DateTimeFormatter.ofPattern("M/d/yyyy"));
        logger.info("Typing date into date picker: {} (formatted as US: {})", date, dateStr);

        typeText(inputField, dateStr);

        wait(500);
        inputField.sendKeys(Keys.ENTER);// Press Enter to confirm the date and close the calendar
        logger.debug("Pressed Enter to confirm date");

        // Wait for the calendar overlay to close
        wait(200);
        WebElement overlay = expandRootElementAndFindElement(datePickerElement, "vaadin-date-picker-overlay");
        waitUntil(ExpectedConditions.invisibilityOfAllElements(overlay));
        logger.debug("Date picker overlay closed successfully");
        logger.debug("Successfully set date: {}", date);
    }

    /**
     * Show a full-screen overlay with title and subtitle.
     * The overlay fades in over 1 second and remains visible until hideOverlay() is called.
     * This is useful for creating intro screens or chapter markers in instruction videos.
     *
     * @param title    Main title text to display
     * @param subtitle Subtitle text (can be null or empty)
     */
    public void showOverlay(String title, String subtitle) {
        try {
            // Wait for page to be fully loaded
            waitForPageLoaded();

            // Escape special characters for JavaScript
            String escapedTitle    = escapeJavaScript(title);
            String escapedSubtitle = subtitle != null ? escapeJavaScript(subtitle) : "";

            String script =
                    "// Remove existing overlay if present\n" +
                            "var existingOverlay = document.getElementById('video-intro-overlay');\n" +
                            "if (existingOverlay) {\n" +
                            "    existingOverlay.remove();\n" +
                            "}\n" +
                            "\n" +
                            "// Create overlay container\n" +
                            "var overlay = document.createElement('div');\n" +
                            "overlay.id = 'video-intro-overlay';\n" +
                            "overlay.style.cssText = 'position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background-color: #000000; z-index: 999999; display: flex; justify-content: center; align-items: center; opacity: 0; transition: opacity 1s ease-in-out;';\n" +
                            "\n" +
                            "// Create content container\n" +
                            "var content = document.createElement('div');\n" +
                            "content.style.cssText = 'text-align: center;';\n" +
                            "\n" +
                            "// Create title element\n" +
                            "var titleDiv = document.createElement('div');\n" +
                            "titleDiv.style.cssText = 'color: #ffffff; font-size: 48px; font-weight: bold; margin-bottom: 20px; font-family: \"Segoe UI\", Tahoma, Geneva, Verdana, sans-serif; text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);';\n" +
                            "titleDiv.textContent = '" + escapedTitle + "';\n" +
                            "content.appendChild(titleDiv);\n" +
                            "\n" +
                            "// Create subtitle element if provided\n" +
                            "if ('" + escapedSubtitle + "') {\n" +
                            "    var subtitleDiv = document.createElement('div');\n" +
                            "    subtitleDiv.style.cssText = 'color: #cccccc; font-size: 24px; font-family: \"Segoe UI\", Tahoma, Geneva, Verdana, sans-serif; text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);';\n" +
                            "    subtitleDiv.textContent = '" + escapedSubtitle + "';\n" +
                            "    content.appendChild(subtitleDiv);\n" +
                            "}\n" +
                            "\n" +
                            "overlay.appendChild(content);\n" +
                            "document.body.appendChild(overlay);\n" +
                            "\n" +
                            "// Trigger fade-in animation\n" +
                            "setTimeout(function() {\n" +
                            "    overlay.style.opacity = '1';\n" +
                            "}, 10);\n";

            executeJavaScript(script);
            logger.info("Displayed overlay with title: '{}'", title);

            // Wait for fade-in animation to complete
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while waiting for overlay fade-in");
            }
        } catch (Exception e) {
            logger.error("Failed to show overlay: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to show overlay: " + e.getMessage(), e);
        }
    }

    /**
     * Show overlay, wait for specified duration, then hide it automatically.
     * Total duration will be: 1s (fade-in) + displaySeconds + 1s (fade-out).
     *
     * @param title          Main title text to display
     * @param subtitle       Subtitle text (can be null or empty)
     * @param displaySeconds How long to display the overlay between fade-in and fade-out
     */
    public void showOverlayAndWait(String title, String subtitle, int displaySeconds) {
        showOverlay(title, subtitle);

        // Wait for the specified display duration
        if (displaySeconds > 0) {
            try {
                Thread.sleep(displaySeconds * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while waiting for overlay display duration");
            }
        }

        hideOverlay();
    }

    /**
     * Internal helper to type text into an input/textarea.
     * Respects humanized typing mode if enabled, using variable delays
     * to simulate real human typing patterns (not perfectly rhythmic).
     */
    protected void typeText(WebElement inputElement, String text) {
        if (text == null || text.isEmpty()) return;

        for (int idx = 0; idx < text.length(); idx++) {
            String ch = String.valueOf(text.charAt(idx));
            inputElement.sendKeys(ch);

            if (typingDelayMillis > 0) {
                try {
                    // Variable delay: base delay +/- 50% randomness
                    // This creates a more natural typing rhythm
                    int minDelay      = typingDelayMillis / 2;
                    int maxDelay      = typingDelayMillis + (typingDelayMillis / 2);
                    int variableDelay = minDelay + random.nextInt(maxDelay - minDelay + 1);

                    // Occasionally add a longer pause (simulating thinking/hesitation)
                    // About 10% chance of a longer pause
                    if (random.nextInt(10) == 0) {
                        variableDelay += typingDelayMillis * (2 + random.nextInt(3)); // 2-4x longer
                    }

                    Thread.sleep(variableDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

}
