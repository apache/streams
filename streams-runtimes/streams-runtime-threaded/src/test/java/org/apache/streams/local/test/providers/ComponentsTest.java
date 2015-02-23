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
package org.apache.streams.local.test.providers;

import org.apache.streams.core.StreamsDatum;
import org.junit.Test;

import static org.junit.Assert.*;

public class ComponentsTest {

    @Test
    public void testShapeShifterProvider2() {
        ShapeShifterProvider shapeShifterProvider = new ShapeShifterProvider(1000, 100, 2);
        shapeShifterProvider.startStream();

        boolean checkedFirst = false;
        int numeric = 0;
        int strings = 0;

        for(StreamsDatum datum : shapeShifterProvider.readCurrent()) {

            if(!checkedFirst) {
                if(!(datum.getDocument() instanceof NumericMessageObject)) {
                    fail("First object must be numeric");
                }
                checkedFirst = true;
            }

            if(datum.getDocument() instanceof NumericMessageObject) {
                numeric++;
            }  else if(datum.getDocument() instanceof NumericStringMessageObject) {
                strings++;
            } else {
                fail("Should only produce these two objects");
            }
        }

        assertTrue("We checked the first doc", checkedFirst);
        assertEquals("Produced 50 Numeric", 50, numeric);
        assertEquals("Produced 50 Strings", 50, strings);
    }


    @Test
    public void testShapeShifterProvider10() {

        ShapeShifterProvider shapeShifterProvider = new ShapeShifterProvider(1000, 100, 10);
        shapeShifterProvider.startStream();

        boolean checkedFirst = false;
        int numeric = 0;
        int strings = 0;

        for(StreamsDatum datum : shapeShifterProvider.readCurrent()) {

            if(!checkedFirst) {
                if(!(datum.getDocument() instanceof NumericMessageObject)) {
                    fail("First object must be numeric");
                }
                checkedFirst = true;
            }

            if(datum.getDocument() instanceof NumericMessageObject) {
                numeric++;
            } else if(datum.getDocument() instanceof NumericStringMessageObject) {
                strings++;
            } else {
                fail("Should only produce these two objects");
            }
        }

        assertTrue("We checked the first doc", checkedFirst);
        assertEquals("Produced 50 Numeric", 50, numeric);
        assertEquals("Produced 50 Strings", 50, strings);
    }


    @Test
    public void testShapeShifterProvider20() {

        ShapeShifterProvider shapeShifterProvider = new ShapeShifterProvider(1000, 100, 20);
        shapeShifterProvider.startStream();

        boolean checkedFirst = false;
        int numeric = 0;
        int strings = 0;

        for(StreamsDatum datum : shapeShifterProvider.readCurrent()) {

            if(!checkedFirst) {
                if(!(datum.getDocument() instanceof NumericMessageObject))
                    fail("First object must be numeric");
                checkedFirst = true;
            }

            if(datum.getDocument() instanceof NumericMessageObject) {
                numeric++;
            } else if(datum.getDocument() instanceof NumericStringMessageObject) {
                strings++;
            } else {
                fail("Should only produce these two objects");
            }
        }

        assertTrue("We checked the first doc", checkedFirst);
        assertEquals("Produced 60 Numeric", 60, numeric);
        assertEquals("Produced 40 Strings", 40, strings);
    }
}