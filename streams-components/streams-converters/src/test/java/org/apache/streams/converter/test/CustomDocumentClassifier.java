/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package org.apache.streams.converter.test;

import org.apache.streams.data.DocumentClassifier;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Support class for
 * {@link org.apache.streams.converter.test.CustomActivityConverterProcessorTest}
 */
public class CustomDocumentClassifier implements DocumentClassifier {

    private ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    @Override
    public List<Class> detectClasses(Object document) {

        CustomType possibleMatchDocument;
        ObjectNode node = null;

        List<Class> classes = new ArrayList<>();

        if( document instanceof String ) {
            classes.add(String.class);
            try {
                possibleMatchDocument = this.mapper.readValue((String)document, CustomType.class);
                if(possibleMatchDocument != null && possibleMatchDocument.getTest() != null)
                    classes.add(CustomType.class);
            } catch (IOException e1) {
                try {
                    node = this.mapper.readValue((String)document, ObjectNode.class);
                    classes.add(ObjectNode.class);
                } catch (IOException ignored) { }
            }
        } else if( document instanceof ObjectNode ){
            classes.add(ObjectNode.class);
            possibleMatchDocument = this.mapper.convertValue(document, CustomType.class);
            if(possibleMatchDocument != null && possibleMatchDocument.getTest() != null) {
                classes.add(CustomType.class);
            }
        } else {
            classes.add(document.getClass());
        }

        return classes;
    }
}
