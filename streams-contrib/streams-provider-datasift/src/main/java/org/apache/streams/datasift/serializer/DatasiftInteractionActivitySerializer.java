package org.apache.streams.datasift.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.datasift.Datasift;
import org.apache.streams.datasift.interaction.*;
import org.apache.streams.pojo.json.*;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.streams.data.util.ActivityUtil.ensureExtensions;

/**
* Created with IntelliJ IDEA.
* User: sblackmon
*/
public class DatasiftInteractionActivitySerializer implements ActivitySerializer<String>, Serializable {

    ObjectMapper mapper = new StreamsDatasiftMapper();

    @Override
    public String serializationFormat() {
        return "application/json+datasift.com.v1.1";
    }

    @Override
    public String serialize(Activity deserialized) {
        throw new UnsupportedOperationException("Cannot currently serialize to Datasift JSON");
    }

    @Override
    public Activity deserialize(String serialized) {

        mapper = StreamsDatasiftMapper.getInstance();

        Datasift datasift = null;

        try {
            datasift = mapper.readValue(serialized, Datasift.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Activity activity = new Activity();
        try {

            activity = convert(datasift);

            return activity;

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Unable to deserialize", e);
        }

    }

    @Override
    public List<Activity> deserializeAll(List<String> serializedList) {
        List<Activity> activities = Lists.newArrayList();
        for( String datasift : serializedList ) {
            activities.add(deserialize(datasift));
        }
        return activities;
    }

    public static Generator buildGenerator(Interaction interaction) {
        return null;
    }

    public static Icon getIcon(Interaction interaction) {
        return null;
    }

    public static Provider buildProvider(Interaction interaction) {
        Provider provider = new Provider();
        provider.setId(interaction.getType());
        return provider;
    }

    public static String getUrls(Interaction interaction) {
        return null;
    }

    public static void addDatasiftExtension(Activity activity, Datasift datasift) {
        Map<String, Object> extensions = org.apache.streams.data.util.ActivityUtil.ensureExtensions(activity);
        extensions.put("datasift", datasift);
    }

    public static String formatId(String... idparts) {
        return Joiner.on(":").join(Lists.asList("id:datasift", idparts));
    }

    public Activity convert(Datasift event) {

        Activity activity = new Activity();
        activity.setActor(buildActor(event.getInteraction()));
        activity.setVerb("post");
        activity.setObject(buildActivityObject(event.getInteraction()));
        activity.setId(formatId(activity.getVerb(), event.getInteraction().getId()));
        activity.setTarget(buildTarget(event.getInteraction()));
        activity.setPublished(event.getInteraction().getCreatedAt());
        activity.setGenerator(buildGenerator(event.getInteraction()));
        activity.setIcon(getIcon(event.getInteraction()));
        activity.setProvider(buildProvider(event.getInteraction()));
        activity.setTitle(event.getInteraction().getTitle());
        activity.setContent(event.getInteraction().getContent());
        activity.setUrl(event.getInteraction().getLink());
        activity.setLinks(getLinks(event.getInteraction()));
        addDatasiftExtension(activity, event);
        if( event.getInteraction().getGeo() != null) {
            addLocationExtension(activity, event.getInteraction());
        }
        return activity;
    }

    public static Actor buildActor(Interaction interaction) {
        Actor actor = new Actor();
        org.apache.streams.datasift.interaction.Author author = interaction.getAuthor();
        if( author != null ) {
            if( author.getId() != null )
                actor.setId(formatId(
                    Optional.fromNullable(
                            author.getId().toString())
                            .orNull()
            ));
            actor.setDisplayName(
                    Optional.fromNullable(interaction.getAuthor().getUsername()).orNull()
            );
            if( author.getAvatar() != null ) {
                Image image = new Image();
                image.setUrl(interaction.getAuthor().getAvatar());
                actor.setImage(image);
            }
            if (interaction.getAuthor().getLink()!=null){
                actor.setUrl(interaction.getAuthor().getLink());
            }
        }

        return actor;
    }

    public static ActivityObject buildActivityObject(Interaction interaction) {
        ActivityObject actObj = new ActivityObject();
        actObj.setObjectType(interaction.getContenttype());
        return actObj;
    }

    public static List<Object> getLinks(Interaction interaction) {
        List<Object> links = Lists.newArrayList();
        return links;
    }

    public static ActivityObject buildTarget(Interaction interaction) {
        return null;
    }

    public static void addLocationExtension(Activity activity, Interaction interaction) {
        Map<String, Object> extensions = ensureExtensions(activity);
        Map<String, Object> location = new HashMap<String, Object>();
        Map<String, Double> coordinates = new HashMap<String, Double>();
        coordinates.put("latitude", interaction.getGeo().getLatitude());
        coordinates.put("longitude", interaction.getGeo().getLongitude());
        location.put("coordinates", coordinates);
        extensions.put("location", location);
    }

}
