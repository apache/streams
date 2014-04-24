package org.apache.streams.tika;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/**
 * Created by rdouglas on 4/24/14.
 */
public class TestTikaProcessor {
    @Test
    public void testSerializability() {
        TikaProcessor processor = new TikaProcessor();
        TikaProcessor clone = SerializationUtils.clone(processor);
    }
}
