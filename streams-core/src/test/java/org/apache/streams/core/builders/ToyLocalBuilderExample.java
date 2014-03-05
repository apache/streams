package org.apache.streams.core.builders;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.test.processors.DoNothingProcessor;
import org.apache.streams.core.test.providers.NumericMessageProvider;
import org.apache.streams.core.test.writer.DoNothingWriter;
import org.apache.streams.core.test.writer.SystemOutWriter;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by rebanks on 2/20/14.
 */
public class ToyLocalBuilderExample {

    /**
     * A simple example of how to run a stream in local mode.
     * @param args
     */
    public static void main(String[] args) {
        StreamBuilder builder = new LocalStreamBuilder(new LinkedBlockingQueue<StreamsDatum>());
        builder.newReadCurrentStream("prov", new NumericMessageProvider(1000000))
                .addStreamsProcessor("proc", new DoNothingProcessor(), 100, "prov")
                .addStreamsPersistWriter("writer", new DoNothingWriter(), 3, "proc");
        builder.start();
    }

}
