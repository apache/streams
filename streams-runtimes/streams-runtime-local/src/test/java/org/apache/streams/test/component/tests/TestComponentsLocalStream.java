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

import org.apache.streams.local.builders.LocalStreamBuilder;
import org.apache.streams.test.component.ExpectedDatumsPersistWriter;
import org.apache.streams.test.component.FileReaderProvider;
import org.apache.streams.test.component.StringToDocumentConverter;
import org.junit.Test;

/**
 * Created by rebanks on 2/28/14.
 */
public class TestComponentsLocalStream {

    @Test
    public void testLocalStreamWithComponent() {
        LocalStreamBuilder builder = new LocalStreamBuilder();
        builder.newReadCurrentStream("provider", new FileReaderProvider("/TestFile.txt",
                                                                        new StringToDocumentConverter()));
        builder.addStreamsPersistWriter("writer", new ExpectedDatumsPersistWriter(new StringToDocumentConverter(),
                "/TestFile.txt"), 1, "provider")
        .start();
    }
}
