/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.examples.greeting;

import com.examples.RepresentationContainer;
import com.examples.patch.JSONPatchContainer;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.HALMapper;
import io.swagger.annotations.ApiOperation;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.Pattern;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A simple JAX-RS greetings resource
 */
@Path("/greetings")
@RequestScoped
public class Greeting {

    private static final Logger LOGGER = Logger.getLogger(Greeting.class.getName());

    private static RepresentationContainer<String, GreetingRepresentation> representations = new RepresentationContainer<>();

    /**
     * The representation message provider.
     */
    private final GreetingProvider greetingProvider;

    private final Map<String, GreetingProducer> greetingProducers = new HashMap<>();
    private final Map<String, GreetingListProducer> greetingListProducers = new HashMap<>();

    /**
     * Using constructor injection to get a configuration property.
     * By default this gets the value from META-INF/microprofile-config
     *
     * @param greetingConfig the configured representation message
     */
    @Inject
    public Greeting(GreetingProvider greetingConfig) {
        populateRepresentations();
        this.greetingProvider = greetingConfig;
        greetingProducers.put("application/json", this::getGreetingG1V4);
        greetingProducers.put("application/hal+json", this::getGreetingG1V4);
        greetingProducers.put("application/hal+json;p=greeting", this::getGreetingG1V4);
        greetingProducers.put("application/hal+json;p=greeting;v=2", this::getGreetingG1V2);
        greetingProducers.put("application/hal+json;p=greeting;v=3", this::getGreetingG1V3);
        greetingProducers.put("application/hal+json;p=greeting;v=4", this::getGreetingG1V4);
        greetingProducers.put("application/json;p=metadata", this::getGreetingMetadata);

        greetingListProducers.put("application/json", this::getGreetingListG1V2);
        greetingListProducers.put("application/hal+json", this::getGreetingListG1V2);
        greetingListProducers.put("application/hal+json;p=greetings", this::getGreetingListG1V2);
        greetingListProducers.put("application/hal+json;p=greetings;v=2", this::getGreetingListG1V2);
        greetingListProducers.put("application/hal+json;p=greetings;v=1", this::getGreetingListG1V1);
        greetingListProducers.put("application/json;p=metadata", this::getGreetingListMetadata);
    }
    /**
     * Create a new representation and disallow replace an existing representation.
     *
     * @param request the actual request
     * @param acceptLanguage the preferred language
     * @param logToken a correlation id for a consumer
     * @param greeting a json formatted input
     * @return response the status, headers etc. send back to the consumer
     *
     * { @code (
     *   {
     *       "greeting": "Halløj!",
     *       "language": "Dansk",
     *       "country": "Danmark",
     *       "native": {
     *           "language": "Dansk",
     *           "country": "Danmark"
     *       },
     *       "_links": {
     *           "self": {
     *               "href": "greetings/halloj",
     *               "title": "Dansk Hilsen Halløj"
     * }
     * }
     * }
     * )}
     */
    @POST
    @Produces({"application/hal+json"})
    @Consumes({"application/json"})
    @ApiOperation(value = "create a new representation")
    public Response createNewGreeting(
            @Context Request request,
            @HeaderParam("Accept-Language") @Pattern(regexp = "^((\\s*[a-z]{2},{0,1}(-{0,1}[a-z]{2}){0,1})+(;q=0\\.[1-9]){0,1},{0,1})+") String acceptLanguage,
            @HeaderParam("X-Log-Token") @Pattern(regexp = "^[a-zA-Z0-9\\-]{36}$") String logToken,
            String greeting) {
        LOGGER.log(Level.INFO, "POST - Greeting");
        Response.Status status = Response.Status.BAD_REQUEST;
        ObjectMapper mapper = new HALMapper();
        try {
            GreetingRepresentation mg = mapper.readValue(greeting, GreetingRepresentation.class);
            String key = getGreetingRef(mg) + "_" + preferredLanguage(acceptLanguage);
            GreetingRepresentation stored = representations.get(key);
            if (stored != null) {
                LOGGER.log(Level.INFO, "Attempted to update an existing Greeting (" + key + ") - in total (" + representations.size() + "):\n" + mg.toHAL());
                String errMsg = "{"
                        + "  \"message\": \"Sorry that your request for updating representation could not be met!\","
                        + "  \"_links\":{"
                        + "      \"href\":\"/greetings/" + stored.getSelf().getHref() + "\","
                        + "      \"type\":\"application/hal+json\","
                        + "      \"title\":\"Update Greeting Resource\""
                        + "      }"
                        + "}";
                return Response
                        .status(Response.Status.CONFLICT)
                        .entity(errMsg)
                        .header("Location", stored.getSelf().getHref())
                        .header("X-Log-Token", validateOrCreateToken(logToken))
                        .build();
            }
            status = createNewGreeting(mg, mg.getSelf().getHref(), greeting, key, logToken + "problem creating new representation");
            if (Response.Status.CREATED.equals(status)) {
                LOGGER.log(Level.INFO, "Parsed new Greeting (" + key + ") - in total (" + representations.size() + "):\n" + mg.toHAL());
                EntityTag et = getETag(mapper.writeValueAsString(representations.get(key)));
                return Response
                        .status(status)
                        .tag(et)
                        .header("Location", mg.getSelf().getHref())
                        .header("X-Log-Token", validateOrCreateToken(logToken))
                        .build();
            }
        } catch (JsonParseException jpe) {
            LOGGER.log(Level.WARNING, "Sorry, I could not parse the input. which was:\n" + greeting.toString(), jpe);
            status = Response.Status.UNSUPPORTED_MEDIA_TYPE;
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Sorry, I could not parse the input. which was:\n" + greeting.toString(), ex);
        }
        return Response.status(status).build();
    }

