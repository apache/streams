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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class RegexUtils {

    public static boolean matches(String line, String regEx) {

        boolean regExpMatches = false;

        if (StringUtils.isNotBlank(line) && StringUtils.isNotBlank(regEx)) {
            if (line.matches(regEx)) {
                regExpMatches = true;
            }
        }
        return regExpMatches;
    }

    public static List<String> getMatches(String line, String regEx, int group){
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(line);
        List<String> matches = new ArrayList<String>();

        while (matcher.find()) {

            matches.add(matcher.group(group));
        }

        return matches;
    }

    public static List<Pair<String,String>> getTwoMatchedGroupsList(List<String> lines, String regEx){
        List<Pair<String,String>> matches = new ArrayList<Pair<String,String>>();

        for( String line : lines ) {

            Pair<String,String> match = getTwoMatchedGroups(line, regEx);
            if( match != null )
                matches.add(match);

        }
        return matches;
    }

    public static Pair<String,String> getTwoMatchedGroups(String line, String regEx){
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(line);
        Pair<String,String> match = null;

        while (matcher.find()) {
            Pair<String,String> pair = new ImmutablePair<String,String>(matcher.group(0), matcher.group(1));
            match = pair;
        }

        return match;
    }

    public static String getMatchedContent(String line, String regEx){
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(line);
        String matchedContent = null;
        if (matcher.find()) {
            matchedContent = matcher.group();
        }

        return matchedContent;
    }
}
