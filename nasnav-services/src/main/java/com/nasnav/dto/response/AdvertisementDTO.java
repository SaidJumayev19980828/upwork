package com.nasnav.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nasnav.dto.ProductBaseInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdvertisementDTO {

    private Long id;
    private ProductBaseInfo product;
    private Integer coins;
    private Integer likes;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime creationDate;
}
