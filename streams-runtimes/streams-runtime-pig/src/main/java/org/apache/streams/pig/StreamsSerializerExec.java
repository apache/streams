package org.apache.streams.pig;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apache.streams.pojo.json.Activity;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by sblackmon on 3/25/14.
 */
@MonitoredUDF(timeUnit = TimeUnit.SECONDS, duration = 10, intDefault = 10)
public class StreamsSerializerExec extends EvalFunc<String> {

    ActivitySerializer activitySerializer;
    ObjectMapper mapper = new ObjectMapper();

    public StreamsSerializerExec(String... execArgs) throws ClassNotFoundException{
        Preconditions.checkNotNull(execArgs);
        System.out.println("A");
        Preconditions.checkArgument(execArgs.length > 0);
        System.out.println("B");
        String classFullName = execArgs[0];
        Preconditions.checkNotNull(classFullName);
        System.out.println("C");
        String[] constructorArgs = new String[execArgs.length-1];
        ArrayUtils.remove(execArgs, 0);
        ArrayUtils.addAll(constructorArgs, execArgs);
        System.out.println("D");
        activitySerializer = StreamsComponentFactory.getSerializerInstance(Class.forName(classFullName));
        System.out.println("E");
    }

    @Override
    public String exec(Tuple input) throws IOException {

        Preconditions.checkNotNull(activitySerializer);
        System.out.println("1");
        Preconditions.checkNotNull(input);
        System.out.println("2");
        Preconditions.checkArgument(input.size() == 1);
        System.out.println("3");
        Configuration conf = UDFContext.getUDFContext().getJobConf();

        String document = (String) input.get(0);

        Preconditions.checkNotNull(document);
        System.out.println("4");
        Activity activity = null;
        try {
            activity = activitySerializer.deserialize(document);
        } catch( Exception e ) {
            e.printStackTrace();
        }
        System.out.println("5");
        Preconditions.checkNotNull(activity);
        System.out.println("6");

        return mapper.writeValueAsString(activity);

    }

}
