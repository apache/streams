package org.apache.streams.gnip.powertrack;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.pojo.json.Activity;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rebanks
 * Date: 9/5/13
 * Time: 3:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class ActivityXMLActivitySerializer implements ActivitySerializer<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityXMLActivitySerializer.class);

    private ObjectMapper mapper;
    private XmlMapper xmlMapper;

    public ActivityXMLActivitySerializer() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);
        xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
        xmlMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        xmlMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);
    }


    @Override
    public String serializationFormat() {
        return "gnip_activity_xml";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String serialize(Activity deserialized) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Activity deserialize(String serializedXML) {
        Activity activity = null;
        try {
            activity = xmlMapper.readValue(new StringReader(setContentIfEmpty(serializedXML)), Activity.class);
            activity = mapper.readValue(new StringReader(fixActivityXML(activity, serializedXML)), Activity.class);
        } catch (Exception e) {
            LOGGER.error("Exception correcting Gnip Activity Xml to Activity format.", e);
            LOGGER.error("Xml that caused error : {}", serializedXML);
        }
        return activity;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Activity> deserializeAll(String serializedList) {
        //TODO Support
        throw new NotImplementedException("Not currently supported by this deserializer");
    }

    private String fixActivityXML(Activity activity, String xml) throws Exception{
        JSONObject jsonObject = new JSONObject(mapper.writeValueAsString(activity));
        JSONObject newObject = (JSONObject) fixActivityXML(jsonObject);
        StringReader str = new StringReader(newObject.toString());
        newObject = formatFixedJSON(newObject);
        newObject = fixDateFormats(newObject, xml);
        newObject = createTopLevelConentField(newObject);
        return newObject.toString();

    }

    private Object fixActivityXML(Object obj) throws Exception {
        if(obj instanceof JSONObject) {
            JSONObject json = new JSONObject();
            JSONObject old = (JSONObject) obj;
            Iterator keys = old.keys();
            while(keys.hasNext()) {
                String key = (String) keys.next();
                if(StringUtils.isBlank(key)) {
                    return fixActivityXML(old.get(key));
                }
                else if(!key.equals("type")){
                    Object o = fixActivityXML(old.get(key));
                    if(o != null)
                        json.put(key, o);
                }

            }
            if(json.keys().hasNext())
                return json;
            else
                return null;
        }
        else {
            return obj;
        }

    }

    private JSONObject formatFixedJSON(JSONObject json) throws Exception{
        JSONObject topLink = json.optJSONObject("link");
        if(topLink != null) {
            json.put("link", topLink.getString("href"));
        }
        JSONObject actor = json.optJSONObject("actor");
        if(actor != null) {
            JSONObject link = actor.optJSONObject("link");
            if(link != null) {
                actor.put("link", link.get("href"));
            }
        }
        JSONObject object = json.optJSONObject("object");
        if(object != null) {
            JSONObject link = object.optJSONObject("link");
            if(link != null) {
                object.put("link", link.get("href"));
            }
        }
        String generator = json.optString("generator");
        if(generator != null) {
            JSONObject gen = new JSONObject();
            gen.put("displayName", generator);
            json.put("generator", gen);
        }
        return json;
    }

    private JSONObject fixDateFormats(JSONObject json, String xml) throws Exception{
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        Document doc = docBuilder.parse(is);
        //why?
        doc.getDocumentElement().normalize();
        if(json.optLong("published", -1L) != -1L) {
            json.put("published", getValueFromXML("published", doc));
        }
        if(json.optLong("updated", -1L) != -1L) {
            json.put("updated", getValueFromXML("updated", doc));
        }
        if(json.optLong("created", -1L) != -1L) {
            json.put("created", getValueFromXML("created", doc));
        }
        return json;
    }

    private JSONObject createTopLevelConentField(JSONObject json) throws JSONException {
        if(!json.isNull("content")) {
            return json;
        }
        JSONObject object = json.optJSONObject("object");
        if(object != null) {
            String content = object.optString("content");
            if(content == null) {
                content = object.optString("summary");
            }
            if(content != null) {
                json.put("content", content);
            }
        }
        return json;
    }

    private String getValueFromXML(String tag, Document doc) throws Exception{
        Element base = (Element) doc.getElementsByTagName("entry").item(0);
        return base.getElementsByTagName(tag).item(0).getTextContent();
    }

    private String setContentIfEmpty(String xml) throws Exception {
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        Document doc = docBuilder.parse(is);
        doc.getDocumentElement().normalize();
        Element base = (Element) doc.getElementsByTagName("entry").item(0);
        NodeList nodeList = base.getChildNodes();
//        for(int i=0; i < nodeList.getLength(); ++i) {
//            System.out.println(nodeList.item(i).getNodeName());
//        }
        Element obj = (Element)base.getElementsByTagName("activity:object").item(0);
        Element content = (Element)obj.getElementsByTagName("content").item(0);
//        System.out.println("Number of child nodes : "+content.getChildNodes().getLength());
//        System.out.println("Text content before : "+content.getTextContent());
        if(content.getTextContent() == null || content.getTextContent().equals("")) {
            content.setTextContent(" ");
        }
//        System.out.println("Number of child nodes after : "+content.getChildNodes().getLength());
//        System.out.println("Text content after : "+content.getTextContent());
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
//        System.out.println(output);
//        System.out.println(output);
//        System.out.println(content);
        return output;
    }
}
