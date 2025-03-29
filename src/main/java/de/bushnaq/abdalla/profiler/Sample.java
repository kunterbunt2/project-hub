package de.bushnaq.abdalla.profiler;

/**
 * One set of profile measurements
 * support nesting
 * support intermediate report
 *
 * @author abdalla
 */
public class Sample implements Cloneable {
    private       long       count       = 0;
    private       long       timeNanoSec = 0;
    private final SampleType type;

    Sample(SampleType type) {
        this.type = type;
    }

    public void add(Sample incSample) {
        count += incSample.count;
        timeNanoSec += incSample.timeNanoSec;
    }

    public void addTimeNanoSec(long timeNanoSec) {
        this.timeNanoSec += timeNanoSec;
    }

    @Override
    public Sample clone() throws CloneNotSupportedException {
        Sample s = (Sample) super.clone();
        return s;
    }

    public long getCount() {
        return count;
    }

    public long getTimeNanoSec() {
        return timeNanoSec;
    }

    public SampleType getType() {
        return type;
    }

    public void reset() {
        count       = 0;
        timeNanoSec = 0;
    }

    public void setCount(long count) {
        this.count = count;
    }

}