package de.bushnaq.abdalla.profiler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * cpu(200)
 * net(300)
 * sql(500)
 * net(200)
 * smb(300)
 * cpu(200)
 * <p>
 * expected result
 * net = 500ms
 * cpu = 500ms
 * sql = 500ms
 * smb = 300ms
 *
 * @author abdalla
 */

/**
 * Profiler should support nested calls
 *
 * @author abdalla
 */
class NestedCallsTest extends BaseUtils {

    private void method1() throws Exception {
        try (Profiler pc = new Profiler(SampleType.TCP)) {
            mockupNetAccess(300);
            sqlMethod();
            mockupNetAccess(200);
        }
        mockupCpuAccess(100);
        try (Profiler pc = new Profiler(SampleType.SMB)) {
            mockupSmbAccess(300);
        }
    }

    @Test
    void nestedCallsTest() throws Exception {
        //        Profiler.enableTraceLogger = true;
        try (Profiler p1 = new Profiler()) {
            try (Profiler p2 = new Profiler(SampleType.CPU)) {
                mockupCpuAccess(200);
                Profiler.incrementCounter("counter-1", 1000);
                method1();
                mockupCpuAccess(200);
            }
        }
        Profiler.log("nestedCallsTest");
        test(new TestResult[]{//
                new TestResult(SampleType.SQL, 500),//
                new TestResult(SampleType.CPU, 500),//
                new TestResult(SampleType.TCP, 500),//
                new TestResult(SampleType.SMB, 300)//
        });
        assertEquals(1000, Profiler.sampleSet.getCounter("counter-1"), "");
        assertEquals(333, Profiler.sampleSet.getCounter("counter-2"), "");
    }

    private void sqlMethod() throws Exception {
        try (Profiler pc = new Profiler(SampleType.SQL)) {
            Profiler.incrementCounter("counter-2", 333);
            mockupSqlAccess(500);
        }
    }

}
