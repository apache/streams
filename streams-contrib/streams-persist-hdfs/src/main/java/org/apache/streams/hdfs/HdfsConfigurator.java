package org.apache.streams.hdfs;

/*
 * #%L
 * streams-persist-hdfs
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 12/10/13.
 */
public class HdfsConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(HdfsConfigurator.class);

    private final static ObjectMapper mapper = new ObjectMapper();

    public static HdfsConfiguration detectConfiguration(Config hdfs) {
        String host = hdfs.getString("host");
        Long port = hdfs.getLong("port");
        String path = hdfs.getString("path");
        String user = hdfs.getString("user");
        String password = hdfs.getString("password");

        HdfsConfiguration hdfsConfiguration = new HdfsConfiguration();

        hdfsConfiguration.setHost(host);
        hdfsConfiguration.setPort(port);
        hdfsConfiguration.setPath(path);
        hdfsConfiguration.setUser(user);
        hdfsConfiguration.setPassword(password);

        return hdfsConfiguration;
    }

    public static HdfsReaderConfiguration detectReaderConfiguration(Config hdfs) {

        HdfsConfiguration hdfsConfiguration = detectConfiguration(hdfs);
        HdfsReaderConfiguration hdfsReaderConfiguration  = mapper.convertValue(hdfsConfiguration, HdfsReaderConfiguration.class);

        String readerPath = hdfs.getString("readerPath");

        hdfsReaderConfiguration.setReaderPath(readerPath);

        return hdfsReaderConfiguration;
    }

    public static HdfsWriterConfiguration detectWriterConfiguration(Config hdfs) {

        HdfsConfiguration hdfsConfiguration = detectConfiguration(hdfs);
        HdfsWriterConfiguration hdfsWriterConfiguration  = mapper.convertValue(hdfsConfiguration, HdfsWriterConfiguration.class);

        String writerPath = hdfs.getString("writerPath");

        hdfsWriterConfiguration.setWriterPath(writerPath);

        return hdfsWriterConfiguration;
    }

}
