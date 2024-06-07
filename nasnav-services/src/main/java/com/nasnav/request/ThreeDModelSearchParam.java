package com.nasnav.request;

import lombok.Data;

@Data
public class ThreeDModelSearchParam extends BaseSearchParams {
    private String name;
    private String barcode;
    private String sku;
    private String description;
    private String color;
    private String size;
    private Integer start;
    private Integer count;
}
