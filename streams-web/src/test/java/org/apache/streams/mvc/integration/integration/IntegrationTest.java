package org.apache.streams.mvc.integration.integration;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.junit.Before;

import javax.servlet.ServletException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:streams_web_applicationContext.xml","classpath:streams_web_servletContext.xml" })
public class IntegrationTest {

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private MockHttpSession session;

    private MockMvc mockMvc;


    private final String publisherRegistrationJson = "{\n" +
            "     \"authToken\": \"token\",\n" +
            "     \"@class\":\"org.apache.streams.osgi.components.activityconsumer.impl.PushActivityConsumer\",    \n" +
            "     \"src\": \"www.providerexample.com\"\n" +
            " }";

    private final String subscriberRegistrationJson = "{\n" +
            "     \"authToken\": \"token\",\n" +
            "     \"@class\":\"org.apache.streams.osgi.components.activitysubscriber.impl.ActivityStreamsSubscriptionImpl\",\n" +
            "     \"filters\": [\n" +
            "         \"tags\"\n" +
            "     ]\n" +
            " }";

    private final String validActivity = "{\n" +
            "     \"id\": \"id\",\n" +
            "     \"verb\": \"verb\",\n" +
            "     \"tags\": \"tags\",\n" +
            "     \"provider\": {\n" +
            "          \"url\": \"www.providerexample.com\"\n" +
            "     },\n" +
            "     \"actor\": {\n" +
            "          \"id\": \"actorid\",\n" +
            "          \"objectType\": \"actorobject\",\n" +
            "          \"displayName\": \"actorname\",\n" +
            "          \"url\": \"www.actorexampleurl.com\"\n" +
            "     },\n" +
            "     \"target\": {\n" +
            "           \"id\": \"targetid\",\n" +
            "           \"displayName\": \"targetname\",\n" +
            "           \"url\": \"www.targeturl.com\"\n" +
            "     },\n" +
            "     \"object\": {\n" +
            "           \"id\": \"objectid\",\n" +
            "           \"displayName\": \"objectname\",\n" +
            "           \"objectType\": \"object\",\n" +
            "           \"url\": \"www.objecturl.org\"\n" +
            "       }\n" +
            " }";

    private final String urlBeginning = "http://localhost:8080/streams-web/app";


    @Before
    public void setup() throws ServletException {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Ignore
    @Test
    public void test_PublisherRegister() throws Exception {
        byte[] publisherBody = publisherRegistrationJson.getBytes();
        byte[] activityBody = validActivity.getBytes();

        ResultActions resultActions1 = this.mockMvc.perform(post("/publisherRegister").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .header("host","localhost:8080")
                .content(publisherBody)
                .accept(MediaType.TEXT_HTML));

        String pubUrl = resultActions1.andReturn().getResponse().getContentAsString();
        pubUrl = pubUrl.substring(urlBeginning.length());

        ResultActions resultActions2 = this.mockMvc.perform(post(pubUrl).session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(activityBody)
                .accept(MediaType.TEXT_HTML));

        resultActions1.andExpect(status().isOk());
        resultActions2.andExpect(status().isOk());
    }

    @Ignore
    @Test
    public void test_SubscriberRegister() throws Exception {
        byte[] subscriberBody = subscriberRegistrationJson.getBytes();

        ResultActions resultActions1 = this.mockMvc.perform(post("/subscriberRegister").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .header("host","localhost:8080")
                .content(subscriberBody)
                .accept(MediaType.TEXT_HTML));

        String subUrl = resultActions1.andReturn().getResponse().getContentAsString();
        subUrl = subUrl.substring(urlBeginning.length());

        ResultActions resultActions2 = this.mockMvc.perform(get(subUrl).session(session)
                .accept(MediaType.APPLICATION_JSON));

        resultActions1.andExpect(status().isOk());
        resultActions2.andExpect(status().isOk());
    }
}
