package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "facebook",
        "twitter",
        "instagram"
})
@Data
@EqualsAndHashCode(callSuper = false)
public class SocialRepresentationObject extends BaseRepresentationObject{

    @JsonProperty("facebook")
    private String facebook;
    @JsonProperty("twitter")
    private String twitter;
    @JsonProperty("instagram")
    private String instagram;

}