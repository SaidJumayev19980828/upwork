package com.nasnav.integration.events.data;

import lombok.Data;

@Data
public class AddressData {
    private String city;
    private String country;
    private String state;
    private String zipCode;    
    private String address;
}
