/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements. See the NOTICE file
distributed with this work for additional information
regarding copyright ownership. The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance *
http://www.apache.org/licenses/LICENSE-2.0 *
Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License. */
package org.apache.streams.instagram.provider.userinfo;

import org.apache.streams.instagram.InstagramConfiguration;
import org.apache.streams.instagram.provider.InstagramAbstractProvider;
import org.apache.streams.instagram.provider.InstagramDataCollector;

/**
 * Instagram provider that pulls UserInfoData from Instagram
 * @see org.apache.streams.instagram.provider.InstagramAbstractProvider
 */
public class InstagramUserInfoProvider extends InstagramAbstractProvider {

    public InstagramUserInfoProvider() {
        super();
    }

    public InstagramUserInfoProvider(InstagramConfiguration config) {
        super(config);
    }

    @Override
    protected InstagramDataCollector getInstagramDataCollector() {
        return new InstagramUserInfoCollector(super.dataQueue, super.config);
    }
}
