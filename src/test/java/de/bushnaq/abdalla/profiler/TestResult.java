package de.bushnaq.abdalla.profiler;


public class TestResult {
    long       time;
    SampleType type;

    public TestResult(SampleType type, long time) {
        this.type = type;
        this.time = time;
    }
}