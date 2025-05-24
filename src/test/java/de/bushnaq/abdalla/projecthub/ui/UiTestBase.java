package de.bushnaq.abdalla.projecthub.ui;

import de.bushnaq.abdalla.projecthub.ui.util.SeleniumHandler;
import de.bushnaq.abdalla.projecthub.util.AbstractGanttTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

/**
 * prepare test server
 * provide application specific utility methods
 */
public class UiTestBase extends AbstractGanttTestUtil {
    protected SeleniumHandler seleniumHandler;

    public UiTestBase() {
//        System.setProperty("javax.net.ssl.keyStore", "config/serverkeystore");
//        System.setProperty("javax.net.ssl.keyStorePassword", "timeTrackerkey");
    }

    //    @Override
    @AfterEach
    public void afterEach(TestInfo testInfo) throws Exception {
//        super.afterEach(testInfo);
        seleniumHandler.cleanup(testInfo);
    }

    //    @Override
    @BeforeEach
    public void beforeEach(TestInfo testInfo) throws Exception {
        super.beforeEach();
        seleniumHandler = new SeleniumHandler();
    }


}
