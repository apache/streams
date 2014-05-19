package org.apache.streams.facebook.test;

/*
 * #%L
 * streams-provider-facebook
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import com.facebook.Post;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
* Created with IntelliJ IDEA.
* User: sblackmon
* Date: 8/20/13
* Time: 5:57 PM
* To change this template use File | Settings | File Templates.
*/
public class FacebookPostSerDeTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(FacebookPostSerDeTest.class);
    //private ActivitySerializer serializer = new TwitterJsonActivitySerializer();
    private ObjectMapper mapper = new ObjectMapper();

    @Ignore
    @Test
    public void Tests()
    {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.TRUE);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);

        InputStream is = FacebookPostSerDeTest.class.getResourceAsStream("/testpost.json");
        Joiner joiner = Joiner.on(" ").skipNulls();
        is = new BoundedInputStream(is, 10000);
        String json;

        try {
            json = joiner.join(IOUtils.readLines(is));
            LOGGER.debug(json);

            Post ser = mapper.readValue(json, Post.class);

            String de = mapper.writeValueAsString(ser);

            LOGGER.debug(de);

            Post serde = mapper.readValue(de, Post.class);

            Assert.assertEquals(ser, serde);

            LOGGER.debug(mapper.writeValueAsString(serde));

        } catch( Exception e ) {
            System.out.println(e);
            e.printStackTrace();
            Assert.fail();
        }
    }
}
