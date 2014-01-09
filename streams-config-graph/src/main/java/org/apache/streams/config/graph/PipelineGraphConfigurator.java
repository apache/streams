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

package org.apache.streams.config.graph;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Created with IntelliJ IDEA.
 * User: sblackmon
 * Date: 9/23/13
 * Time: 10:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class PipelineGraphConfigurator {

    public static Graph pipeline = loadPipeline();

    private static Graph loadPipeline() {

        Graph pipeline = new SingleGraph("pipelines");

        // this class looks for any pipelines specified with a graph definition
        // each is loaded into the execution graph
        // the application is responsible for launching each
        Enumeration<URL> pipelineFiles;
        try {
            pipelineFiles = PipelineGraphConfigurator.class.getClassLoader().getResources("*.dot");

            for( URL pipelineFile : Collections.list(pipelineFiles) ) {
                File file = new File(pipelineFile.toURI());
                String filePath = file.getAbsolutePath();
                FileSource fileSource = FileSourceFactory.sourceFor(filePath);

                fileSource.addSink(pipeline);

                try {
                    fileSource.begin(filePath);

                    while (fileSource.nextEvents()) {
                        // Optionally some code here ...
                    }
                } catch( IOException e) {
                    e.printStackTrace();
                }

                try {
                    fileSource.end();
                } catch( IOException e) {
                    e.printStackTrace();
                } finally {
                    fileSource.removeSink(pipeline);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return pipeline;
    }
}
