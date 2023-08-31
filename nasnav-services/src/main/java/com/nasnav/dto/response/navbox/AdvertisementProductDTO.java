package com.nasnav.dto.response.navbox;

import com.nasnav.dto.ProductDetailsDTO;
import com.nasnav.dto.response.BrandsDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdvertisementProductDTO {
    private Long id;
    private Long productId;
    private ProductDetailsDTO productDetailsDTO;
    private BrandsDTO brandsDTO;
    private Integer coins;
    private Integer likes;
}
