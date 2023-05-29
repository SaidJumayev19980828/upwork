package com.nasnav.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;


@Data
public class PackageRegisteredByUserDTO {
    @JsonProperty("user_id")
    private Long userId;
    @JsonProperty("package_id")
    private Long packageId;
    private Date registeredDate;
}
