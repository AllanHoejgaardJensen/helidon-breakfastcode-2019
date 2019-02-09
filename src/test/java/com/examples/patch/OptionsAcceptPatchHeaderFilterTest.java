package com.examples.patch;

import com.examples.patch.OptionsAcceptPatchHeaderFilter;
import org.junit.jupiter.api.Test;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OptionsAcceptPatchHeaderFilterTest {

    @Test
    public void testFilterWithCorrectHeaderInfo() throws IOException {
        OptionsAcceptPatchHeaderFilter oaphf = new OptionsAcceptPatchHeaderFilter();
        ContainerResponseContext responseContext = mock(ContainerResponseContext.class);
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getMethod()).thenReturn("OPTIONS");
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap();
        headers.add("Accept-Patch","application/patch+json");
        when(responseContext.getHeaders()).thenReturn(headers);
        oaphf.filter(requestContext, responseContext);
        assertEquals(1, headers.size());
    }

    @Test
    public void testFilterWithInCorrectHeaderInfo() throws IOException {
        OptionsAcceptPatchHeaderFilter oaphf = new OptionsAcceptPatchHeaderFilter();
        ContainerResponseContext responseContext = mock(ContainerResponseContext.class);
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getMethod()).thenReturn("OPTIONS");
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap();
        headers.add("No-Accept-Patch","application/patch+json");
        when(responseContext.getHeaders()).thenReturn(headers);
        oaphf.filter(requestContext, responseContext);
        assertEquals(2, headers.size());
        assertTrue(headers.containsKey("Accept-Patch"));
        assertTrue(headers.containsKey("No-Accept-Patch"));
    }
}
