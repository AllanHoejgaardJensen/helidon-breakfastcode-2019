package com.examples.greeting;

import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * A HAL based representation of a collection of greetings links having the optional embeddedGreetings greetings.
 * 
 * HAL allows for you to have objects linked and embeddedGreetings, these linked objects must be available,
 the embeddedGreetings can be available according to the hypertext cache pattern in the HAL specification.
 * 
 */
@Resource
@ApiModel(value = "Greetings", description = "A Greeting List")
public class GreetingsRepresentation {
    private String info;
    
    @Link("self")
    private HALLink self;
    
    @Link("greetings")
    private Collection<HALLink> greetings;

    @EmbeddedResource("greetings")
    private Collection<GreetingRepresentation> embeddedGreetings;

    public GreetingsRepresentation(String info, Collection<GreetingRepresentation> greetings) {
        this.info = info;
        this.self = new HALLink.Builder("/greetings")
                .name("greetingslist")
                .title("A list of greetings")
                .type("application/hal+json;p=greetings")
                .build();
        embeddedGreetings = new ArrayList<>(greetings.size());
        embeddedGreetings.addAll(greetings);
        this.greetings = new ArrayList<>();
        this.greetings.addAll(greetings.stream()
                .map(greeting -> new HALLink.Builder(greeting.getSelf().getHref())
                        .title(greeting.getSelf().getTitle())
                        .type(greeting.getSelf().getType())
                        //.seen(representation.getSelf().getSeen())
                        .build()
                )
                .collect(Collectors.toList()));
    }

    @ApiModelProperty(
            access = "public",
            name = "info",
            value = "information about the response - a list of greetings")
    public String getInfo() {
        return info;
    }

    @ApiModelProperty(
            access = "public",
            name = "self",
            value = "information about the resource itself")
    public HALLink getSelf() {
        return self;
    }

    public Collection<HALLink> getGreetings() {
        return Collections.unmodifiableCollection(greetings);
    }

    public Collection<GreetingRepresentation> getEmbeddedGreetings() {
        return Collections.unmodifiableCollection(embeddedGreetings);
    }    
}
