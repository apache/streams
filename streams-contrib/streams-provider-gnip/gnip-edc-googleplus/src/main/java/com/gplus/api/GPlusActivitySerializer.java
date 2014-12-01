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

package com.gplus.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.pojo.extensions.ExtensionUtil;
import org.apache.streams.gnip.powertrack.GnipActivityFixer;
import org.apache.streams.pojo.json.Activity;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: mdelaet
 * Date: 8/30/13
 * Time: 10:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class GPlusActivitySerializer implements ActivitySerializer<String> {
    private final static Logger LOGGER = LoggerFactory.getLogger(GPlusActivitySerializer.class);

    @Override
    public String serializationFormat() {
        return "application/gplus+xml";
    }

    @Override
    public String serialize(Activity deserialized) {
        ObjectMapper jsonMapper = new ObjectMapper();
        String jsonString = new String();
        try{
            jsonString = jsonMapper.writeValueAsString(deserialized);
        }catch(Exception e){
            LOGGER.error("Exception serializing Activity Object: " + e);
        }
        return jsonString;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Activity deserialize(String serialized) {
        ObjectMapper jsonMapper = new ObjectMapper();
        JSONObject jsonObject = new JSONObject();
        JSONObject fixedObject = new JSONObject();

        try{
            jsonObject = new JSONObject(serialized);
            fixedObject = GnipActivityFixer.fix(jsonObject);
        }catch(Exception e){
            LOGGER.error("Exception deserializing string: " + e);
        }

        Activity activity = new Activity();
        try {
            activity = jsonMapper.readValue(fixedObject.toString(), Activity.class);
            Map<String, Object> extension = ExtensionUtil.ensureExtensions(activity);
            extension.put("likes", fixedObject.getJSONObject("object").getJSONObject("plusoners").get("totalItems"));
            extension.put("rebroadcast", fixedObject.getJSONObject("object").getJSONObject("resharers").get("totalItems"));
        } catch( Exception e ) {
            LOGGER.error(jsonObject.toString());
            LOGGER.error(fixedObject.toString());
            e.printStackTrace();
        }
        return activity;
    }

    @Override
    public List<Activity> deserializeAll(List<String> serializedList) {
        //TODO Support
        throw new NotImplementedException("Not currently supported by this deserializer");
    }
}
