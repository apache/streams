package org.apache.streams.data.moreover;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;

import java.math.BigInteger;
import java.util.Iterator;

public class MoreoverResultSetWrapper implements StreamsResultSet {

    private MoreoverResult underlying;

    public MoreoverResultSetWrapper(MoreoverResult underlying) {
        this.underlying = underlying;
    }

    @Override
    public long getStartTime() {
        return underlying.getStart();
    }

    @Override
    public long getEndTime() {
        return underlying.getEnd();
    }

    @Override
    public String getSourceId() {
        return underlying.getClientId();
    }

    @Override
    public BigInteger getMaxSequence() {
        return underlying.getMaxSequencedId();
    }

    @Override
    public Iterator<StreamsDatum> iterator() {
        return underlying.iterator();
    }
}
