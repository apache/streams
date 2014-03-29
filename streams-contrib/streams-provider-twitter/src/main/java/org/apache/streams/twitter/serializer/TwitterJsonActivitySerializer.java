package org.apache.streams.twitter.serializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.jackson.StreamsJacksonModule;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Provider;
import org.apache.streams.twitter.pojo.Delete;
import org.apache.streams.twitter.pojo.Retweet;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.provider.TwitterEventClassifier;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by sblackmon on 3/26/14.
 */
public class TwitterJsonActivitySerializer implements ActivitySerializer<String>
{

    public TwitterJsonActivitySerializer() {

    }

    public static final DateTimeFormatter TWITTER_FORMAT = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss Z yyyy");
    public static final DateTimeFormatter ACTIVITY_FORMAT = ISODateTimeFormat.basicDateTime();

    public static ObjectMapper mapper;
    static {
        mapper = new ObjectMapper();
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(new StreamsJacksonModule() {
            {
                addDeserializer(DateTime.class, new StdDeserializer<DateTime>(DateTime.class) {
                    @Override
                    public DateTime deserialize(JsonParser jpar, DeserializationContext context) throws IOException, JsonProcessingException {
                        return TWITTER_FORMAT.parseDateTime(jpar.getValueAsString());
                    }
                });
            }
        });
        //AnnotationIntrospector introspector = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
        //mapper.setAnnotationIntrospector(introspector);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.TRUE);
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, Boolean.TRUE);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);
        mapper.configure(DeserializationFeature.WRAP_EXCEPTIONS, Boolean.FALSE);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, Boolean.TRUE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    }

    TwitterJsonTweetActivitySerializer tweetActivitySerializer = new TwitterJsonTweetActivitySerializer();
    TwitterJsonRetweetActivitySerializer retweetActivitySerializer = new TwitterJsonRetweetActivitySerializer();
    TwitterJsonDeleteActivitySerializer deleteActivitySerializer = new TwitterJsonDeleteActivitySerializer();

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

        Class documentSubType = TwitterEventClassifier.detectClass(serialized);

        Activity activity;
        if( documentSubType == Tweet.class )
            activity = tweetActivitySerializer.deserialize(serialized);
        else if( documentSubType == Retweet.class )
            activity = retweetActivitySerializer.deserialize(serialized);
        else if( documentSubType == Delete.class )
            activity = deleteActivitySerializer.deserialize(serialized);
        else throw new ActivitySerializerException("unrecognized type");

        return activity;
    }

    @Override
    public List<Activity> deserializeAll(List<String> serializedList) {
        throw new NotImplementedException();
    }

    public static Provider getProvider() {
        Provider provider = new Provider();
        provider.setId("id:providers:twitter");
        return provider;
    }

    public static void addTwitterExtension(Activity activity, ObjectNode event) {
        Map<String, Object> extensions = org.apache.streams.data.util.ActivityUtil.ensureExtensions(activity);
        extensions.put("twitter", event);
    }

    public static String formatId(String... idparts) {
        return Joiner.on(":").join(Lists.asList("id:twitter", idparts));
    }
}
