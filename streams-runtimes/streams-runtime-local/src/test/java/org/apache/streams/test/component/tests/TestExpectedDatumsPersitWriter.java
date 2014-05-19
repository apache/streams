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
import org.apache.streams.test.component.ExpectedDatumsPersistWriter;
import org.apache.streams.test.component.StringToDocumentConverter;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by rebanks on 2/28/14.
 */
public class TestExpectedDatumsPersitWriter {

    private static final StreamsDatum[] INPUT_DATUMS = new StreamsDatum[] {
            new StreamsDatum("Document1"),
            new StreamsDatum("Document2"),
            new StreamsDatum("Document3"),
            new StreamsDatum("Document4")
//            Uncomment to prove failures occur, or comment out a datum above
//            ,new StreamsDatum("Document5")
    };

    @Test
    public void testExpectedDatumsPersistWriterFileName() {
        testDatums(new ExpectedDatumsPersistWriter(new StringToDocumentConverter(), "/TestFile.txt"));
    }



    private void testDatums(ExpectedDatumsPersistWriter writer) {
        writer.prepare(null);
        for(StreamsDatum datum : INPUT_DATUMS) {
            writer.write(datum);
        }
        writer.cleanUp();
    }

}
