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

package org.apache.streams.datasift.serializer;

import com.google.common.base.Preconditions;
import org.apache.streams.data.ActivityConverter;
import org.apache.streams.data.DocumentClassifier;
import org.apache.streams.datasift.Datasift;
import org.apache.streams.datasift.instagram.Instagram;
import org.apache.streams.datasift.interaction.Interaction;
import org.apache.streams.datasift.twitter.Twitter;

/**
 * Created by sblackmon on 11/6/14.
 */
public class DatasiftEventClassifier implements DocumentClassifier {

    public DatasiftEventClassifier() {

    }

    private static DatasiftEventClassifier instance = new DatasiftEventClassifier();

    public static DatasiftEventClassifier getInstance() {
        return instance;
    }

    public Class detectClass(Object document) {

        Preconditions.checkArgument(document instanceof Datasift);
        Datasift datasift = (Datasift)document;
        if(datasift.getTwitter() != null) {
            return Twitter.class;
        } else if(datasift.getInstagram() != null) {
            return Instagram.class;
        } else {
            return Interaction.class;
        }
    }

}
