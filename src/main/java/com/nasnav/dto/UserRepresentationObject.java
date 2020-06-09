package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
    @JsonProperty("addresses")
    public Set<AddressRepObj> addresses;

    @JsonProperty("organization_id")
    public Long organizationId;
    @JsonProperty("store_id")
    public Long shopId;
    @JsonProperty("roles")
    public Set<String> roles;
}
