package de.bushnaq.abdalla.profiler;

import org.junit.jupiter.api.Test;

/**
 * calc(200)
 * net(300)
 * sql(500)
 * net(200)
 * smb(300)
 * calc(200)
 * <p>
 * expected result
 * net = 500ms
 * calc = 400ms
 * sql = 500ms
 * smb = 300ms
 *
 * @author abdalla
 */

/**
 * Profiler should support logging intermediate results
 *
 * @author abdalla
 */
class NestedLoggingTest extends BaseUtils {

    private void method1() throws Exception {
        try (Profiler timeKeeping = new Profiler("method1", SampleType.CPU)) {
            try (Profiler pc = new Profiler(SampleType.TCP)) {
                mockupNetAccess(300);
                sqlMethod();
                mockupNetAccess(200);
            }
            test(new TestResult[]{//
                    new TestResult(SampleType.SQL, 500),//
                    new TestResult(SampleType.TCP, 500)//
            });

            mockupCpuAccess(100);
            try (Profiler pc = new Profiler(SampleType.SMB)) {
                mockupSmbAccess(300);
            }
            test(new TestResult[]{//
                    new TestResult(SampleType.SMB, 300)//
            });
        }
        test(new TestResult[]{//
                new TestResult(SampleType.SQL, 500),//
                new TestResult(SampleType.CPU, 100),//
                new TestResult(SampleType.TCP, 500),//
                new TestResult(SampleType.SMB, 300)//
        });
    }

    private void method2() throws Exception {
        try (Profiler timeKeeping = new Profiler("method2", SampleType.CPU)) {
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
    }

    @Test
    void nestedLogsTest() throws Exception {
        //        Profiler.enableTraceLogger = true;
        try (Profiler timeKeeping = new Profiler(this.getClass().getSimpleName(), SampleType.CPU)) {
            mockupCpuAccess(200);
            method1();
            mockupCpuAccess(200);
            method2();
            mockupCpuAccess(100);
        }
    }

    private void sqlMethod() throws Exception {
        try (Profiler pc = new Profiler(SampleType.SQL)) {
            mockupSqlAccess(500);
        }
    }

}
