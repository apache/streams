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

package org.apache.streams.lucene;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.w2olabs.core.graph.CommunityRepository;
import com.w2olabs.core.graph.CommunityRepositoryResolver;
import com.w2olabs.core.graph.entities.Entity;
import com.w2olabs.streams.pojo.W2OActivity;
import com.w2olabs.util.guice.GuiceInjector;
import com.w2olabs.util.tagging.SimpleVerbatim;
import com.w2olabs.util.tagging.engines.international.LanguageTag;
import com.w2olabs.util.tagging.engines.international.TaggingEngineLanguage;
import org.apache.avro.data.Json;
import org.apache.commons.lang3.StringUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * References:
 * Some helpful references to help
 * Purpose              URL
 * -------------        ----------------------------------------------------------------
 * [Status Codes]       http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
 * [Test Cases]         http://greenbytes.de/tech/tc/httpredirects/
 * [t.co behavior]      https://dev.twitter.com/docs/tco-redirection-behavior
 */

public class LuceneSimpleTaggingProcessor implements StreamsProcessor
{
    private final static String STREAMS_ID = "LuceneSimpleTaggingProcessor";

    private final static Logger LOGGER = LoggerFactory.getLogger(LuceneSimpleTaggingProcessor.class);

    private ObjectMapper mapper;

    private Queue<StreamsDatum> inQueue;
    private Queue<StreamsDatum> outQueue;

    private String community;
    private JsonPath[] textPaths;
    private String[] jsonPathsToText;
    private List<LanguageTag> tags;
    private String metaDataKey;

    private static TaggingEngineLanguage<LanguageTag, SimpleVerbatim> taggingEngine;

    /**
     * Constructor for a tagging processor that will operate on the document of StreamsDatum at the paths noted by
     * the json paths paramter
     * @param community
     * @param jsonPathsToText
     */
    public LuceneSimpleTaggingProcessor(String community, String[] jsonPathsToText) {
        this(community, jsonPathsToText, null, null);
    }

    /**
     * Constructor for a tagging processor that will operate on some meta data field of the StreamsDatum indicated by the
     * meta data key.  The data in the meta data field is still expected to be json or able to be converted to json or
     * a list of such data.
     * @param community
     * @param jsonPathsToText
     * @param metaDataKey
     */
    public LuceneSimpleTaggingProcessor(String community, String[] jsonPathsToText, String metaDataKey) {
        this(community, jsonPathsToText, metaDataKey, null);
    }


    /**
     * For testing purposes. LanguageTag are not serializable and need to be set through the community resolver in production.
     * @param community
     * @param jsonPathsToText
     * @param tags
     */
    public LuceneSimpleTaggingProcessor(String community, String[] jsonPathsToText, String metaDataKey, List<LanguageTag> tags) {
        this.community = community;
        this.jsonPathsToText = jsonPathsToText;
        this.tags = tags;
        this.metaDataKey = metaDataKey;
        verifyJsonPathstoText(this.jsonPathsToText);
    }

//    public LuceneSimpleTaggingProcessor(Queue<StreamsDatum> inQueue) {
//        this.inQueue = inQueue;
//        this.outQueue = new LinkedBlockingQueue<StreamsDatum>();
//    }

//    public void stop() {
//
//    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {


        LOGGER.debug("{} processing {}", STREAMS_ID, entry.getDocument().getClass());

        List<StreamsDatum> result = Lists.newArrayList();

        List<String> jsons = Lists.newLinkedList();
        ObjectNode node;
        // first check for valid json
        if(this.metaDataKey == null)
            jsons.add(getJson(entry.getDocument()));
        else
            getMetaDataJsons(entry, jsons);

