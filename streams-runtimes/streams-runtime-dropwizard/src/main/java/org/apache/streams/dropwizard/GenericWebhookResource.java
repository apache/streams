package org.apache.streams.dropwizard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Splitter;
import com.google.common.collect.Queues;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResource;
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

@Resource
@Path("/streams/webhooks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GenericWebhookResource implements StreamsProvider, StreamsResource {

    public GenericWebhookResource() {
    }

    private static final Logger log = LoggerFactory
            .getLogger(GenericWebhookResource.class);

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
            log.warn(e.toString(), e);

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

            GenericWebhookData objectWrapper = mapper.readValue(body, GenericWebhookData.class);

            for( ObjectNode item : objectWrapper.getData()) {

                String json = mapper.writeValueAsString(item);

                StreamsDatum datum = new StreamsDatum(json);

                lock.writeLock().lock();
                ComponentUtils.offerUntilSuccess(datum, providerQueue);
                lock.writeLock().unlock();
            }

            Boolean success = true;

            response.put("success", success);

            return Response.status(200).entity(response).build();

        } catch (Exception e) {
            log.warn(e.toString(), e);
        }

        return Response.status(500).build();
    }

    public List<ObjectNode> getData(GenericWebhookData wrapper) {
        return wrapper.getData();
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