package org.apache.streams.datasift.provider;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.datasift.Datasift;

/**
 * Converts a {@link org.apache.streams.datasift.Datasift} object to a StreamsDatum
 */
public interface DatasiftTypeConverter {

    /**
     * Converts a {@link org.apache.streams.datasift.Datasift} object to a StreamsDatum
     * @param datasift
     * @return
     */
    public StreamsDatum convert(Datasift datasift);


}
