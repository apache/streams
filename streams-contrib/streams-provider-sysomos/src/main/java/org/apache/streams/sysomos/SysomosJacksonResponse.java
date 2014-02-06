package org.apache.streams.sysomos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysomos.xml.BeatApi;
import com.sysomos.xml.BeatApi.BeatResponse.Beat;
import com.sysomos.xml.ObjectFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rebanks
 * Date: 11/19/13
 * Time: 3:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class SysomosJacksonResponse implements SysomosResponse {

    private int numResponses = 0;
    private List<Beat> beats;
    private boolean hasError = false;
    private String xmlString;
    private int index;
    private ObjectMapper mapper;
    private String errorMessage;

    public SysomosJacksonResponse(String xmlString) {
        try {
            this.xmlString = xmlString;
            JAXBContext context = JAXBContext.newInstance(new Class[] {ObjectFactory.class});
//            JAXBContext context = JAXBContext.newInstance(BeatApi.class.getName(), ObjectFactory.class.getClassLoader());
            Unmarshaller unmarshaller = context.createUnmarshaller();
            BeatApi beatApi = (BeatApi) unmarshaller.unmarshal(new StringReader(xmlString));
            this.beats = beatApi.getBeatResponse().getBeat();
            this.numResponses = beatApi.getBeatResponse().getCount();
            this.index = 0;
            this.hasError = xmlString.contains("<errors>") && xmlString.contains("<error>") && xmlString.contains("<errorMessage>");
            if(this.hasError) {
                this.errorMessage = xmlString.substring(xmlString.lastIndexOf("<errorMessage>"), xmlString.lastIndexOf("</errorMessage>"));
            }
            this.mapper = new ObjectMapper();
//            System.out.println(mapper.writeValueAsString(beatApi));
        } catch (Exception e) {
            this.hasError = true;
            e.printStackTrace();
        }
    }

    @Override
    public int getNumResults() {
        return this.numResponses;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasError() {
        return this.hasError;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getXMLResponseString() {
        return this.xmlString;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasNext() {
        return this.index < this.numResponses;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String next() {
        try {
            return this.mapper.writeValueAsString(this.beats.get(index++));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
