/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.webserver.jersey;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import io.helidon.common.http.DataChunk;
import io.helidon.common.http.Http;
import io.helidon.common.reactive.OutputStreamPublisher;
import io.helidon.common.reactive.ReactiveStreamsAdapter;
import io.helidon.webserver.ConnectionClosedException;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;

import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;

/**
 * The ResponseWriter.
 */
class ResponseWriter implements ContainerResponseWriter {

    private static final Logger LOGGER = Logger.getLogger(ResponseWriter.class.getName());

    private final OutputStreamPublisher publisher = new OutputStreamPublisher() {
        @Override
        public void write(byte[] b) throws IOException {
            try {
                super.write(b);
            } catch (ConnectionClosedException e) {
                throw new IOException("Cannot publish more bytes due to a connection close.", e);
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            try {
                super.write(b, off, len);
            } catch (ConnectionClosedException e) {
                throw new IOException("Cannot publish more bytes due to a connection close.", e);
            }
        }

        @Override
        public void write(int b) throws IOException {
            try {
                super.write(b);
            } catch (ConnectionClosedException e) {
                throw new IOException("Cannot publish more bytes due to a connection close.", e);
            }
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } catch (ConnectionClosedException e) {
                throw new IOException("Cannot close the connection because it's already closed.", e);
            }
        }

        @Override
        public void flush() throws IOException {
            try {
                super.flush();
            } catch (ConnectionClosedException e) {
                throw new IOException("Cannot flush on the connection because it's closed.", e);
            }
        }
    };

    private final ServerResponse res;
    private final ServerRequest req;
    private final CompletableFuture<Void> whenHandleFinishes;

    ResponseWriter(ServerResponse res, ServerRequest req, CompletableFuture<Void> whenHandleFinishes) {
        this.res = res;
        this.req = req;
        this.whenHandleFinishes = whenHandleFinishes;
    }

    @Override
    public OutputStream writeResponseStatusAndHeaders(long contentLength, ContainerResponse context)
            throws ContainerException {

        res.status(Http.ResponseStatus.create(context.getStatus(), context.getStatusInfo().getReasonPhrase()));

        if (contentLength >= 0) {
            res.headers().put(Http.Header.CONTENT_LENGTH, String.valueOf(contentLength));
        } else {
            if (400 != context.getStatus()) {
                res.headers().put(Http.Header.TRANSFER_ENCODING, "chunked");
            }
        }

        for (Map.Entry<String, List<String>> entry : context.getStringHeaders().entrySet()) {
            res.headers().put(entry.getKey(), entry.getValue());
        }

        // in case of SSE every response chunk needs to be flushed
        boolean doFlush = MediaType.SERVER_SENT_EVENTS_TYPE.isCompatible(context.getMediaType());

        res.send(
                ReactiveStreamsAdapter.publisherToFlow(
                        ReactiveStreamsAdapter.publisherFromFlow(publisher)
                                /*
                                The problem is that Jersey reuses the byte[] array it uses for writing
                                to the OutputStream.write() method. As such we must duplicate the array
                                because we need to return from the 'write()' method sooner than when
                                the underlying TCP server flushes the bytes.

                                Following fails when writing large amount of data to the response:
                                .map(byteBuffer -> new ResponseChunk(false, byteBuffer))));
                                 */
                                .map(byteBuffer -> {
                                    try {
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        WritableByteChannel ch = Channels.newChannel(stream);
                                        ch.write(byteBuffer);
                                        return DataChunk.create(doFlush, ByteBuffer.wrap(stream.toByteArray()));
                                    } catch (IOException e) {
                                        throw new IllegalStateException("this never happens", e);
                                    }
                                })));

        return publisher;
    }

    @Override
    public boolean suspend(long timeOut, TimeUnit timeUnit, TimeoutHandler timeoutHandler) {
        if (timeOut != 0) {
            throw new UnsupportedOperationException("Currently, time limited suspension is not supported!");
        }
        return true;
    }

    @Override
    public void setSuspendTimeout(long timeOut, TimeUnit timeUnit) throws IllegalStateException {
        throw new UnsupportedOperationException("Currently, extending the suspension time is not supported!");
    }

    @Override
    public void commit() {
        try {
            // Jersey doesn't close the OutputStream when there is no entity
            // as such the publisher needs to be closed from here ...
            // it is assumed it's possible to close the publisher, the OutputStream, multiple times
            publisher.close();
        } catch (IOException e) {
            // based on implementation of 'close', this never happens
            throw new IllegalStateException("Unexpected IO Exception received!", e);
        }
    }

    @Override
    public void failure(Throwable error) {
        LOGGER.finer(() -> "Jersey handling finished with an exception; message: " + error.getMessage());

        req.next(error);
    }

    @Override
    public boolean enableResponseBuffering() {
        // Jersey should not try to do the buffering
        return false;
    }
}
