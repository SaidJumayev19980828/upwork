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

    private Long id;
    private String name;
    private String email;
    private String image;
    private String phoneNumber;
    private String mobile;
    private List<AddressRepObj> addresses;
    private String firstName;
    private String lastName;
    private Long organizationId;
    private Long shopId;
    private Set<String> roles;
    private String status;
    private LocalDateTime creationDate;
    private Long familyId;
    private Long tierId;
    private Boolean allowReward;
    private LocalDateTime dateOfBirth;
    private LocalDateTime tierCreatedAt;
    private Long boosterId;
    private String referral;
    private Boolean isInfluencer;
    private LocalDateTime lastLogin;

    public String getReferral() {
        return id + "";
    }
}
