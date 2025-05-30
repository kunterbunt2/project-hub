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
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * handles vaadin ui tests using selenium
 */
@Component
public class SeleniumHandler {
    private       WebDriver     driver;
    private final Duration      implicitWaitDuration;
    private       WebDriverWait wait;
    private       Duration      waitDuration;

    public SeleniumHandler() {
        this(Duration.ofSeconds(10), Duration.ofSeconds(30));
    }

    public SeleniumHandler(Duration implicitWaitDuration, Duration waitDuration) {
        this.implicitWaitDuration = implicitWaitDuration;
        this.waitDuration         = waitDuration;
        //chrome
    }

    public void click(String id) {
        waitUntil(ExpectedConditions.elementToBeClickable(By.id(id)));
        WebElement element = findElement(By.id(id));
        element.click();
    }

    @PreDestroy
    public void destroy() {
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

    public WebElement expandRootElementAndFindElement(WebElement element, String elementName) {
        String script = String.format("return arguments[0].shadowRoot.querySelector('%s')", elementName);
        //        Object o = ((JavascriptExecutor) driver).executeScript(script, element);
        //        List<WebElement> l = (List<WebElement>) ((JavascriptExecutor) driver).executeScript("return arguments[0].shadowRoot.children", element);
        //        int i = 0;
        //        for (WebElement e : l) {
        //            System.out.println(i++ + " " + e.getText() + " " + e.getTagName());
        //        }
        WebElement ele = (WebElement) ((JavascriptExecutor) getDriver()).executeScript(script, element);
        return ele;
    }

    public WebElement findElement(By by) {
        return getDriver().findElement(by);
    }

    public List<WebElement> findElements(By by) {
        return getDriver().findElements(by);
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

    public String getCurrentUrl() {
        return getDriver().getCurrentUrl();
    }

    private WebDriver getDriver() {
        if (driver == null) {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
            options.addArguments("--remote-allow-origins=*");
            driver = new ChromeDriver(options);
            wait   = new WebDriverWait(getDriver(), waitDuration);
            //Applied wait time
            setImplicit(implicitWaitDuration);
            //maximize window
            getDriver().manage().window().maximize();
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
        //        if (value.isEmpty()) {
        //            fail("filter TextField not empy");
        //            return null;
        //        } else {
        return value;
        //        }
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
        //        if (value.isEmpty()) {
        //            fail("filter TextField not empy");
        //            return null;
        //        } else {
        return value;
        //        }
    }

    public String getTitle() {
        return getDriver().getTitle();
    }

    public Duration getWaitDuration() {
        return waitDuration;
    }

    public void selectGridRow(String gridRowBaseId, Class<?> viewClass, String rowName) {
        String  url             = getRouteValue(viewClass);
        boolean outerTesting    = true;
        int     outerIterations = 12;//5 seconds
//        WebElement label           = findElement(By.id(labelId));
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
//        assertEquals(rowName, label.getText());
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

    private void testForAnyError() {
        assertEquals(-1, getTitle().indexOf("HTTP Status"), String.format("HTTP error detected '%s'.", getTitle()));
    }

    private void testForError(String errorTitle) {
        //        String errorMessage = String.format("HTTP Status %d – Not Found", errorCode);
        Assertions.assertTrue(getTitle().indexOf(errorTitle) != -1, String.format("Expected '%s', but got '%s'.", errorTitle, getTitle()));
    }

    public void toggleCheckbox(String id) {
        WebElement e = findElement(By.id(id));
        WebElement i = e.findElement(By.tagName("input"));
        i.click();
    }
    //    public void checkCheckbox(String id) {
    //        WebElement e = findElement(By.id(id));
    //        WebElement i = e.findElement(By.tagName("input"));
    //        if (!i.isSelected()) {
    //            i.click();
    //        }
    //    }

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
                    driver.quit();//quit the driver
                    driver = null;
                    return false;
                }

                // Check for timeout if specified
                if (timeoutMillis > 0 && System.currentTimeMillis() - startTime > timeoutMillis) {
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
                driver.quit();//quit the driver
                driver = null;
                return true;
            }
        }
    }
}
