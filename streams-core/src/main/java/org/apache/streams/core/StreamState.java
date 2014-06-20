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

package org.apache.streams.core;

/**
 * Created by sblackmon on 1/6/14.
 */
public enum StreamState {
    RUNNING,  //Stream is currently connected and running
    STOPPED,  // Stream has been shut down and is stopped
    CONNECTING, //Stream is attempting to connect to server
    SHUTTING_DOWN, //Stream has initialized shutdown
    DISCONNECTED //Stream has unintentionally lost connection
}

