package com.nasnav.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nasnav.dto.ProductBaseInfo;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdvertisementDTO {

    private Long id;
    private ProductBaseInfo product;
    private Integer coins;
    private Integer likes;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime creationDate;
}
