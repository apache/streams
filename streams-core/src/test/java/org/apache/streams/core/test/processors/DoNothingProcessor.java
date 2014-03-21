package org.apache.streams.core.test.processors;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by rebanks on 2/20/14.
 */
public class DoNothingProcessor implements StreamsProcessor {

    List<StreamsDatum> result;

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
