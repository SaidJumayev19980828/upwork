package com.nasnav.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nasnav.dto.response.navbox.StatisticsCartItem;

import lombok.Data;

@Data
public class UserCartInfo {
    Long id;
    String name;
    String email;
    @JsonProperty("phone_number")
    String phoneNumber;
    List<StatisticsCartItem> items;
}