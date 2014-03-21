package org.apache.streams.test.component.tests;

import org.apache.streams.core.builders.LocalStreamBuilder;
import org.apache.streams.test.component.ExpectedDatumsPersistWriter;
import org.apache.streams.test.component.FileReaderProvider;
import org.apache.streams.test.component.StringToDocumentConverter;
import org.junit.Test;

/**
 * Created by rebanks on 2/28/14.
 */
public class TestCompoentsLocalStream {

    @Test
    public void testLocalStreamWithComponent() {
        LocalStreamBuilder builder = new LocalStreamBuilder();
        builder.newReadCurrentStream("provider", new FileReaderProvider("/TestFile.txt",
                                                                        new StringToDocumentConverter()));
        builder.addStreamsPersistWriter("writer", new ExpectedDatumsPersistWriter(new StringToDocumentConverter(),
                "/TestFile.txt"), 1, "provider")
        .start();
    }
}
