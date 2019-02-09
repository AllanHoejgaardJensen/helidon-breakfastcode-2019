package com.examples.greeting;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Includes the native part of a representation.
 * The native part includes the language spoken and the country.
 */
@ApiModel(value = "native", description = "the probably prefered language of the consumer")
public class GreetingNativeRepresentation {
    
    private String language;
    private String country;

    public GreetingNativeRepresentation() {
        // required by Jackson
    }

    public GreetingNativeRepresentation(String language, String country) {
        this.language = language;
        this.country = country;
    }

    @ApiModelProperty(
            access = "public",
            name = "language",
            example = "English",
            value = "the preferred language for consumer of the actual representation.")
    public String getLanguage() {
        return language;
    }

    @ApiModelProperty(
            access = "public",
            name = "country",
            example = "English",
            value = "the country from which the language of the consumer origins.")
    public String getCountry() {
        return country;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.language);
        hash = 59 * hash + Objects.hashCode(this.country);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final GreetingNativeRepresentation other = (GreetingNativeRepresentation) obj;
        if (!Objects.equals(this.language, other.language))
            return false;
        if (!Objects.equals(this.country, other.country))
            return false;
        return true;
    }
    
}

