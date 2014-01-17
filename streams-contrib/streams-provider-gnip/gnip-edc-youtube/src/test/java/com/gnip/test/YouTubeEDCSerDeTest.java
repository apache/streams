package test.java.com.gnip.test;

//import org.codehaus.jackson.map.ObjectMapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

//import com.fasterxml.jackson.xml.XmlMapper;
//import com.gnip.xmlpojo.generated.FacebookEDC;

/**
 * Created with IntelliJ IDEA.
 * User: rebanks
 * Date: 8/21/13
 * Time: 11:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class YouTubeEDCSerDeTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(YouTubeEDCSerDeTest.class);

    private ObjectMapper mapper = new ObjectMapper();
//    XmlMapper mapper = new XmlMapper();

    @Test
    public void Tests()   throws Exception
    {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);

        InputStream is = YouTubeEDCSerDeTest.class.getResourceAsStream("/YoutubeEDC.xml");
        if(is == null) System.out.println("null");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
        xmlMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        xmlMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);

        ObjectMapper jsonMapper = new ObjectMapper();

        try {
            while (br.ready()) {
                String line = br.readLine();
                //LOGGER.debug(line);

                Object activityObject = xmlMapper.readValue(line, Object.class);

                String jsonObject = jsonMapper.writeValueAsString(activityObject);

                //LOGGER.debug(jsonObject);
            }
        } catch( Exception e ) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
