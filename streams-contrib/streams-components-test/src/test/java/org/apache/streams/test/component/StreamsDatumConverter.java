package org.apache.streams.test.component;

import org.apache.streams.core.StreamsDatum;

import java.io.Serializable;

/**
 * Created by rebanks on 2/27/14.
 */
public interface StreamsDatumConverter extends Serializable {

    public StreamsDatum convert(String s);
}
