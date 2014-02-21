package org.apache.streams.sysomos;

import com.sysomos.xml.BeatApi;
import com.sysomos.xml.ObjectFactory;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RequestBuilder {

    private static Pattern _pattern = Pattern.compile("code: ([0-9]+)");
    //private Logger log = LoggerFactory.getLogger(RequestBuilder.class);

    /**
     * Returns the full url need to execute a request.
     * http://api.sysomos.com/dev/v1/heartbeat/content?apiKey=YOUR
     * -APIKEY&hid=YOUR-HEARTBEAT-ID&offset=0&size=10&
     * addedAfter=2010-10-15T13:00:00Z&addedBefore=2010-10-18T13:00:00Z
     *
     * @return
     * @throws SysomosException
     * @throws java.net.MalformedURLException
     */
    protected abstract URL getFullRequestUrl() throws SysomosException, MalformedURLException;


    public String getFullRequestUrlString() throws SysomosException, MalformedURLException {
        return getFullRequestUrl().toString();
    }



    /**
     * @throws java.net.MalformedURLException
     * @throws java.io.IOException
     */
    public BeatApi.BeatResponse execute() throws SysomosException {
        URL url;
        String urlString = null;
        try {
            url = this.getFullRequestUrl();
            urlString = url.toString();
        } catch (MalformedURLException e1) {
            throw new SysomosException(e1);
        }
        try {
            HttpURLConnection cn = (HttpURLConnection) url.openConnection();
            cn.setRequestMethod("GET");
            cn.addRequestProperty("Content-Type", "text/xml;charset=UTF-8");
            cn.setDoInput(true);
            cn.setDoOutput(false);
            StringWriter writer = new StringWriter();
            IOUtils.copy(new InputStreamReader(cn.getInputStream()), writer);
            writer.flush();
            //System.out.println(writer.toString());
            String xmlResponse = writer.toString();
            if(xmlResponse == null || xmlResponse.equals("")) {
                throw new SysomosException("XML Response from Sysomos was empty : "+xmlResponse+"\n"+cn.getResponseMessage(), cn.getResponseCode());
            }
            BeatApi.BeatResponse response;
            JAXBContext context = JAXBContext.newInstance(new Class[] {ObjectFactory.class});
//            JAXBContext context = JAXBContext.newInstance(BeatApi.class.getName(), ObjectFactory.class.getClassLoader());
            Unmarshaller unmarshaller = context.createUnmarshaller();
            BeatApi beatApi = (BeatApi) unmarshaller.unmarshal(new StringReader(xmlResponse));
            return beatApi.getBeatResponse();
        } catch (IOException e) {
//            e.printStackTrace();
            //log.error("Error executing request : {}", e, urlString);
            String message = e.getMessage();
            Matcher match = _pattern.matcher(message);
            if(match.find()) {
                int errorCode = Integer.parseInt(match.group(1));
                throw new SysomosException(message, e, errorCode);
            }
            else {
                throw new SysomosException(e.getMessage(), e);
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

}
