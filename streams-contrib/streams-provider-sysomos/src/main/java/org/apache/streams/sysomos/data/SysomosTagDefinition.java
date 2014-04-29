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
package org.apache.streams.sysomos.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Sysomos Heartbeat Tag.  These tags are defined within the Sysomos system and exposed via the data feed.
 */
public class SysomosTagDefinition {

    private String tagName;
    private String displayName;
    private List<String> queries;

    public SysomosTagDefinition(String tagName, String displayName) {
        this.tagName = tagName;
        this.displayName = displayName;
        this.queries = new ArrayList<String>();
    }

    public String getTagName() {
        return this.tagName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public List<String> getQueries() {
        List<String> result = new ArrayList<String>();
        result.addAll(this.queries);
        return result;
    }

    public void addQuery(String query) {
        this.queries.add(query);
    }

    public boolean hasTagName(String tagName) {
        return this.tagName.equals(tagName);
    }

    public boolean hasQuery(String query) {
        return this.queries.contains(query);
    }

    public boolean hasDisplayName(String displayName) {
        return this.displayName.equals(displayName);
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof SysomosTagDefinition)) {
            return false;
        }
        SysomosTagDefinition that = (SysomosTagDefinition) o;
        if(!this.tagName.equals(that.tagName)) {
            return false;
        }
        if(!this.displayName.equals(that.displayName)) {
            return false;
        }
        if(this.queries.size() != that.queries.size()) {
            return false;
        }
        for(int i=0; i < this.queries.size(); ++i) {
            if(!that.queries.contains(this.queries.get(i))) {
                return false;
            }
        }
        return true;
    }
}
