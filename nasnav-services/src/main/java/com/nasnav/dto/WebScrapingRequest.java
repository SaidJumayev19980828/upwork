package com.nasnav.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class WebScrapingRequest {
    @NotNull(message = "Organization Id is required")
    private Long organizationId;
    @NotNull(message = "Url is required")
    private String url;
}
