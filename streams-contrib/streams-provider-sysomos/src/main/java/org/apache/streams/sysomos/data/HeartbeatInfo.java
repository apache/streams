/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.streams.sysomos.data;

import org.apache.streams.sysomos.data.SysomosTagDefinition;
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
 * Represents Heatbeat metadata from the Sysomos API
 */
public class HeartbeatInfo {

    private Document doc;
    private List<SysomosTagDefinition> tags;

    public HeartbeatInfo(String xmlString) throws Exception {
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
