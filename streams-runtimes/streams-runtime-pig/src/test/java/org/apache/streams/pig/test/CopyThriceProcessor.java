package org.apache.streams.pig.test;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;

import java.util.LinkedList;
import java.util.List;

/**
 * Used to Test Pig processor wrapper
 */
public class CopyThriceProcessor implements StreamsProcessor {

    List<StreamsDatum> result;

    public CopyThriceProcessor() {
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        this.result = new LinkedList<StreamsDatum>();
        result.add(entry);
        result.add(entry);
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
