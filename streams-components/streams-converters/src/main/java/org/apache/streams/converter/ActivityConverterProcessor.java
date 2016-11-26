/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package org.apache.streams.converter;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.core.util.DatumUtils;
import org.apache.streams.pojo.json.Activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * ActivityConverterProcessor is a utility processor for converting any datum document
 * to an Activity.
 *
 * <p/>
 * By default it will handle string json and objectnode representation of existing Activities,
 * translating them into the POJO representation(s) preferred by each registered/detected
 * ActivityConverter.
 *
 * <p/>
 * To use this capability without a dedicated stream processor, just use ActivityConverterUtil.
 */
public class ActivityConverterProcessor implements StreamsProcessor {

  public static final String STREAMS_ID = "ActivityConverterProcessor";

  private static final Logger LOGGER = LoggerFactory.getLogger(ActivityConverterProcessor.class);

  private ActivityConverterUtil converterUtil;

  private ActivityConverterProcessorConfiguration configuration;

  public ActivityConverterProcessor() {
  }

  public ActivityConverterProcessor(ActivityConverterProcessorConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public List<StreamsDatum> process(StreamsDatum entry) {

    List<StreamsDatum> result = new LinkedList<>();
    Object document = entry.getDocument();

    try {

      // first determine which classes this document might actually be
      List<Activity> activityList = converterUtil.convert(document);

      for (Activity activity : activityList) {
        StreamsDatum datum = DatumUtils.cloneDatum(entry);
        datum.setId(activity.getId());
        datum.setDocument(activity);
        result.add(datum);
      }

    } catch (Exception ex) {
      LOGGER.warn("General exception in process! " + ex.getMessage());
    }

    return result;

  }

  @Override
  public void prepare(Object configurationObject) {
    if (configurationObject instanceof ActivityConverterProcessorConfiguration) {
      converterUtil = ActivityConverterUtil.getInstance((ActivityConverterProcessorConfiguration) configurationObject);
    } else {
      converterUtil = ActivityConverterUtil.getInstance();
    }
  }

  @Override
  public void cleanUp() {

  }

}
