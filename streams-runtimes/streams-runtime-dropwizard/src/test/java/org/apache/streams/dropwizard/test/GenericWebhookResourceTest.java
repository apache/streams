package org.apache.streams.dropwizard.test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.apache.streams.dropwizard.GenericWebhookData;
import org.apache.streams.dropwizard.GenericWebhookResource;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Person;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.xml.ws.Response;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Tests {@link: org.apache.streams.dropwizard.GenericWebhookResource}
 */
public class GenericWebhookResourceTest {

    private static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    private static final GenericWebhookResource genericWebhookResource = new GenericWebhookResource();

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(genericWebhookResource)
            .build();

    @Test
    public void testPostJson() {
        Assert.assertEquals(400, genericWebhookResource.json(null, "{").getStatus());
        Assert.assertEquals(400, genericWebhookResource.json(null, "}").getStatus());
        Assert.assertEquals(400, genericWebhookResource.json(null, "srg").getStatus());
        Assert.assertEquals(400, genericWebhookResource.json(null, "123").getStatus());
        Assert.assertEquals(200, genericWebhookResource.json(null, "{}").getStatus());
        Assert.assertEquals(200, genericWebhookResource.json(null, "{\"valid\":\"true\"}").getStatus());
    };

    @Test
    public void testPostJsonNewLine() {
        Assert.assertEquals(200, genericWebhookResource.json_new_line(null, "{}").getStatus());
        Assert.assertEquals(400, genericWebhookResource.json_new_line(null, "notvalid").getStatus());
        Assert.assertEquals(200, genericWebhookResource.json_new_line(null, "{\"valid\":\"true\"}").getStatus());
        Assert.assertEquals(200, genericWebhookResource.json_new_line(null, "{\"valid\":\"true\"}\n{\"valid\":\"true\"}\r{\"valid\":\"true\"}").getStatus());
    };

    @Test
    public void testPostJsonMeta() throws JsonProcessingException {
        Assert.assertEquals(200, genericWebhookResource.json_meta(null, "{}").getStatus());
        Assert.assertEquals(400, genericWebhookResource.json_meta(null, "notvalid").getStatus());
        GenericWebhookData testPostJsonMeta = new GenericWebhookData()
                .withHash("test")
                .withDeliveredAt(DateTime.now())
                .withCount(1)
                .withHashType("type")
                .withId("test");
        List<ObjectNode> testPostJsonData = Lists.newArrayList();
        testPostJsonData.add(mapper.createObjectNode().put("valid", "true"));
        testPostJsonMeta.setData(testPostJsonData);
        String testPostJsonEntity = mapper.writeValueAsString(testPostJsonMeta);
        Assert.assertEquals(200, genericWebhookResource.json_meta(null, testPostJsonEntity).getStatus());

    };


}
