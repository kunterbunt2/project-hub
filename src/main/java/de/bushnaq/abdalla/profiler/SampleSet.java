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

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SampleSet implements Cloneable {
    private final Map<String, Long>       counterMap = new HashMap<>();
    private final Map<SampleType, Sample> sampleMap  = new HashMap<>();
    @Setter
    @Getter
    private       String                  subject;
    @Getter
    private       long                    totalStart = System.nanoTime();
    @Setter
    @Getter
    private       SampleType              type       = SampleType.CPU;

    public void add(SampleSet incrementealSamples) {
        for (SampleType type : incrementealSamples.sampleKeySet()) {
            addSample(incrementealSamples.getSample(type));
        }
        for (String name : incrementealSamples.counterKeySet()) {
            addCounter(name, incrementealSamples.getCounter(name));
        }
    }

    private void addCounter(String name, Long incCounter) {
        Long counter = counterMap.get(name);
        if (counter == null) {
            counterMap.put(name, incCounter);
        } else {
            counterMap.put(name, counter + incCounter);
        }
    }

    private void addSample(Sample incSample) {
        Sample sample = sampleMap.get(incSample.getType());
        if (sample == null) {
            sampleMap.put(incSample.getType(), incSample);
        } else {
            sample.add(incSample);
        }
    }

    @Override
    public SampleSet clone() throws CloneNotSupportedException {
        SampleSet s = new SampleSet();

        for (SampleType sampleType : sampleMap.keySet()) {
            s.sampleMap.put(sampleType, sampleMap.get(sampleType).clone());
        }
        //        for (String string : counterMap.keySet()) {
        //            s.sampleMap.put(string, counterMap.get(string).clone());
        //        }

        s.counterMap.putAll(counterMap);
        s.type       = type;
        s.totalStart = totalStart;
        s.subject    = subject;
        return s;
    }

    public Set<String> counterKeySet() {
        return counterMap.keySet();
    }

    public Long getCounter(String counterName) {
        return counterMap.get(counterName);
    }

    public Sample getSample() {
        return sampleMap.get(type);
    }

    public Sample getSample(SampleType type) {
        return sampleMap.get(type);
    }

    public void put(String counterName, Long counter) {
        counterMap.put(counterName, counter);
    }

    public void putSample(SampleType type, Sample sample) {
        sampleMap.put(type, sample);
    }

    public void reset() {
        for (SampleType type : sampleMap.keySet()) {
            Sample sample = sampleMap.get(type);
            sample.reset();
        }
        counterMap.clear();
        totalStart = System.nanoTime();
        subject    = null;
    }

    public Set<SampleType> sampleKeySet() {
        return sampleMap.keySet();
    }

    public void setTotalStart(long totalStart) {
        this.totalStart = totalStart;
    }

}
