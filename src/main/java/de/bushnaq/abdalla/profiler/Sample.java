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