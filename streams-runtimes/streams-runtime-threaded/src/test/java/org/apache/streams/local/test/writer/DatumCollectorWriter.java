package org.apache.streams.local.test.writer;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;

import java.util.ArrayList;
import java.util.List;

public class DatumCollectorWriter implements StreamsPersistWriter {

    private final List<StreamsDatum> datums = new ArrayList<StreamsDatum>();

    public List<StreamsDatum> getDatums() { return this.datums; }

    @Override
    public void write(StreamsDatum entry) {
        this.datums.add(entry);
    }

    @Override
    public void prepare(Object configurationObject) {

    }

    @Override
    public void cleanUp() {

    }
}
