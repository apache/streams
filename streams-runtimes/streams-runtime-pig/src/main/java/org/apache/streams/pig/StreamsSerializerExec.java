package org.apache.streams.pig;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by sblackmon on 3/25/14.
 */
@MonitoredUDF(timeUnit = TimeUnit.SECONDS, duration = 10, intDefault = 10)
public class StreamsSerializerExec extends SimpleEvalFunc<String> {

    ActivitySerializer activitySerializer;
    ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    public StreamsSerializerExec(String... execArgs) throws ClassNotFoundException{
        Preconditions.checkNotNull(execArgs);
        Preconditions.checkArgument(execArgs.length > 0);
        String classFullName = execArgs[0];
        Preconditions.checkNotNull(classFullName);
        String[] constructorArgs = new String[execArgs.length-1];
        ArrayUtils.remove(execArgs, 0);
        ArrayUtils.addAll(constructorArgs, execArgs);
        activitySerializer = StreamsComponentFactory.getSerializerInstance(Class.forName(classFullName));
    }

    public String call(String input) throws IOException {

        Preconditions.checkNotNull(activitySerializer);
        Preconditions.checkNotNull(input);

        Activity activity = null;
        try {
            activity = activitySerializer.deserialize(input);

            Preconditions.checkNotNull(activity);

            return mapper.writeValueAsString(activity);

        } catch( Exception e ) {
            e.printStackTrace();

            return null;
        }

    }

}
