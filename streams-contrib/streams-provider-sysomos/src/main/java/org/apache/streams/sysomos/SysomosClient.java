package org.apache.streams.sysomos;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper for the Sysomos API.
 */
public class SysomosClient {

    public static final String BASE_URL_STRING = "http://api.sysomos.com/";
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd'T'hh:mm:ssZ";
    private static final String HEARTBEAT_INFO_URL = "http://api.sysomos.com/v1/heartbeat/info?apiKey={api_key}&hid={hid}";
    private static Pattern _pattern = Pattern.compile("code: ([0-9]+)");

    private String apiKey;

    private HttpURLConnection client;

    public SysomosClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public HeartbeatInfo getHeartbeatInfo(String hid) throws Exception {
        String urlString = HEARTBEAT_INFO_URL.replace("{api_key}", this.apiKey);
        urlString = urlString.replace("{hid}", hid);
        String xmlResponse = execute(new URL(urlString));
        return new HeartbeatInfo(xmlResponse);
    }

    private String execute(URL url) throws SysomosException {
        String urlString = url.toString();

        try {
            client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("GET");
            client.addRequestProperty("Content-Type", "text/xml;charset=UTF-8");
            client.setDoInput(true);
            client.setDoOutput(false);
            StringWriter writer = new StringWriter();
            IOUtils.copy(new InputStreamReader(client.getInputStream()), writer);
            writer.flush();
            //System.out.println(writer.toString());
            return writer.toString();
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
        }
    }

}
