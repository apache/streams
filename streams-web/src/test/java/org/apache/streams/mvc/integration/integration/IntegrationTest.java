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
import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:streams_web_applicationContext.xml","classpath:streams_web_servletContext.xml" })
public class IntegrationTest {

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private MockHttpSession session;
    @Autowired
    private MockHttpServletRequest request;

    private MockMvc mockMvc;


    private final String publisherRegistrationJson = "{\n" +
            "     \"authToken\": \"token\",\n" +
            "     \"@class\":\"org.apache.streams.osgi.components.activityconsumer.impl.PushActivityConsumer\",    \n" +
            "     \"src\": \"http.example.com:8888\"\n" +
            " }";

    private final String subscriberRegistrationJson = "{\n" +
            "     \"authToken\": \"token\",\n" +
            "     \"@class\":\"org.apache.streams.osgi.components.activitysubscriber.impl.ActivityStreamsSubscriptionImpl\",\n" +
            "     \"filters\": [\n" +
            "         \"tags\"\n" +
            "     ]\n" +
            " }";


    @Before
    public void setup() throws ServletException {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @After
    public void after(){
    }

    @Ignore
    @Test
    public void test_PublisherRegister() throws Exception {
        byte[] contentBody = publisherRegistrationJson.getBytes();

        this.mockMvc.perform(post("/publisherRegister").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk());
    }

    @Ignore
    @Test
    public void test_SubscriberRegister() throws Exception {
        byte[] contentBody = subscriberRegistrationJson.getBytes();

        this.mockMvc.perform(post("/subscriberRegister").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk());
    }
}
