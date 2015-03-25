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

package org.apache.streams.data.moreover;

import com.fasterxml.aalto.stax.InputFactoryImpl;
import com.fasterxml.aalto.stax.OutputFactoryImpl;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.moreover.api.Article;
import com.moreover.api.ArticlesResponse;
import org.apache.streams.core.StreamsDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;


public class MoreoverResult implements Iterable<StreamsDatum> {

    private static final Logger logger = LoggerFactory.getLogger(MoreoverResult.class);

    private ObjectMapper mapper;
    private XmlMapper xmlMapper;

    private String xmlString;
    private String jsonString;
    private ArticlesResponse resultObject;
    private ArticlesResponse.Articles articles;
    private List<Article> articleArray;
    private long start;
    private long end;
    private String clientId;
    private BigInteger maxSequencedId = BigInteger.ZERO;

    protected ArticlesResponse response;
    protected List<StreamsDatum> list = Lists.newArrayList();

    protected MoreoverResult(String clientId, String xmlString, long start, long end) {
        this.xmlString = xmlString;
        this.clientId = clientId;
        this.start = start;
        this.end = end;
        XmlFactory f = new XmlFactory(new InputFactoryImpl(),
                new OutputFactoryImpl());

        JacksonXmlModule module = new JacksonXmlModule();

        module.setDefaultUseWrapper(false);

        xmlMapper = new XmlMapper(f, module);

        xmlMapper
                .configure(
                        DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                        Boolean.TRUE);
        xmlMapper
                .configure(
                        DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,
                        Boolean.TRUE);
        xmlMapper
                .configure(
                        DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY,
                        Boolean.TRUE);
        xmlMapper.configure(
                DeserializationFeature.READ_ENUMS_USING_TO_STRING,
                Boolean.TRUE);
        xmlMapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                Boolean.FALSE);

        mapper = new ObjectMapper();

        mapper
                .configure(
                        DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                        Boolean.TRUE);
        mapper.configure(
                DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,
                Boolean.TRUE);
        mapper
                .configure(
                        DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY,
                        Boolean.TRUE);
        mapper.configure(
                DeserializationFeature.READ_ENUMS_USING_TO_STRING,
                Boolean.TRUE);

    }

    public String getClientId() {
        return clientId;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public BigInteger process() {

        try {
            this.resultObject = xmlMapper.readValue(xmlString, ArticlesResponse.class);
        } catch (JsonMappingException e) {
            // theory is this may not be fatal
            this.resultObject = (ArticlesResponse) e.getPath().get(0).getFrom();
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("Unable to process document:");
            logger.warn(xmlString);
        }

        if( this.resultObject.getStatus().equals("FAILURE"))
        {
            logger.warn(this.resultObject.getStatus());
            logger.warn(this.resultObject.getMessageCode());
            logger.warn(this.resultObject.getUserMessage());
            logger.warn(this.resultObject.getDeveloperMessage());
        }
        else
        {
            this.articles = resultObject.getArticles();
            this.articleArray = articles.getArticle();

            for (Article article : articleArray) {
                BigInteger sequenceid = new BigInteger(article.getSequenceId());
                list.add(new StreamsDatum(article, sequenceid));
                logger.trace("Prior max sequence Id {} current candidate {}", this.maxSequencedId, sequenceid);
                if (sequenceid.compareTo(this.maxSequencedId) > 0) {
                    this.maxSequencedId = sequenceid;
                }
            }
        }

        return this.maxSequencedId;
    }

    public String getXmlString() {
        return this.xmlString;
    }

    public BigInteger getMaxSequencedId() {
        return this.maxSequencedId;
    }

    @Override
    public Iterator<StreamsDatum> iterator() {
        return list.iterator();
    }

    protected static class JsonStringIterator implements Iterator<Serializable> {

        private Iterator<Serializable> underlying;

        protected JsonStringIterator(Iterator<Serializable> underlying) {
            this.underlying = underlying;
        }

        @Override
        public boolean hasNext() {
            return underlying.hasNext();
        }

        @Override
        public String next() {
            return underlying.next().toString();
        }

        @Override
        public void remove() {
            underlying.remove();
        }
    }
}
