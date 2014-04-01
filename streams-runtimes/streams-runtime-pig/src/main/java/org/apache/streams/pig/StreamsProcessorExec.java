package org.apache.streams.pig;

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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by sblackmon on 3/25/14.
 */
@MonitoredUDF(timeUnit = TimeUnit.SECONDS, duration = 30, intDefault = 10)
public class StreamsProcessorExec extends EvalFunc<DataBag> {

    TupleFactory mTupleFactory = TupleFactory.getInstance();
    BagFactory mBagFactory = BagFactory.getInstance();

    StreamsProcessor streamsProcessor;

    public StreamsProcessorExec(String... execArgs) throws ClassNotFoundException{
        Preconditions.checkNotNull(execArgs);
        Preconditions.checkArgument(execArgs.length > 0);
        String classFullName = execArgs[0];
        Preconditions.checkNotNull(classFullName);
        String[] constructorArgs = new String[execArgs.length-1];
        ArrayUtils.remove(execArgs, 0);
        ArrayUtils.addAll(constructorArgs, execArgs);
        streamsProcessor = StreamsComponentFactory.getProcessorInstance(Class.forName(classFullName));
        streamsProcessor.prepare(null);
    }

    @Override
    public DataBag exec(Tuple line) throws IOException {

        if (line == null || line.size() == 0)
            return null;

        Configuration conf = UDFContext.getUDFContext().getJobConf();

        String id = (String)line.get(0);
        String provider = (String)line.get(1);
        Long timestamp = (Long)line.get(2);
        String object = (String)line.get(3);

        StreamsDatum entry = new StreamsDatum(object);

        List<StreamsDatum> resultSet = streamsProcessor.process(entry);
        List<Tuple> resultTupleList = Lists.newArrayList();

        for( StreamsDatum resultDatum : resultSet ) {
            Tuple tuple = mTupleFactory.newTuple();
            tuple.append(id);
            tuple.append(provider);
            tuple.append(timestamp);
            tuple.append(resultDatum.getDocument());
            resultTupleList.add(tuple);
        }

        DataBag result = mBagFactory.newDefaultBag(resultTupleList);

        return result;

    }

    public void finish() {
        streamsProcessor.cleanUp();
    }
}
