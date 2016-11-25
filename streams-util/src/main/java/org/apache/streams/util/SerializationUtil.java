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

package org.apache.streams.util;

import org.apache.commons.io.input.ClassLoaderObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * SerializationUtil contains methods for serializing, deserializing, and cloning
 * documents and tasks.
 */
public class SerializationUtil {

  /**
   * serialize Object as byte array.
   *
   * <p/>
   * BORROwED FROM APACHE STORM PROJECT
   *
   * @param obj Object
   * @return byte[]
   */
  public static byte[] serialize(Object obj) {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(obj);
      oos.close();
      return bos.toByteArray();
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  /**
   * deserialize byte array as Object.
   *
   * <p/>
   * BORROwED FROM APACHE STORM PROJECT
   *
   * @param serialized byte[]
   * @return Object
   */
  public static Object deserialize(byte[] serialized) {
    try {
      ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      ObjectInputStream ois = new ClassLoaderObjectInputStream(classLoader, bis);
      Object ret = ois.readObject();
      ois.close();
      return ret;
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    } catch (ClassNotFoundException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * clone Object by serialization.
   * @param obj Object
   * @param <T> type
   * @return cloned Object
   */
  public static <T> T cloneBySerialization(T obj) {
    if ( obj != null ) {
      return (T) deserialize(serialize(obj));
    } else {
      return null;
    }
  }
}
