package org.apache.streams.local.test.providers;

import org.apache.streams.core.StreamsDatum;
import org.junit.Test;

import static org.junit.Assert.*;

public class ComponentsTest {

    @Test
    public void testShapeShifterProvider2() {
        ShapeShifterProvider shapeShifterProvider = new ShapeShifterProvider(1000, 100, 2);

        boolean checkedFirst = false;
        int numeric = 0;
        int strings = 0;

        for(StreamsDatum datum : shapeShifterProvider.readCurrent()) {

            if(!checkedFirst) {
                if(!(datum.getDocument() instanceof NumericMessageObject))
                    fail("First object must be numeric");
                checkedFirst = true;
            }

            if(datum.getDocument() instanceof NumericMessageObject)
                numeric++;
            else if(datum.getDocument() instanceof NumericStringMessageObject)
                strings++;
            else
                fail("Should only produce these two objects");
        }

        assertTrue("We checked the first doc", checkedFirst);
        assertEquals("Produced 50 Numeric", 50, numeric);
        assertEquals("Produced 50 Strings", 50, strings);
    }


    @Test
    public void testShapeShifterProvider10() {

        ShapeShifterProvider shapeShifterProvider = new ShapeShifterProvider(1000, 100, 10);

        boolean checkedFirst = false;
        int numeric = 0;
        int strings = 0;

        for(StreamsDatum datum : shapeShifterProvider.readCurrent()) {

            if(!checkedFirst) {
                if(!(datum.getDocument() instanceof NumericMessageObject))
                    fail("First object must be numeric");
                checkedFirst = true;
            }

            if(datum.getDocument() instanceof NumericMessageObject)
                numeric++;
            else if(datum.getDocument() instanceof NumericStringMessageObject)
                strings++;
            else
                fail("Should only produce these two objects");
        }

        assertTrue("We checked the first doc", checkedFirst);
        assertEquals("Produced 50 Numeric", 50, numeric);
        assertEquals("Produced 50 Strings", 50, strings);
    }


    @Test
    public void testShapeShifterProvider20() {

        ShapeShifterProvider shapeShifterProvider = new ShapeShifterProvider(1000, 100, 20);

        boolean checkedFirst = false;
        int numeric = 0;
        int strings = 0;

        for(StreamsDatum datum : shapeShifterProvider.readCurrent()) {

            if(!checkedFirst) {
                if(!(datum.getDocument() instanceof NumericMessageObject))
                    fail("First object must be numeric");
                checkedFirst = true;
            }

            if(datum.getDocument() instanceof NumericMessageObject)
                numeric++;
            else if(datum.getDocument() instanceof NumericStringMessageObject)
                strings++;
            else
                fail("Should only produce these two objects");
        }

        assertTrue("We checked the first doc", checkedFirst);
        assertEquals("Produced 60 Numeric", 60, numeric);
        assertEquals("Produced 40 Strings", 40, strings);
    }

}
