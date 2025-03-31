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

package de.bushnaq.abdalla.profiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseUtils {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected TestResult findInTestResult(TestResult[] testResults, SampleType sampleType) {
        for (TestResult testResult : testResults) {
            if (testResult.type.equals(sampleType)) {
                return testResult;
            }
        }
        return null;
    }

    protected void mockupCpuAccess(int ms) throws InterruptedException {
        sleep(ms);
    }

    protected void mockupNetAccess(int ms) throws InterruptedException {
        sleep(ms);
    }

    protected void mockupSmbAccess(int ms) throws InterruptedException {
        sleep(ms);
    }

    protected void mockupSqlAccess(int ms) throws InterruptedException {
        sleep(ms);
    }

    protected void sleep(int ms) throws InterruptedException {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    protected void test(TestResult[] testResults) {
        for (SampleType sampleType : Profiler.intermediateSampleSet.sampleKeySet()) {

            TestResult testResult = findInTestResult(testResults, sampleType);
            if (testResult != null) {
                assertEquals((double) testResult.time, Profiler.intermediateSampleSet.getSample(testResult.type).getTimeNanoSec() / 1000000, 50,
                        String.format("bad %s measurment", testResult.type.name()));
            } else {
                assertEquals(0.0, Profiler.intermediateSampleSet.getSample(sampleType).getTimeNanoSec() / 1000000, 50,
                        String.format("bad %s measurment", sampleType.name()));
            }
        }
        ProfilerResult result = Profiler.intermediateResult;
        long           delta  = (result.delta * 100) / result.totalDelta;
        assertThat(delta, is(lessThanOrEqualTo(2L)));

    }

}
