/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.apache.streams.plugins;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;

import org.apache.juneau.annotation.Beanp;
import org.jsonschema2pojo.AbstractAnnotator;

/**
 * Add @BeanProperty annotations to getters, setters, anyGetter, and anySetter
 */
public class JuneauPojoAnnotator extends AbstractAnnotator {

  /**
   * Add the necessary annotation to associate a Java field with a
   * JSON property
   *
   * @param field
   *            the field that holds the value of the given JSON property
   * @param propertyName
   *            the name of the JSON property that this field holds
   */
  public void propertyField(JFieldVar field, JDefinedClass clazz,
                            String propertyName, JsonNode propertyNode) {
    field.annotate(Beanp.class).param("value", propertyName);
  }


  /**
   * Add the necessary annotation to mark a Java method as the getter for a
   * JSON property
   *
   * @param getter
   *            the method that will be used to get the value of the given
   *            JSON property
   * @param propertyName
   *            the name of the JSON property that this getter gets
   */
  public void propertyGetter(JMethod getter, String propertyName) {
    getter.annotate(Beanp.class).param("value", propertyName);
  }

  /**
   * Add the necessary annotation to mark a Java method as the setter for a
   * JSON property
   *
   * @param setter
   *            the method that will be used to set the value of the given
   *            JSON property
   * @param propertyName
   *            the name of the JSON property that this setter sets
   */
  public void propertySetter(JMethod setter, String propertyName) {
    setter.annotate(Beanp.class).param("value", propertyName);
  }

  /**
   * Add the necessary annotation to mark a Java method as the getter for
   * additional JSON property values that do not match any of the other
   * property names found in the bean.
   *
   * @param getter
   *            the method that will be used to get the values of additional
   *            properties
   */
  public void anyGetter(JMethod getter) {
    getter.annotate(Beanp.class).param("name", "*");
  }

  /**
   * Add the necessary annotation to mark a Java method as the setter for
   * additional JSON property values that do not match any of the other
   * property names found in the bean.
   *
   * @param setter
   *            the method that will be used to set the values of additional
   *            properties
   */
  public void anySetter(JMethod setter) {
    setter.annotate(Beanp.class).param("name", "*");
  }
}
