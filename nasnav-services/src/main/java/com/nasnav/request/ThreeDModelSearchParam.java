package com.nasnav.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
