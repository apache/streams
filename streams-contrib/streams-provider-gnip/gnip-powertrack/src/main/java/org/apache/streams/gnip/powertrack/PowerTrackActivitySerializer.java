package org.apache.streams.gnip.powertrack;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.pojo.json.Activity;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rebanks
 * Date: 9/5/13
 * Time: 2:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class PowerTrackActivitySerializer implements ActivitySerializer<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PowerTrackActivitySerializer.class);

    private ObjectMapper mapper;

    public PowerTrackActivitySerializer() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);
    }



    @Override
    public String serializationFormat() {
        return "gnip_powertrack";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String serialize(Activity deserialized) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Activity deserialize(String serialized) {
        Activity activity = null;
        try {
            JSONObject jsonObject = new JSONObject(serialized);
            String content = jsonObject.optString("content", null);
            if(content == null) {
                content = jsonObject.optString("body", null);
                if(content == null) {
                    content = jsonObject.optString("summary", null);
                    if(content == null) {
                        JSONObject object = jsonObject.optJSONObject("object");
                        if(object != null) {
                            content = object.optString("content", null);
                            if(content == null) {
                                content = object.optString("body", null);
                                if(content == null) {
                                    content = object.optString("summary", null);
                                }
                            }
                        }
                    }
                }

            }
            if(content != null) {
                jsonObject.put("content", content);
            }
            String dateTime = jsonObject.optString("postedTime");
            if(dateTime != null) {
                jsonObject.put("published", dateTime);
            }
            JSONObject actor = jsonObject.optJSONObject("actor");
            if(actor != null) {
                String url = actor.optString("image");
                if(url != null) {
                    JSONObject media = new JSONObject();
                    media.put("url", url);
                    actor.put("image", media);
                }
            }
            serialized = jsonObject.toString();
            StringReader reader = new StringReader(serialized);
            activity = this.mapper.readValue(reader, Activity.class);
        } catch (Exception e) {
            LOGGER.error("Exception deserializing powertrack string to Activity Object.", e);
            LOGGER.error("Exception on json : {}", serialized);
        }
        return activity;
    }

    @Override
    public List<Activity> deserializeAll(String serializedList) {
        //TODO Support
        throw new NotImplementedException("Not currently supported by this deserializer");
    }
}
