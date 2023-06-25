package com.nasnav.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Positive;


@Data
public class PackageRegisteredByUserDTO {
    @Positive(message = "package_id can't be null or 0")
    @JsonProperty("package_id")
    private Long packageId;
}
