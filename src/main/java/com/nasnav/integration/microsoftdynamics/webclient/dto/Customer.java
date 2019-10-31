package com.nasnav.integration.microsoftdynamics.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class Customer {
    @JsonProperty("AX_ID")
    private Long id; //customer account
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    @JsonProperty(value = "dob")
    private Date birthDate; //date of birth;
    private boolean gender; //male = true
    private Address address;
}
