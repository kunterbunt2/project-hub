package de.bushnaq.abdalla.profiler;

import org.junit.jupiter.api.Test;

class SimpleNestedLoggingTest extends BaseUtils {

    private void method1() throws Exception {
        try (Profiler timeKeeping = new Profiler("method1", SampleType.TCP)) {
            mockupNetAccess(300);
        }
        test(new TestResult[]{new TestResult(SampleType.TCP, 300)});
    }

    @Test
    void simpleNestedLogsTest() throws Exception {
        Profiler.enableTraceLogger = true;
        try (Profiler timeKeeping = new Profiler(this.getClass().getSimpleName(), SampleType.CPU)) {
            mockupCpuAccess(200);
            method1();
            mockupCpuAccess(200);
        }
        test(new TestResult[]{new TestResult(SampleType.TCP, 300), new TestResult(SampleType.CPU, 400)});
    }

}
