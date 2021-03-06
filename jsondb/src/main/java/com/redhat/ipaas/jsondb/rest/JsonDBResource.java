/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.ipaas.jsondb.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.function.Consumer;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.redhat.ipaas.jsondb.GetOptions;
import com.redhat.ipaas.jsondb.ImmutableGetOptions;
import com.redhat.ipaas.jsondb.JsonDB;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Provides a REST API to read/update a Key/Value database presented to the user a
 * single large persistent JSON tree/document.  You can read or update subsets of the
 * the document by specifying the path to the nodes in the JSON document that your
 * interested in working with.
 *
 * This API aims to be very similar Firebase REST API:
 * https://firebase.google.com/docs/database/rest/start
 */
@Path("/jsondb")
public class JsonDBResource {
    public static final String APPLICATION_JAVASCRIPT = "application/javascript";
    private final JsonDB jsondb;

    public JsonDBResource(JsonDB jsondb) {
        this.jsondb = jsondb;
    }

    @Produces({APPLICATION_JSON, APPLICATION_JAVASCRIPT})
    @Path("/{path: .*}.json")
    @GET
    public Response get(
        @PathParam("path") String path,
        @QueryParam("print") String print,
        @QueryParam("shallow") Boolean shallow,
        @QueryParam("callback") String callback
    ) {
        ImmutableGetOptions.Builder optionsBuilder = GetOptions.builder();
        if ("pretty".equals(print)) {
            optionsBuilder.prettyPrint(true);
        } else if ("silent".equals(print)) {
            if( jsondb.exists(path) ) {
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
        if( shallow!=null ) {
            optionsBuilder.prettyPrint(true);
        }

        String contentType = APPLICATION_JSON;
        if( callback!=null ) {
            contentType = APPLICATION_JAVASCRIPT;
            optionsBuilder.callback(callback);
        }

        Consumer<OutputStream> stream = jsondb.getAsStreamingOutput(path, optionsBuilder.build());
        if( stream == null ) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        StreamingOutput streamingOutput = x-> stream.accept(x);
        return Response.ok(streamingOutput).header(CONTENT_TYPE, contentType).build();
    }

    @Path("/{path: .*}.json")
    @Consumes(APPLICATION_JSON)
    @PUT
    public void set(@PathParam("path") String path, InputStream body) throws IOException {
        jsondb.set(path, body);
    }

    @Path("/{path: .*}.json")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @POST
    public HashMap<String, String> push(@PathParam("path") String path, InputStream body) throws IOException {
        HashMap<String, String> result = new HashMap<>();
        result.put("name", jsondb.push(path, body));
        return result;
    }


    @Path("/{path: .*}.json")
    @Consumes(APPLICATION_JSON)
    @DELETE
    public Response delete(@PathParam("path") String path) throws IOException {
        Response.Status status;
        if( jsondb.delete(path) ) {
            status = Response.Status.NO_CONTENT;
        } else {
            status = Response.Status.NOT_FOUND;
        }
        return Response.status(status).build();
    }
}