        for(String json : jsons) {
            try {
                node = (ObjectNode) mapper.readTree(json);
            } catch (IOException e) {
                e.printStackTrace();
                return result;
            }

            List<SimpleVerbatim> verbatimList = convertEntryToWorkUnit(json);
            Map<SimpleVerbatim, List<LanguageTag>> objectTags = taggingEngine.findMatches(verbatimList);
            Set<String> tagSet = Sets.newHashSet();
            for( List<LanguageTag> fieldtags : objectTags.values() ) {
                for( LanguageTag tag : fieldtags ) {
                    tagSet.add(tag.getTag());
                }
            }

            ArrayNode tagArray = JsonNodeFactory.instance.arrayNode();
            Set<String> tags = Sets.newHashSet();
            for( String tag : tagSet ) {
                if(tags.add(tag)){
                    tagArray.add(tag);
                }
            }



            // need utility methods for get / create specific node
            ObjectNode extensions = (ObjectNode) node.get("extensions");
            if(extensions == null) {
                extensions = JsonNodeFactory.instance.objectNode();
                node.put("extensions", extensions);
            }
            ObjectNode w2o = (ObjectNode) extensions.get("w2o");
            if(w2o == null) {
                w2o = JsonNodeFactory.instance.objectNode();
                extensions.put("w2o", w2o);
            }
            w2o.put("tags", tagArray);
            w2o.put("contentTags", tagArray);
            if(entry.getDocument() instanceof W2OActivity) {
                entry.setDocument(mapper.convertValue(node, W2OActivity.class));
            }
            else if(entry.getDocument() instanceof Activity) {
                entry.setDocument(mapper.convertValue(node, Activity.class));
            }
            else if(entry.getDocument() instanceof String) {
                try {
                    entry.setDocument(mapper.writeValueAsString(node));
                } catch (JsonProcessingException jpe) {
                    LOGGER.error("Exception while converting ObjectNode to string. Outputing as ObjectNode. {}", jpe);
                    entry.setDocument(node);
                }
            }
            else {
                entry.setDocument(node);
            }
            result.add(entry);
        }
        return result;
    }

    private void getMetaDataJsons(StreamsDatum datum, List<String> jsons) {
       if(datum.getMetadata() == null)
           return;
        Object obj = datum.getMetadata().get(this.metaDataKey);
        if(obj == null) {
            LOGGER.debug("Object at key={} was NULL.", this.metaDataKey);
            return;
        }
        if(obj instanceof List) {
            List list = (List) obj;
            String json;
            for(Object o : list) {
                json = getJson(o);
                if(json != null) {
                    jsons.add(json);
                }
            }
        }
        else {
            String json = getJson(obj);
            if(json != null) {
                jsons.add(json);
            }
        }
    }

    private String getJson(Object object) {
        String json = null;
        if( object instanceof String ) {
            json = (String) object;
        } else if(object instanceof Activity){
            try {
                json = mapper.writeValueAsString(object);
            } catch (JsonProcessingException jpe) {
                json = null;
                LOGGER.error("Failed to convert Activity to String : {}", jpe);
            }
        } else {
            ObjectNode node = (ObjectNode) object;
            json = node.asText();
        }
        return json;
    }

    @Override
    public void prepare(Object o) {
        mapper = StreamsJacksonMapper.getInstance();
        if(this.tags == null) {
            resolver = injector.getInstance(CommunityRepositoryResolver.class);
            repo = resolver.get(community);
            List<Entity> entities = repo.getTaggableEntities();
            taggingEngine = new TaggingEngineLanguage<LanguageTag, SimpleVerbatim>(createLanguageTagsFromRexsterTags(entities));
        }
        else {
            taggingEngine = new TaggingEngineLanguage<LanguageTag, SimpleVerbatim>(this.tags);
        }
        compileTextJsonPaths();
    }

    @Override
    public void cleanUp() {

    }

    private static List<LanguageTag> createLanguageTagsFromRexsterTags(List<Entity> graphTags) {
        List<LanguageTag> result = new ArrayList<LanguageTag>(graphTags.size());
        LOGGER.info("Attempting to convert {} Graph tags into Language tags.", graphTags.size());
        String tagLang;
        for(Entity tag : graphTags) {
//            net.sf.json.JSONObject json = null;
            try {
//                json = (net.sf.json.JSONObject) tag;
                tagLang = tag.getAdditionalProperties().get("language") == null ? "en" : tag.getAdditionalProperties().get("language").toString();
            } catch (Exception e) {
                tagLang = "en";
            }
            LOGGER.debug("Tag : {}\n{}\n{}", new String[] {tag.getIdentifier(),tag.getQuery(),tagLang});
            if(TaggingEngineLanguage.SUPPORTED.contains(tagLang)) {
                try {
                    result.add(new LanguageTag(tag.getIdentifier(),tag.getQuery(), tagLang));
                } catch (Exception e) {
                    e.printStackTrace();
                    //result.add(new LanguageTag(tag.getIdentifier(), tag.getQuery(), tagLang));
                }
            }
            else {
                LOGGER.warn("Attempted to load a tag for Language, {}, but that language is not currently supported. SimpleTag is being ignored!");
            }
        }
        LOGGER.info("Loaded {} Language Tags. {} tags were not supported.", result.size(), graphTags.size() - result.size());
        return result;
    }

    // Does basic verification that paths are not null and in JsonPath syntax.
    private String[] verifyJsonPathstoText(String[] jsonPathsToText) {
        RuntimeException e = null;
        for(String path : jsonPathsToText) {
            if(StringUtils.isEmpty(path) || !path.matches("[$][.][a-zA-z0-9.]+")) {
                LOGGER.error("Invalid JsonPath path : {}", path);
                e = new RuntimeException("Invalid JsonPath paths!");
            }
        }
        if(e != null)
            throw e;
        return jsonPathsToText;
    }

    // Compiles jsonPathToText to JsonPath
    private void compileTextJsonPaths() {
        this.textPaths = new JsonPath[this.jsonPathsToText.length];
        for(int i=0; i < this.jsonPathsToText.length; ++i) {
            this.textPaths[i] = JsonPath.compile(this.jsonPathsToText[i]);
        }
    }

    private List<SimpleVerbatim> convertEntryToWorkUnit(String json) {
        List<SimpleVerbatim> textFields = new ArrayList<SimpleVerbatim>();
        for(JsonPath path : this.textPaths) {
            try {
                Object pathObject = path.read(json);
                if( pathObject instanceof String )
                    textFields.add(new SimpleVerbatim((String) pathObject) );
                else if( pathObject instanceof List ) {
                    List<String> pathObjectList = (List<String>) pathObject;
                    for( String pathItem : pathObjectList ) {
                        textFields.add(new SimpleVerbatim(pathItem) );
                    }
                }
            } catch( InvalidPathException x ) {
                LOGGER.debug("{}: {}", x.getMessage(), path.getPath());
            } catch( ClassCastException x ) {
                LOGGER.warn(x.getMessage());
            }
        }

        if(textFields.size() == 0)
            return null;

        return textFields;
    }
}