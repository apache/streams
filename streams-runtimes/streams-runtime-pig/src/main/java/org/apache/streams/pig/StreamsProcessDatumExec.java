package org.apache.streams.pig;

/*
 * #%L
 * streams-runtime-pig
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
public class StreamsProcessDatumExec extends EvalFunc<DataBag> {

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
