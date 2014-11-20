package org.apache.streams.core;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

/**
 * Created by sblackmon on 11/20/14.
 */
public interface StreamsResource {

    public Response json(HttpHeaders headers, String body);

    public Response json_new_line(HttpHeaders headers, String body);

    public Response json_meta(HttpHeaders headers, String body);

}
