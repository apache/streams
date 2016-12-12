/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
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

package org.apache.streams.rss.serializer;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.rss.Enclosure;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndImage;
import com.rometools.rome.feed.synd.SyndLinkImpl;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * Since SyndEntry is not Serializable, we cannot emit them from any StreamOperation.  So the CommunityRssProvider
 * converts the SyndEntries to ObjectNodes using this class.
 */
public class SyndEntrySerializer {

  private static final Logger LOGGER = LoggerFactory.getLogger(SyndEntrySerializer.class);

  public ObjectNode deserialize(SyndEntry entry) {
    return deserializeRomeEntry(entry);
  }

  private ObjectNode deserializeRomeEntry(SyndEntry entry) {
    JsonNodeFactory factory = JsonNodeFactory.instance;
    ObjectNode root = factory.objectNode();

    serializeString(entry.getAuthor(), "author", root);
    serializeListOfStrings(entry.getAuthors(), "authors", root, factory);
    serializeCategories(root, factory, entry.getCategories());
    serializeContents(root, factory, entry.getContents());
    serializeListOfStrings(entry.getContributors(), "contributors", root, factory);
    serializeDescription(root, factory, entry.getDescription());
    serializeEnclosures(root, factory, entry.getEnclosures());
    serializeForeignMarkUp(root, factory, entry.getForeignMarkup());
    serializeString(entry.getLink(), "link", root);
    serializeLinks(root, factory, entry.getLinks());
    serializeModules(root, factory, entry.getModules());
    serializeDate(root, entry.getPublishedDate(), "publishedDate");
    serializeSource(root, factory, entry.getSource());
    serializeString(entry.getTitle(), "title", root);
    serializeDate(root, entry.getUpdatedDate(), "updateDate");
    serializeString(entry.getUri(), "uri", root);

    return root;
  }


  private void serializeCategories(ObjectNode root, JsonNodeFactory factory, List categories) {
    if (categories == null || categories.size() == 0) {
      return;
    }
    ArrayNode cats = factory.arrayNode();
    for (Object obj : categories) {
      if (obj instanceof com.rometools.rome.feed.rss.Category) {
        ObjectNode catNode = factory.objectNode();
        com.rometools.rome.feed.rss.Category category = (com.rometools.rome.feed.rss.Category) obj;
        if (category.getDomain() != null) {
          catNode.put("domain", category.getDomain());
        }
        if (category.getValue() != null) {
          catNode.put("value", category.getValue());
        }
        cats.add(catNode);
      } else if (obj instanceof com.rometools.rome.feed.atom.Category) {
        com.rometools.rome.feed.atom.Category category = (com.rometools.rome.feed.atom.Category) obj;
        ObjectNode catNode = factory.objectNode();
        if (category.getLabel() != null) {
          catNode.put("label", category.getLabel());
        }
        if (category.getScheme() != null) {
          catNode.put("scheme", category.getScheme());
        }
        if (category.getSchemeResolved() != null) {
          catNode.put("schemeResolved", category.getSchemeResolved());
        }
        if (category.getTerm() != null ) {
          catNode.put("term", category.getTerm());
        }
        cats.add(catNode);
      }
    }
    root.put("categories", cats);
  }

  private void serializeContents(ObjectNode root, JsonNodeFactory factory, List contents) {
    if (contents == null || contents.size() == 0) {
      return;
    }
    ArrayNode contentsArray = factory.arrayNode();
    for (Object obj : contents) {
      ObjectNode content = factory.objectNode();
      if (obj instanceof com.rometools.rome.feed.rss.Content) {
        com.rometools.rome.feed.rss.Content rssContent = (com.rometools.rome.feed.rss.Content) obj;
        content.put("type", rssContent.getType());
        content.put("value", rssContent.getValue());
      }
      if (obj instanceof com.rometools.rome.feed.atom.Content) {
        com.rometools.rome.feed.atom.Content atomContent = (com.rometools.rome.feed.atom.Content) obj;
        content.put("type", atomContent.getType());
        content.put("value", atomContent.getValue());
        content.put("mode", atomContent.getMode());
        content.put("src", atomContent.getSrc());
      }
      contentsArray.add(content);
    }
    root.put("contents", contentsArray);
  }

  private void serializeDate(ObjectNode root, Date date, String key) {
    DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
    if (date == null) {
      return;
    }
    root.put(key, formatter.print(date.getTime()));
  }

  private void serializeDescription(ObjectNode root, JsonNodeFactory factory, SyndContent synd) {
    if (synd == null) {
      return;
    }
    ObjectNode content = factory.objectNode();
    if (synd.getValue() != null) {
      content.put("value", synd.getValue());
    }
    if (synd.getMode() != null) {
      content.put("mode", synd.getMode());
    }
    if (synd.getType() != null) {
      content.put("type", synd.getType());
    }
    root.put("description", content);
  }

  private void serializeEnclosures(ObjectNode root, JsonNodeFactory factory, List enclosures) {
    if (enclosures == null || enclosures.size() == 0) {
      return;
    }
    ArrayNode encls = factory.arrayNode();
    for (Object obj : enclosures) {
      if (obj instanceof Enclosure) {
        Enclosure enclosure = (Enclosure) obj;
        ObjectNode encl = factory.objectNode();
        if (enclosure.getType() != null) {
          encl.put("type", enclosure.getType());
        }
        if (enclosure.getUrl() != null) {
          encl.put("url", enclosure.getUrl());
        }
        encl.put("length", enclosure.getLength());
        encls.add(encl);
      } else if (obj instanceof SyndEnclosure) {
        SyndEnclosure enclosure = (SyndEnclosure) obj;
        ObjectNode encl = factory.objectNode();
        if (enclosure.getType() != null) {
          encl.put("type", enclosure.getType());
        }
        if (enclosure.getUrl() != null) {
          encl.put("url", enclosure.getUrl());
        }
        encl.put("length", enclosure.getLength());
        encls.add(encl);
      } else {
        LOGGER.warn("serializeEnclosures does not handle type : {}", obj.getClass().toString());
      }
    }
    root.put("enclosures", encls);
  }

