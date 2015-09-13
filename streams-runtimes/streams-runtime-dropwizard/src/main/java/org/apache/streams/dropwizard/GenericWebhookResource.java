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

package org.apache.streams.dropwizard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Splitter;
import com.google.common.collect.Queues;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.util.ComponentUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

/**
 * GenericWebhookResource provides basic webhook connectivity.
 *
 * Add processors / persistWriters that read from "GenericWebhookResource" to
 * consume data posted to streams.
 */
@Resource
@Path("/streams/webhooks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GenericWebhookResource implements StreamsProvider {

    public static final String STREAMS_ID = "GenericWebhookResource";

    public GenericWebhookResource() {
    }

    private static final Logger log = LoggerFactory
            .getLogger(GenericWebhookResource.class);

    private static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    protected Queue<StreamsDatum> providerQueue = new ConcurrentLinkedQueue<>();

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    private static Pattern newLinePattern = Pattern.compile("(\\r\\n?|\\n)", Pattern.MULTILINE);

    @Override
    public String getId() {
        return STREAMS_ID;
    }

    @POST
    @Path("json")
    public Response json(@Context HttpHeaders headers,
                                  String body) {

        ObjectNode response = mapper.createObjectNode();
        int responseCode = Response.Status.BAD_REQUEST.getStatusCode();

        try {
            ObjectNode item = mapper.readValue(body, ObjectNode.class);

            StreamsDatum datum = new StreamsDatum(body);

            lock.writeLock().lock();
            ComponentUtils.offerUntilSuccess(datum, providerQueue);
            lock.writeLock().unlock();

            Boolean success = true;

            response.put("success", success);

            responseCode = Response.Status.OK.getStatusCode();

        } catch (Exception e) {
            log.warn(e.toString(), e);

            Boolean success = false;

            response.put("success", success);
            responseCode = Response.Status.BAD_REQUEST.getStatusCode();

        } finally {
            return Response.status(responseCode).entity(response).build();

        }
    }

    @POST
    @Path("json_new_line")
    public Response json_new_line(@Context HttpHeaders headers,
                                           String body) {

        ObjectNode response = mapper.createObjectNode();
        int responseCode = Response.Status.BAD_REQUEST.getStatusCode();

        if (body.equalsIgnoreCase("{}")) {

            Boolean success = true;

            response.put("success", success);
            responseCode = Response.Status.OK.getStatusCode();
            return Response.status(responseCode).entity(response).build();
        }

        try {

            for( String line : Splitter.on(newLinePattern).split(body)) {
                ObjectNode item = mapper.readValue(line, ObjectNode.class);

                StreamsDatum datum = new StreamsDatum(item);

                lock.writeLock().lock();
                ComponentUtils.offerUntilSuccess(datum, providerQueue);
                lock.writeLock().unlock();

            }

            Boolean success = true;

            response.put("success", success);
            responseCode = Response.Status.OK.getStatusCode();

        } catch (Exception e) {
            log.warn(e.toString(), e);

            Boolean success = false;

            response.put("success", success);
            responseCode = Response.Status.BAD_REQUEST.getStatusCode();

        } finally {
            return Response.status(responseCode).entity(response).build();

        }

    }

    @POST
    @Path("json_meta")
    public Response json_meta(@Context HttpHeaders headers,
                                       String body) {

        ObjectNode response = mapper.createObjectNode();
        int responseCode = Response.Status.BAD_REQUEST.getStatusCode();

        if (body.equalsIgnoreCase("{}")) {

            Boolean success = true;

            response.put("success", success);
            responseCode = Response.Status.OK.getStatusCode();

            return Response.status(responseCode).entity(response).build();
        }

        try {

            GenericWebhookData objectWrapper = mapper.readValue(body, GenericWebhookData.class);

            for( ObjectNode item : objectWrapper.getData()) {

                StreamsDatum datum = new StreamsDatum(item);

                lock.writeLock().lock();
                ComponentUtils.offerUntilSuccess(datum, providerQueue);
                lock.writeLock().unlock();
            }

            Boolean success = true;

            response.put("success", success);
            responseCode = Response.Status.OK.getStatusCode();

        } catch (Exception e) {
            log.warn(e.toString(), e);

            Boolean success = false;

            response.put("success", success);
            responseCode = Response.Status.BAD_REQUEST.getStatusCode();
        } finally {
            return Response.status(responseCode).entity(response).build();
        }

    }

    @Override
    public void startStream() {
        return;
    }

    @Override
    public StreamsResultSet readCurrent() {

        StreamsResultSet current;

        lock.writeLock().lock();
        current = new StreamsResultSet(Queues.newConcurrentLinkedQueue(providerQueue));
        providerQueue.clear();
        lock.writeLock().unlock();

        return current;

    }

    @Override
    public StreamsResultSet readNew(BigInteger sequence) {
        return null;
    }

    @Override
    public StreamsResultSet readRange(DateTime start, DateTime end) {
        return null;
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public void prepare(Object configurationObject) {

    }

    @Override
    public void cleanUp() {

    }

}