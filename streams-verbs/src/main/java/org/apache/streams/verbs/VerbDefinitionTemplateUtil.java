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

package org.apache.streams.verbs;

import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;

import org.apache.commons.lang3.StringUtils;
import org.stringtemplate.v4.ST;

/**
 * Transforms VerbDefinition templates into readable strings.
 */
public class VerbDefinitionTemplateUtil {

  /**
   * Transform Activity into readable string using ObjectCombination title.
   * @param activity Activity
   * @param objectCombination ObjectCombination
   * @return String
   */
  public static String asString(Activity activity, ObjectCombination objectCombination) {

    return asString("*", activity, objectCombination);

  }

  /**
   * Transform Activity into readable string using ObjectCombination title and specified language.
   * @param language language
   * @param activity Activity
   * @param objectCombination ObjectCombination
   * @return String
   */
  public static String asString(String language, Activity activity, ObjectCombination objectCombination) {

    String template = (String) objectCombination.getTemplates().getAdditionalProperties().get(language);
    template = template.replace('{', '<');
    template = template.replace('}', '>');
    ST st = new ST(template);
    st.add("actor", displayName(activity.getActor()));
    st.add("provider", displayName(activity.getProvider()));
    st.add("object", displayName(activity.getObject()));
    st.add("target", displayName(activity.getTarget()));

    return st.render();
  }

  /**
   * Readable display Name for ActivityObject.
   * @param activityObject ActivityObject
   * @return displayName
   */
  public static String displayName(ActivityObject activityObject) {
    if ( activityObject == null ) {
      return "";
    } else if (StringUtils.isNotBlank(activityObject.getDisplayName())) {
      return activityObject.getDisplayName();
    } else if (StringUtils.isNotBlank(activityObject.getObjectType())) {
      return activityObject.getObjectType();
    } else if (StringUtils.isNotBlank(activityObject.toString())) {
      return activityObject.toString();
    } else {
      return "";
    }
  }
}