  private void serializeForeignMarkUp(ObjectNode root, JsonNodeFactory factory, Object foreignMarkUp) {
    if (foreignMarkUp == null) {
      return;
    }
    if (foreignMarkUp instanceof String) {
      root.put("foreignEnclosures", (String) foreignMarkUp);
    } else if (foreignMarkUp instanceof List) {
      List foreignList = (List) foreignMarkUp;
      if (foreignList.size() == 0) {
        return;
      }
      if (foreignList.get(0) instanceof String) {
        serializeListOfStrings(foreignList, "foreignEnclosures", root, factory);
      } else {
        LOGGER.debug("SyndEntry.getForeignMarkUp is not of type String. Need to handle the case of class : {}",
            ((List)foreignMarkUp).get(0).getClass().toString());
      }
    } else {
      LOGGER.debug("SyndEntry.getForeignMarkUp is not of an expected type. Need to handle the case of class : {}",
          foreignMarkUp.getClass().toString());
    }
  }

  private void serializeImage(ObjectNode root, JsonNodeFactory factory, SyndImage image) {
    if (image == null) {
      return;
    }
    ObjectNode imageNode = factory.objectNode();
    serializeString(image.getDescription(), "description", imageNode);
    serializeString(image.getLink(), "link", imageNode);
    serializeString(image.getUrl(), "url", imageNode);
    serializeString(image.getTitle(), "title", imageNode);
    root.put("image", imageNode);
  }

  private void serializeListOfStrings(List toSerialize, String key, ObjectNode node, JsonNodeFactory factory) {
    if (toSerialize == null || toSerialize.size() == 0) {
      return;
    }
    ArrayNode keyNode = factory.arrayNode();
    for (Object obj : toSerialize) {
      if (obj instanceof String) {
        keyNode.add((String) obj);
      } else {
        LOGGER.debug("Array at Key:{} was expecting item types of String. Received class : {}", key, obj.getClass().toString());
      }
    }
    node.put(key, keyNode);
  }

  private void serializeLinks(ObjectNode root, JsonNodeFactory factory, List links) {
    if( links.size() == 0 ) {
      root.put("links", factory.arrayNode());
    } else  if (links.get(0) instanceof String) {
      serializeListOfStrings(links, "links", root, factory);
    } else if (links.get(0) instanceof SyndLinkImpl) {
      ArrayNode linksArray = factory.arrayNode();
      SyndLinkImpl syndLink;
      ObjectNode linkNode;
      for (Object obj : links) {
        linkNode = factory.objectNode();
        syndLink = (SyndLinkImpl) obj;
        linkNode.put("rel", syndLink.getRel());
        linkNode.put("href", syndLink.getHref());
        linkNode.put("type", syndLink.getType());
        linkNode.put("length", syndLink.getLength());
        linkNode.put("hrefLang", syndLink.getHreflang());
        linkNode.put("title", syndLink.getTitle());
        linksArray.add(linkNode);
      }
      root.put("links", linksArray);
    } else {
      LOGGER.error("No implementation for handling links of class : {}", links.get(0).getClass().toString());
    }
  }

  private void serializeModules(ObjectNode root, JsonNodeFactory factory, List modules) {
    if (modules == null || modules.size() == 0) {
      return;
    }
    ArrayNode modulesArray = factory.arrayNode();
    for (Object obj : modules) {
      if (obj instanceof Module) {
        Module mod = (Module) obj;
        if (mod.getUri() != null) {
          modulesArray.add(mod.getUri());
        }
      } else {
        LOGGER.debug("SyndEntry.getModules() items are not of type Module. Need to handle the case of class : {}",
            obj.getClass().toString());
      }
    }
    root.put("modules", modulesArray);
  }

  private void serializeSource(ObjectNode root, JsonNodeFactory factory, SyndFeed source) {
    if (source == null) {
      return;
    }
    ObjectNode sourceNode = factory.objectNode();
    serializeString(source.getAuthor(), "author", sourceNode);
    serializeListOfStrings(source.getAuthors(), "authors", sourceNode, factory);
    serializeCategories(sourceNode, factory, source.getCategories());
    serializeString(source.getCopyright(), "copyright", sourceNode);
    serializeListOfStrings(source.getContributors(), "contributors", sourceNode, factory);
    serializeString(source.getDescription(), "description", sourceNode);
    serializeDescription(sourceNode, factory, source.getDescriptionEx());
    // source.getEntries(); wtf?
    serializeString(source.getFeedType(), "feedType", sourceNode);
    serializeImage(sourceNode, factory, source.getImage());
    serializeForeignMarkUp(sourceNode, factory, source.getForeignMarkup());
    serializeString(source.getLanguage(), "language", sourceNode);
    serializeString(source.getLink(), "link", sourceNode);
    serializeListOfStrings(source.getLinks(), "links", sourceNode, factory);
    serializeModules(sourceNode, factory, source.getModules());
    serializeDate(sourceNode, source.getPublishedDate(), "publishedDate");
    serializeString(source.getTitle(), "title", sourceNode);
    serializeString(source.getUri(), "uri", sourceNode);

    root.put("source", sourceNode);
  }

  private void serializeString(String string, String key, ObjectNode node) {
    if (string != null && !string.equals("")) {
      node.put(key, string);
    }
  }

}
