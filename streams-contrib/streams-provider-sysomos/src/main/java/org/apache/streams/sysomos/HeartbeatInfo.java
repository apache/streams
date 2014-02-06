package org.apache.streams.sysomos;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rebanks
 * Date: 5/1/13
 * Time: 4:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class HeartbeatInfo {

    private Document doc;
    private List<SysomosTagDefinition> tags;

    protected HeartbeatInfo(String xmlString) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
        this.doc = docBuilder.parse(new InputSource(new ByteArrayInputStream(xmlString.getBytes("utf-8"))));
        this.tags = new ArrayList<SysomosTagDefinition>();
        createTagDefinitions();
    }


    private void createTagDefinitions() {
        this.tags = new ArrayList<SysomosTagDefinition>();
        NodeList tagList = this.doc.getElementsByTagName("tag");

        for(int i=0; i < tagList.getLength(); ++i) {
            Node tag =  tagList.item(i);
            SysomosTagDefinition tagDefinition = createSysomosTagDefinitionFromNode(tag);
            if(this.hasTagName(tagDefinition.getTagName())) {
                SysomosTagDefinition otherTag = this.getTagWithTagName(tagDefinition.getTagName());
                if(!otherTag.getDisplayName().equals(tagDefinition.getDisplayName())) {
                    throw new RuntimeException("A single tag ("+otherTag.getTagName()+") has multiple display names ("+otherTag.getDisplayName()+" , "+tagDefinition.getDisplayName()+")");
                }
                else {
                    List<String> otherQueries = otherTag.getQueries();
                    for(String query : tagDefinition.getQueries()) {
                        if(!otherQueries.contains(query)) {
                            otherTag.addQuery(query);
                        }
                    }
                }
            }
            else {
                this.tags.add(tagDefinition);
            }

        }
    }

    private SysomosTagDefinition createSysomosTagDefinitionFromNode(Node tag) {
        Element tagElement = (Element) tag;
        SysomosTagDefinition tagDefinition = new SysomosTagDefinition(tagElement.getElementsByTagName("name").item(0).getTextContent(),
                tagElement.getElementsByTagName("displayName").item(0).getTextContent());
        NodeList taggingRule = tagElement.getElementsByTagName("taggingRule");
        for(int i=0; i < taggingRule.getLength(); ++i) {
            Element rule = (Element) taggingRule.item(i);
            NodeList queries = rule.getElementsByTagName("query");
            for(int j=0; j < queries.getLength(); ++j) {
                Element query = (Element) queries.item(j);
                tagDefinition.addQuery(query.getTextContent());
            }
        }
        return tagDefinition;
    }

    public boolean hasTagName(String tagName) {
        for(SysomosTagDefinition tag : this.tags) {
            if(tag.hasTagName(tagName)) {
                return true;
            }
        }
        return false;
    }

    public SysomosTagDefinition getTagWithTagName(String tagName) {
        for(SysomosTagDefinition tag : this.tags) {
            if(tag.hasTagName(tagName)) {
                return tag;
            }
        }
        return null;
    }

    public boolean hasTagWithDisplayName(String displayName) {
        for(SysomosTagDefinition tag : this.tags) {
            if(tag.hasDisplayName(displayName)) {
                return true;
            }
        }
        return false;
    }

    public SysomosTagDefinition getTagWithDisplayName(String displayName) {
        for(SysomosTagDefinition tag : this.tags) {
            if(tag.hasDisplayName(displayName)) {
                return tag;
            }
        }
        return null;
    }

    public List<SysomosTagDefinition> getTagDefinitions() {
        List<SysomosTagDefinition> result = new ArrayList<SysomosTagDefinition>();
        result.addAll(this.tags);
        return result;
    }

}
