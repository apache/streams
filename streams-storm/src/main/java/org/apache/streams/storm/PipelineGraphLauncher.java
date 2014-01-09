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

package org.apache.streams.storm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.util.RegexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sblackmon
 * Date: 9/20/13
 * Time: 11:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class PipelineGraphLauncher {

    private static final Logger log = LoggerFactory.getLogger(PipelineGraphLauncher.class);

    private static Config streamsConfiguration;

    private static List<Pair<String,Class>> topologies;

    private static List<Pair<String,Class>> resolveClasses(List<Pair<String,String>> topologyPairs) throws IOException, ClassNotFoundException {

        List<Pair<String,Class>> topologies = new ArrayList<Pair<String,Class>>();

        for( Pair<String,String> pair : topologyPairs ) {
            String topologyId = pair.getLeft();
            Class topologyClass = Class.forName(pair.getRight());
            topologies.add(new ImmutablePair(topologyId, topologyClass));
        }

        return topologies;
    }

    private static List<Pair<String,Class>> loadTopologiesFromPipelineTopologyListFile(File file) throws IOException, ClassNotFoundException {

        List<String> lines = IOUtils.readLines(FileUtils.openInputStream(file));
        String pattern = "^([\\w-]*)[\\s]*?([\\w.]*)$";

        List<Pair<String,String>> topologyPairs = RegexUtils.getTwoMatchedGroupsList(lines, pattern);

        topologies = resolveClasses(topologyPairs);

        for( Pair<String,String> pair : topologyPairs ) {
            String topologyId = pair.getLeft();
            Class topologyClass = Class.forName(pair.getRight());
            topologies.add(new ImmutablePair(topologyId, topologyClass));
        }

        return topologies;
    }

    private static List<Pair<String,Class>> loadTopologiesFromPipelineGraphFile(File file) throws IOException, ClassNotFoundException {

        List<String> lines = IOUtils.readLines(FileUtils.openInputStream(file));
        String pattern = "$([\\w-]*)\\s([\\w.)";

        List<Pair<String,String>> topologyPairs = RegexUtils.getTwoMatchedGroupsList(lines, pattern);

        topologies = resolveClasses(topologyPairs);

        return topologies;
    }

    public static boolean isLocal(String[] args) {
        if(args.length >= 1 && args[1].equals("deploy"))
            return false;
        else return true;
    }

    public static void main(String[] args) throws Exception {

        if(args.length < 3) {
            log.error("Not enough arguments");
            log.error("  storm {local,deploy} <pipelinedef>");
            return;
        }
        if(!args[1].equals("deploy")) {
            log.error("Not a deploy");
            log.error("  storm {local,deploy} <pipelinedef>");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();

        URL configFileUrl = PipelineGraphLauncher.class.getResource(args[0]);

        File configFile;
        try {
            configFile = new File(configFileUrl.toURI());
        } catch(URISyntaxException e) {
            configFile = new File(configFileUrl.getPath());
        }

        streamsConfiguration = StreamsConfigurator.config;

//        String pipelineIdentifier = streamsConfiguration.getPipeline();
//
//        for( Map.Entry<String, Object> moduleConfig : streamsConfiguration.getAdditionalProperties().entrySet()) {
//
//        }

//      each defined graph becomes a topology

//
//        URL pipelineFileUrl = PipelineGraphLauncher.class.getResource(args[1]);
//
//        File pipelineFile;
//        try {
//            pipelineFile = new File(pipelineFileUrl.toURI());
//        } catch(URISyntaxException e) {
//            pipelineFile = new File(pipelineFileUrl.getPath());
//        }
//
//        topologies = loadTopologiesFromPipelineTopologyListFile(pipelineFile);
//
//        for( Pair<String,Class> topology : topologies ) {
//            Class topologyClass = topology.getRight();
//
//            try {
//                Constructor ctor = topologyClass.getDeclaredConstructor(
//                    String.class,
//                    StreamsConfiguration.class);
//                ctor.setAccessible(true);
//                Object topologyObject = ctor.newInstance(pipelineIdentifier, streamsConfiguration);
//                Runnable runnable = (Runnable) topologyObject;
//                runnable.run();
//            } catch (InstantiationException x) {
//                log.warn(x.getMessage());
//                x.printStackTrace();
//            } catch (IllegalAccessException x) {
//                log.warn(x.getMessage());
//                x.printStackTrace();
//            } catch (InvocationTargetException x) {
//                log.warn(x.getMessage());
//                x.printStackTrace();
//            } catch (NoSuchMethodException x) {
//                log.warn(x.getMessage());
//                x.printStackTrace();
//
//                try {
//                    Constructor ctor = topologyClass.getDeclaredConstructor(
//                            String[].class);
//                    ctor.setAccessible(true);
//                    Object topologyObject = ctor.newInstance(args);
//                    Method main = topologyClass.getMethod("main", String[].class);
//                    main.invoke(topologyObject, args);
//                } catch (InstantiationException x2) {
//                    log.warn(x2.getMessage());
//                    x.printStackTrace();
//                } catch (IllegalAccessException x2) {
//                    log.warn(x2.getMessage());
//                    x.printStackTrace();
//                } catch (InvocationTargetException x2) {
//                    log.warn(x2.getMessage());
//                    x.printStackTrace();
//                } catch (NoSuchMethodException x2) {
//                    log.error(x2.getMessage());
//                    x.printStackTrace();
//                }
//            }
//        }
    }
}
