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

package org.apache.streams.datasift.provider;

import com.datasift.client.stream.DeletedInteraction;
import com.datasift.client.stream.StreamEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.datasift.Datasift;
import org.apache.streams.datasift.DatasiftConfiguration;
import org.apache.streams.datasift.DatasiftWebhookData;
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
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

/**
 * {@code DatasiftPushProvider} is an implementation of the {@link org.apache.streams.core.StreamsProvider} interface, with
 * annotations that allow it to bind as jersey resources within streams-runtime-dropwizard.
 *
 * Whereas GenericWebhookResource outputs ObjectNode datums, DatasiftPushProvider outputs Datasift datums, with
 * metadata when the json_meta endpoint is used.
 */
@Resource
@Path("/streams/webhooks/datasift")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DatasiftPushProvider implements StreamsProvider {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatasiftPushProvider.class);

    private static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    protected Queue<StreamsDatum> providerQueue = new ConcurrentLinkedQueue<>();

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    private static Pattern newLinePattern = Pattern.compile("(\\r\\n?|\\n)", Pattern.MULTILINE);

    @POST
    @Path("json")
    public Response json(@Context HttpHeaders headers,
                         String body) {

        ObjectNode response = mapper.createObjectNode();

        StreamsDatum datum = new StreamsDatum(body);

        lock.writeLock().lock();
        ComponentUtils.offerUntilSuccess(datum, providerQueue);
        lock.writeLock().unlock();

        Boolean success = true;

        response.put("success", success);

        return Response.status(200).entity(response).build();

    }

    @POST
    @Path("json_new_line")
    public Response json_new_line(@Context HttpHeaders headers,
                                  String body) {

        ObjectNode response = mapper.createObjectNode();

        if (body.equalsIgnoreCase("{}")) {

            Boolean success = true;

            response.put("success", success);

            return Response.status(200).entity(response).build();
        }

        try {

            for( String item : Splitter.on(newLinePattern).split(body)) {
                StreamsDatum datum = new StreamsDatum(item);

                lock.writeLock().lock();
                ComponentUtils.offerUntilSuccess(datum, providerQueue);
                lock.writeLock().unlock();

            }

            Boolean success = true;

            response.put("success", success);

            return Response.status(200).entity(response).build();

        } catch (Exception e) {
            LOGGER.warn(e.toString(), e);

            Boolean success = false;

            response.put("success", success);

            return Response.status(500).entity(response).build();

        }

    }

    @POST
    @Path("json_meta")
    public Response json_meta(@Context HttpHeaders headers,
                              String body) {

        //log.debug(headers.toString(), headers);

        //log.debug(body.toString(), body);

        ObjectNode response = mapper.createObjectNode();

        if (body.equalsIgnoreCase("{}")) {

            Boolean success = true;

            response.put("success", success);

            return Response.status(200).entity(response).build();
        }

        try {

            DatasiftWebhookData objectWrapper = mapper.readValue(body, DatasiftWebhookData.class);

            for( Datasift item : objectWrapper.getInteractions()) {

                String json = mapper.writeValueAsString(item);

                StreamsDatum datum = new StreamsDatum(json);
                if( item.getInteraction() != null &&
                    !Strings.isNullOrEmpty(item.getInteraction().getId())) {
                    datum.setId(item.getInteraction().getId());
                }
                if( item.getInteraction() != null &&
                    item.getInteraction().getCreatedAt() != null) {
                    datum.setTimestamp(item.getInteraction().getCreatedAt());
                }
                Map<String, Object> metadata = Maps.newHashMap();
                metadata.put("hash", objectWrapper.getHash());
                metadata.put("hashType", objectWrapper.getHashType());
                metadata.put("id",objectWrapper.getId());

                if( item.getInteraction() != null &&
                        item.getInteraction().getTags() != null &&
                        item.getInteraction().getTags().size() > 0) {
                    metadata.put("tags", item.getInteraction().getTags());
                }

                datum.setMetadata(metadata);

                lock.writeLock().lock();
                ComponentUtils.offerUntilSuccess(datum, providerQueue);
                lock.writeLock().unlock();
            }

            Boolean success = true;

            response.put("success", success);

            return Response.status(200).entity(response).build();

        } catch (Exception e) {
            LOGGER.warn(e.toString(), e);
        }

        return Response.status(500).build();
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
