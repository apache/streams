/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.apache.streams.sysomos.org.apache.streams.sysomos.proessor;

import com.google.common.collect.Lists;
import com.sysomos.xml.BeatApi;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.sysomos.conversion.SysomosBeatActivityConverter;

import java.util.List;

/**
 * Stream processor that converts Sysomos type to Activity
 */
public class SysomosTypeConverter implements StreamsProcessor {

    private SysomosBeatActivityConverter converter;

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        if(entry.getDocument() instanceof BeatApi.BeatResponse.Beat) {
            entry.setDocument(converter.convert((BeatApi.BeatResponse.Beat)entry.getDocument()));
            return Lists.newArrayList(entry);
        } else {
            return Lists.newArrayList();
        }
    }

    @Override
    public void prepare(Object configurationObject) {
        converter = new SysomosBeatActivityConverter();
    }

    @Override
    public void cleanUp() {
        //NOP
    }
}
