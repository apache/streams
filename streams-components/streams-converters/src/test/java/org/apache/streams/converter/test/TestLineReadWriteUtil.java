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

package org.apache.streams.converter.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import junit.framework.Assert;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.streams.converter.LineReadWriteUtil;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Tests for
 * @see {@link LineReadWriteUtil}
 */
public class TestLineReadWriteUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(TestLineReadWriteUtil.class);

    ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    private static Random rand = new Random();

    @Test
    public void TestLineReadWrite () throws Exception {

        List<List<String>> fieldArrays = Lists.newArrayList();
        fieldArrays.add(new ArrayList<String>());
        fieldArrays.add(Lists.newArrayList("ID"));
        fieldArrays.add(Lists.newArrayList("DOC"));
        fieldArrays.add(Lists.newArrayList("ID", "DOC"));
        fieldArrays.add(Lists.newArrayList("ID", "TS", "DOC"));
        fieldArrays.add(Lists.newArrayList("ID", "TS", "META", "DOC"));
        fieldArrays.add(Lists.newArrayList("ID", "SEQ", "TS", "META", "DOC"));

        TestLineReadWriteCase(fieldArrays.get(0), null, null);
        TestLineReadWriteCase(fieldArrays.get(1), "\t", null );
        TestLineReadWriteCase(fieldArrays.get(2), "\t", "\n" );
        TestLineReadWriteCase(fieldArrays.get(3), null, "\n" );
        TestLineReadWriteCase(fieldArrays.get(4), "|", "\n" );
        TestLineReadWriteCase(fieldArrays.get(5), "|", "\\0" );

    }

    public void TestLineReadWriteCase(List<String> fields, String fieldDelimiter, String lineDelimiter) throws Exception {

        LineReadWriteUtil lineReadWriteUtil;
        if( lineDelimiter != null && fieldDelimiter != null ) lineReadWriteUtil = LineReadWriteUtil.getInstance(fields, fieldDelimiter, lineDelimiter);
        else if( lineDelimiter != null ) lineReadWriteUtil = LineReadWriteUtil.getInstance(fields, null, lineDelimiter);
        else if( fieldDelimiter != null ) lineReadWriteUtil = LineReadWriteUtil.getInstance(fields, fieldDelimiter);
        lineReadWriteUtil = LineReadWriteUtil.getInstance(fields);

        StreamsDatum testDatum = randomDatum();
        String writeResult = lineReadWriteUtil.convertResultToString(testDatum);
        assert !Strings.isNullOrEmpty(writeResult);
        StreamsDatum readResult = lineReadWriteUtil.processLine(writeResult);
        assert readResult != null;
        assert !Strings.isNullOrEmpty(readResult.getId()) || !Strings.isNullOrEmpty((String)readResult.getDocument());
        if( fields.size() == 5 )
            Assert.assertEquals(testDatum.toString(), readResult.toString());

    }

    public static StreamsDatum randomDatum() {

        StreamsDatum datum = new StreamsDatum(UUID.randomUUID().toString());
        datum.setId(UUID.randomUUID().toString());
        datum.setTimestamp(DateTime.now());
        BigInteger result = new BigInteger(64, rand);
        datum.setSequenceid(result);
        Map<String,Object> metadata = Maps.newHashMap();
        metadata.put("a", UUID.randomUUID().toString());
        datum.setMetadata(metadata);
        return datum;
    }
}
