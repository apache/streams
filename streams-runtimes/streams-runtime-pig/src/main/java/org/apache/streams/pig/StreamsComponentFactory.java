package org.apache.streams.pig;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang.ArrayUtils;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.data.ActivitySerializer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by sblackmon on 3/25/14.
 */
public class StreamsComponentFactory {

    public static ActivitySerializer getSerializerInstance(Class<?> serializerClazz) {

        Object object = null;
        try {
            object = serializerClazz.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Preconditions.checkNotNull(object);

        ActivitySerializer serializer = (ActivitySerializer) object;

        return serializer;

    }

    public static StreamsProcessor getProcessorInstance(Class<?> processorClazz) {

        Object object = null;
        try {
            object = processorClazz.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        StreamsProcessor processor = (StreamsProcessor) object;
        return processor;

    }
}
