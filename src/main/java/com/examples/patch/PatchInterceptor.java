package com.examples.patch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * JAX-RS reader interceptor supporting simple PATCH .
 */
@Provider
@Priority(Priorities.ENTITY_CODER)
public class PatchInterceptor implements ReaderInterceptor {

    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final Logger LOGGER = Logger.getLogger(PatchInterceptor.class.getName());

    @SuppressWarnings("unchecked")
    @Override
    public Object aroundReadFrom(ReaderInterceptorContext readerInterceptorContext) throws IOException {
        LOGGER.log(Level.INFO, "!-------------- PATCH Object Interceptor around readFrom()");
        if (!"application/patch+json".equals(readerInterceptorContext.getMediaType().toString())) {
            return readerInterceptorContext.proceed();
        }
        String body = "{\"error\":\"input could not be parsed\"}";
        try {
            body = getString(readerInterceptorContext.getInputStream());
            String input = convertInput(body);
            readerInterceptorContext.setInputStream(new ByteArrayInputStream(input.getBytes(CHARSET)));
        } catch (JsonMappingException ex) {
            LOGGER.log(Level.WARNING, "Unable to parse input from stream into a PATCH Object", ex);
            readerInterceptorContext.setInputStream(new ByteArrayInputStream(body.getBytes(CHARSET)));
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Unable to read input from stream", ex);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to parse ", e);
        }
        return readerInterceptorContext.proceed();
    }

    String convertInput(String body) throws IOException, JsonProcessingException {
        LOGGER.log(Level.INFO, "!-------------- PATCH Object Interceptor convertInput()");
        ObjectMapper mapper = new ObjectMapper();
        try {
            JSONPatchContainer[] json = mapper.readValue(body, JSONPatchContainer[].class);
            return mapper.writeValueAsString(Arrays.toString(json));
        } catch (JsonProcessingException jpe) {
            JSONPatchContainer json = mapper.readValue(body, JSONPatchContainer.class);
            return mapper.writeValueAsString(json);
        }
    }

    String getString(InputStream input) throws IOException {
        LOGGER.log(Level.INFO, "!-------------- PATCH Object Interceptor getString()");
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input, CHARSET))) {
            return buffer.lines().collect(Collectors.joining());
        } 
    }
}
