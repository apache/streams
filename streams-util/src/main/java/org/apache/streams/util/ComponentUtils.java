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

package org.apache.streams.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Queue;

/**
 * Created by sblackmon on 3/31/14.
 */
public class ComponentUtils {

    public static void offerUntilSuccess(Object entry, Queue queue) {

        boolean success;
        do {
            synchronized( ComponentUtils.class ) {
                success = queue.offer(entry);
            }
            Thread.yield();
        }
        while( !success );
    }

    public static String pollUntilStringNotEmpty(Queue queue) {

        String result = null;
        do {
            synchronized( ComponentUtils.class ) {
                try {
                    result = (String) queue.remove();
                } catch( Exception e ) {}
            }
            Thread.yield();
        }
        while( result == null && !StringUtils.isNotEmpty(result) );

        return result;
    }

}
