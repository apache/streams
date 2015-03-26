package org.apache.streams.rss.provider;

import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CollectorProcessor implements StreamsProcessor {
    private AtomicInteger docCount = new AtomicInteger(0);

    public CollectorProcessor() {
        super();
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        if(entry != null && entry.getDocument() != null && entry.getDocument().toString().length() > 0) {
            docCount.addAndGet(1);
        }

        return Lists.newArrayList(entry);
    }

    @Override
    public void prepare(Object configurationObject) {

    }

    @Override
    public void cleanUp() {

    }

    public int getDocCount() {
        return docCount.get();
    }
}
