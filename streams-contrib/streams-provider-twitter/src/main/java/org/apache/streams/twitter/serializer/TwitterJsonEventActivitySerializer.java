package org.apache.streams.twitter.serializer;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Generator;
import org.apache.streams.pojo.json.Icon;
import org.apache.streams.pojo.json.Provider;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
* Created with IntelliJ IDEA.
* User: mdelaet
* Date: 9/30/13
* Time: 9:24 AM
* To change this template use File | Settings | File Templates.
*/
public abstract class TwitterJsonEventActivitySerializer implements ActivitySerializer<String>, Serializable {

    public static final DateTimeFormatter TWITTER_FORMAT = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss Z yyyy");
    public static final DateTimeFormatter ACTIVITY_FORMAT = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss Z yyyy");

    public static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String serializationFormat() {
        return "application/json+vnd.twitter.com.v1";
    }

    @Override
    public String serialize(Activity deserialized) {
        throw new UnsupportedOperationException("Cannot currently serialize to Twitter JSON");
    }

    @Override
    public Activity deserialize(String serialized) throws ActivitySerializerException {
        serialized = serialized.replaceAll("\\[[ ]*\\]", "null");

        System.out.println(serialized);

        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
        mapper.setAnnotationIntrospector(introspector);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, Boolean.FALSE);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);
        mapper.configure(DeserializationFeature.WRAP_EXCEPTIONS, Boolean.TRUE);

        try {
            ObjectNode event = (ObjectNode) mapper.readTree(serialized);

            System.out.println(event.toString());

            Activity activity = convert(event);

            System.out.println(activity.toString());

            return activity;

        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to deserialize", e);
        }

    }

    public abstract Activity convert(ObjectNode event) throws ActivitySerializerException;

    @Override
    public List<Activity> deserializeAll(List<String> serializedList) {
        throw new NotImplementedException("Not currently implemented");
    }

    public static Date parse(String str) throws ParseException {
        DateTime dateTime;
        String dstr;
        dateTime = TWITTER_FORMAT.parseDateTime(str);
        dstr = ACTIVITY_FORMAT.print(dateTime);
        return ACTIVITY_FORMAT.parseDateTime(dstr).toDate();
    }

    public static Generator buildGenerator(ObjectNode event) {
        return null;
    }

    public static Icon getIcon(ObjectNode event) {
        return null;
    }

    public static Provider buildProvider(ObjectNode event) {
        Provider provider = new Provider();
        provider.setId("id:providers:twitter");
        return provider;
    }

    public static String getUrls(ObjectNode event) {
        return null;
    }

    public static void addTwitterExtension(Activity activity, ObjectNode event) {
        Map<String, Object> extensions = org.apache.streams.data.util.ActivityUtil.ensureExtensions(activity);
        extensions.put("twitter", event);
    }

    public static String formatId(String... idparts) {
        return Joiner.on(":").join(Lists.asList("id:twitter", idparts));
    }

}
