package org.apache.streams.core.test.writer;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;

/**
 * Created by rebanks on 2/20/14.
 */
public class SystemOutWriter implements StreamsPersistWriter {
    @Override
    public void write(StreamsDatum entry) {
        System.out.println(entry.document);
    }

    @Override
    public void prepare(Object configurationObject) {

    }

    @Override
    public void cleanUp() {
        System.out.println("Clean up called writer!");
    }
}
