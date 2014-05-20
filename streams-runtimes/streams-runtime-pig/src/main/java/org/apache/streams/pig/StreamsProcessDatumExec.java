package org.apache.streams.pig;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import datafu.pig.util.AliasableEvalFunc;
import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.pig.EvalFunc;
import org.apache.pig.builtin.MonitoredUDF;
import org.apache.pig.data.*;
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

    @Override
    public DataBag exec(Tuple input) throws IOException {

        if (input == null || input.size() == 0)
            return null;

        DataBag output = BagFactory.getInstance().newDefaultBag();

        Configuration conf = UDFContext.getUDFContext().getJobConf();

//      I would prefer it work this way, but at the moment it doesn't

//        String id = getString(input, "id");
//        String source = getString(input, "source");
//        Long timestamp;
//        try {
//            timestamp = getLong(input, "timestamp");
//        } catch( Exception e ) {
//            timestamp = RFC3339Utils.parseUTC(getString(input, "timestamp")).getMillis();
//        }
//        String object = getString(input, "object");

        String id = (String) input.get(0);
        String source = (String) input.get(1);
        Long timestamp;
        try {
            timestamp = (Long) input.get(2);
        } catch( Exception e ) {
            timestamp = RFC3339Utils.parseUTC((String)input.get(2)).getMillis();
        }
        String object = (String) input.get(3);

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
        // Check that we were passed two fields
        String error = "Expected: id\tsource\ttimestamp\tobject";
        if (schema.size() != 4) {
            throw new RuntimeException(error);
        }

        try {
            // Get the types for both columns and check them.  If they are
            // wrong, figure out what types were passed and give a good error
            // message.
            if (schema.getField(0).type != DataType.CHARARRAY &&
                    schema.getField(0).type != DataType.LONG) {
                error += "Problem with id: must be CHARARRAY or LONG";
                error += "\t(";
                error += DataType.findTypeName(schema.getField(0).type);
                error += ")\n";
                throw new RuntimeException(error);
            }
            if (schema.getField(1).type != DataType.CHARARRAY) {
                error += "Problem with source: must be CHARARRAY";
                error += "\t(";
                error += DataType.findTypeName(schema.getField(1).type);
                error += ")\n";
                throw new RuntimeException(error);
            }
            if (schema.getField(2).type != DataType.CHARARRAY &&
                    schema.getField(2).type != DataType.LONG) {
                error += "Problem with timestamp: must be CHARARRAY or LONG";
                error += "\t(";
                error += DataType.findTypeName(schema.getField(2).type);
                error += ")\n";
                throw new RuntimeException(error);
            }
            if (schema.getField(3).type != DataType.CHARARRAY) {
                error += "Problem with object: must be CHARARRAY";
                error += "\t(";
                error += DataType.findTypeName(schema.getField(3).type);
                error += ")\n";
                throw new RuntimeException(error);
            }
        } catch (Exception e) {
            throw new RuntimeException(error);
        }

        // Always hand back the same schema we are passed
        return schema;
    }
}
