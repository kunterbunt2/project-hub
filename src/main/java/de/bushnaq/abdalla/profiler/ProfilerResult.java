package de.bushnaq.abdalla.profiler;

public class ProfilerResult {
    long      delta;
    SampleSet sampleSet;
    long      totalDelta;

    public ProfilerResult(long totalStart, SampleSet sampleMap) {
        this.sampleSet = sampleMap;
        totalDelta     = System.nanoTime() - totalStart;

        long sum = 0;
        for (SampleType type : sampleMap.sampleKeySet()) {
            Sample profilingCounter = sampleMap.getSample(type);
            sum += profilingCounter.getTimeNanoSec();
        }
        delta = totalDelta - sum;
    }

}
