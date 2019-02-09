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
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class GreetingTestIT {

    private static Server server;

    @BeforeAll
    public static void setupServer() {
        server = Service.startServer();
    }

    @Test
    public void testHelloGreetingFromEnglishV4() {
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
    public void testHelloGreetingFromEnglishV3() {
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(getConnectionString("/greetings/hello"))
                .request()
                .accept("application/hal+json;p=greeting;v=3")
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
        assertFalse(msg.contains("\"seen\":\""));
        assertFalse(msg.contains("\"type\":\"application/hal+json;p=greeting"));
        String contentType = response.getMediaType().toString();
        assertEquals("application/hal+json;p=greeting;v=3", contentType);
        assertNotNull(response.getHeaders().get("X-Log-Token"));
    }
    @Test
    public void testNonexistentGreetingFromEnglishV3() {
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(getConnectionString("/greetings/hillo"))
                .request()
                .accept("application/hal+json;p=greeting;v=3")
                .acceptLanguage("en").get(Response.class);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testHelloGreetingFromEnglishV2() {
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(getConnectionString("/greetings/hello"))
                .request()
                .accept("application/hal+json;p=greeting;v=2")
                .acceptLanguage("en").get(Response.class);
        assertEquals(200, response.getStatus());
        String msg = response.readEntity(String.class);
        assertEquals("{"
                + "\"greeting\":\"Hello!\","
                + "\"language\":\"English\","
                + "\"country\":\"England\","
                + "\"native\":{"
                + "\"language\":\"English\","
                + "\"country\":\"England\""
                + "},"
                + "\"_links\":{"
                + "\"href\":\"/greetings/hello\","
                + "\"title\":\"English Greeting Hello\""
                + "}"
                + "}", msg);
        String contentType = response.getMediaType().toString();
        assertEquals("application/hal+json;p=greeting;v=", contentType.substring(0, contentType.length() - 1));
        assertNotNull(response.getHeaders().get("X-Log-Token"));
    }

    @Test
    public void testHelloGreetingFromEnglishV2Specific() {
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(getConnectionString("/greetings/hello"))
                .request()
                .accept("application/hal+json;p=greeting;v=2")
                .acceptLanguage("en")
                .get(Response.class);
        String msg = response.readEntity(String.class);
        assertEquals("{"
                + "\"greeting\":\"Hello!\","
                + "\"language\":\"English\","
                + "\"country\":\"England\","
                + "\"native\":{"
                + "\"language\":\"English\","
                + "\"country\":\"England\""
                + "},"
                + "\"_links\":{"
                + "\"href\":\"/greetings/hello\","
                + "\"title\":\"English Greeting Hello\""
                + "}"
                + "}", msg);
        assertEquals("application/hal+json;p=greeting;v=2", response.getMediaType().toString());
        assertNotNull(response.getHeaders().get("X-Log-Token"));
    }

    @Test
    public void testHelloGreetingFromDane() {
        Client client = ClientBuilder.newClient();

        Response response = client.target(getConnectionString("/greetings/hello")).request().accept("application/hal+json").acceptLanguage("da").get(Response.class);
        String msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Hello!\","));
        assertTrue(msg.contains("\"language\":\"English\","));
        assertTrue(msg.contains("\"country\":\"England\","));
        assertTrue(msg.contains("\"native\":{\"language\":\"Engelsk\",\"country\":\"England\"}"));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("/greetings/hello\""));
        assertTrue(msg.contains("\"title\":\"Engelsk Hilsen Hello\""));
        String contentType = response.getMediaType().toString();
        assertEquals("application/hal+json;p=greeting;v=", contentType.substring(0, contentType.length() - 1));
        assertNotNull(response.getHeaders().get("X-Log-Token"));
    }

    @Test
    public void testHelloGreetingFromDanishAcceptLanguage() {
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(getConnectionString("/greetings/hello"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .get(Response.class);
        assertEquals(200, response.getStatus());
        String msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Hello!\","));
        assertTrue(msg.contains("\"language\":\"English\""));
        assertTrue(msg.contains("\"country\":\"England\""));
        assertTrue(msg.contains("\"native\":{\"language\":\"Engelsk\",\"country\":\"England\"}"));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("/greetings/hello\""));
        assertTrue(msg.contains("\"title\":\"Engelsk Hilsen Hello\""));
        String contentType = response.getMediaType().toString();
        assertEquals("application/hal+json;p=greeting;v=", contentType.substring(0, contentType.length() - 1));
        assertNotNull(response.getHeaders().get("X-Log-Token"));
        response = client
                .target(getConnectionString("/greetings/hello"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da, en-gb;q=0.9, en;q=0.8, fr;q=0.5")
                .get(Response.class);
        msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Hello!\","));
        assertTrue(msg.contains("\"language\":\"English\","));
        assertTrue(msg.contains("\"country\":\"England\","));
        assertTrue(msg.contains("\"native\":{\"language\":\"Engelsk\",\"country\":\"England\"}"));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("/greetings/hello\""));
        assertTrue(msg.contains("\"title\":\"Engelsk Hilsen Hello\""));
        response = client
                .target(getConnectionString("/greetings/hello"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da, en, da;q=0.9, en-gb;q=0.8, fr;q=0.5")
                .get(Response.class);
        msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Hello!\","));
        assertTrue(msg.contains("\"language\":\"English\","));
        assertTrue(msg.contains("\"country\":\"England\","));
        assertTrue(msg.contains("\"native\":{\"language\":\"Engelsk\",\"country\":\"England\"}"));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("/greetings/hello\""));
        assertTrue(msg.contains("\"title\":\"Engelsk Hilsen Hello\""));
        response = client
                .target(getConnectionString("/greetings/hello"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("")
                .get(Response.class);
        msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Hello!\","));
        assertTrue(msg.contains("\"language\":\"English\","));
        assertTrue(msg.contains("\"country\":\"England\","));
        assertTrue(msg.contains("\"native\":{\"language\":\"Engelsk\",\"country\":\"England\"}"));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("/greetings/hello\""));
        assertTrue(msg.contains("\"title\":\"Engelsk Hilsen Hello\""));
        response = client
                .target(getConnectionString("/greetings/hello"))
                .request()
                .accept("application/hal+json")
                .get(Response.class);
        msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Hello!\","));
        assertTrue(msg.contains("\"language\":\"English\","));
        assertTrue(msg.contains("\"country\":\"England\","));
        assertTrue(msg.contains("\"native\":{\"language\":\"Engelsk\",\"country\":\"England\"}"));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("/greetings/hello\""));
        assertTrue(msg.contains("\"title\":\"Engelsk Hilsen Hello\""));
    }

    @Test
    public void testHalloGreetingFromEnglishAcceptLanguage() {
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("en, da;q=0.9, en-gb;q=0.8, fr;q=0.5")
                .get(Response.class);
        String msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Hallo!\","));
        assertTrue(msg.contains("\"language\":\"Dansk\","));
        assertTrue(msg.contains("\"country\":\"Danmark\","));
        assertTrue(msg.contains("\"native\":{\"language\":\"Danish\",\"country\":\"Denmark\""));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("/greetings/hallo\""));
        assertTrue(msg.contains("\"title\":\"Danish Greeting Hallo\""));

        response = client
                .target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("en, da, da;q=0.9, en-gb;q=0.8, fr;q=0.5")
                .get(Response.class);
        msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Hallo!\","));
        assertTrue(msg.contains("\"language\":\"Dansk\","));
        assertTrue(msg.contains("\"country\":\"Danmark\","));
        assertTrue(msg.contains("\"native\":{\"language\":\"Danish\",\"country\":\"Denmark\"}"));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("/greetings/hallo\""));
        assertTrue(msg.contains("\"title\":\"Danish Greeting Hallo\""));
        response = client
                .target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("en, da, da;q=0.9, en-gb; q=0.8, fr; q=0.5")
                .get(Response.class);
        msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Hallo!\","));
        assertTrue(msg.contains("\"language\":\"Dansk\","));
        assertTrue(msg.contains("\"country\":\"Danmark\","));
        assertTrue(msg.contains("\"native\":{\"language\":\"Danish\",\"country\":\"Denmark\"}"));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("/greetings/hallo\""));
        assertTrue(msg.contains("\"title\":\"Danish Greeting Hallo\""));
        String nonExisting = "{"
                + "\"message\":\"Sorry your representation does not exist yet!\","
                + "\"_links\":{"
                + "\"greetings\":{"
                + "\"href\":\"/greetings\","
                + "\"type\":\"application/hal+json\","
                + "\"title\":\"List of existing greetings\""
                + "}"
                + "}"
                + "}";
        response = client
                .target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage(" en-gb")
                .get(Response.class);
        msg = response.readEntity(String.class);
        assertEquals(nonExisting, msg);

        response = client
                .target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("en-us, en-gb;q=0.8, fr;q=0.5")
                .get(Response.class);
        msg = response.readEntity(String.class);
        assertEquals(nonExisting, msg);
    }

    @Test
    public void testHalloGreetingFromDanish() {
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .get(Response.class);
        String msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Hallo!\","));
        assertTrue(msg.contains("\"language\":\"Dansk\","));
        assertTrue(msg.contains("\"country\":\"Danmark\""));
        assertTrue(msg.contains("\"native\":{\"language\":\"Dansk\",\"country\":\"Danmark\"}"));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("greetings/hallo\""));
        assertTrue(msg.contains("\"title\":\"Dansk Hilsen Hallo\""));
        String contentType = response.getMediaType().toString();
        assertEquals("application/hal+json;p=greeting;v=", contentType.substring(0, contentType.length() - 1));
        assertNotNull(response.getHeaders().get("X-Log-Token"));
    }

    @Test
    public void testHalloGreetingFromEmptyDanish() {
        Client client = ClientBuilder.newClient();

        Response response = client.target(getConnectionString("/greetings/hallo")).request().accept("application/hal+json").acceptLanguage("").get(Response.class);
        assertEquals(200, response.getStatus());
        String msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Hallo!\","));
        assertTrue(msg.contains("\"language\":\"Dansk\","));
        assertTrue(msg.contains("\"country\":\"Danmark\","));
        assertTrue(msg.contains("\"native\":{\"language\":\"Dansk\",\"country\":\"Danmark\""));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("greetings/hallo\""));
        assertTrue(msg.contains("\"title\":\"Dansk Hilsen Hallo\""));
        String contentType = response.getMediaType().toString();
        assertEquals("application/hal+json;p=greeting;v=", contentType.substring(0, contentType.length() - 1));
        assertNotNull(response.getHeaders().get("X-Log-Token"));
    }

    @Test
    public void testHalloGreetingFromNullDanish() {
        Client client = ClientBuilder.newClient();

        Locale locale = null;
        Response response = client.target(getConnectionString("/greetings/hallo")).request().accept("application/hal+json").acceptLanguage(locale).get(Response.class);
        String msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Hallo!\","));
        assertTrue(msg.contains("\"language\":\"Dansk\","));
        assertTrue(msg.contains("\"country\":\"Danmark\""));
        assertTrue(msg.contains("\"native\":{\"language\":\"Dansk\",\"country\":\"Danmark\"}"));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("greetings/hallo\""));
        assertTrue(msg.contains("\"title\":\"Dansk Hilsen Hallo\""));
        String contentType = response.getMediaType().toString();
        assertEquals("application/hal+json;p=greeting;v=", contentType.substring(0, contentType.length() - 1));
        assertNotNull(response.getHeaders().get("X-Log-Token"));
    }

    @Test
    public void testHalloGreetingFromDanishWithLogToken() {
        Client client = ClientBuilder.newClient();

        Response response = client.target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .header("X-Log-Token", "noget-man-kan-kende")
                .get(Response.class);
        assertNotNull(response.getHeaders().get("X-Log-Token"));
        assertEquals("noget-man-kan-kende", response.getHeaderString("X-Log-Token"));
    }

    @Test
    public void testHalloGreetingFromDanishWithEmptyLogToken() {
        Client client = ClientBuilder.newClient();

        Response response = client.target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .header("X-Log-Token", "")
                .get(Response.class);
        assertNotNull(response.getHeaders().get("X-Log-Token"));
    }

    @Test
    public void testHalloGreetingFromDanishWithNullLogToken() {
        Client client = ClientBuilder.newClient();

        Response response = client.target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .header("X-Log-Token", null)
                .get(Response.class);
        assertNotNull(response.getHeaders().get("X-Log-Token"));
    }

    @Test
    public void testHalloGreetingFromDanishWithoutLogToken() {
        Client client = ClientBuilder.newClient();

        Response response = client.target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .get(Response.class);
        assertNotNull(response.getHeaders().get("X-Log-Token"));
    }

    @Test
    public void testHalloGreetingFromDanishWithNonChangedETagAndLastModified() {
        Client client = ClientBuilder.newClient();

        Response response = client.target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .get(Response.class);
        assertEquals(200, response.getStatus());
        assertNotNull(response.getHeaders().get("etag"));
        String etag = response.getHeaderString("etag");
        assertNotNull(response.getHeaders().get("last-modified"));
        String lastModified = response.getHeaderString("last-modified");
        response = client.target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .header("If-None-Match", etag)
                .header("If-Modified-Since", lastModified)
                .get(Response.class);
        assertEquals(304, response.getStatus());
        response = client.target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .header("If-None-Match", "\"fakeETag\"")
                .header("If-Modified-Since", lastModified)
                .get(Response.class);
        assertEquals(200, response.getStatus());
        response = client.target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .header("If-None-Match", etag)
                .header("If-Modified-Since", "0")
                .get(Response.class);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testHalloGreetingFromDanishWithNonChangedETagAndLastModifiedV3() {
        Client client = ClientBuilder.newClient();

        Response response = client.target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json;p=greeting;v=3")
                .acceptLanguage("da")
                .get(Response.class);
        assertEquals(200, response.getStatus());
        assertNotNull(response.getHeaders().get("etag"));
        String etag = response.getHeaderString("etag");
        assertNotNull(response.getHeaders().get("last-modified"));
        String lastModified = response.getHeaderString("last-modified");
        response = client.target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json;p=greeting;v=3")
                .acceptLanguage("da")
                .header("If-None-Match", etag)
                .header("If-Modified-Since", lastModified)
                .get(Response.class);
        assertEquals(304, response.getStatus());
        response = client.target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json;p=greeting;v=3")
                .acceptLanguage("da")
                .header("If-None-Match", "\"fakeETag\"")
                .header("If-Modified-Since", lastModified)
                .get(Response.class);
        assertEquals(200, response.getStatus());
        response = client.target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json;p=greeting;v=3")
                .acceptLanguage("da")
                .header("If-None-Match", etag)
                .header("If-Modified-Since", "0")
                .get(Response.class);
        assertEquals(200, response.getStatus());
    }


    @Test
    public void testHalloGreetingFromDanishWithChangedETagAndNonChangedLastModified() {
        Client client = ClientBuilder.newClient();

        Response response = client.target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .get(Response.class);
        assertNotNull(response.getHeaders().get("etag"));
        String etag = response.getHeaderString("etag");
        String lastModified = response.getHeaderString("last-modified");
        assertEquals(200, response.getStatus());
        response = client.target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .header("If-None-Match", etag)
                .header("If-Modified-Since", lastModified.substring(0, lastModified.length() - 1))
                .get(Response.class);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testHalloGreetingFromDanishWithChangedETagAndNotModifiedLastModified() {
        Client client = ClientBuilder.newClient();

        Response response = client.target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .get(Response.class);
        assertNotNull(response.getHeaders().get("etag"));
        String etag = response.getHeaderString("etag");
        assertNotNull(response.getHeaders().get("last-modified"));
        String lastModified = response.getHeaderString("last-modified");
        assertEquals(200, response.getStatus());
        response = client.target(getConnectionString("/greetings/hallo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .header("If-None-Match", "\"nomatch\"")
                .header("If-Modified-Since", lastModified)
                .get(Response.class);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testNonExistentGreeting() {
        Client client = ClientBuilder.newClient();

        Response response = client.target(getConnectionString("/greetings/ballo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("en")
                .get(Response.class);
        String msg = response.readEntity(String.class);
        assertEquals(
                "{"
                + "\"message\":\"Sorry your representation does not exist yet!\","
                + "\"_links\":{"
                + "\"greetings\":{"
                + "\"href\":\"/greetings\","
                + "\"type\":\"application/hal+json\","
                + "\"title\":\"List of existing greetings\""
                + "}"
                + "}"
                + "}", msg);
        assertEquals("application/hal+json", response.getMediaType().toString());
        assertNotNull(response.getHeaders().get("X-Log-Token"));
    }

    @Test
    public void testNonExistentGreetingG1V2() {
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(getConnectionString("/greetings/ballo"))
                .request()
                .accept("application/hal+json;p=greeting;v=2")
                .acceptLanguage("en")
                .get(Response.class);
        String msg = response.readEntity(String.class);
        assertEquals("{"
                + "  \"message\": \"Sorry your representation does not exist yet!\","
                + "  \"_links\":{"
                + "      \"href\":\"/greetings\","
                + "      \"type\":\"application/hal+json\","
                + "      \"title\":\"List of exixting greetings\""
                + "      }"
                + "}", msg);
        assertEquals("application/hal+json", response.getMediaType().toString());
        assertNotNull(response.getHeaders().get("X-Log-Token"));
    }

    @Test
    public void testNonSupportedAccept() {
        Client client = ClientBuilder.newClient();

        Response response = client.target(getConnectionString("/greetings/hallo")).request().accept("application/hal+json;p=unrealgreeting").acceptLanguage("en").get(Response.class);
        assertEquals(415, response.getStatus());
    }

    @Test
    public void testCreateFrenchGreetingForDanish() {
        Client client = ClientBuilder.newClient();

        String entity = "{\"greeting\":\"Allo!\",\"language\":\"Français\",\"country\":\"France\",\"native\":{\"language\":\"Fransk\",\"country\":\"Frankrig\"},\"_links\":{\"self\":{\"href\":\"greetings/allo\",\"title\":\"Fransk Hilsen Allo\"}}}";
        Response response = client
                .target(getConnectionString("/greetings"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .post(Entity.json(entity));
        assertEquals(201, response.getStatus());
        assertTrue(response.getHeaderString("Location").contains("greetings/allo"));
        response = client.target(getConnectionString("/greetings/allo")).request().accept("application/hal+json").acceptLanguage("da").get(Response.class);
        assertEquals(200, response.getStatus());
        String msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Allo!\","));
        assertTrue(msg.contains("\"language\":\"Français\","));
        assertTrue(msg.contains("\"country\":\"France\","));
        assertTrue(msg.contains("\"native\":{\"language\":\"Fransk\",\"country\":\"Frankrig\"}"));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("greetings/allo\",\"title\":\"Fransk Hilsen Allo\"}}"));
        response = client
                .target(getConnectionString("/greetings"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .post(Entity.json(entity));
        assertEquals(409, response.getStatus());
        assertTrue(response.getHeaderString("Location").contains("greetings/allo"));
        response = client.target(getConnectionString("/greetings/allo")).request().accept("application/hal+json").acceptLanguage("da").get(Response.class);
        assertEquals(200, response.getStatus());
        msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Allo!\","));
        assertTrue(msg.contains("\"language\":\"Français\","));
        assertTrue(msg.contains("\"country\":\"France\","));
        assertTrue(msg.contains("\"native\":{\"language\":\"Fransk\",\"country\":\"Frankrig\"}"));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("greetings/allo\",\"title\":\"Fransk Hilsen Allo\"}}"));
    }

    @Test
    public void testReCreateFrenchGreetingForDanish() {
        Client client = ClientBuilder.newClient();

        Response response = client.target(getConnectionString("/greetings/allo")).request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .get(Response.class);
            assertNotNull(response.getHeaders().get("etag"));
            String eTag = response.getHeaderString("etag");
        if (200 == response.getStatus()) {
            response = client.target(getConnectionString("/greetings/allo")).request()
                    .accept("application/hal+json")
                    .acceptLanguage("da")
                    .header("If-None-Match", eTag)
                    .delete(Response.class);
            assertEquals(204, response.getStatus());
        }
        String entity = "{\"greeting\":\"Allo!\",\"language\":\"Français\",\"country\":\"France\",\"native\":{\"language\":\"Fransk\",\"country\":\"Frankrig\"},\"_links\":{\"self\":{\"href\":\"greetings/allo\",\"title\":\"Fransk Hilsen Allo\"}}}";
        response = client
                .target(getConnectionString("/greetings"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .post(Entity.json(entity));
        assertEquals(201, response.getStatus());
        assertTrue(response.getHeaderString("Location").contains("greetings/allo"));
        assertNotNull(response.getHeaders().get("etag"));
        String postETag = response.getHeaderString("etag");
        response = client.target(getConnectionString("/greetings/allo")).request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .get(Response.class);
        assertEquals(200, response.getStatus());
        assertNotNull(response.getHeaders().get("etag"));
        String getETag = response.getHeaderString("etag");
        assertEquals(postETag, getETag);
        String msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Allo!\","));
        assertTrue(msg.contains("\"language\":\"Français\","));
        assertTrue(msg.contains("\"country\":\"France\","));
        assertTrue(msg.contains("\"native\":{\"language\":\"Fransk\",\"country\":\"Frankrig\"}"));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("greetings/allo\",\"title\":\"Fransk Hilsen Allo\"}}"));
        response = client
                .target(getConnectionString("/greetings/allo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .header("If-None-Match", getETag)
                .put(Entity.json(entity));
        assertEquals(200, response.getStatus());
        assertNotNull(response.getHeaders().get("etag"));
        String putETag = response.getHeaderString("etag");
        assertEquals(postETag, putETag);

        response = client
                .target(getConnectionString("/greetings/allo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .header("If-None-Match", getETag)
                .put(Entity.json(entity));
        assertEquals(200, response.getStatus());
        assertNotNull(response.getHeaders().get("etag"));
        String newPutETag = response.getHeaderString("etag");
        assertEquals(putETag, newPutETag);

        entity = "{\"greeting\":\"Allo!\",\"language\":\"Français\",\"country\":\"France\",\"native\":{\"language\":\"Fransk\",\"country\":\"Frankrig\"},\"_links\":{\"self\":{\"href\":\"greetings/allo\",\"title\":\"Fransk Hilsen Allo Rettet\"}}}";
        String etag = response.getHeaderString("etag");
        response = client
                .target(getConnectionString("/greetings/allo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .header("If-None-Match", etag)
                .put(Entity.json(entity));
        assertEquals(200, response.getStatus());
        assertTrue(response.getHeaderString("Location").contains("greetings/allo"));
        assertNotNull(response.getHeaders().get("etag"));
        String changedETag = response.getHeaderString("etag");
        assertFalse(putETag.equals(changedETag));
        response = client.target(getConnectionString("/greetings/allo")).request()
                .accept("application/hal+json")
                .acceptLanguage("da").get(Response.class);
        assertEquals(200, response.getStatus());
        msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Allo!\","));
        assertTrue(msg.contains("\"language\":\"Français\","));
        assertTrue(msg.contains("\"country\":\"France\","));
        assertTrue(msg.contains("\"native\":{\"language\":\"Fransk\",\"country\":\"Frankrig\"}"));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("greetings/allo\",\"title\":\"Fransk Hilsen Allo Rettet\"}}"));
        assertNotNull(response.getHeaders().get("etag"));
        String newGetETag = response.getHeaderString("etag");
        assertEquals(changedETag, newGetETag);

        response = client
                .target(getConnectionString("/greetings/allo"))
                .request()
                .accept("application/hal+json")
                .header("If-None-Match", putETag)
                .acceptLanguage("da")
                .put(Entity.json(entity));
        assertEquals(409, response.getStatus());
    }

    @Test
    public void testReCreateNonCorrectFrenchGreetingForDanish() {
        Client client = ClientBuilder.newClient();

        String entity = "{\"nosuchield\":\"Allo!\",\"language\":\"Fransk\",\"country\":\"Frankrig\",\"native\":{\"language\":\"Français\",\"country\":\"France\"},\"_links\":{\"self\":{\"href\":\"greetings/allo\",\"title\":\"Fransk Hilsen Allo Rettet\"}}}";
        Response response = client
                .target(getConnectionString("/greetings/allo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .put(Entity.json(entity));
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testCreateDeleteDanishGreetingForDanish() {
        Client client = ClientBuilder.newClient();

        String entity = "{\"greeting\":\"Hejsa!\",\"language\":\"Dansk\",\"country\":\"Danmark\",\"native\":{\"language\":\"Dansk\",\"country\":\"Danmark\"},\"_links\":{\"self\":{\"href\":\"greetings/hejsada\",\"title\":\"Dansk Hilsen Hejsada\"}}}";
        Response response = client
                .target(getConnectionString("/greetings/hejsada"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .put(Entity.json(entity));
        assertEquals(201, response.getStatus());
        assertNotNull(response.getHeaders().get("etag"));
        String eTag = response.getHeaderString("etag");
        response = client
                .target(getConnectionString("/greetings/hejsaa"))
                .request()
                .accept("application/json")
                .acceptLanguage("da")
                .delete();
        assertEquals(404, response.getStatus());
        response = client
                .target(getConnectionString("/greetings/hejsada"))
                .request()
                .accept("application/json")
                .acceptLanguage("da")
                .header("If-None-Match", "\"32a704cB\"")
                .delete();
        assertEquals(409, response.getStatus());
        response = client
                .target(getConnectionString("/greetings/hejsada"))
                .request()
                .accept("application/json")
                .acceptLanguage("da")
                .header("If-None-Match", eTag)
                .delete();
        assertEquals(204, response.getStatus());
        response = client
                .target(getConnectionString("/greetings/hejsada"))
                .request()
                .accept("application/json")
                .acceptLanguage("da")
                .delete();
        assertEquals(404, response.getStatus());
        entity = "{\"greeting\":\"Hejsada!\",\"language\":\"Dansk\",\"country\":\"Danmark\",\"native\":{\"language\":\"Dansk\",\"country\":\"Danmark\"},\"_links\":{\"self\":{\"href\":\"greetings/hejsada\",\"title\":\"Dansk Hilsen Hejsada\"}}}";
        response = client
                .target(getConnectionString("/greetings/hejsada"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .put(Entity.json(entity));
        assertEquals(201, response.getStatus());
        assertNotNull(response.getHeaders().get("etag"));
        eTag = response.getHeaderString("etag");
        response = client
                .target(getConnectionString("/greetings/hejsada"))
                .request()
                .accept("application/json")
                .acceptLanguage("da")
                .header("If-None-Match", eTag)
                .delete();
        assertEquals(204, response.getStatus());
        response = client
                .target(getConnectionString("/greetings/hejsada"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .put(Entity.json(entity));
        assertEquals(201, response.getStatus());
        assertNotNull(response.getHeaders().get("etag"));
        eTag = response.getHeaderString("etag");
        response = client
                .target(getConnectionString("/greetings/hejsada"))
                .request()
                .accept("application/json")
                .acceptLanguage("da")
                .header("If-None-Match", eTag)
                .delete();
        assertEquals(204, response.getStatus());
    }

    @Test
    public void testInitialCreateFrenchGreetingForSwedish() {
        Client client = ClientBuilder.newClient();

        String entity = "{\"greeting\":\"Allo!\",\"language\":\"Français\",\"country\":\"France\",\"native\":{\"language\":\"Franska\",\"country\":\"Frankrike\"},\"_links\":{\"self\":{\"href\":\"greetings/allo\",\"title\":\"Franska Hilsnen Allo\"}}}";
        Response response = client
                .target(getConnectionString("/greetings/allo"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("se")
                .put(Entity.json(entity));
        assertEquals(201, response.getStatus());
        assertTrue(response.getHeaderString("Location").contains("greetings/allo"));
        response = client.target(getConnectionString("/greetings/allo")).request().accept("application/hal+json").acceptLanguage("se").get(Response.class);
        assertEquals(200, response.getStatus());
        String msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Allo!\","));
        assertTrue(msg.contains("\"language\":\"Français\","));
        assertTrue(msg.contains("\"country\":\"France\","));
        assertTrue(msg.contains("\"native\":{\"language\":\"Franska\",\"country\":\"Frankrike\"}"));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("greetings/allo\",\"title\":\"Franska Hilsnen Allo\"}}"));
    }

    @Test
    public void testCreateGreetingAtWrongLocation() {
        Client client = ClientBuilder.newClient();

        String entity = "{\"greeting\":\"Allo!\",\"language\":\"French\",\"country\":\"France\",\"native\":{\"language\":\"Français\",\"country\":\"France\"},\"_links\":{\"self\":{\"href\":\"greetings/allo\",\"title\":\"French Greeting Allo\"}}}";
        Response response = client
                .target(getConnectionString("/greetings/wrong"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("en")
                .put(Entity.json(entity));
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testReplaceGreetingAtWrongLocation() {
        Client client = ClientBuilder.newClient();

        String entity = "{\"greeting\":\"Hola!\",\"language\":\"Spanish\",\"country\":\"Spain\",\"native\":{\"language\":\"Spanish\",\"country\":\"Spain\"},\"_links\":{\"self\":{\"href\":\"greetings/hola\",\"title\":\"Spanish Greeting Hola\"}}}";
        Response response = client
                .target(getConnectionString("/greetings/hola"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("en")
                .put(Entity.json(entity));
        assertEquals(201, response.getStatus());
        response = client
                .target(getConnectionString("/greetings/wrong"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("en")
                .put(Entity.json(entity));
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testCreateFrenchGreetingForFrench() {
        Client client = ClientBuilder.newClient();

        String entity = "{\"greeting\":\"Allo!\",\"language\":\"Français\",\"country\":\"France\",\"native\":{\"language\":\"Français\",\"country\":\"France\"},\"_links\":{\"self\":{\"href\":\"greetings/allo\",\"title\":\"Un Salute Français Allo\"}}}";
        Response response = client
                .target(getConnectionString("/greetings"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("fr")
                .post(Entity.json(entity));
        assertEquals(201, response.getStatus());
        assertTrue(response.getHeaderString("Location").contains("greetings/allo"));
        response = client.target(getConnectionString("/greetings/allo")).request().accept("application/hal+json").acceptLanguage("fr").get(Response.class);
        assertEquals(200, response.getStatus());
        String msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Allo!\","));
        assertTrue(msg.contains("\"language\":\"Français\","));
        assertTrue(msg.contains("\"country\":\"France\","));
        assertTrue(msg.contains("\"native\":{\"language\":\"Français\",\"country\":\"France\"}"));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("greetings/allo\",\"title\":\"Un Salute Français Allo\"}}"));
    }

    @Test
    public void testCreateFrenchGreetingForGerman() {
        Client client = ClientBuilder.newClient();

        String entity = "{\"greeting\":\"Allo!\",\"language\":\"Français\",\"country\":\"France\",\"native\":{\"language\":\"Französisch\",\"country\":\"Frankreich\"},\"_links\":{\"self\":{\"href\":\"greetings/allo\",\"title\":\"Ein Französischer Grüß\"}}}";
        Response response = client
                .target(getConnectionString("/greetings"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("de")
                .post(Entity.json(entity));
        assertEquals(201, response.getStatus());
        assertTrue(response.getHeaderString("Location").contains("greetings/allo"));

        response = client.target(getConnectionString("/greetings/allo")).request().accept("application/hal+json").acceptLanguage("de").get(Response.class);
        assertEquals(200, response.getStatus());
        String msg = response.readEntity(String.class);
        assertTrue(msg.contains("\"greeting\":\"Allo!\","));
        assertTrue(msg.contains("\"language\":\"Français\","));
        assertTrue(msg.contains("\"country\":\"France\","));
        assertTrue(msg.contains("\"native\":{\"language\":\"Französisch\",\"country\":\"Frankreich\"}"));
        assertTrue(msg.contains("\"_links\":{\"self\":{\"href\":"));
        assertTrue(msg.contains("greetings/allo\",\"title\":\"Ein Französischer Grüß\"}}"));
    }

    @Test
    public void testUnparsableInput() {
        Client client = ClientBuilder.newClient();

        String entity = "{\"noField\":\"Allo!\",\"language\":\"Französisch\",\"country\":\"Frankreich\",\"native\":{\"language\":\"Français\",\"country\":\"France\"},\"_links\":{\"self\":{\"href\":\"greetings/allo\",\"title\":\"Ein Französischer Grüß\"}}}";
        Response response = client
                .target(getConnectionString("/greetings"))
                .request()
                .accept("application/hal+json")
                .acceptLanguage("da")
                .post(Entity.json(entity));
        assertEquals(400, response.getStatus());
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
