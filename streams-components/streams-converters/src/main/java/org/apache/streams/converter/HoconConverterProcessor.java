/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package org.apache.streams.converter;

import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.core.util.DatumUtils;
import org.apache.streams.pojo.json.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * HoconConverterProcessor is a utility processor for converting any datum document
 * with translation rules expressed as HOCON in the classpath or at a URL.
 *
 * To use this capability without a dedicated stream processor, just use HoconConverterUtil.
 */
public class HoconConverterProcessor implements StreamsProcessor {

    public static final String STREAMS_ID = "HoconConverterProcessor";

    private final static Logger LOGGER = LoggerFactory.getLogger(HoconConverterProcessor.class);

    protected Class outClass;
    protected String hocon;
    protected String inPath;
    protected String outPath;

    public HoconConverterProcessor(Class outClass, String hocon, String inPath, String outPath) {
        this.outClass = outClass;
        this.hocon = hocon;
        this.inPath = inPath;
        this.outPath = outPath;
    }

    @Override
    public String getId() {
        return STREAMS_ID;
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {

        List<StreamsDatum> result = Lists.newLinkedList();
        Object document = entry.getDocument();

        Object outDoc = HoconConverterUtil.getInstance().convert(document, outClass, hocon, inPath, outPath);

        StreamsDatum datum = DatumUtils.cloneDatum(entry);
        datum.setDocument(outDoc);
        result.add(datum);

        return result;
    }

    @Override
    public void prepare(Object configurationObject) {

    }

    @Override
    public void cleanUp() {

    }
};
