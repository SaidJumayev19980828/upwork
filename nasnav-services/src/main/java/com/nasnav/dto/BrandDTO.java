package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BrandDTO {
    @Schema(name = "Brand ID. If empty: new brand", example = "12345")
    @JsonProperty("brand_id")
    public Long id;

    @Schema(name = "Operation type .. create or update", example = "create", required = true)
    @JsonProperty("operation")
    public String operation;

    @Schema(name = "Brand name, required for creating a brand", example = "Alfa Romeo")
    @JsonProperty("name")
    public String name;

    @Schema(name = "Brand p_name, it will be generated automatically if not provided", example = "alfa-romeo")
    @JsonProperty("p_name")
    public String pname;

    @Schema(name = "Brand priority, default value is 0 ", example = "2")
    public Integer priority;
}
