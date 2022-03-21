package com.nasnav.payments.paymob;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Merchant {
    private Long id;
    private LocalDate createdAt;
    private List<String> phones;
    private List<String> companyEmails;
    private String companyName;
    private String state;
    private String country;
    private String city;
    private String postalCode;
    private String street;
}
