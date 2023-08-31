package com.nasnav.dto.response;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandsDTO {
    private Long id;

    private Integer categoryId;

    private String bannerImage;

    private String pname;

    private String darkLogo;

    private String coverUrl;
}