    /**
     * A Greeting can be addressed specifically and the consumer can specify what language he/she prefers.
     * <p>
     * A LogToken can be part of the request and that will be returned in the response. If no LogToken is present in the request a new one is extracted and returned to the
     * consumer. The format for the LogToken is a 36 long string that can consist of a-z, A-Z,0-9 and - In other words: small letters, capital letters and numbers and hyphens
     * <p>
     * @param request the actual request
     * @param acceptLanguage the preferred language
     * @param accept the accepted response format
     * @param logToken a correlation id for a consumer
     * @param eTag the concrete instance of the lists contents version seen temporally
     * @return String that will be returned containing "application/hal+json".
     */
    @GET
    @Produces({"application/hal+json", "application/json"})
    @ApiOperation(value = "list all greetings", response = GreetingsRepresentation.class)
    public Response getGreetingsList(
            @Context Request request,
            @HeaderParam("Accept") String accept,
            @HeaderParam("Accept-Language")
            @Pattern(regexp = "^((\\s*[a-z]{2},{0,1}(-{0,1}[a-z]{2}){0,1})+(;q=0\\.[1-9]){0,1},{0,1})+") String acceptLanguage,
            @HeaderParam("X-Log-Token") @Pattern(regexp = "^[a-zA-Z0-9\\-]{36}$") String logToken,
            @HeaderParam("If-None-Match") String eTag) {
        return greetingListProducers.getOrDefault(accept, this::handle406UnsupportedGreetings).getResponse(request, accept, acceptLanguage, logToken, eTag);
    }

