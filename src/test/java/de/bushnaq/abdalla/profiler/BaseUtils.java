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
