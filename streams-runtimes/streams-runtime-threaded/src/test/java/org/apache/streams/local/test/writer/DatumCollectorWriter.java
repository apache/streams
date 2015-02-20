package org.apache.streams.local.test.writer;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatumCollectorWriter implements StreamsPersistWriter {

    private final List<StreamsDatum> datums = Collections.synchronizedList(new ArrayList<StreamsDatum>());
    private boolean cleanupCalled = false;
    private boolean prepareCalled = false;

    public boolean wasCleanupCalled() { return this.cleanupCalled; }
    public boolean wasPrepeareCalled() { return this.prepareCalled; }

    public List<StreamsDatum> getDatums() { return this.datums; }

    @Override
    public void write(StreamsDatum entry) {
        this.datums.add(entry);
    }

    @Override
    public void prepare(Object configurationObject) {
        this.prepareCalled = true;
    }

    @Override
    public void cleanUp() {
        this.cleanupCalled = true;
    }
}
