package org.apache.streams.datasift.serializer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.datasift.interaction.Interaction;
import org.apache.streams.datasift.provider.DatasiftEventClassifier;
import org.apache.streams.datasift.twitter.Twitter;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Provider;

import java.util.List;
import java.util.Map;

/**
 * Created by sblackmon on 3/26/14.
 */
public class DatasiftJsonActivitySerializer implements ActivitySerializer<String>
{

    public DatasiftJsonActivitySerializer() {

    }

    DatasiftInteractionActivitySerializer datasiftInteractionActivitySerializer = new DatasiftInteractionActivitySerializer();
    DatasiftInteractionActivitySerializer datasiftTwitterActivitySerializer = new DatasiftTwitterActivitySerializer();

    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public String serialize(Activity deserialized) throws ActivitySerializerException {
        throw new NotImplementedException();
    }

    @Override
    public Activity deserialize(String serialized) throws ActivitySerializerException {

        Class documentSubType = DatasiftEventClassifier.detectClass(serialized);

        Activity activity;

        // AT THE MOMENT USING ONLY INTERACTION SERIALIZER

//        if( documentSubType == Twitter.class )
//            activity = datasiftTwitterActivitySerializer.deserialize(serialized);
//        else if( documentSubType == Interaction.class )
            activity = datasiftInteractionActivitySerializer.deserialize(serialized);
//        else throw new ActivitySerializerException("unrecognized type");

        return activity;
    }

    @Override
    public List<Activity> deserializeAll(List<String> serializedList) {
        throw new NotImplementedException();
    }

    public static void addDatasiftExtension(Activity activity, ObjectNode event) {
        Map<String, Object> extensions = org.apache.streams.data.util.ActivityUtil.ensureExtensions(activity);
        extensions.put("datasift", event);
    }

    public static String formatId(String... idparts) {
        return Joiner.on(":").join(Lists.asList("id:datasift", idparts));
    }
}
