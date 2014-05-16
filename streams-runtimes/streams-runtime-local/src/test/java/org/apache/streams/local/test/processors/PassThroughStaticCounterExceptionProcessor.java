package org.apache.streams.local.test.processors;

import org.apache.streams.core.StreamsDatum;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by rdouglas on 5/16/14.
 */
public class PassThroughStaticCounterExceptionProcessor extends PassThroughStaticCounterProcessor{
    private final int numErrorsToThrow;
    private int numErrorsThrown;

    public PassThroughStaticCounterExceptionProcessor(int delay, int numErrorsToThrow) {
        super(delay);

        this.numErrorsToThrow = numErrorsToThrow <= 0 ? 1 : numErrorsToThrow;
        this.numErrorsThrown = 0;
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        super.sleepSafely();
        super.count++;
        List<StreamsDatum> result = new LinkedList<StreamsDatum>();

        if(this.numErrorsThrown++ < this.numErrorsToThrow) {
            throw new RuntimeException();
        } else {
            result.add(entry);
            SEEN_DATA.add(super.id);
        }

        return result;
    }
}
