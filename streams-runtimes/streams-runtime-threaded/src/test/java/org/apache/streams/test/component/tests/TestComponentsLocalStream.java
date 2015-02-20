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
package org.apache.streams.test.component.tests;

import org.apache.streams.threaded.builders.ThreadedStreamBuilder;
import org.apache.streams.test.component.ExpectedDatumsPersistWriter;
import org.apache.streams.test.component.FileReaderProvider;
import org.apache.streams.test.component.StringToDocumentConverter;
import org.junit.Test;

/**
 * test with realistic readers and writers
 */
public class TestComponentsLocalStream {

    @Test
    public void testLocalStreamWithComponent() {
        new ThreadedStreamBuilder()
            .newReadCurrentStream("provider", new FileReaderProvider("/TestFile.txt", new StringToDocumentConverter()))
            .addStreamsPersistWriter("writer", new ExpectedDatumsPersistWriter(new StringToDocumentConverter(), "/TestFile.txt"), 1, "provider")
            .start();
    }
}
