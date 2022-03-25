package com.nasnav.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BrandIdAndPriority {

    @JsonProperty("brand_id")
    private Long id;
    private Integer priority;

}
