/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.apache.streams.regex;

/*
 * #%L
 * streams-processor-regex
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.streams.core.StreamsProcessor;

/**
 * Processes the content of an {@link org.apache.streams.pojo.json.Activity} object to extract the Hashtags and add
 * them to the appropriate extensions object
 */
public class RegexHashtagExtractor extends AbstractRegexExtensionExtractor<String> implements StreamsProcessor{

    public final static String DEFAULT_PATTERN = "#\\w+";
    public final static String PATTERN_CONFIG_KEY = "HashtagPattern";
    public final static String EXTENSION_KEY = "hashtags";

    public RegexHashtagExtractor() {
        super(PATTERN_CONFIG_KEY, EXTENSION_KEY, DEFAULT_PATTERN);
    }


    @Override
    protected String prepareObject(String extracted) {
        return extracted.substring(1);
    }
}
