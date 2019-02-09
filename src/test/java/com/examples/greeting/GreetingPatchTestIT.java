package com.examples.greeting;

import com.examples.patch.JSONPatchContainer;
import com.examples.patch.PatchInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.helidon.microprofile.server.Server;
import org.glassfish.jersey.client.ClientConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.client.*;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class GreetingPatchTestIT {

    private static Server server;

    @BeforeAll
    public static void setupServer() {
        server = Service.startServer();
    }

    @Test
    public void testMapper() throws IOException {
        String patch = "{\"op\":\"replace\",\"path\":\"language\",\"value\":\"Synnejysk\"}";
        ObjectMapper mapper = new ObjectMapper();
        JSONPatchContainer patchR = mapper.readValue(patch, JSONPatchContainer.class);
        assertEquals(patch, patchR.toString());
    }

    @Test
    void testContainerWorking() {
        Client client = ClientBuilder.newClient();
        Response response = client
                .target(getConnectionString("/greetings/hello"))
                .request()
                .accept("application/hal+json;p=greeting;v=4")
                .acceptLanguage("en").get(Response.class);
        assertEquals(200, response.getStatus());
        String msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Hello!\""));
        assertTrue(msg.contains("\"language\":\"English\""));
        assertTrue(msg.contains("\"country\":\"England\""));
        assertTrue(msg.contains("\"native\":{"
                + "\"language\":\"English\","
                + "\"country\":\"England\""
                + "}"));
        assertTrue(msg.contains("\"_links\":{"));
        assertTrue(msg.contains("\"href\":\"/greetings/hello\""));
        assertTrue(msg.contains("\"title\":\"English Greeting Hello\""));
        assertTrue(msg.contains("\"seen\":\""));
        assertTrue(msg.contains("\"type\":\"application/hal+json;p=greeting"));
        String contentType = response.getMediaType().toString();
        assertEquals("application/hal+json;p=greeting;v=4", contentType);
        assertNotNull(response.getHeaders().get("X-Log-Token"));
    }

    @Test
    public void testUpdateGreetingLanguage() {
        ClientConfig config = new ClientConfig();
        config.register(PatchInterceptor.class);
        Client client = ClientBuilder.newClient(config);
        String entity = "{\"greeting\":\"Mooojn!\",\"language\":\"Dansk\",\"country\":\"Danmark\",\"native\":{\"language\":\"Dansk\",\"country\":\"Danmark\"},\"_links\":{\"self\":{\"href\":\"greetings/mooojn\",\"title\":\"Sønderjysk Hilsen Møøøjn\"}}}";
        Response response = client
                .target(getConnectionString("/greetings/mooojn"))
                .request()
                .acceptLanguage("son")
                .method("PUT", Entity.entity(entity, "application/json"), Response.class);
        assertEquals(201, response.getStatus());
        assertTrue(response.getHeaderString("Location").contains("greetings/mooojn"));
        response = client
                .target(getConnectionString("/greetings/mooojn"))
                .request()
                .accept("application/json")
                .acceptLanguage("son")
                .get(Response.class);
        assertEquals(200, response.getStatus());
        String msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Mooojn!\","));
        assertTrue(msg.contains("\"language\":\"Dansk\","));
        assertTrue(msg.contains("\"country\":\"Danmark\","));
        assertTrue(msg.contains("\"native\":{\"language\":\"Dansk\",\"country\":\"Danmark\"}"));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("greetings/mooojn\",\"title\":\"Sønderjysk Hilsen Møøøjn\"}}"));

        EntityTag eTag = response.getEntityTag();
        response = client
                .target(getConnectionString("/greetings/mooojn"))
                .request()
                .header("If-None-Match", new EntityTag("invalidETag"))
                .header("Content-Type", "application/patch+json")
                .acceptLanguage("son")
                .method("PATCH", Entity.entity("{\"op\":\"replace\",\"path\":\"language\",\"value\":\"Synnejysk\"}","application/patch+json"), Response.class);
        assertEquals(409, response.getStatus());
        Entity e = Entity.entity("{\"op\":\"replace\",\"path\":\"language\",\"value\":\"Synnejysk\"}", "application/patch+json");
        response = client
                .target(getConnectionString("/greetings/mooojn"))
                .request()
                .acceptLanguage("son")
                .header("If-None-Match", eTag)
                .method("PATCH", e, Response.class);
        assertEquals(200, response.getStatus());
        assertTrue(response.readEntity(String.class).contains("value is replaced"));
        response = client
                .target(getConnectionString("/greetings/mooojn"))
                .request()
                .accept("application/json")
                .acceptLanguage("son")
                .get(Response.class);
        assertEquals(200, response.getStatus());
        msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Mooojn!\","));
        assertTrue(msg.contains("\"language\":\"Synnejysk\","));
        assertTrue(msg.contains("\"country\":\"Danmark\","));
        assertTrue(msg.contains("\"native\":{\"language\":\"Dansk\",\"country\":\"Danmark\"}"));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("greetings/mooojn\",\"title\":\"Sønderjysk Hilsen Møøøjn\"}}"));
    }

    @Test
    public void testUpdateGreetingLanguageWrongContentType() {
        Client client = ClientBuilder.newClient();

        String entity = "{\"greeting\":\"Mooojn!\",\"language\":\"Dansk\",\"country\":\"Danmark\",\"native\":{\"language\":\"Dansk\",\"country\":\"Danmark\"},\"_links\":{\"self\":{\"href\":\"greetings/moooojn\",\"title\":\"Sønderjysk Hilsen Møøøjn\"}}}";
        Response response = client
                .target(getConnectionString("/greetings/moooojn"))
                .request()
                .acceptLanguage("son")
                .method("PUT", Entity.entity(entity, "application/json"), Response.class);
        assertEquals(201, response.getStatus());
        assertTrue(response.getHeaderString("Location").contains("greetings/moooojn"));
        response = client
                .target(getConnectionString("/greetings/moooojn"))
                .request()
                .accept("application/json")
                .acceptLanguage("son")
                .get(Response.class); //TODO target added
        assertEquals(200, response.getStatus());
        String msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Mooojn!\","));
        assertTrue(msg.contains("\"language\":\"Dansk\","));
        assertTrue(msg.contains("\"country\":\"Danmark\","));
        assertTrue(msg.contains("\"native\":{\"language\":\"Dansk\",\"country\":\"Danmark\"}"));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("greetings/moooojn\",\"title\":\"Sønderjysk Hilsen Møøøjn\"}}"));
        EntityTag eTag = response.getEntityTag();
        response = client
                .target(getConnectionString("/greetings/moooojn"))
                .request()
                .acceptLanguage("son")
                .header("If-None-Match", eTag)
                .method("PATCH",
                        Entity.entity("{\"op\":\"replace\",\"path\":\"language\",\"value\":\"Synnejysk\"}",
                                "application/some+json"),
                        Response.class);
        assertEquals(415, response.getStatus());
        response = client
                .target(getConnectionString("/greetings/moooojn"))
                .request()
                .accept("application/json")
                .acceptLanguage("son")
                .get(Response.class);
        assertEquals(200, response.getStatus());
        msg = response.readEntity(String.class);
        assertFalse(msg.contains("\"language\":\"Synnejysk\","));
    }

    @Test
    public void testUpdateNonParseablePath() {
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/json")
                .acceptLanguage("en")
                .get(Response.class);
        assertEquals(200, response.getStatus());
        EntityTag eTag = response.getEntityTag();
        response = client
                .target(getConnectionString("/greetings/hallo"))
                .request()
                .acceptLanguage("en")
                .header("If-None-Match", eTag)
                .method("PATCH",
                        Entity.entity("{\"op\":\"replace\",\"path\":\"language/nonexisting\",\"value\":\"itisnotgonnahappen\"}",
                                "application/patch+json"),
                        Response.class);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testUpdateNonParseablePatch() {
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/json")
                .acceptLanguage("en")
                .get(Response.class);
        assertEquals(200, response.getStatus());
        EntityTag eTag = response.getEntityTag();
        response = client
                .target(getConnectionString("/greetings/hallo"))
                .request()
                .acceptLanguage("en")
                .header("If-None-Match", eTag)
                .method("PATCH",
                        Entity.entity("{\"operation\":\"replace\",\"path\":\"language\",\"value\":\"itisnotgonnahappen\"}",
                                "application/patch+json"),
                        Response.class);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testUpdateNonExistingOperationGreetingLanguage() {
        Client client = ClientBuilder.newClient();

        String entity = "{\"greeting\":\"Mooojn!\",\"language\":\"Dansk\",\"country\":\"Danmark\",\"native\":{\"language\":\"Dansk\",\"country\":\"Danmark\"},\"_links\":{\"self\":{\"href\":\"greetings/mooooojn\",\"title\":\"Sønderjysk Hilsen Møøøjn\"}}}";
        Response response = client
                .target(getConnectionString("/greetings/mooooojn"))
                .request()
                .accept("application/json")
                .acceptLanguage("son")
                .get(Response.class);
        assertEquals(404, response.getStatus());
        response = client
                .target(getConnectionString("/greetings/mooooojn"))
                .request()
                .acceptLanguage("son")
                .method("PUT", Entity.entity(entity, "application/json"), Response.class);
        assertEquals(201, response.getStatus());
        assertTrue(response.getHeaderString("Location").contains("greetings/mooooojn"));
        response = client
                .target(getConnectionString("/greetings/mooooojn"))
                .request()
                .accept("application/json")
                .acceptLanguage("son")
                .get(Response.class);
        assertEquals(200, response.getStatus());
        EntityTag eTag = response.getEntityTag();
        response = client
                .target(getConnectionString("/greetings/mooooojn"))
                .request()
                .acceptLanguage("son")
                .header("If-None-Match", eTag)
                .method("PATCH",
                        Entity.entity("{\"op\":\"someotherop\",\"path\":\"language\",\"value\":\"itisnotgonnahappen\"}",
                                "application/patch+json"),
                        Response.class);
        assertEquals(400, response.getStatus());
        assertTrue(response.readEntity(String.class).contains("{\"error\":\"only operation replace is supported\"}"));
    }

    @Test
    public void testUpdateExistingOperationNonExistingAttribueGreetingLanguage() {
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(getConnectionString("/greetings/moooooojn"))
                .request()
                .accept("application/json")
                .acceptLanguage("son")
                .get(Response.class);
        assertEquals(404, response.getStatus());
        String entity = "{\"greeting\":\"Mooojn!\",\"language\":\"Dansk\",\"country\":\"Danmark\",\"native\":{\"language\":\"Dansk\",\"country\":\"Danmark\"},\"_links\":{\"self\":{\"href\":\"greetings/moooooojn\",\"title\":\"Sønderjysk Hilsen Møøøjn\"}}}";
        response = client
                .target(getConnectionString("/greetings/moooooojn"))
                .request()
                .acceptLanguage("son")
                .method("PUT", Entity.entity(entity, "application/json"), Response.class);
        assertEquals(201, response.getStatus());
        assertTrue(response.getHeaderString("Location").contains("greetings/moooooojn"));
        response = client
                .target(getConnectionString("/greetings/moooooojn"))
                .request()
                .accept("application/json")
                .acceptLanguage("son")
                .get(Response.class);
        assertEquals(200, response.getStatus());
        EntityTag eTag = response.getEntityTag();
        response = client
                .target(getConnectionString("/greetings/moooooojn"))
                .request()
                .acceptLanguage("son")
                .header("If-None-Match", eTag)
                .method("PATCH",
                        Entity.entity("{\"op\":\"replace\",\"path\":\"nonexisting\",\"value\":\"itisnotgonnahappen\"}",
                                "application/patch+json"),
                        Response.class);
        assertEquals(400, response.getStatus());
        String msg = response.readEntity(String.class);
        assertTrue(msg.contains("value was not replaced"));
        response = client
                .target(getConnectionString("/greetings/moooooojn"))
                .request()
                .acceptLanguage("son")
                .header("If-None-Match", eTag)
                .method("PATCH",
                        Entity.entity("{\"op\":\"replace\",\"path\":\"_links/self\",\"value\":\"notReplaced\"}",
                                "application/patch+json"),
                        Response.class);
        assertEquals(400, response.getStatus());
        msg = response.readEntity(String.class);
        assertTrue(msg.contains("value could not be replaced"));
    }


    @Test
    public void testUpdateNonExistingGreetingLanguage() {
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(getConnectionString("/greetings/itdoesnotexist"))
                .request()
                .acceptLanguage("en")
                .method("PATCH",
                        Entity.entity("{\"op\":\"replace\",\"path\":\"language\",\"value\":\"whoCares\"}",
                                "application/patch+json"),
                        Response.class);
        assertEquals(404, response.getStatus());
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