    /**
     * Create a new representation or replace an existing representation.
     *
     * @param request the received request
     * @param acceptLanguage the preferred language
     * @param logToken a correlation id for a consumer
     * @param eTag the actual instance content version for a given representation
     * @param greeting a json formatted input
     * @param resource the concrete resource
     * @return response the status, headers etc. for consumer
     *
     * {@code(
     *   {
     *       "greeting": "Halløj!",
     *       "language": "Dansk",
     *       "country": "Danmark",
     *       "native": {
     *           "language": "Dansk",
     *           "country": "Danmark"
     *       },
     *       "_links": {
     *           "self": {
     *               "href": "greetings/halloj",
     *               "title": "Dansk Hilsen Halløj osv"
     * }
     * }
     * }
     * )}
     */
    @PUT
    @Path("{representation}")
    @Produces({"application/hal+json"})
    @Consumes({"application/json"})
    @ApiOperation(value = "replace a representation", response = GreetingRepresentation.class)
    public Response replaceOrCreateGreeting(
            @Context Request request,
            @HeaderParam("Accept-Language") @Pattern(regexp = "^((\\s*[a-z]{2},{0,1}(-{0,1}[a-z]{2}){0,1})+(;q=0\\.[1-9]){0,1},{0,1})+") String acceptLanguage,
            @HeaderParam("X-Log-Token") @Pattern(regexp = "^[a-zA-Z0-9\\-]{36}$") String logToken,
            @HeaderParam("If-None-Match") String eTag,
            @PathParam("representation") @Pattern(regexp = "^[a-z0-9\\-]+$") String resource,
            String greeting) {
        LOGGER.log(Level.INFO, "PUT - Greeting");
        ObjectMapper mapper = new HALMapper();
        Response.Status status = Response.Status.BAD_REQUEST;
        try {
            GreetingRepresentation mappedGreeting = mapper.readValue(greeting, GreetingRepresentation.class);
            String key = getGreetingRef(mappedGreeting) + "_" + preferredLanguage(acceptLanguage);
            GreetingRepresentation stored = representations.get(key);
            final String msg = "Greeting (" + key + ") - in total (" + representations.size() + "):\n" + mappedGreeting.toHAL();
            final String inconsistency = "Href and resource mismatch - target:" + resource + " object:" + msg;
            GreetingRepresentation receivedGreeting = new GreetingRepresentation(mappedGreeting);
            EntityTag et = null;
            if (stored == null) {
                status = createNewGreeting(receivedGreeting, resource, msg, key, inconsistency);
                et = getETag(mapper.writeValueAsString(representations.get(key)));
            } else if (isRessourceIdCorrect(stored, resource)) {
                et = getETag(mapper.writeValueAsString(stored));
                Response.ResponseBuilder builder = request.evaluatePreconditions(et);
                if (builder == null) {
                    return Response.status(Response.Status.CONFLICT)
                            .entity("{\"error\":\"object has been updated, please get newest version\"}")
                            .header("X-Log-Token", validateOrCreateToken(logToken))
                            .build();
                }
                status = replaceGreeting(msg, key, receivedGreeting);
                et = getETag(mapper.writeValueAsString(representations.get(key)));
            } else {
                LOGGER.log(Level.INFO, inconsistency);
                status = Response.Status.BAD_REQUEST;
            }
            return Response
                    .status(status)
                    .tag(et)
                    .header("Location", receivedGreeting.getSelf().getHref())
                    .header("X-Log-Token", validateOrCreateToken(logToken))
                    .build();
        } catch (JsonParseException jpe) {
            LOGGER.log(Level.WARNING, "Sorry, I could not parse the input. which was:\n" + greeting.toString(), jpe);
            status = Response.Status.UNSUPPORTED_MEDIA_TYPE;
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Sorry, I could not parse the input. which was:\n" + greeting, ex);
        }
        return Response.status(status).build();
    }

    /**
     * A Greeting can be deleted.
     * <p>
     * A LogToken can be part of the request and that will be returned in the response. If no LogToken is present in the request a new one is extracted and returned to the
     * consumer. The format for the LogToken is a 36 long string that can consist of a-z, A-Z,0-9 and - In other words: small letters, capital letters and numbers and hyphens
     * <p>
     * @param request the actual request received
     * @param accept the chosen accepted content-type by consumer
     * @param acceptLanguage client can set the preferred preferredLanguage(s) as in HTTP spec.
     * @param logToken a correlation id for a consumer
     * @param eTag the actual instance content version for a given representation
     * @param greeting the representation to delete.
     * @return status, headers etc. to consumer
     */
    @DELETE
    @Path("{representation}")
    @Consumes({"application/json"})
    @ApiOperation(value = "delete a representation")
    public Response deleteGreeting(
            @Context Request request,
            @HeaderParam("Accept") String accept,
            @HeaderParam("Accept-Language") @Pattern(regexp = "^((\\s*[a-z]{2},{0,1}(-{0,1}[a-z]{2}){0,1})+(;q=0\\.[1-9]){0,1},{0,1})+") String acceptLanguage,
            @HeaderParam("X-Log-Token") @Pattern(regexp = "^[a-zA-Z0-9\\-]{36}$") String logToken,
            @HeaderParam("If-None-Match") String eTag,
            @PathParam("representation") @Pattern(regexp = "[a-z]*") String greeting) {
        LOGGER.log(Level.INFO, "DELETE - Greeting");

        String key = greeting + "_" + preferredLanguage(acceptLanguage);
        GreetingRepresentation stored = representations.get(key);
        ObjectMapper mapper = new HALMapper();
        Response.Status status;
        if (stored == null) {
            LOGGER.log(Level.INFO, "Attempted to delete a non-existing Greeting " + key);
            status = Response.Status.NOT_FOUND;
        } else {
            try {
                EntityTag et = getETag(mapper.writeValueAsString(stored));
                Response.ResponseBuilder builder = request.evaluatePreconditions(et);
                if (builder == null) {
                    return Response.status(Response.Status.CONFLICT)
                            .entity("{\"error\":\"object has been updated, please get newest version\"}")
                            .header("X-Log-Token", validateOrCreateToken(logToken))
                            .build();
                }
            } catch (JsonProcessingException ex) {
                LOGGER.log(Level.WARNING, "Delete of object failed " + key);
            }
            LOGGER.log(Level.INFO, "Deleted " + key);
            status = Response.Status.NO_CONTENT;
            try {
                EntityTag et = getETag(mapper.writeValueAsString(representations.get(key)));
                representations.remove(key);
                LOGGER.log(Level.INFO, "Greetings " + representations.size());
                return Response
                        .status(status)
                        .tag(et)
                        .header("X-Log-Token", validateOrCreateToken(logToken))
                        .build();

            } catch (JsonProcessingException pe) {
                LOGGER.log(Level.WARNING, "Delete of object failed " + key);
            }
        }
        return Response
                .status(status)
                .header("X-Log-Token", validateOrCreateToken(logToken))
                .build();
    }

