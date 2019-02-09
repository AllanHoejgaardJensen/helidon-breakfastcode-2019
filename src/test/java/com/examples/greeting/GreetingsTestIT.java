package com.examples.greeting;

import io.helidon.microprofile.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.*;

public class GreetingsTestIT {
    private Server srv;


    private static Server server;

    @BeforeAll
    public static void setupServer() {
        server = Service.startServer();
    }

    @Test
    public void testGetList() {
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(getConnectionString("/greetings"))
                .request()
                .accept("application/json")
                .acceptLanguage("da")
                .get(Response.class);
        assertEquals(200, response.getStatus());
        response = client
                .target(getConnectionString("/greetings"))
                .request()
                .accept("application/notype")
                .acceptLanguage("da")
                .get(Response.class);
        assertEquals(406, response.getStatus());
        response = client
                .target(getConnectionString("/greetings"))
                .request()
                .get(Response.class);
        assertEquals(415, response.getStatus());
        response = client
                .target(getConnectionString("/representation"))
                .request()
                .get(Response.class);
        assertEquals(404, response.getStatus());
        response = client
                .target(getConnectionString("/greetings"))
                .request()
                .accept("application/problem+json")
                .acceptLanguage("da")
                .get(Response.class);
        assertEquals(406, response.getStatus());
        response = client
                .target(getConnectionString("/greetings/grrrr"))
                .request()
                .accept("application/angry+json")
                .acceptLanguage("da")
                .get(Response.class);
        assertEquals(406, response.getStatus());
        response = client
                .target(getConnectionString("/greetings/grrrr"))
                .request()
                .accept("application/json")
                .acceptLanguage("da")
                .get(Response.class);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testGetDynamicGreetingsList() {
        Client client = ClientBuilder.newClient();

        Response response = client.target(getConnectionString("/greetings")).request().accept("application/hal+json").acceptLanguage("da").get(Response.class);
        assertEquals(200, response.getStatus());
        String msg = response.readEntity(String.class);
        assertTrue(msg.contains("{\"href\":"));
        assertTrue(msg.contains("greetings/hallo\","));
        assertTrue(msg.contains("\"title\":\"Dansk Hilsen Hallo\"}"));
        assertEquals("application/hal+json;p=greetings;v=2", response.getMediaType().toString());
        assertNotNull(response.getHeaderString("etag"));
        String initialETag = response.getHeaderString("etag");

        String entity = "{\"greeting\":\"Hejsa!\",\"language\":\"Dansk\",\"country\":\"Danmark\",\"native\":{\"language\":\"Dansk\",\"country\":\"Danmark\"},\"_links\":{\"self\":{\"href\":\"greetings/hejsa\",\"title\":\"Dansk Hilsen Hejsa\"}}}";
        response = client.target(getConnectionString("/greetings/hejsa")).request().accept("application/hal+json").acceptLanguage("da").get(Response.class);
        assertEquals(404, response.getStatus());
        response = client
                .target(getConnectionString("/greetings"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .post(Entity.json(entity));
        assertEquals(201, response.getStatus());
        assertTrue(response.getHeaderString("Location").contains("greetings/hejsa"));
        response = client.target(getConnectionString("/greetings/hejsa")).request().accept("application/hal+json").acceptLanguage("da").get(Response.class);
        assertEquals(200, response.getStatus());

        response = client.target(getConnectionString("/greetings")).request().accept("application/hal+json").acceptLanguage("da").get(Response.class);
        String msgAfter = response.readEntity(String.class);
        assertTrue(msgAfter.length() > msg.length());
        assertFalse(msg.contains("Hejsa"));
        assertTrue(msgAfter.contains("Hejsa"));
        assertNotNull(response.getHeaderString("etag"));
        String resultingETag = response.getHeaderString("etag");
        assertFalse(resultingETag.equals(initialETag));
    }

    @Test
    public void testNotModifiedList() {
        Client client = ClientBuilder.newClient();

        Response response = client.target(getConnectionString("/greetings")).request().accept("application/hal+json").acceptLanguage("da").get(Response.class);
        assertEquals(200, response.getStatus());
        assertNotNull(response.getHeaders().get("etag"));
        String etag = response.getHeaderString("etag");
        response = client.target(getConnectionString("/greetings")).request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .header("If-None-Match", etag)
                .get(Response.class);
        assertEquals(304, response.getStatus());
    }

    @Test
    public void testGetDynamicGreetingsListV1() {
        Client client = ClientBuilder.newClient();

        Response response = client.target(getConnectionString("/greetings")).request().accept("application/hal+json;p=greetings;v=1").acceptLanguage("da").get(Response.class);
        assertEquals(200, response.getStatus());
        String msg = response.readEntity(String.class);
        assertTrue(msg.contains("{\"href\":"));
        assertTrue(msg.contains("greetings/hallo\",\"title\":\"Dansk Hilsen Hallo\"}"));
        assertEquals("application/hal+json;p=greetings;v=1", response.getMediaType().toString());
        String entity = "{\"greeting\":\"Hejog!\",\"language\":\"Dansk\",\"country\":\"Danmark\",\"native\":{\"language\":\"Dansk\",\"country\":\"Danmark\"},\"_links\":{\"self\":{\"href\":\"greetings/hejog\",\"title\":\"Dansk Hilsen Hejog\"}}}";
        response = client.target(getConnectionString("/greetings/hejog")).request().accept("application/hal+json").acceptLanguage("da").get(Response.class);
        assertEquals(404, response.getStatus());
        response = client
                .target(getConnectionString("/greetings"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .post(Entity.json(entity));
        assertEquals(201, response.getStatus());
        assertTrue(response.getHeaderString("Location").contains("greetings/hejog"));
        response = client.target(getConnectionString("/greetings/hejog")).request().accept("application/hal+json").acceptLanguage("da").get(Response.class);
        assertEquals(200, response.getStatus());

        response = client.target(getConnectionString("/greetings")).request().accept("application/hal+json;p=greetings;v=1").acceptLanguage("da").get(Response.class);
        String msgAfter = response.readEntity(String.class);
        assertTrue(msgAfter.length() > msg.length());
        assertFalse(msg.contains("Hejog"));
        assertTrue(msgAfter.contains("Hejog"));
    }

    @AfterAll
    static void destroyClass() {
        CDI<Object> current = CDI.current();
        ((SeContainer) current).close();
    }

    private String getConnectionString(String path) {
        return "http://localhost:" + server.port() + path;
    }

}
