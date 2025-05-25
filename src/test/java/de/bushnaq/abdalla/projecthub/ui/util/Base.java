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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

/**
 * restore/drop test databases
 * log test start/stop
 */
public class Base {
    public static final String ARTIFACT_VERSION = "1.0.0-SNAPSHOT";
    long count = 0;
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    long time = 0;

    @AfterEach
    protected void afterEach(TestInfo testInfo) throws Exception {
        logger.info("----------------------------------------------");
        logger.info("stop " + testInfo.getDisplayName());
        logger.info("==============================================");
        long count2 = 0;
        long time2  = 0;
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            count2 += gc.getCollectionCount();
            time2 += gc.getCollectionTime();
        }
        logger.info("Total Garbage Collections: " + (count2 - count));
        logger.info("Total Garbage Collection Time (ms): " + (time2 - time));
    }

    @BeforeEach
    protected void beforeEach(TestInfo testInfo) throws Exception {
        count = 0;
        time  = 0;
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            count += gc.getCollectionCount();
            time += gc.getCollectionTime();
        }
        logger.info("==============================================");
        logger.info("start " + testInfo.getDisplayName());
        logger.info("----------------------------------------------");

    }


}
