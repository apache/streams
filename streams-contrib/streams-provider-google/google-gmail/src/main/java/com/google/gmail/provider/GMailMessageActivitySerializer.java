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

package com.google.gmail.provider;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.googlecode.gmail4j.GmailException;
import com.googlecode.gmail4j.GmailMessage;
import com.googlecode.gmail4j.javamail.JavaMailGmailMessage;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPSSLStore;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.pojo.extensions.ExtensionUtil;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Generator;
import org.apache.streams.pojo.json.Icon;
import org.apache.streams.pojo.json.Provider;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.internet.MimeMultipart;
import java.util.List;
import java.util.Map;

/**
* Created with IntelliJ IDEA.
* User: mdelaet
* Date: 9/30/13
* Time: 9:24 AM
* To change this template use File | Settings | File Templates.
*/
public class GMailMessageActivitySerializer implements ActivitySerializer<GmailMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GMailMessageActivitySerializer.class);

    GMailProvider provider;

    ObjectMapper mapper = new ObjectMapper();

    public GMailMessageActivitySerializer(GMailProvider provider) {

        this.provider = provider;

        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, Boolean.FALSE);

        mapper.addMixInAnnotations(IMAPSSLStore.class, MessageMixIn.class);
        mapper.addMixInAnnotations(IMAPFolder.class, MessageMixIn.class);
        mapper.addMixInAnnotations(IMAPMessage.class, MessageMixIn.class);
        mapper.addMixInAnnotations(MimeMultipart.class, MessageMixIn.class);
        mapper.addMixInAnnotations(JavaMailGmailMessage.class, MessageMixIn.class);

    }

    public GMailMessageActivitySerializer() {
    }

    @Override
    public String serializationFormat() {
        return "gmail.v1";
    }

    @Override
    public GmailMessage serialize(Activity activity) {
        return null;
    }

    @Override
    public Activity deserialize(GmailMessage gmailMessage) {

        Activity activity = new Activity();
        activity.setId(formatId(this.provider.getConfig().getUserName(), String.valueOf(gmailMessage.getMessageNumber())));
        activity.setPublished(new DateTime(gmailMessage.getSendDate()));
        Provider provider = new Provider();
        provider.setId("http://gmail.com");
        provider.setDisplayName("GMail");
        activity.setProvider(provider);
        Actor actor = new Actor();
        actor.setId(gmailMessage.getFrom().getEmail());
        actor.setDisplayName(gmailMessage.getFrom().getName());
        activity.setActor(actor);
        activity.setVerb("email");
        ActivityObject object = new ActivityObject();
        try {
            object.setId(gmailMessage.getTo().get(0).getEmail());
            object.setDisplayName(gmailMessage.getTo().get(0).getName());
        } catch( GmailException e ) {
            LOGGER.warn(e.getMessage());
        }
        activity.setTitle(gmailMessage.getSubject());
        try {
            activity.setContent(gmailMessage.getContentText());
        } catch( GmailException e ) {
            LOGGER.warn(e.getMessage());
        }
        activity.setObject(object);

//        try {
//            // if jackson can't serialize the object, find out now
//            String jsonString = mapper.writeValueAsString(gmailMessage);
//            ObjectNode jsonObject = mapper.valueToTree(gmailMessage);
//            // since it can, write the entire source object to extensions.gmail
//            Map<String, Object> extensions = Maps.newHashMap();
//            extensions.put("gmail", gmailMessage);
//            activity.setAdditionalProperty("extensions", extensions);
//        } catch (JsonProcessingException e) {
//            LOGGER.debug("Failed Json Deserialization");
//            e.printStackTrace();
//        }

        return activity;
    }

    @Override
    public List<Activity> deserializeAll(List<GmailMessage> serializedList) {
        throw new NotImplementedException("Not currently implemented");
    }

    public Activity convert(ObjectNode event) {
        return null;
    }

    public static Generator buildGenerator(ObjectNode event) {
        return null;
    }

    public static Icon getIcon(ObjectNode event) {
        return null;
    }

    public static Provider buildProvider(ObjectNode event) {
        Provider provider = new Provider();
        provider.setId("id:providers:gmail");
        return provider;
    }

    public static List<Object> getLinks(ObjectNode event) {
        return null;
    }

    public static String getUrls(ObjectNode event) {
        return null;
    }

    public static void addGMailExtension(Activity activity, GmailMessage gmailMessage) {
        Map<String, Object> extensions = ExtensionUtil.ensureExtensions(activity);
        extensions.put("gmail", gmailMessage);
    }

    public static String formatId(String... idparts) {
        return Joiner.on(":").join(Lists.asList("id:gmail", idparts));
    }

    interface MessageMixIn {
        @JsonManagedReference
        @JsonIgnore
        IMAPSSLStore getDefaultFolder(); // we don't need it!
        @JsonManagedReference
        @JsonIgnore
        IMAPSSLStore getPersonalNamespaces(); // we don't need it!
        @JsonManagedReference
        @JsonIgnore
        IMAPFolder getStore(); // we don't need it!
        //        @JsonManagedReference
//        @JsonIgnore
//        @JsonBackReference
        //IMAPFolder getParent(); // we don't need it!
        @JsonManagedReference
        @JsonIgnore
        @JsonBackReference
        IMAPMessage getFolder(); // we don't need it!
        @JsonManagedReference
        @JsonIgnore
        @JsonProperty("parent")
        @JsonBackReference
        MimeMultipart getParent();
    }

}
