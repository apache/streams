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
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.joda.time.DateTime;

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
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * FOR TESTING PURPOSES ONLY.
 *
 * The StreamProvider reads from a File or InputStream.  Each line of the file will be emitted as the document of a
 * streams datum.
 *
 */
public class FileReaderProvider implements StreamsProvider {

    private String fileName;
    private InputStream inStream;
    private Scanner scanner;
    private StreamsDatumConverter converter;
    private AtomicBoolean running = new AtomicBoolean(true);

    public FileReaderProvider(String filePathInResources, StreamsDatumConverter converter) {
        this.fileName = filePathInResources;
        this.converter = converter;
    }

    @Override
    public void startStream() {

    }

    @Override
    public StreamsResultSet readCurrent() {
        return new ResultSet();
    }

    @Override
    public StreamsResultSet readNew(BigInteger sequence) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StreamsResultSet readRange(DateTime start, DateTime end) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    @Override
    public void prepare(Object configurationObject) {
        this.scanner = new Scanner(FileReaderProvider.class.getResourceAsStream(this.fileName));
    }

    @Override
    public void cleanUp() {
        this.scanner.close();
        this.running.set(false);
    }

    private class ResultSet extends StreamsResultSet {

        public ResultSet() {
            super(null);
        }


        @Override
        public Iterator<StreamsDatum> iterator() {
            return new FileProviderIterator();
        }

        private class FileProviderIterator implements Iterator<StreamsDatum> {
            @Override
            public boolean hasNext() {
                return scanner.hasNextLine();
            }

            @Override
            public StreamsDatum next() {
                return converter.convert(scanner.nextLine());
            }

            @Override
            public void remove() {

            }
        }
    }
}