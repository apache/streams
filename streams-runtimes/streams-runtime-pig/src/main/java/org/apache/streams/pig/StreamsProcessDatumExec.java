package org.apache.streams.pig;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import datafu.pig.util.AliasableEvalFunc;
import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.pig.EvalFunc;
import org.apache.pig.builtin.MonitoredUDF;
import org.apache.pig.data.BagFactory;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import org.apache.pig.impl.util.UDFContext;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.data.util.RFC3339Utils;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by sblackmon on 3/25/14.
 */
@MonitoredUDF(timeUnit = TimeUnit.SECONDS, duration = 30, intDefault = 10)
public class StreamsProcessDatumExec extends AliasableEvalFunc<DataBag> {

    TupleFactory mTupleFactory = TupleFactory.getInstance();
    BagFactory mBagFactory = BagFactory.getInstance();

    StreamsProcessor streamsProcessor;

    public StreamsProcessDatumExec(String... execArgs) throws ClassNotFoundException{
        Preconditions.checkNotNull(execArgs);
        Preconditions.checkArgument(execArgs.length > 0);
        String classFullName = execArgs[0];
        Preconditions.checkNotNull(classFullName);
        String[] constructorArgs = new String[execArgs.length-1];
        ArrayUtils.remove(execArgs, 0);
        ArrayUtils.addAll(constructorArgs, execArgs);
        if( constructorArgs.length == 0 )
            streamsProcessor = StreamsComponentFactory.getProcessorInstance(Class.forName(classFullName));
        else
            streamsProcessor = StreamsComponentFactory.getProcessorInstance(Class.forName(classFullName), constructorArgs);
        streamsProcessor.prepare(null);
    }

    @Override
    public DataBag exec(Tuple input) throws IOException {

        if (input == null || input.size() == 0)
            return null;

        DataBag output = BagFactory.getInstance().newDefaultBag();

        Configuration conf = UDFContext.getUDFContext().getJobConf();

        String id = getString(input, "id");
        String source = getString(input, "source");
        Long timestamp;
        try {
            timestamp = getLong(input, "timestamp");
        } catch( Exception e ) {
            timestamp = RFC3339Utils.parseUTC(getString(input, "timestamp")).getMillis();
        }
        String object = getString(input, "object");

        StreamsDatum entry = new StreamsDatum(object, id, new DateTime(timestamp));

        List<StreamsDatum> resultSet = streamsProcessor.process(entry);
        List<Tuple> resultTupleList = Lists.newArrayList();

        for( StreamsDatum resultDatum : resultSet ) {
            Tuple tuple = mTupleFactory.newTuple();
            tuple.append(id);
            tuple.append(source);
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

    @Override
    public Schema getOutputSchema(Schema schema) {
        return null;
    }
}
