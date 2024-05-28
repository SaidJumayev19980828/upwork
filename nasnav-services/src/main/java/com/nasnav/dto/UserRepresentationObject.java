package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.enumerations.Gender;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

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
    private Long tierId;
    private Boolean allowReward;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateOfBirth;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime tierCreatedAt;
    private String referral;
    private Boolean isInfluencer = false;
    private LocalDateTime lastLogin;
    private Gender gender;
    private Long BankAccountId;
    private Long influencerId;
    private Boolean isGuided;
    private Long followersCount;
    private Long followingsCount;
    private Integer eventsCount;

    public String getReferral() {
        return id + "";
    }
}
