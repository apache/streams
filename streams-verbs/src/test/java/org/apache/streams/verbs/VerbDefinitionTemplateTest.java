package org.apache.streams.verbs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Provider;
import org.junit.Test;

import java.lang.annotation.Target;

/**
 * Tests for {$link: org.apache.streams.verbs.VerbDefinitionTemplateUtil}
 */
public class VerbDefinitionTemplateTest {

    ObjectMapper mapper = new ObjectMapper();

    /**
     * Test application of template with no field
     */
    @Test
    public void testNoField() throws Exception {
        Activity activity = new Activity().withVerb("nofields");
        VerbDefinition definition = mapper.readValue(VerbDefinitionResolverTest.class.getResourceAsStream("/nofields.json"), VerbDefinition.class);
        assert VerbDefinitionTemplateUtil.asString(activity, definition.getObjects().get(0)).contains("something");
    }

    /**
     * Test application of template with top-level fields
     */
    @Test
    public void testTopField() throws Exception {
        Actor actor = new Actor();
        actor.setObjectType("page");
        actor.setDisplayName("Paige");
        Provider provider = new Provider();
        provider.setObjectType("application");
        provider.setDisplayName("Ahp");
        ActivityObject object = new ActivityObject();
        object.setObjectType("task");
        object.setDisplayName("Tsk");
        ActivityObject target = new ActivityObject();
        target.setObjectType("person");
        target.setDisplayName("Homie");
        Activity activity = new Activity().withVerb("post");
        activity.setActor(actor);
        activity.setProvider(provider);
        activity.setObject(object);
        activity.setTarget(target);
        VerbDefinition definition = mapper.readValue(VerbDefinitionTest.class.getResourceAsStream("/post.json"), VerbDefinition.class);
        String message = VerbDefinitionTemplateUtil.asString(activity, definition.getObjects().get(0));
        assert message.contains("Paige");
        assert message.contains("Ahp");
        assert message.contains("Tsk");
        assert message.contains("Homie");
    }

    /**
     * Test application of template with second-level fields
     */
    @Test
    public void testSecondFields() {

    }

}
