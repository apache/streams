package org.apache.streams.data;

/*
 * #%L
 * streams-provider-moreover
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


import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.streams.pojo.json.Activity;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;

import static org.apache.streams.data.util.MoreoverTestUtil.test;

public class MoreoverXmlActivitySerializerTest {
    ActivitySerializer serializer;
    private String xml;

    @Before
    public void setup() throws IOException {
        serializer = new MoreoverXmlActivitySerializer();
        xml = loadXml();
    }

    @Test
    public void loadData() throws Exception {
        List<Activity> activities = serializer.deserializeAll(Lists.newArrayList(xml));
        for (Activity activity : activities) {
            test(activity);
        }
    }

    private String loadXml() throws IOException {
        StringWriter writer = new StringWriter();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("moreover.xml");
        IOUtils.copy(resourceAsStream, writer, Charset.forName("UTF-8"));
        return writer.toString();
    }

}
