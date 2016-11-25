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

package org.apache.streams.moreover;

import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.pojo.json.Activity;

import com.moreover.api.Article;
import com.moreover.api.ArticlesResponse;
import com.moreover.api.ObjectFactory;
import org.apache.commons.lang.SerializationException;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Deserializes the Moreover Article XML and converts it to an instance of {@link Activity}.
 */
public class MoreoverXmlActivitySerializer implements ActivitySerializer<String> {

  //JAXBContext is threadsafe (supposedly)
  private final JAXBContext articleContext;
  private final JAXBContext articlesContext;

  public MoreoverXmlActivitySerializer() {
    articleContext = createContext(Article.class);
    articlesContext = createContext(ArticlesResponse.class);
  }

  @Override
  public String serializationFormat() {
    return "application/xml+vnd.moreover.com.v1";
  }

  @Override
  public String serialize(Activity deserialized) {
    throw new UnsupportedOperationException("Cannot currently serialize to Moreover");
  }

  @Override
  public Activity deserialize(String serialized) {
    Article article = deserializeMoreover(serialized);
    return MoreoverUtils.convert(article);
  }

  @Override
  public List<Activity> deserializeAll(List<String> serializedList) {
    List<Activity> activities = new LinkedList<Activity>();
    for (String item : serializedList) {
      ArticlesResponse response = deserializeMoreoverResponse(item);
      for (Article article : response.getArticles().getArticle()) {
        activities.add(MoreoverUtils.convert(article));
      }
    }
    return activities;
  }

  private Article deserializeMoreover(String serialized) {
    try {
      Unmarshaller unmarshaller = articleContext.createUnmarshaller();
      return (Article) unmarshaller.unmarshal(new StringReader(serialized));
    } catch (JAXBException ex) {
      throw new SerializationException("Unable to deserialize Moreover data", ex);
    }
  }

  private ArticlesResponse deserializeMoreoverResponse(String serialized) {
    try {
      Unmarshaller unmarshaller = articlesContext.createUnmarshaller();
      return ((JAXBElement<ArticlesResponse>) unmarshaller.unmarshal(new StringReader(serialized))).getValue();
    } catch (JAXBException ex) {
      throw new SerializationException("Unable to deserialize Moreover data", ex);
    }
  }

  private JAXBContext createContext(Class articleClass) {
    JAXBContext context;
    try {
      context = JAXBContext.newInstance(articleClass.getPackage().getName(), ObjectFactory.class.getClassLoader());
    } catch (JAXBException ex) {
      throw new IllegalStateException("Unable to create JAXB Context for Moreover data", ex);
    }
    return context;
  }
}
