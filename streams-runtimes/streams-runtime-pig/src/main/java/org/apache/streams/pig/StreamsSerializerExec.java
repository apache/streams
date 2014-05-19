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
import org.apache.streams.jackson.StreamsJacksonMapper;
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

    @Override
    public String exec(Tuple input) throws IOException {

        Preconditions.checkNotNull(activitySerializer);
        Preconditions.checkNotNull(input);
        Preconditions.checkArgument(input.size() == 1);
        Configuration conf = UDFContext.getUDFContext().getJobConf();

        String document = (String) input.get(0);

        Preconditions.checkNotNull(document);
        Activity activity = null;
        try {
            activity = activitySerializer.deserialize(document);
        } catch( Exception e ) {
            e.printStackTrace();
        }
        Preconditions.checkNotNull(activity);

        return mapper.writeValueAsString(activity);

    }

}
