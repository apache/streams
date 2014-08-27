package org.apache.streams.util;

import java.io.*;

/**
 * Created by rebanks on 2/18/14.
 */
public class SerializationUtil {

    /**
     * BORROwED FROM APACHE STORM PROJECT
     * @param obj
     * @return
     */
    public static byte[] serialize(Object obj) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.close();
            return bos.toByteArray();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * BORROwED FROM APACHE STORM PROJECT
     * @param serialized
     * @return
     */
    public static Object deserialize(byte[] serialized) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Object ret = ois.readObject();
            ois.close();
            return ret;
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        } catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object cloneBySerialization(Object obj) {
        return deserialize(serialize(obj));
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] serialized, Class<T> klass) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Object ret = ois.readObject();
            ois.close();
            if(klass.isInstance(ret))
                return (T)ret;
            else
                throw new RuntimeException("Serialization is not of correct type");

        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        } catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public static <T> T cloneBySerialization(T obj, Class<T> klass) {
        return deserialize(serialize(obj), klass);
    }
}
