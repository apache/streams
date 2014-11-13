package org.apache.streams.data;

import org.apache.streams.data.ActivityConverter;

/**
 * Created by sblackmon on 11/13/14.
 */
public class ActivityConverterFactory {

    /**
     * Identifies a class that con convert this document to an activity
     *
     * @param converterClass known class of the converter
     * @return an appropriate ActivityConverter
     */
    public static ActivityConverter getInstance(Class converterClass) {

        ActivityConverter instance;
        try {
            instance = (ActivityConverter)converterClass.newInstance();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
        return instance;
    }

}
