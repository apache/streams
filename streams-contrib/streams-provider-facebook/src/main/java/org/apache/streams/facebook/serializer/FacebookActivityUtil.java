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

package org.apache.streams.facebook.serializer;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.facebook.Cover;
import org.apache.streams.facebook.Location;
import org.apache.streams.facebook.Page;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Image;
import org.apache.streams.pojo.json.Provider;

import java.util.HashMap;
import java.util.Map;

import static org.apache.streams.data.util.ActivityUtil.ensureExtensions;

public class FacebookActivityUtil {
    /**
     * Updates the given Activity object with the values from the Page
     * @param page the object to use as the source
     * @param activity the target of the updates.  Will receive all values from the Page.
     * @throws org.apache.streams.exceptions.ActivitySerializerException
     */
    public static void updateActivity(Page page, Activity activity) throws ActivitySerializerException {
        activity.setActor(buildActor(page));
        activity.setId(null);
        activity.setProvider(getProvider());
    }

    /**
     * Gets the common facebook {@link org.apache.streams.pojo.json.Provider} object
     * @return a provider object representing Facebook
     */
    public static Provider getProvider() {
        Provider provider = new Provider();
        provider.setId("id:providers:facebook");
        provider.setDisplayName("Facebook");

        return provider;
    }

    /**
     * Builds the activity {@link org.apache.streams.pojo.json.Actor} object from the Page
     * @param page the object to use as the source
     * @return a valid Actor populated from the Page
     */
    public static Actor buildActor(Page page) {
        Actor actor = new Actor();
        actor.setId(formatId(
                Optional.fromNullable(
                        page.getId())
                        .or(Optional.of(page.getId().toString()))
                        .orNull()
        ));

        actor.setDisplayName(page.getName());
        actor.setAdditionalProperty("handle", page.getUsername());
        actor.setSummary(page.getAbout());

        if (page.getLink()!=null){
            actor.setUrl(page.getLink());
        }

        Image profileImage = new Image();
        Cover cover = page.getCover();

        if(cover != null)
            profileImage.setUrl(cover.getSource());
        actor.setImage(profileImage);

        buildExtensions(actor, page);

        return actor;
    }

    /**
     * Builds the actor extensions given the page object
     * @param actor
     * @param page
     */
    public static void buildExtensions(Actor actor, Page page) {
        Map<String, Object> extensions = new HashMap<String, Object>();
        Location location = page.getLocation();

        if(location != null)
            extensions.put("location", page.getLocation().toString());

        extensions.put("favorites", page.getLikes());
        extensions.put("followers", page.getTalkingAboutCount());

        extensions.put("screenName", page.getUsername());

        actor.setAdditionalProperty("extensions", extensions);
    }

    /**
     * Formats the ID to conform with the Apache Streams activity ID convention
     * @param idparts the parts of the ID to join
     * @return a valid Activity ID in format "id:facebook:part1:part2:...partN"
     */
    public static String formatId(String... idparts) {
        return Joiner.on(":").join(Lists.asList("id:facebook", idparts));
    }
}