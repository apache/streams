package com.datasift.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.datasift.interaction.Interaction;
import org.apache.streams.datasift.provider.DatasiftEventClassifier;
import org.apache.streams.datasift.serializer.DatasiftJsonActivitySerializer;
import org.apache.streams.datasift.serializer.StreamsDatasiftMapper;
import org.apache.streams.pojo.json.Activity;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
* Created with IntelliJ IDEA.
* User: sblackmon
* Date: 8/20/13
* Time: 5:57 PM
* To change this template use File | Settings | File Templates.
*/
public class DatasiftActivitySerDeTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatasiftActivitySerDeTest.class);
    private ObjectMapper mapper = StreamsDatasiftMapper.getInstance();

    private DatasiftJsonActivitySerializer datasiftJsonActivitySerializer = new DatasiftJsonActivitySerializer();

    //    @Ignore
    @Test
    public void Tests()
    {
        InputStream is = DatasiftActivitySerDeTest.class.getResourceAsStream("/part-r-00000.json");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        int linenumber = 1;

        try {
            while (br.ready()) {

                String line = br.readLine();
                LOGGER.debug(line);
                if(!StringUtils.isEmpty(line))
                {
                    LOGGER.info("raw: {}", line);

                    Class detected = DatasiftEventClassifier.detectClass(line);

                    Activity activity = datasiftJsonActivitySerializer.deserialize(line);

                    String activitystring = mapper.writeValueAsString(activity);

                    LOGGER.info("activity: {}", activitystring);

                    assertThat(activity, is(not(nullValue())));

                    assertThat(activity.getId(), is(not(nullValue())));
                    assertThat(activity.getActor(), is(not(nullValue())));
                    assertThat(activity.getActor().getId(), is(not(nullValue())));
                    assertThat(activity.getVerb(), is(not(nullValue())));
                    assertThat(activity.getProvider(), is(not(nullValue())));

                    assertEquals(activity.getVerb(), "post");

                }
                linenumber++;
            }

        } catch( Exception e ) {
            LOGGER.error("Failed on line: {}", + linenumber);
            e.printStackTrace();
            Assert.fail();
        }
    }
}
