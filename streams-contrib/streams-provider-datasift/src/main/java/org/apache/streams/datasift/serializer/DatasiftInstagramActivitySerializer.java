/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/
package org.apache.streams.datasift.serializer;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.streams.datasift.Datasift;
import org.apache.streams.datasift.instagram.From;
import org.apache.streams.datasift.instagram.Instagram;
import org.apache.streams.instagram.serializer.util.InstagramActivityUtil;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.apache.streams.data.util.ActivityUtil.ensureExtensions;

/**
 *
 */
public class DatasiftInstagramActivitySerializer extends DatasiftDefaultActivitySerializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasiftInstagramActivitySerializer.class);

    @Override
    public Activity convert(Datasift event) {
        Activity activity = super.convert(event);

        Instagram instagram = event.getInstagram();

        activity.setActor(buildActor(event, instagram));
        activity.setId(formatId(activity.getVerb(), event.getInteraction().getId()));
        activity.setProvider(InstagramActivityUtil.getProvider());
        activity.setLinks(getLinks(event.getInstagram()));

        activity.setVerb(selectVerb(event));
        addInstagramExtensions(activity, instagram);
        return activity;
    }

    /**
     * Gets links from the object
     * @return
     */
    private List<String> getLinks(Instagram instagram) {
        List<String> result = Lists.newLinkedList();
        if( instagram.getMedia() != null ) {
            result.add(instagram.getMedia().getImage());
            result.add(instagram.getMedia().getVideo());
        }
        return result;
    }

    public Actor buildActor(Datasift event, Instagram instagram) {
        Actor actor = super.buildActor(event.getInteraction());
        From user = instagram.getFrom();

        actor.setDisplayName(firstStringIfNotNull(user.getFullName()));
        actor.setId(formatId(Optional.fromNullable(
                user.getId())
                .or(actor.getId())));

        Image profileImage = new Image();
        String profileUrl = null;
        profileUrl = user.getProfilePicture();
        profileImage.setUrl(profileUrl);
        actor.setImage(profileImage);

        return actor;
    }

    public void addInstagramExtensions(Activity activity, Instagram instagram) {
        Map<String, Object> extensions = ensureExtensions(activity);
        List<String> hashTags;
        if(instagram.getMedia() != null) {
            hashTags = instagram.getMedia().getTags();
            extensions.put("hashtags", hashTags);
            extensions.put("keywords", activity.getContent());
        } else {
            extensions.put("keywords", activity.getContent());

        }

    }

    private String selectVerb(Datasift event) {
        if( event.getInteraction().getSubtype().equals("like"))
            return "like";
        else
            return "post";
    }

    public static String formatId(String... idparts) {
        return Joiner.on(":").join(Lists.asList("id:instagram", idparts));
    }

}
