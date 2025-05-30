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

import de.bushnaq.abdalla.projecthub.util.AbstractGanttTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

/**
 * prepare test server
 * provide application specific utility methods
 */

public class UiTestBase extends AbstractGanttTestUtil {
    //    SeleniumHandler seleniumHandler
    public UiTestBase() {
//        this.seleniumHandler=seleniumHandler;
//        System.setProperty("javax.net.ssl.keyStore", "config/serverkeystore");
//        System.setProperty("javax.net.ssl.keyStorePassword", "timeTrackerkey");
    }

    //    @Override
    @AfterEach
    public void afterEach(TestInfo testInfo) throws Exception {
//        super.afterEach(testInfo);
//        seleniumHandler.cleanup(testInfo);
    }

    //    @Override
    @BeforeEach
    public void beforeEach(TestInfo testInfo) throws Exception {
        super.beforeEach();
//        seleniumHandler = new SeleniumHandler();
    }

}
