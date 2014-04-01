package org.apache.streams.pig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.pig.EvalFunc;
import org.apache.pig.builtin.MonitoredUDF;
import org.apache.pig.data.BagFactory;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.util.UDFContext;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by sblackmon on 3/25/14.
 */
@MonitoredUDF(timeUnit = TimeUnit.SECONDS, duration = 30, intDefault = 10)
public class StreamsProcessDocumentExec extends EvalFunc<String> {

    TupleFactory mTupleFactory = TupleFactory.getInstance();
    BagFactory mBagFactory = BagFactory.getInstance();

    StreamsProcessor streamsProcessor;
    ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    public StreamsProcessDocumentExec(String... execArgs) throws ClassNotFoundException{
        System.out.println("A");
        Preconditions.checkNotNull(execArgs);
        System.out.println("B");
        Preconditions.checkArgument(execArgs.length > 0);
        System.out.println("C");
        String processorFullName = execArgs[0];
        System.out.println("D");
        Preconditions.checkNotNull(processorFullName);
        System.out.println("E");
        streamsProcessor = StreamsComponentFactory.getProcessorInstance(Class.forName(processorFullName));
        System.out.println("F");
        streamsProcessor.prepare(null);
        System.out.println("G");
    }

    @Override
    public String exec(Tuple input) throws IOException {

        System.out.println("H");
        Preconditions.checkNotNull(streamsProcessor);
        Preconditions.checkNotNull(input);
        Preconditions.checkArgument(input.size() == 1);
        System.out.println("I");

        String document = (String) input.get(0);

        Preconditions.checkNotNull(document);

        System.out.println(document);

        StreamsDatum entry = new StreamsDatum(document);

        Preconditions.checkNotNull(entry);

        System.out.println(entry);

        List<StreamsDatum> resultSet = streamsProcessor.process(entry);

        System.out.println(resultSet);

        Object resultDoc = null;
        for( StreamsDatum resultDatum : resultSet ) {
            resultDoc = resultDatum.getDocument();
        }

        Preconditions.checkNotNull(resultDoc);

        System.out.println(resultDoc);

        if( resultDoc instanceof String )
            return (String) resultDoc;
        else if( resultDoc instanceof ObjectNode)
            return mapper.writeValueAsString(resultDoc);
        else
            return null;

    }

    public void finish() {
        streamsProcessor.cleanUp();
    }
}
