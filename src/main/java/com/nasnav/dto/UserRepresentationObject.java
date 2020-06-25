package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class UserRepresentationObject {

    public Long id;
    public String name;
    public String email;
    public String image;
    @JsonProperty("phone_number")
    public String phoneNumber;
    public String mobile;
    public List<AddressRepObj> addresses;

    @JsonProperty("organization_id")
    public Long organizationId;
    @JsonProperty("store_id")
    public Long shopId;
    public Set<String> roles;
}
