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

package org.apache.streams.gnip.powertrack;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.pojo.json.Activity;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: mdelaet
 * Date: 8/23/13
 * Time: 9:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class GnipActivityFixer {

    private final static Logger LOGGER = LoggerFactory.getLogger(GnipActivityFixer.class);

    private ObjectMapper mapper;
    private XmlMapper xmlMapper;

    public GnipActivityFixer(){
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);
        xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
        xmlMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        xmlMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);
    };

    public static void findNullContents(JSONObject json, ArrayList<String> drilldownKeys, HashMap<ArrayList<String>, JSONObject> nullContents) throws Exception {

        Iterator itr = json.keys();
        while (itr.hasNext()){
            String element = (String) itr.next();

            if (StringUtils.isBlank(element)){
                nullContents.put(drilldownKeys, json);

            }
            else{
                try {
                    drilldownKeys.add(element);
                    if(json.get(element) instanceof JSONObject)
                        findNullContents((JSONObject) json.get(element), drilldownKeys, nullContents);
                }catch(Exception e){
                    LOGGER.info("Failed to convert in findNullKeys, " + e);
                    LOGGER.error("Element : {}", element);
                    LOGGER.error(json.toString());
                    break;
                }
            }
            drilldownKeys = new ArrayList<String>();
        }
    }

    public static void editJson(JSONObject json, ArrayList<String> keyPath, Object nullFragment) throws JSONException {
        Integer numKeys = keyPath.size();
        JSONObject newJson = new JSONObject();
        if (numKeys > 1){
            for (int i = numKeys-1; i > 0; i-=1){
                String key = keyPath.get(i);
                if (i == numKeys -1){
                    newJson = newJson.put(key, nullFragment);
                }
                else {
                    newJson = newJson.put(key, newJson);
                }
            }
            json.put(keyPath.get(0), newJson);
        }
        else{
            json.put(keyPath.get(0), nullFragment);
        }
    }

    public static Activity fix(Activity activity) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        String des = mapper.writeValueAsString(activity);
        JSONObject json = new JSONObject(des);

        HashMap<ArrayList<String>, JSONObject> nullContents = new HashMap<ArrayList<String>, JSONObject>();
        ArrayList<String> drilldownKeys = new ArrayList<String>();

        findNullContents(json, drilldownKeys, nullContents);

        for ( Map.Entry<ArrayList<String>,JSONObject> entry : nullContents.entrySet() ) {
            JSONObject frag = entry.getValue();
            editJson(json, entry.getKey(), frag.get(""));
        }

        StringReader str = new StringReader(json.toString());
        Activity testAct = null;
        try {
            testAct = mapper.readValue(str, Activity.class);
        } catch (Exception e) {
            LOGGER.error("Exception creating activity.", e);
            LOGGER.error("JSON : {}"+json.toString());
        }

        return testAct;
    };

    public static JSONObject fix(JSONObject json) throws Exception {

        HashMap<ArrayList<String>, JSONObject> nullContents = new HashMap<ArrayList<String>, JSONObject>();
        ArrayList<String> drilldownKeys = new ArrayList<String>();

        findNullContents(json, drilldownKeys, nullContents);

        for ( Map.Entry<ArrayList<String>,JSONObject> entry : nullContents.entrySet() ) {
            JSONObject frag = entry.getValue();
            editJson(json, entry.getKey(), frag.get(""));
        }

        return json;
    };

}