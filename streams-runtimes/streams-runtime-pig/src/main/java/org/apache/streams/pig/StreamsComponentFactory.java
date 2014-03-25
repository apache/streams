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

    public static StreamsProcessor getProcessorInstance(Class<?> clazz, String... args) {

        Object object = null;
        try {
            int constructorStringCount = args.length;
            List<Class> constructorSignature;
            if( constructorStringCount == 0 )
                constructorSignature = Lists.newArrayList();
            else {
                constructorSignature = Lists.newArrayListWithCapacity(args.length);
                for (int i = 0; i < constructorStringCount; i++)
                    constructorSignature.add(String.class);
            }
            String[] constructorArgs = args;
            object = clazz.getConstructor(constructorSignature.toArray(new Class[args.length])).newInstance(constructorArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        StreamsProcessor processor = (StreamsProcessor) object;
        return processor;

    }
}
