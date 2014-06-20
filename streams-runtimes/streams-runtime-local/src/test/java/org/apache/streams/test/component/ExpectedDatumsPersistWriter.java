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

package org.apache.streams.test.component;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by rebanks on 2/27/14.
 */
public class ExpectedDatumsPersistWriter implements StreamsPersistWriter{

    private StreamsDatumConverter converter;
    private String fileName;
    private List<StreamsDatum> expectedDatums;
    private int counted = 0;
    private int expectedSize = 0;

    public ExpectedDatumsPersistWriter(StreamsDatumConverter converter, String filePathInResources) {
        this.converter = converter;
        this.fileName = filePathInResources;
    }



    @Override
    public void write(StreamsDatum entry) {
        int index = this.expectedDatums.indexOf(entry);
        assertNotEquals("Datum not expected. "+entry.toString(), -1, index);
        this.expectedDatums.remove(index);
        ++this.counted;
    }

    @Override
    public void prepare(Object configurationObject) {
        Scanner scanner = new Scanner(ExpectedDatumsPersistWriter.class.getResourceAsStream(this.fileName));
        this.expectedDatums = new LinkedList<StreamsDatum>();
        while(scanner.hasNextLine()) {
            this.expectedDatums.add(this.converter.convert(scanner.nextLine()));
        }
        this.expectedSize = this.expectedDatums.size();
    }

    @Override
    public void cleanUp() {
        assertEquals("Did not received the expected number of StreamsDatums", this.expectedSize, this.counted);
    }
}
