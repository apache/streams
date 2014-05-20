package org.apache.streams.pig.test;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;

import java.util.LinkedList;
import java.util.List;

/**
 * Used to Test Pig processor wrapper
 */
public class DoNothingProcessor implements StreamsProcessor {

    List<StreamsDatum> result;

    public DoNothingProcessor() {
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        this.result = new LinkedList<StreamsDatum>();
        result.add(entry);
        return result;
    }

    @Override
    public void prepare(Object configurationObject) {

    }

    @Override
    public void cleanUp() {
        System.out.println("Processor clean up!");
    }
}
