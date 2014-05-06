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
        String[] prepareArgs = (String[]) ArrayUtils.remove(execArgs, 0);
        streamsProcessor = StreamsComponentFactory.getProcessorInstance(Class.forName(classFullName));
        if( execArgs.length == 1 ) {
            streamsProcessor.prepare(null);
        } else if( execArgs.length > 1 ) {
            streamsProcessor.prepare(prepareArgs);
        }
    }

    public String call(String input) throws IOException {

        Preconditions.checkNotNull(streamsProcessor);

        try {

            Preconditions.checkNotNull(input);

            StreamsDatum entry = new StreamsDatum(input);

            Preconditions.checkNotNull(entry);

            List<StreamsDatum> resultSet = streamsProcessor.process(entry);

            Object resultDoc = null;
            resultDoc = resultSet.get(0).getDocument();

            Preconditions.checkNotNull(resultDoc);

            if( resultDoc instanceof String )
                return (String) resultDoc;
            else
                return mapper.writeValueAsString(resultDoc);

        } catch (Exception e) {
            System.err.println("Error with " + input.toString());
            return null;
        }

    }

    public void finish() {
        streamsProcessor.cleanUp();
    }
}
