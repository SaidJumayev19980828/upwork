package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UserRepresentationObject {

    public Long id;
    public String name;
    public String email;
    public String image;
    public String phoneNumber;
    public String mobile;
    public List<AddressRepObj> addresses;
    public String firstName;
    public String lastName;
    public Long organizationId;
    public Long shopId;
    public Set<String> roles;
    public String status;
    public LocalDateTime creationDate;
    public Long familyId;
    public Long tierId;
    public Boolean allowReward;
    public LocalDateTime dateOfBirth;
    public LocalDateTime tierCreatedAt;
    public Long boosterId;
    public String referral;

    public String getReferral() {
        return id + "";
    }
}
