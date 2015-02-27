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

package org.apache.streams.datasift.csdl;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class DatasiftCsdlUtil {

	private static final Logger log = LoggerFactory
			.getLogger(DatasiftCsdlUtil.class);

	public static String csdlFromTwitterUserIds(List<String> list) throws Exception {
		
		StringBuilder csdlBuilder = new StringBuilder();

        csdlBuilder.append("twitter.user.id in [");
        ListIterator<String> listIterator = Lists.newArrayList(list).listIterator();
        while( listIterator.hasNext() ) {
            csdlBuilder.append(listIterator.next());
            if (listIterator.hasNext())
                csdlBuilder.append(",");
        }
        csdlBuilder.append("]\n");
        csdlBuilder.append(" OR\n");
        csdlBuilder.append("twitter.in_reply_to_user_id contains_any \"");
        listIterator = Lists.newArrayList(list).listIterator();
        while( listIterator.hasNext() ) {
            csdlBuilder.append(listIterator.next());
            if (listIterator.hasNext())
                csdlBuilder.append(",");
        }
        csdlBuilder.append("\"\n");
        csdlBuilder.append(" OR\n");
        csdlBuilder.append("twitter.mention_ids in [");
        listIterator = Lists.newArrayList(list).listIterator();
        while( listIterator.hasNext() ) {
            csdlBuilder.append(listIterator.next());
            if (listIterator.hasNext())
                csdlBuilder.append(",");
        }
        csdlBuilder.append("]\n");

		log.debug(csdlBuilder.toString());
		
		return csdlBuilder.toString();
	}
	
	public static String csdlFromTwitterUserNames(List<String> list) throws Exception {

        StringBuilder csdlBuilder = new StringBuilder();

        csdlBuilder.append("twitter.user.screen_name contains_any \"");
        ListIterator<String> listIterator = Lists.newArrayList(list).listIterator();
        while( listIterator.hasNext() ) {
            csdlBuilder.append(listIterator.next());
            if (listIterator.hasNext())
                csdlBuilder.append(",");
        }
        csdlBuilder.append("\"\n");
        csdlBuilder.append(" OR\n");
        csdlBuilder.append("twitter.in_reply_to_screen_name contains_any \"");
        listIterator = Lists.newArrayList(list).listIterator();
        while( listIterator.hasNext() ) {
            csdlBuilder.append(listIterator.next());
            if (listIterator.hasNext())
                csdlBuilder.append(",");
        }
        csdlBuilder.append("\"\n");
        csdlBuilder.append(" OR\n");
        csdlBuilder.append("twitter.mentions contains_any \"");
        listIterator = Lists.newArrayList(list).listIterator();
        while( listIterator.hasNext() ) {
            csdlBuilder.append(listIterator.next());
            if (listIterator.hasNext())
                csdlBuilder.append(",");
        }
        csdlBuilder.append("\"\n");

        log.debug(csdlBuilder.toString());

        return csdlBuilder.toString();
	}

    public static String csdlFromKeywords(List<String> include, List<String> exclude) throws Exception {

        StringBuilder csdlBuilder = new StringBuilder();

        csdlBuilder.append("interaction.content contains_any \"");
        ListIterator<String> listIterator = Lists.newArrayList(include).listIterator();
        while( listIterator.hasNext() ) {
            csdlBuilder.append(listIterator.next());
            if (listIterator.hasNext())
                csdlBuilder.append(",");
        }
        csdlBuilder.append("\"\n");
        csdlBuilder.append(" AND NOT ( \n");
        csdlBuilder.append("interaction.content \"");
        listIterator = Lists.newArrayList(exclude).listIterator();
        while( listIterator.hasNext() ) {
            csdlBuilder.append(listIterator.next());
            if (listIterator.hasNext())
                csdlBuilder.append(",");
        }
        csdlBuilder.append("\"\n");
        csdlBuilder.append(")\n");

        log.debug(csdlBuilder.toString());

        return csdlBuilder.toString();
    }

    //public static String csdlMultifieldMatch(Multimap<String, String> includes, Multimap<String, String> excludes) throws Exception {
    public static String csdlMultifieldMatch(Multimap<String, String> require1, Multimap<String, String> require2, Multimap<String, String> exclude) throws Exception {

        StringBuilder csdlBuilder = new StringBuilder();

        String require1String;
        String require2String = null;
        String excludeString = null;
        List<String> require1clauses = Lists.newArrayList();
        for( String includeField : require1.keySet() ) {
            StringBuilder clauseBuilder = new StringBuilder();
            Collection<String> values = require1.get(includeField);
            String match_clause = null ;
            if( values.size() > 1 )
                match_clause = "contains_any";
            else if( values.size() == 1 )
                match_clause = "contains";
            if( match_clause != null ) {
                clauseBuilder.append(includeField + " " + match_clause + " \"");
                Joiner.on(",").skipNulls().appendTo(clauseBuilder, values);
                clauseBuilder.append("\"");
                require1clauses.add(clauseBuilder.toString());
            }
        }
        require1String = "(\n" + Joiner.on("\nOR\n").skipNulls().join(require1clauses) + "\n)\n";

        if( require2 != null && require2.keySet().size() > 0 ) {
            List<String> require2clauses = Lists.newArrayList();
            for (String includeField : require2.keySet()) {
                StringBuilder clauseBuilder = new StringBuilder();
                Collection<String> values = require2.get(includeField);
                String match_clause = null;
                if (values.size() > 1)
                    match_clause = "contains_any";
                else if (values.size() == 1)
                    match_clause = "contains";
                if( match_clause != null ) {
                    clauseBuilder.append(includeField + " " + match_clause + " \"");
                    Joiner.on(",").skipNulls().appendTo(clauseBuilder, values);
                    clauseBuilder.append("\"");
                    require2clauses.add(clauseBuilder.toString());
                }
            }
            require2String = "(\n" + Joiner.on("\nOR\n").skipNulls().join(require2clauses) + "\n)\n";
        }

        if( exclude != null && exclude.keySet().size() > 0) {
            List<String> excludeclauses = Lists.newArrayList();
            for (String includeField : exclude.keySet()) {
                StringBuilder clauseBuilder = new StringBuilder();
                Collection<String> values = exclude.get(includeField);
                String match_clause = null;
                if (values.size() > 1)
                    match_clause = "contains_any";
                else if (values.size() == 1)
                    match_clause = "contains";
                if( match_clause != null ) {
                    clauseBuilder.append(includeField + " " + match_clause + " \"");
                    Joiner.on(",").skipNulls().appendTo(clauseBuilder, values);
                    clauseBuilder.append("\"");
                    excludeclauses.add(clauseBuilder.toString());
                }
            }
            excludeString = "(\n" + Joiner.on("\nOR\n").skipNulls().join(excludeclauses) + "\n)\n";
        }

        Joiner.on("AND\n").skipNulls().appendTo(csdlBuilder, require1String, require2String);
        csdlBuilder.append("AND NOT\n" + excludeString);

        log.debug(csdlBuilder.toString());

        return csdlBuilder.toString();
    }
}
