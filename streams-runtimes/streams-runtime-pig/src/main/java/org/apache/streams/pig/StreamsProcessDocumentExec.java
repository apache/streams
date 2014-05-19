package org.apache.streams.pig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import datafu.pig.util.SimpleEvalFunc;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by sblackmon on 3/25/14.
 */
@MonitoredUDF(timeUnit = TimeUnit.SECONDS, duration = 30, intDefault = 10)
public class StreamsProcessDocumentExec extends SimpleEvalFunc<String> {

    StreamsProcessor streamsProcessor;
    ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    public StreamsProcessDocumentExec(String... execArgs) throws ClassNotFoundException{
        Preconditions.checkNotNull(execArgs);
        Preconditions.checkArgument(execArgs.length > 0);
        String classFullName = execArgs[0];
        Preconditions.checkNotNull(classFullName);
        String[] prepareArgs = new String[execArgs.length-1];
        ArrayUtils.remove(execArgs, 0);
        ArrayUtils.addAll(prepareArgs, execArgs);
        streamsProcessor = StreamsComponentFactory.getProcessorInstance(Class.forName(classFullName));
        if( execArgs.length == 1 ) {
            streamsProcessor.prepare(null);
        } else if( execArgs.length > 1 ) {
            streamsProcessor.prepare(prepareArgs);
        }
    }

    public String call(String document) throws IOException {

        Preconditions.checkNotNull(streamsProcessor);
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

        if( resultDoc instanceof String )
            return (String) resultDoc;
        else
            return mapper.writeValueAsString(resultDoc);

    }

    public void finish() {
        streamsProcessor.cleanUp();
    }
}
