package com.reddit.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.data.util.ActivityUtil;
import org.apache.streams.gnip.powertrack.GnipActivityFixer;
import org.apache.streams.pojo.json.Activity;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: mdelaet
 * Date: 8/29/13
 * Time: 8:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class RedditActivitySerializer implements ActivitySerializer<String> {
    private final static Logger LOGGER = LoggerFactory.getLogger(RedditActivitySerializer.class);

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final String PROVIDER_NAME = "facebook";


    @Override
    public String serializationFormat() {
        return "application/reddit+xml";
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

        HashMap<String, String> raw = new HashMap<String, String>();
        raw.put("original", serialized);

        try{
            jsonObject = new JSONObject(serialized);
            fixedObject = GnipActivityFixer.fix(jsonObject);
        }catch(Exception e){
            LOGGER.error("Exception deserializing string: " + e);
        }

        Activity activity = new Activity();
        try {
            activity = jsonMapper.readValue(fixedObject.toString(), Activity.class);
            Map<String, Object> extension = ActivityUtil.ensureExtensions(activity);
            if (fixedObject.names().toString().contains("object")){
                if (fixedObject.getJSONObject("object").names().toString().contains("statistics")){
                    if (fixedObject.getJSONObject("object").getJSONObject("statistics").names().toString().contains("upVotes")){
                        extension.put("likes", fixedObject.getJSONObject("object").getJSONObject("statistics").get("upVotes"));
                    }
                }
            }

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
