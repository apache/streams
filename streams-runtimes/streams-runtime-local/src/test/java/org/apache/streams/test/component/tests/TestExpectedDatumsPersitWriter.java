package org.apache.streams.test.component.tests;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.test.component.ExpectedDatumsPersistWriter;
import org.apache.streams.test.component.StringToDocumentConverter;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by rebanks on 2/28/14.
 */
public class TestExpectedDatumsPersitWriter {

    private static final StreamsDatum[] INPUT_DATUMS = new StreamsDatum[] {
            new StreamsDatum("Document1"),
            new StreamsDatum("Document2"),
            new StreamsDatum("Document3"),
            new StreamsDatum("Document4")
//            Uncomment to prove failures occur, or comment out a datum above
//            ,new StreamsDatum("Document5")
    };

    @Test
    public void testExpectedDatumsPersistWriterFileName() {
        testDatums(new ExpectedDatumsPersistWriter(new StringToDocumentConverter(), "/TestFile.txt"));
    }



    private void testDatums(ExpectedDatumsPersistWriter writer) {
        writer.prepare(null);
        for(StreamsDatum datum : INPUT_DATUMS) {
            writer.write(datum);
        }
        writer.cleanUp();
    }

}
