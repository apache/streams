package org.apache.streams.data.moreover;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Queue;

public class MoreoverResultSetWrapper extends StreamsResultSet {

    public MoreoverResultSetWrapper(MoreoverResult underlying) {
        super((Queue<StreamsDatum>)underlying);
    }

}