    /**
     * A Greeting can be updated.
     * <p>
     * A LogToken can be part of the request and that will be returned in the response. If no LogToken is present in the request a new one is extracted and returned to the
     * consumer. The format for the LogToken is a 36 long string that can consist of a-z, A-Z,0-9 and - In other words: small letters, capital letters and numbers and hyphens
     * <p>
     * @param request the actual request
     * @param accept the chosen accepted content-type by consumer
     * @param acceptLanguage client can set the preferred preferredLanguage(s) as in HTTP spec.
     * @param eTag which is the header "If-None-Match" the etag which sets the expected state for the representation to be updated
     * @param logToken a correlation id for a consumer
     * @param greeting the representation to update.
     * @param patch the patch that is used for updating the representation
     * @return status, headers etc. to consumer
     */
    @PATCH
    @Path("{representation}")
    @Consumes({"application/patch+json", "application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "update a representation")
    public Response updateGreeting(
            @Context Request request,
            @HeaderParam("Accept") String accept,
            @HeaderParam("Accept-Language") @Pattern(regexp = "^((\\s*[a-z]{2},{0,1}(-{0,1}[a-z]{2}){0,1})+(;q=0\\.[1-9]){0,1},{0,1})+") String acceptLanguage,
            @HeaderParam("If-None-Match") String eTag,
            @HeaderParam("X-Log-Token") @Pattern(regexp = "^[a-zA-Z0-9\\-]{36}$") String logToken,
            @PathParam("representation") @Pattern(regexp = "[a-z]*") String greeting,
            String patch) {
        LOGGER.log(Level.INFO, "PATCH - Greeting");

        String key = greeting + "_" + preferredLanguage(acceptLanguage);
        Response.Status status = Response.Status.BAD_REQUEST;
        GreetingRepresentation stored = representations.get(key);
        if (stored == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            ObjectMapper om = new HALMapper();
            try {
                EntityTag et = getETag(om.writeValueAsString(stored));
                Response.ResponseBuilder builder = request.evaluatePreconditions(et);
                if (builder != null) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JSONPatchContainer patchR = mapper.readValue(patch, JSONPatchContainer.class);
                        if (patchR.getOperation().equals("replace")) {
                            try {
                                if (!patchR.replaceValue(stored)) {
                                    return getPatchResponse(Response.Status.BAD_REQUEST, "{\"error\":\"value could not be replaced\"}",
                                            stored.getSelf().getHref(), logToken);
                                } else {
                                    representations.alterchCode();
                                }
                            } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException | SecurityException ex) {
                                LOGGER.log(Level.WARNING,"PATCH::value is not replaced");
                                return getPatchResponse(Response.Status.BAD_REQUEST, "{\"error\":\"value was not replaced\"}",
                                        stored.getSelf().getHref(), logToken);
                            }
                            LOGGER.log(Level.INFO,"PATCH::value is replaced");
                            return getPatchResponse(Response.Status.OK, "{\"status\":\"value is replaced\"}",
                                    stored.getSelf().getHref(), logToken);
                        } else {
                            LOGGER.log(Level.WARNING,"PATCH::only operation replace is supported");
                            return getPatchResponse(
                                    Response.Status.BAD_REQUEST, "{\"error\":\"only operation replace is supported\"}",
                                    stored.getSelf().getHref(), logToken);
                        }
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING,"PATCH::IO Exception happened");
                        Response.status(Response.Status.BAD_REQUEST).build();
                    }
                } else {
                    LOGGER.log(Level.WARNING, "PATCH::object has been updated, please get newest version");
                    return getPatchResponse(
                            Response.Status.CONFLICT, "{\"error\":\"object has been updated, please get newest version\"}",
                            stored.getSelf().getHref(), logToken);
                }
            } catch (JsonProcessingException ex) {
                LOGGER.log(Level.SEVERE, "Could not map List to json", ex);
                status = Response.Status.UNSUPPORTED_MEDIA_TYPE;
            }
        }
        return Response.status(status).build();
    }


    /**
     * A Greeting can be addressed specifically and the consumer can specify what language he/she prefers.
     * <p>
     * A LogToken can be part of the request and that will be returned in the response. If no LogToken is present in the request a new one is extracted and returned to the
     * consumer. The format for the LogToken is a 36 long string that can consist of a-z, A-Z,0-9 and - In other words: small letters, capital letters and numbers and hyphens
     * <p>
     * @param request the actual request
     * @param uriInfo the URI information
     * @param accept the chosen accepted content-type by consumer
     * @param acceptLanguage client can set the preferred preferredLanguage(s) as in HTTP spec.
     * @param logToken a correlation id for a consumer
     * @param eTag the version of the list, it changes every time the list is changed
     * @param greeting the representation wanted by consumer
     * @return String that will be returned containing "application/hal+json".
     */
    @GET
    @Path("{representation}")
    @Produces({"application/json", "application/hal+json"})
    @ApiOperation(value = "get a representation", response = GreetingRepresentation.class)
    public Response getGreeting(
            @Context Request request, @Context UriInfo uriInfo,
            @HeaderParam("Accept") String accept,
            @HeaderParam("Accept-Language") @Pattern(regexp = "^((\\s*[a-z]{2},{0,1}(-{0,1}[a-z]{2}){0,1})+(;q=0\\.[1-9]){0,1},{0,1})+") String acceptLanguage,
            @HeaderParam("X-Log-Token") @Pattern(regexp = "^[a-zA-Z0-9\\-]{36}$") String logToken,
            @HeaderParam("If-None-Match") String eTag,
            @PathParam("representation") @Pattern(regexp = "[a-z]*") String greeting) {
        return greetingProducers.getOrDefault(accept, this::handle406UnsupportedGreetings).getResponse(request, accept, acceptLanguage, greeting, logToken);
    }

    private Response getGreetingListG1V2(Request request, String accept, String acceptLanguage, String logToken, String eTag) {
        LOGGER.log(Level.INFO, "GreetingList G1V2");
        EntityTag et = getETag(representations.getChCode());
        Response.ResponseBuilder builder = request.evaluatePreconditions(et);
        if (builder != null) {
            return builder.build();
        }
        CacheControl cacheControl = new CacheControl();
        int maxAge = 30;
        cacheControl.setMaxAge(maxAge);
        int version = 2;
        Collection<GreetingRepresentation> greetingsList = representations.values()
                .stream()
                .map(gr -> new GreetingRepresentation(gr))
                .collect(Collectors.toList());
        GreetingsRepresentation gr = new GreetingsRepresentation("This is the information v2HAL", greetingsList);
        ObjectMapper halMapper = new HALMapper();
        String json = "{\"error\":\"could not parse object\"}";
        try {
            json = halMapper.writeValueAsString(gr);
        } catch (JsonProcessingException ex) {
            LOGGER.log(Level.SEVERE, "Could not map List to json", ex);
        }
        return Response.ok()
                .entity(json)
                .tag(et)
                .type("application/hal+json;p=greetings;v=" + version)
                .cacheControl(cacheControl)
                .header("X-Log-Token", validateOrCreateToken(logToken))
                .build();
    }

    private Response getGreetingListG1V1(Request request, String accept, String acceptLanguage, String logToken, String eTag) {
        LOGGER.log(Level.INFO, "GreetingList G1V1");

        CacheControl cacheControl = new CacheControl();
        int maxAge = 30;
        int version = 1;
        cacheControl.setMaxAge(maxAge);
        return Response.ok()
                .entity(getGreetingList(version))
                .type("application/hal+json;p=greetings;v=" + version)
                .cacheControl(cacheControl)
                .header("X-Log-Token", validateOrCreateToken(logToken))
                .build();
    }

    private Response getGreetingListMetadata(Request request, String accept, String acceptLanguage, String logToken, String eTag) {
        LOGGER.log(Level.INFO, "GreetingList Metadata");
        String entity = "{"
                + "  \"metadata\":  {"
                + "      \"versions\":\"....\","
                + "      \"deprecations\":\"....\","
                + "      \"relations\":\"....\","
                + "      \"specifications\":\"....\","
                + "      \"reports\":\"....\","
                + "      \"issues\":\"....\","
                + "      \"history\":\"....\","
                + "      \"help\":\"....\""
                + "      }"
                + "}";
        return Response
                .status(200)
                .entity(entity)
                .type("application/hal+json;p=metadata")
                .header("X-Log-Token", validateOrCreateToken(logToken))
                .build();
    }


    /**
     * Implements latest version of the representation service.
     * <p>
     * The consumer roll back by entering the full content-type in the Accept header in this case {@code application/json;p=greeting;v=1} or more specific and correct as that
     * is the actual format used. {@code application/hal+json;p=greeting;v=1}
     */
    private Response getGreetingG1V4(Request request, String accept,  String acceptLanguage, String greeting, String logToken) {
        LOGGER.log(Level.INFO, "Greeting G1V4");
        String language = preferredLanguage(acceptLanguage);
        final String key = greeting + "_" + language;
        GreetingRepresentation entity = representations.get(key);
        if (entity == null) {
            Response response = getNoGreetingFound(logToken, key);
            return response;
        }
        ObjectMapper mapper = new HALMapper();
        Date lastModified = getLastModified();
        EntityTag eTag = getETag(entity.toString());
        String entityResponse = entity.toString();
        try {
            eTag = getETag(mapper.writeValueAsString(entity));
            entityResponse = mapper.writeValueAsString(entity);
        } catch (JsonProcessingException ex) {
            LOGGER.log(Level.WARNING, "Could not map entity:\n " + entity.toString(), ex);
        }
        Response.ResponseBuilder builder = request.evaluatePreconditions(lastModified, eTag);
        if (builder != null) {
            return builder.build();
        }
        CacheControl cacheControl = new CacheControl();
        int maxAge = 60;
        cacheControl.setMaxAge(maxAge);
        return Response.ok()
                .entity(entityResponse)
                .type("application/hal+json;p=greeting;v=4")
                .cacheControl(cacheControl)
                .tag(eTag)
                .lastModified(lastModified)
                .header("X-Log-Token", validateOrCreateToken(logToken))
                .build();
    }

    private Response getGreetingG1V3(Request request, String accept, String acceptLanguage, String greeting, String logToken) {
        LOGGER.log(Level.INFO, "Greeting G1V3");
        String language = preferredLanguage(acceptLanguage);
        GreetingRepresentation greetingEntity = representations.get(greeting + "_" + language);
        if (greetingEntity == null) {
            return getNoGreetingFound(logToken, greeting + "_" + language);
        }
        return getResponse(request, logToken, greetingEntity.toHAL(), 3);
    }

    private Response getGreetingG1V2(Request request, String accept, String acceptLanguage, String greeting, String logToken) {
        LOGGER.log(Level.INFO, "Greeting G1V2");
        String language = preferredLanguage(acceptLanguage);
        GreetingRepresentation greetingEntity = representations.get(greeting + "_" + language);
        if (greetingEntity == null) {
            String entity = "{"
                    + "  \"message\": \"Sorry your representation does not exist yet!\","
                    + "  \"_links\":{"
                    + "      \"href\":\"/greetings\","
                    + "      \"type\":\"application/hal+json\","
                    + "      \"title\":\"List of exixting greetings\""
                    + "      }"
                    + "}";
            return Response
                    .status(404)
                    .entity(entity)
                    .type("application/hal+json")
                    .header("X-Log-Token", validateOrCreateToken(logToken))
                    .build();
        }
        return getResponse(request, logToken, greetingEntity.toHATEOAS(), 2);
    }

    private Response getGreetingMetadata(Request request, String accept, String acceptLanguage, String greeting, String logToken) {
        LOGGER.log(Level.INFO, "Greeting Metadata");
        String entity = "{"
                + "  \"metadata\":  {"
                + "      \"versions\":\"....\","
                + "      \"deprecations\":\"....\","
                + "      \"relations\":\"....\","
                + "      \"specifications\":\"....\","
                + "      \"reports\":\"....\","
                + "      \"issues\":\"....\","
                + "      \"history\":\"....\","
                + "      \"help\":\"....\""
                + "      }"
                + "}";
        return Response
                .status(200)
                .entity(entity)
                .type("application/hal+json;p=metadata")
                .header("X-Log-Token", validateOrCreateToken(logToken))
                .build();
    }

    private Response.Status replaceGreeting(final String msg, String key, GreetingRepresentation receivedGreeting) {
        Response.Status status;
        LOGGER.log(Level.INFO, "Parsed Replaceable ", msg);
        status = Response.Status.OK;
        representations.add(key, receivedGreeting);
        return status;
    }

    private Response.Status createNewGreeting(GreetingRepresentation receivedGreeting, String resource, final String msg, String key, final String inconsistency) {
        LOGGER.log(Level.INFO, "Create new Greeting");
        Response.Status status;
        if (isRessourceIdCorrect(receivedGreeting, resource)) {
            LOGGER.log(Level.INFO, "Parsed New ", msg);
            GreetingRepresentation newGreeting = new GreetingRepresentation(receivedGreeting);
            status = Response.Status.CREATED;
            representations.add(key, newGreeting);
        } else {
            LOGGER.log(Level.INFO, inconsistency, msg);
            status = Response.Status.BAD_REQUEST;
        }
        return status;
    }

    private Response getNoGreetingFound(String logToken, String key) {
        LOGGER.log(Level.INFO, "No Greeting Found token: " + logToken +  " Greeeting: " + key);
        if ("hallihallo_da".equals(key)) {
            return getMovedResponse(logToken);
        }
        if ("howdydoydy_da".equals(key)){
            return getProblemResponse(logToken);
        }
        String entity;
        entity = "{"
                + "\"message\":\"Sorry your representation does not exist yet!\","
                + "\"_links\":{"
                + "\"greetings\":{"
                + "\"href\":\"/greetings\","
                + "\"type\":\"application/hal+json\","
                + "\"title\":\"List of existing greetings\""
                + "}"
                + "}"
                + "}";
        Response response = Response
                .status(Response.Status.NOT_FOUND)
                .entity(entity)
                .type("application/hal+json")
                .header("X-Log-Token", validateOrCreateToken(logToken))
                .build();
        return response;
    }

    private Response getResponse(Request request, String logToken, String entity, int version) {
        Date lastModified = getLastModified();
        EntityTag eTag = getETag(entity);
        Response.ResponseBuilder builder = request.evaluatePreconditions(lastModified, eTag);
        if (builder != null) {
            LOGGER.info("* building * 301 * on basis of builder");
            return builder.build();
        }
        CacheControl cacheControl = new CacheControl();
        int maxAge = 60;
        cacheControl.setMaxAge(maxAge);
        return Response
                .ok(entity)
                .type("application/hal+json;p=greeting;v=" + version)
                .cacheControl(cacheControl)
                .tag(eTag)
                .lastModified(lastModified)
                .header("X-Log-Token", validateOrCreateToken(logToken))
                .build();
    }

    private Response getPatchResponse(Response.Status status, String entity, String href, String logToken) {
        return Response
                .status(status)
                .entity(entity)
                .header("Location", href)
                .header("X-Log-Token", validateOrCreateToken(logToken))
                .build();
    }

    private Response getMovedResponse(String logToken) {
        LOGGER.info("Yeah - that was moved");
        String entity;
        entity = "{"
                + "\"message\":\"Sorry your representation does not exist yet!\","
                + "\"_links\":{"
                + "\"greetings\":{"
                + "\"href\":\"/greetings\","
                + "\"type\":\"application/hal+json\","
                + "\"title\":\"List of existing greetings\""
                + "}"
                + "}"
                + "}";
        return Response
                .status(Response.Status.MOVED_PERMANENTLY)
                .entity(entity)
                .type("application/hal+json")
                .header("Location", "/greetings/hallo")
                .header("X-Log-Token", validateOrCreateToken(logToken))
                .build();
    }

    private Response getProblemResponse(String logToken) {
        LOGGER.info("I see problems");
        String entity;
        entity = "{"
                + "\"type\":\"non-existent greeting!\","
                + "\"title\":\"Sorry your representation does not exist!\","
                + "\"detail\":\"You can create one using POST at greetings or PUT at greetings/{greeting}\""
                + "}";
        return Response
                .status(Response.Status.NOT_FOUND)
                .entity(entity)
                .type("application/problem+json")
                .header("Location", "/greetings")
                .header("X-Log-Token", validateOrCreateToken(logToken))
                .build();
    }

    private String preferredLanguage(String preferred) {
        if (preferred == null || preferred.isEmpty()) {
            return "da";
        }
        String[] languages = preferred.split(",");
        String[] preferredLanguage = Arrays.stream(languages).filter(s -> !s.contains(";")).toArray(String[]::new);
        return preferredLanguage[0];
    }
    /**
     * using a non-mockable way to get time in an interval of 10 secs to showcase the last modified header so if you are doing this for real and want to use time - pls use Instant
     * and Clock
     */
    private Date getLastModified() {
        return Date.from(Instant.ofEpochMilli(1565074000000L)); // Tue, 06 Aug 2019 06:46:40 GMT
    }

    private EntityTag getETag(String entity) {
        if (entity == null) entity = representations.getChCode();
        return new EntityTag(Integer.toHexString(entity.hashCode()), false);
    }

    private String validateOrCreateToken(String token) {
        if (token != null && !"".equals(token)) {
            return token;
        }
        return UUID.randomUUID().toString();
    }

    interface GreetingProducer {
        Response getResponse(Request request, String accept, String acceptLanguage, String greeting, String logToken);
    }

    private Response handle406UnsupportedGreetings(Request request, String... params) {
        String msg = Arrays.toString(params);
        LOGGER.log(Level.INFO, "Attempted to get a list by an unsupported content type {0}", msg);
        String entity;
        entity = "{"
                + "  \"message\":\"Sorry your representation of greetings does not exist!\","
                + "  \"accepted\":{"
                + "    \"application/json\", \"application/hal+json\", \"application/json;p=greeting\","
                + "    \"application/json;p=greetings;v=2\", \"application/json;p=greetings;v=1\", "
                + "    \"application/hal+json;p=metadata\""
                + "  }"
                + "}";
        return Response
                .status(Response.Status.NOT_ACCEPTABLE)
                .entity(entity)
                .build();
    }

    private Response handle415Unsupported(Request request, String... params) {
        String msg = Arrays.toString(params);
        LOGGER.log(Level.INFO, "Attempted to get an nonsupported content type {0}", msg);
        return Response
                .status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
                .build();
    }

    private String getGreetingRef(GreetingRepresentation newGreeting) {
        String ref = newGreeting.getSelf().getHref();
        String resources = "greetings/";
        int start = ref.indexOf(resources) + resources.length();
        String result = ref.substring(start).toLowerCase();
        return result;
    }

    private String getGreetingList(int version) {
        final String template = "{"
                + "\"greetings\":{"
                + "\"info\":\"a list containing current greetings\","
                + "\"_links\":{"
                + "\"self\":{"
                + "\"href\":\"/greetings\","
                + "\"type\":\"application/hal+json;p=greetinglist;v=" + version + "\","
                + "\"title\":\"List of Greetings\""
                + "},"
                + "\"greetings\":"
                + "["
                + getResultingGreetingsList()
                + "]"
                + "}"
                + "}"
                + "}";
        return template;
    }

    private String getResultingGreetingsList() {
        String result;
        StringBuilder list = new StringBuilder();
        representations
                .entrySet()
                .stream()
                .map((entry) -> list
                        .append("{\"href\":\"")
                        .append(entry.getValue().getSelf().getHref())
                        .append("\",\"title\":\"")
                        .append(entry.getValue().getSelf().getTitle())
                        .append("\"},"))
                .collect(Collectors.joining());
        result = list.substring(0, list.length() - 1);
        return result;
    }

    private boolean isRessourceIdCorrect(GreetingRepresentation greeting, String resource) {
        return greeting.getSelf().getHref().contains(resource);
    }

    interface GreetingListProducer {
        Response getResponse(Request request, String accept, String language, String logToken, String eTag);
    }

    /**
     * construct a fixture for demo purposes
     */
    private void populateRepresentations() {
        if (representations.isEmpty()) {
            HALLink self = new HALLink.Builder("/greetings/hallo")
                    .title("Dansk Hilsen Hallo")
                    .seen(Instant.now())
                    .name("Danish Greeting Hallo")
                    .templated(false)
                    .hreflang("da")
                    .type("application/hal+json;p=greeting")
                    .build();
            representations.add("hallo_da",
                    new GreetingRepresentation("Hallo!", "Dansk", "Danmark",
                            new GreetingNativeRepresentation("Dansk", "Danmark"), self));

            self = new HALLink.Builder("/greetings/hallo")
                    .title("Danish Greeting Hallo")
                    .seen(Instant.now())
                    .name("Danish Greeting Hallo")
                    .templated(false)
                    .hreflang("en")
                    .type("application/hal+json;p=greeting")
                    .build();
            representations.add("hallo_en",
                    new GreetingRepresentation("Hallo!", "Dansk", "Danmark",
                            new GreetingNativeRepresentation("Danish", "Denmark"), self));

            self = new HALLink.Builder("/greetings/hello")
                    .title("Engelsk Hilsen Hello")
                    .seen(Instant.now())
                    .name("English Greeting Hello")
                    .templated(false)
                    .hreflang("da")
                    .type("application/hal+json;p=greeting")
                    .build();
            representations.add("hello_da",
                    new GreetingRepresentation("Hello!", "English", "England",
                            new GreetingNativeRepresentation("Engelsk", "England"), self));

            self = new HALLink.Builder("/greetings/hello")
                    .title("English Greeting Hello")
                    .seen(Instant.now())
                    .name("English Greeting Hello")
                    .templated(false)
                    .hreflang("en")
                    .type("application/hal+json;p=greeting")
                    .build();
            representations.add("hello_en",
                    new GreetingRepresentation("Hello!", "English", "England",
                            new GreetingNativeRepresentation("English", "England"), self));
            LOGGER.log(Level.INFO, "Default data bootstrap activated", representations.size());
        }
    }

}
