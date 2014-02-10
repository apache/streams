package org.apache.streams.hdfs;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 12/10/13.
 */
public class HdfsConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(HdfsConfigurator.class);

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

}
