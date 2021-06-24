package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    private String facebook;
    private String twitter;
    private String instagram;
    private String youtube;
    private String linkedin;
    private String pinterest;
    private String whatsapp;
}