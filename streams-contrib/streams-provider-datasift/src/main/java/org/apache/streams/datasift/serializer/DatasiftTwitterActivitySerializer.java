package org.apache.streams.datasift.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.datasift.Datasift;
import org.apache.streams.datasift.interaction.Interaction;
import org.apache.streams.pojo.json.*;
import org.apache.streams.twitter.serializer.StreamsTwitterMapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.streams.data.util.ActivityUtil.ensureExtensions;

/**
* Created with IntelliJ IDEA.
* User: sblackmon
*/
public class DatasiftTwitterActivitySerializer extends DatasiftInteractionActivitySerializer {

    public Activity convert(Datasift event) {
        Activity activity = super.convert(event);
        activity.getExtensions().setAdditionalProperty("datasift", event.getTwitter());
        return activity;
    }

}
