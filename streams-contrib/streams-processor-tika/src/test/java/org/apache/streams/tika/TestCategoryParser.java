package org.apache.streams.tika;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/**
 * Created by rdouglas on 4/24/14.
 */
public class TestCategoryParser {

    @Test
    public void testSerializability() {
        CategoryParser parser = new CategoryParser();
        CategoryParser clone = SerializationUtils.clone(parser);
    }
}
