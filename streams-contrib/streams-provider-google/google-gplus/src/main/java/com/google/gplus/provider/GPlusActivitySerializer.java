package com.google.gplus.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.pojo.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
* Created with IntelliJ IDEA.
* User: mdelaet
* Date: 9/30/13
* Time: 9:24 AM
* To change this template use File | Settings | File Templates.
*/
public class GPlusActivitySerializer implements ActivitySerializer<com.google.api.services.plus.model.Activity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GPlusActivitySerializer.class);

    GPlusProvider provider;

    ObjectMapper mapper = new ObjectMapper();

    public GPlusActivitySerializer(GPlusProvider provider) {

        this.provider = provider;

        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, Boolean.FALSE);

    }

    public GPlusActivitySerializer() {
    }

    @Override
    public String serializationFormat() {
        return "gplus.v1";
    }

    @Override
    public com.google.api.services.plus.model.Activity serialize(Activity deserialized) {
        throw new NotImplementedException("Not currently implemented");
    }

    @Override
    public Activity deserialize(com.google.api.services.plus.model.Activity gplusActivity) {

        // There is totally a better way to do this
        //   1) Deep copy all jackson fields that overlap
        //   2) Check all objects are present
        //   3) Check essential fields have values
        //   4) Any that don't, set them based on other fields that are present

        Activity activity = new Activity();
        activity.setId(formatId(gplusActivity.getId()));
        activity.setPublished(new Date(gplusActivity.getPublished().getValue()));
        Provider provider = new Provider();
        provider.setId("http://plus.google.com");
        provider.setDisplayName("GPlus");
        activity.setProvider(provider);
        Actor actor = new Actor();
        actor.setId(gplusActivity.getActor().getId());
        actor.setDisplayName(gplusActivity.getActor().getDisplayName());
        actor.setUrl(gplusActivity.getActor().getUrl());
        activity.setActor(actor);
        activity.setVerb(gplusActivity.getVerb());
        ActivityObject object = new ActivityObject();
        object.setId(gplusActivity.getObject().getId());
        object.setUrl(gplusActivity.getObject().getUrl());
        object.setContent(gplusActivity.getObject().getContent());
        activity.setTitle(gplusActivity.getTitle());
        activity.setContent(gplusActivity.getObject().getContent());
        activity.setObject(object);

        return activity;
    }

    @Override
    public List<Activity> deserializeAll(List<com.google.api.services.plus.model.Activity> serializedList) {
        throw new NotImplementedException("Not currently implemented");
    }

    public static Generator buildGenerator(ObjectNode event) {
        return null;
    }

    public static Icon getIcon(ObjectNode event) {
        return null;
    }

    public static Provider buildProvider(ObjectNode event) {
        Provider provider = new Provider();
        provider.setId("id:providers:gmail");
        return provider;
    }

    public static List<Object> getLinks(ObjectNode event) {
        return null;
    }

    public static String getUrls(ObjectNode event) {
        return null;
    }

    public static String formatId(String... idparts) {
        return Joiner.on(":").join(Lists.asList("id:gmail", idparts));
    }

}
