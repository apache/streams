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

package org.apache.streams.exceptions;

/**
 *  ActivitySerializerException is a typed exception appropriate when a valid Activity
 *  cannot be created from a given document.
 */
public class ActivitySerializerException extends Exception {

    public ActivitySerializerException() {
    }

    public ActivitySerializerException(String message) {
        super(message);
    }

    public ActivitySerializerException(Throwable cause) {
        super(cause);
    }

    public ActivitySerializerException(String message, Throwable cause) {
        super(message, cause);
    }

}
