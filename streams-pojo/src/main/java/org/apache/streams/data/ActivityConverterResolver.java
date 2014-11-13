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

package org.apache.streams.data;

import org.apache.streams.exceptions.ActivitySerializerException;

import java.io.Serializable;

/**
 * Serializes and deserializes Activities
 */
public interface ActivityConverterResolver extends Serializable {

    /**
     * Identifies a class that con convert this document to an activity
     *
     * @param documentClass known or expected class of the document
     * @return class of an appropriate ActivitySerializer
     */
    Class bestSerializer(Class documentClass) throws ActivitySerializerException;

}
