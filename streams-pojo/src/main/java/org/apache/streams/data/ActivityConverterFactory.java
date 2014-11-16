package org.apache.streams.data;

/**
 * Instantiates a class that con convert a type of document to an activity
 */
 public class ActivityConverterFactory {

    /**
     * Instantiates a class that con convert this type of document to an activity
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
