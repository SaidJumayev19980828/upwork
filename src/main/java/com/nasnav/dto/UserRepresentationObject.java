package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserRepresentationObject {

    @JsonProperty("id")
    public Long id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("email")
    public String email;

    @JsonProperty("image")
    public String image;

    @JsonProperty("phone")
    public String phoneNumber;

    @JsonProperty("mobile")
    public String mobile;

    @JsonProperty("address")
    public Address address;
}
