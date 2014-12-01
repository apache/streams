package org.apache.streams.datasift.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.datasift.Datasift;
import org.apache.streams.datasift.interaction.Interaction;
import org.apache.streams.datasift.links.Links;
import org.apache.streams.datasift.util.StreamsDatasiftMapper;
import org.apache.streams.pojo.extensions.ExtensionUtil;
import org.apache.streams.pojo.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.streams.data.util.ActivityUtil.ensureExtensions;

/**
 *
 */
public class DatasiftInteractionActivitySerializer implements ActivitySerializer<Datasift>, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasiftInteractionActivitySerializer.class);

    private static DatasiftInteractionActivitySerializer instance = new DatasiftInteractionActivitySerializer();

    public static DatasiftInteractionActivitySerializer getInstance() {
        return instance;
    }

    ObjectMapper mapper = StreamsDatasiftMapper.getInstance();

    @Override
    public String serializationFormat() {
        return "application/json+datasift.com.v1.1";
    }

    @Override
    public Datasift serialize(Activity deserialized) {
        throw new UnsupportedOperationException("Cannot currently serialize to Datasift JSON");
    }

    public Activity deserialize(String datasiftJson) {
        try {
            return deserialize(this.mapper.readValue(datasiftJson, Datasift.class));
        } catch (Exception e) {
            LOGGER.error("Exception while trying convert,\n {},\n to a Datasift object.", datasiftJson);
            LOGGER.error("Exception : {}", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Activity deserialize(Datasift serialized) {

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

    public static Generator buildGenerator(Interaction interaction) {
        Generator generator = new Generator();
        generator.setDisplayName(interaction.getSource());
        generator.setId(interaction.getSource());
        return generator;
    }

    public static Icon getIcon(Interaction interaction) {
        return null;
    }

    public static Provider buildProvider(Interaction interaction) {
        Provider provider = new Provider();
        provider.setId("id:providers:"+interaction.getType());
        provider.setDisplayName(interaction.getType());
        return provider;
    }

    public static String getUrls(Interaction interaction) {
        return null;
    }

    public static void addDatasiftExtension(Activity activity, Datasift datasift) {
        Map<String, Object> extensions = ExtensionUtil.ensureExtensions(activity);
        extensions.put("datasift", datasift);
    }

    public static String formatId(String... idparts) {
        return Joiner.on(":").join(Lists.asList("id:datasift", idparts));
    }

    public Activity convert(Datasift event) {

        Preconditions.checkNotNull(event);
        Preconditions.checkNotNull(event.getInteraction());

        Activity activity = new Activity();
        activity.setActor(buildActor(event.getInteraction()));
        activity.setVerb(selectVerb(event));
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
        activity.setLinks(getLinks(event));
        addDatasiftExtension(activity, event);
        if( event.getInteraction().getGeo() != null) {
            addLocationExtension(activity, event.getInteraction());
        }
        return activity;
    }

    private String selectVerb(Datasift event) {
        return "post";
    }

    public Actor buildActor(Interaction interaction) {
        Actor actor = new Actor();
        org.apache.streams.datasift.interaction.Author author = interaction.getAuthor();
        if(author == null) {
            LOGGER.warn("Interaction does not contain author information.");
            return actor;
        }
        String userName = author.getUsername();
        String name = author.getName();
        Long id  = author.getId();
        if(userName != null) {
            actor.setDisplayName(userName);
        } else {
            actor.setDisplayName(name);
        }

        if(id != null) {
            actor.setId(id.toString());
        } else {
            if(userName != null)
                actor.setId(userName);
            else
                actor.setId(name);
        }
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
        actObj.setUrl(interaction.getLink());
        actObj.setId(formatId("post",interaction.getId()));
        actObj.setContent(interaction.getContent());

        return actObj;
    }

    public static List<String> getLinks(Datasift event) {
        List<String> result = Lists.newArrayList();
        Links links = event.getLinks();
        if(links == null)
            return null;
        for(Object link : links.getNormalizedUrl()) {
            if(link != null) {
                if(link instanceof String) {
                    result.add((String) link);
                } else {
                    LOGGER.warn("link is not of type String : {}", link.getClass().getName());
                }
            }
        }
        return result;
    }

    public static ActivityObject buildTarget(Interaction interaction) {
        return null;
    }

    public static void addLocationExtension(Activity activity, Interaction interaction) {
        Map<String, Object> extensions = ExtensionUtil.ensureExtensions(activity);
        Map<String, Object> location = new HashMap<String, Object>();
        Map<String, Double> coordinates = new HashMap<String, Double>();
        coordinates.put("latitude", interaction.getGeo().getLatitude());
        coordinates.put("longitude", interaction.getGeo().getLongitude());
        location.put("coordinates", coordinates);
        extensions.put("location", location);
    }

    public static String firstStringIfNotNull(List<Object> list) {
        if( list != null && list.size() > 0) {
            return (String) list.get(0);
        } else return null;
    }
}
