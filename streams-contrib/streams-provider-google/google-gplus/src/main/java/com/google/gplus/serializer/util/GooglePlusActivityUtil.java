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

package com.google.gplus.serializer.util;

import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.pojo.extensions.ExtensionUtil;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Image;
import org.apache.streams.pojo.json.Provider;

import com.google.api.services.plus.model.Comment;
import com.google.api.services.plus.model.Person;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * GooglePlusActivityUtil helps convert c.g.Person and c.g.Activity into o.a.s.p.j.o.Page and o.a.s.p.j.Activity.
 */
public class GooglePlusActivityUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(GooglePlusActivityUtil.class);

  /**
   * Given a {@link Person} object and an
   * {@link Activity} object, fill out the appropriate details.
   *
   * @param item Person
   * @param activity Activity
   * @throws ActivitySerializerException ActivitySerializerException
   */
  public static void updateActivity(Person item, Activity activity) throws ActivitySerializerException {
    activity.setActor(buildActor(item));
    activity.setVerb("update");

    activity.setId(formatId(activity.getVerb(), Optional.ofNullable(item.getId()).orElse(null)));

    activity.setProvider(getProvider());
  }

  /**
   * Given a {@link List} of {@link Comment} objects and an
   * {@link Activity}, update that Activity to contain all comments
   *
   * @param comments input List of Comment
   * @param activity output Activity
   */
  public static void updateActivity(List<Comment> comments, Activity activity) {
    for (Comment comment : comments) {
      addComment(activity, comment);
    }

    Map<String, Object> extensions = ExtensionUtil.getInstance().ensureExtensions(activity);
    extensions.put("comment_count", comments.size());
  }

  /**
   * Given a Google Plus {@link com.google.api.services.plus.model.Activity},
   * convert that into an Activity streams formatted {@link Activity}
   *
   * @param gPlusActivity input c.g.a.s.p.m.Activity
   * @param activity output o.a.s.p.j.Activity
   */
  public static void updateActivity(com.google.api.services.plus.model.Activity gPlusActivity, Activity activity) {
    activity.setActor(buildActor(gPlusActivity.getActor()));
    activity.setVerb("post");
    activity.setTitle(gPlusActivity.getTitle());
    activity.setUrl(gPlusActivity.getUrl());
    activity.setProvider(getProvider());

    if (gPlusActivity.getObject() != null) {
      activity.setContent(gPlusActivity.getObject().getContent());
    }

    activity.setId(formatId(activity.getVerb(), Optional.ofNullable(gPlusActivity.getId()).orElse(null)));

    DateTime published = new DateTime(String.valueOf(gPlusActivity.getPublished()));
    activity.setPublished(published);

    setObject(activity, gPlusActivity.getObject());
    addGPlusExtensions(activity, gPlusActivity);
  }

  /**
   * Adds a single {@link Comment} to the Object.Attachments
   * section of the passed in {@link Activity}
   *
   * @param activity output o.a.s.p.j.Activity
   * @param comment input c.g.a.s.p.m.Comment
   */
  private static void addComment(Activity activity, Comment comment) {
    ActivityObject obj = new ActivityObject();

    obj.setId(comment.getId());
    obj.setPublished(new DateTime(String.valueOf(comment.getPublished())));
    obj.setUpdated(new DateTime(String.valueOf(comment.getUpdated())));
    obj.setContent(comment.getObject().getContent());
    obj.setObjectType(comment.getObject().getObjectType());

    Map<String, Object> extensions = new HashMap<>();
    extensions.put("googlePlus", comment);

    obj.setAdditionalProperty("extensions", extensions);

    if (activity.getObject() == null) {
      activity.setObject(new ActivityObject());
    }
    if (activity.getObject().getAttachments() == null) {
      activity.getObject().setAttachments(new ArrayList<>());
    }

    activity.getObject().getAttachments().add(obj);
  }

  /**
   * Add in necessary extensions from the passed in {@link com.google.api.services.plus.model.Activity} to the
   * {@link Activity} object
   *
   * @param activity output o.a.s.p.j.Activity
   * @param gPlusActivity input c.g.a.s.p.m.Activity
   */
  private static void addGPlusExtensions(Activity activity, com.google.api.services.plus.model.Activity gPlusActivity) {

    activity.getAdditionalProperties().put("googlePlus", gPlusActivity);

    Map<String, Object> extensions = ExtensionUtil.getInstance().ensureExtensions(activity);

    com.google.api.services.plus.model.Activity.PlusObject object = gPlusActivity.getObject();

    if (object != null) {
      com.google.api.services.plus.model.Activity.PlusObject.Plusoners plusoners = object.getPlusoners();
      if (plusoners != null) {
        Map<String, Object> likes = new HashMap<>();
        likes.put("count", plusoners.getTotalItems());
        extensions.put("likes", likes);
      }

      com.google.api.services.plus.model.Activity.PlusObject.Resharers resharers = object.getResharers();
      if (resharers != null) {
        Map<String, Object> rebroadcasts = new HashMap<>();
        rebroadcasts.put("count", resharers.getTotalItems());
        extensions.put("rebroadcasts", rebroadcasts);
      }

      extensions.put("keywords", object.getContent());
    }
  }

  /**
   * Set the {@link ActivityObject} field given the passed in
   * {@link com.google.api.services.plus.model.Activity.PlusObject}
   *
   * @param activity output $.object as o.a.s.p.j.ActivityObject
   * @param plusObject input c.g.a.s.p.m.Activity.PlusObject
   */
  private static void setObject(Activity activity, com.google.api.services.plus.model.Activity.PlusObject plusObject) {
    if (plusObject != null) {
      ActivityObject activityObject = new ActivityObject();

      activityObject.setContent(plusObject.getContent());
      activityObject.setObjectType(plusObject.getObjectType());

      List<ActivityObject> attachmentsList = new ArrayList<>();
      for (com.google.api.services.plus.model.Activity.PlusObject.Attachments attachments : plusObject.getAttachments()) {
        ActivityObject attach = new ActivityObject();

        attach.setContent(attachments.getContent());
        attach.setDisplayName(attachments.getDisplayName());
        attach.setObjectType(attachments.getObjectType());
        attach.setUrl(attachments.getUrl());

        Image image = new Image();
        com.google.api.services.plus.model.Activity.PlusObject.Attachments.Image image1 = attachments.getImage();

        if (image1 != null) {
          image.setUrl(image1.getUrl());
          attach.setImage(image);
        }

        attachmentsList.add(attach);
      }

      activityObject.setAttachments(attachmentsList);

      activity.setObject(activityObject);
    }
  }

  /**
   * Given a {@link com.google.api.services.plus.model.Activity.Actor} object, return a fully fleshed
   * out {@link ActivityObject} actor
   *
   * @param gPlusActor input c.g.a.s.p.m.Activity.Actor
   * @return {@link ActivityObject} output $.actor as o.a.s.p.j.ActivityObject
   */
  private static ActivityObject buildActor(com.google.api.services.plus.model.Activity.Actor gPlusActor) {
    ActivityObject actor = new ActivityObject();

    actor.setDisplayName(gPlusActor.getDisplayName());
    actor.setId(formatId(String.valueOf(gPlusActor.getId())));
    actor.setUrl(gPlusActor.getUrl());

    Image image = new Image();
    com.google.api.services.plus.model.Activity.Actor.Image googlePlusImage = gPlusActor.getImage();

    if (googlePlusImage != null) {
      image.setUrl(googlePlusImage.getUrl());
    }
    actor.setImage(image);

    return actor;
  }

  /**
   * Extract the relevant details from the passed in {@link Person} object and build
   * an actor with them
   *
   * @param person Person
   * @return Actor constructed with relevant Person details
   */
  private static ActivityObject buildActor(Person person) {
    ActivityObject actor = new ActivityObject();

    actor.setUrl(person.getUrl());
    actor.setDisplayName(person.getDisplayName());
    actor.setId(formatId(String.valueOf(person.getId())));

    if (person.getAboutMe() != null) {
      actor.setSummary(person.getAboutMe());
    } else if (person.getTagline() != null) {
      actor.setSummary(person.getTagline());
    }

    Image image = new Image();
    Person.Image googlePlusImage = person.getImage();

    if (googlePlusImage != null) {
      image.setUrl(googlePlusImage.getUrl());
    }
    actor.setImage(image);

    Map<String, Object> extensions = new HashMap<>();

    extensions.put("followers", person.getCircledByCount());
    extensions.put("googleplus", person);
    actor.setAdditionalProperty("extensions", extensions);

    return actor;
  }

  /**
   * Gets the common googleplus {@link Provider} object
   * @return a provider object representing GooglePlus
   */
  public static Provider getProvider() {
    Provider provider = new Provider();
    provider.setId("id:providers:googleplus");
    provider.setDisplayName("GooglePlus");
    return provider;
  }

  /**
   * Formats the ID to conform with the Apache Streams activity ID convention
   * @param idparts the parts of the ID to join
   * @return a valid Activity ID in format "id:googleplus:part1:part2:...partN"
   */
  public static String formatId(String... idparts) {
    return String.join(":",
        Stream.concat(Arrays.stream(new String[]{"id:googleplus"}), Arrays.stream(idparts)).collect(Collectors.toList()));
  }
}