/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.streams.hdfs.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.hdfs.HdfsConfiguration;
import org.apache.streams.hdfs.HdfsReaderConfiguration;
import org.apache.streams.hdfs.HdfsWriterConfiguration;
import org.apache.streams.hdfs.WebHdfsPersistReader;
import org.apache.streams.hdfs.WebHdfsPersistWriter;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Test reading and writing documents
 */
public class TestHdfsPersist {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestHdfsPersist.class);

    ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    @Before
    public void setup() {
        File file = new File("/target/TestHdfsPersist/");
        if( file.exists())
            file.delete();
    }

    @Test
    public void TestHdfsPersist() throws Exception {

        List<List<String>> fieldArrays = Lists.newArrayList();
        fieldArrays.add(new ArrayList<String>());
        fieldArrays.add(Lists.newArrayList("ID"));
        fieldArrays.add(Lists.newArrayList("ID", "DOC"));
        fieldArrays.add(Lists.newArrayList("ID", "TS", "DOC"));
        fieldArrays.add(Lists.newArrayList("ID", "TS", "META", "DOC"));

        for( List<String> fields : fieldArrays )
            TestHdfsPersistCase(fields);

    }

    public void TestHdfsPersistCase(List<String> fields) throws Exception {

        HdfsConfiguration hdfsConfiguration = new HdfsConfiguration().withScheme(HdfsConfiguration.Scheme.FILE).withHost("localhost").withUser("cloudera").withPath("target/TestHdfsPersist");
        hdfsConfiguration.setFields(fields);
        HdfsWriterConfiguration hdfsWriterConfiguration = MAPPER.convertValue(hdfsConfiguration, HdfsWriterConfiguration.class);
        if( fields.size() % 2 == 1 )
            hdfsWriterConfiguration.setCompression(HdfsWriterConfiguration.Compression.GZIP);
        hdfsWriterConfiguration.setWriterFilePrefix("activities");
        hdfsWriterConfiguration.setWriterPath(Integer.toString(fields.size()));
        WebHdfsPersistWriter writer = new WebHdfsPersistWriter(hdfsWriterConfiguration);

        writer.prepare(null);

        InputStream testActivityFolderStream = TestHdfsPersist.class.getClassLoader()
                .getResourceAsStream("activities");
        List<String> files = IOUtils.readLines(testActivityFolderStream, Charsets.UTF_8);

        int count = 0;

        for( String file : files) {
            LOGGER.info("File: " + file );
            InputStream testActivityFileStream = TestHdfsPersist.class.getClassLoader()
                    .getResourceAsStream("activities/" + file);
            Activity activity = MAPPER.readValue(testActivityFileStream, Activity.class);
            activity.getAdditionalProperties().remove("$license");
            StreamsDatum datum = new StreamsDatum(activity, activity.getVerb());
            writer.write( datum );
            LOGGER.info("Wrote: " + activity.getVerb() );
            count++;
        }

        writer.cleanUp();

        HdfsReaderConfiguration hdfsReaderConfiguration = MAPPER.convertValue(hdfsConfiguration, HdfsReaderConfiguration.class);

        WebHdfsPersistReader reader = new WebHdfsPersistReader(hdfsReaderConfiguration);
        hdfsReaderConfiguration.setReaderPath(new Integer(fields.size()).toString());

        reader.prepare(null);

        StreamsResultSet resultSet = reader.readAll();

        assert( resultSet.size() == count);

    }
}
