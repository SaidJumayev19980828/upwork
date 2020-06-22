package com.nasnav.shipping.services.bosta.webclient.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Receiver {
    private String firstName;
    private String lastName;
    private String phone;
    private String country;
    private String email;
}
