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
package org.apache.streams.facebook.provider.page;

import facebook4j.Cover;
import facebook4j.Page;
import facebook4j.Place;

import java.net.URL;
import java.util.Date;

public class TestPage implements Page {
    private String id;
    private String name;

    public TestPage(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URL getLink() {
        return null;
    }

    @Override
    public String getCategory() {
        return null;
    }

    @Override
    public Boolean isPublished() {
        return null;
    }

    @Override
    public Boolean canPost() {
        return null;
    }

    @Override
    public Integer getLikes() {
        return null;
    }

    @Override
    public Place.Location getLocation() {
        return null;
    }

    @Override
    public String getPhone() {
        return null;
    }

    @Override
    public Integer getCheckins() {
        return null;
    }

    @Override
    public URL getPicture() {
        return null;
    }

    @Override
    public Cover getCover() {
        return null;
    }

    @Override
    public String getWebsite() {
        return null;
    }

    @Override
    public Integer getTalkingAboutCount() {
        return null;
    }

    @Override
    public String getAccessToken() {
        return null;
    }

    @Override
    public Boolean isCommunityPage() {
        return null;
    }

    @Override
    public Integer getWereHereCount() {
        return null;
    }

    @Override
    public Date getCreatedTime() {
        return null;
    }
}
