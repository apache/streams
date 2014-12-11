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

import java.io.Serializable;
import java.util.List;

/**
 * DocumentClassifier assists with ActivityConversion, by determining whether a document may be
 * parseable into a POJO for which an ActivityConverter exists.
 */
public interface DocumentClassifier extends Serializable {

    /**
     * Assess the structure of the document, and identify whether the provided document is
     * a structural match for one or more typed forms.
     *
     * @param document the document
     * @return a serializable pojo class this document matches
     */
    List<Class> detectClasses(Object document);

}
