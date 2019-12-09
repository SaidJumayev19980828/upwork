package com.nasnav.integration.microsoftdynamics.webclient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Customer {
    @JsonProperty("AX_ID")
    private String accountNumber; //customer account
    
    @JsonProperty("first_name")
    private String firstName;
    
    @JsonProperty("middle_name")
    private String middleName;
    
    @JsonProperty("last_name")
    private String lastName;
    
    @JsonProperty("Email")
    private String email;

    @JsonProperty(value = "dob")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate; //date of birth;
    
    @JsonProperty("Gender")
    private int gender; //male = 1
    
    private List<Address> addresses;
    
    
    @JsonProperty("Phone Number")
    private String phoneNumber;
    
    @JsonProperty(value = "Customer")
    private List<CustomerRepObj> obj; // for getting customers only !
}
