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

package org.apache.streams.datasift.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.streams.datasift.Datasift;
import org.apache.streams.datasift.instagram.Instagram;
import org.apache.streams.datasift.twitter.Twitter;
import org.apache.streams.datasift.util.StreamsDatasiftMapper;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.util.files.StreamsScannerUtil;
import org.junit.Test;

import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Created by sblackmon on 12/13/13.
 */
public class DatasiftEventClassifierTest {

    private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance(Lists.newArrayList(StreamsDatasiftMapper.DATASIFT_FORMAT));

    @Test
    public void testTwitterDetection() throws Exception {

        Scanner scanner = StreamsScannerUtil.getInstance("/twitter_datasift_json.txt");

        String line = null;
        while(scanner.hasNextLine()) {
            line = scanner.nextLine();
            Datasift datasift = MAPPER.readValue(line, Datasift.class);
            assert(DatasiftEventClassifier.detectClass(datasift) == Twitter.class);
            assert(DatasiftEventClassifier.bestSerializer(datasift) instanceof DatasiftTwitterActivitySerializer);
        }
    }

    @Test
    public void testInstagramDetection() throws Exception {

        Scanner scanner = StreamsScannerUtil.getInstance("/instagram_datasift_json.txt");

        String line = null;
        while(scanner.hasNextLine()) {
            line = scanner.nextLine();
            Datasift datasift = MAPPER.readValue(line, Datasift.class);
            assert(DatasiftEventClassifier.detectClass(datasift) == Instagram.class);
            assert(DatasiftEventClassifier.bestSerializer(datasift) instanceof DatasiftInstagramActivitySerializer);
        }
    }
    

}
