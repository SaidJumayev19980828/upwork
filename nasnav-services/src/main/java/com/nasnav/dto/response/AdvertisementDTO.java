package com.nasnav.dto.response;

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
    private Integer maxCoins;
    private LocalDateTime creationDate;
}
