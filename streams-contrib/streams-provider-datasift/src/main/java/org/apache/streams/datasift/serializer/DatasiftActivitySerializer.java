package org.apache.streams.datasift.serializer;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.datasift.Datasift;
import org.apache.streams.datasift.interaction.Interaction;
import org.apache.streams.pojo.json.*;

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
* User: mdelaet
* Date: 9/30/13
* Time: 9:24 AM
* To change this template use File | Settings | File Templates.
*/
public class DatasiftActivitySerializer implements ActivitySerializer<Datasift>, Serializable {

    public static final String DATE_FORMAT = "EEE MMM dd HH:mm:ss Z yyyy";

    ObjectMapper mapper = new StreamsDatasiftMapper();

    @Override
    public String serializationFormat() {
        return "application/json+datasift.com.v1.1";
    }

    @Override
    public Datasift serialize(Activity deserialized) {
        throw new UnsupportedOperationException("Cannot currently serialize to Datasift JSON");
    }

    @Override
    public Activity deserialize(Datasift serialized) {

        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
        mapper.setAnnotationIntrospector(introspector);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);
        mapper.configure(DeserializationFeature.WRAP_EXCEPTIONS, Boolean.TRUE);

        try {

            Activity activity = convert(serialized);

            return activity;

        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to deserialize", e);
        }

    }

    @Override
    public List<Activity> deserializeAll(List<Datasift> datasifts) {
        List<Activity> activities = Lists.newArrayList();
        for( Datasift datasift : datasifts ) {
            activities.add(deserialize(datasift));
        }
        return activities;
    }

    public static Date parse(String str) {
        Date date;
        String dstr;
        DateFormat fmt = new SimpleDateFormat(DATE_FORMAT);
        DateFormat out = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            date = fmt.parse(str);
            dstr = out.format(date);
            return out.parse(dstr);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format", e);
        }
    }

    public static Generator buildGenerator(Interaction interaction) {
        return null;
    }

    public static Icon getIcon(Interaction interaction) {
        return null;
    }

    public static Provider buildProvider(Interaction interaction) {
        Provider provider = new Provider();
        provider.setId("id:providers:twitter");
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
        actor.setId(formatId(interaction.getAuthor().getId().toString()));
        actor.setDisplayName(interaction.getAuthor().getUsername());
        Image image = new Image();
        image.setUrl(interaction.getAuthor().getAvatar());
        actor.setImage(image);
        if (interaction.getAuthor().getLink()!=null){
            actor.setUrl(interaction.getAuthor().getLink());
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
