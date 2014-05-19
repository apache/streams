package org.apache.streams.test.component.tests;

/*
 * #%L
 * streams-runtime-local
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

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.test.component.FileReaderProvider;
import org.apache.streams.test.component.StringToDocumentConverter;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Created by rebanks on 2/28/14.
 */
public class TestFileReaderProvider {


    @Test
    public void testFileReaderProviderFileName() {
        String fileName = "/TestFile.txt";
        FileReaderProvider provider = new FileReaderProvider(fileName, new StringToDocumentConverter());
        provider.prepare(null);
        StreamsResultSet resultSet = provider.readCurrent();
        int count = 0;
        for(StreamsDatum datum : resultSet) {
            ++count;
        }
        assertEquals(4, count);
        provider.cleanUp();
    }






}
