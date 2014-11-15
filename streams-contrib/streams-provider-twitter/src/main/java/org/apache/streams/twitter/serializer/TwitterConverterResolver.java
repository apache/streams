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

package org.apache.streams.twitter.serializer;

import org.apache.streams.data.ActivityConverterResolver;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.twitter.pojo.*;

/**
 * Created by sblackmon on 12/13/13.
 */
public class TwitterConverterResolver implements ActivityConverterResolver {

    public TwitterConverterResolver() {

    }

    private static TwitterConverterResolver instance = new TwitterConverterResolver();

    public static TwitterConverterResolver getInstance() {

        if( instance == null )
            instance = new TwitterConverterResolver();
        return instance;

    }

    @Override
    public Class bestSerializer(Class documentClass) throws ActivitySerializerException {

        if (documentClass == Retweet.class)
            return TwitterJsonRetweetActivityConverter.class;
        else if (documentClass == Delete.class)
            return TwitterJsonDeleteActivityConverter.class;
        else if (documentClass == User.class)
            return TwitterJsonUserActivityConverter.class;
        else if (documentClass == UserstreamEvent.class)
            return TwitterJsonUserstreameventActivityConverter.class;
        else if (documentClass == FriendList.class)
            return null;
        else
            return TwitterJsonTweetActivityConverter.class;

    }
}
