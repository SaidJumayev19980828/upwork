package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ImageTypeResponse {
    @JsonProperty(value = "type_id")
    private Long id;
    @JsonProperty(value = "organization_id")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Long organizationId;
    @JsonProperty(value = "label")
    private String label;
    @JsonProperty(value = "text")
    private String text;

    public ImageTypeResponse(Long id, Long organizationId, String label, String text) {
        this.id = id;
        this.organizationId = organizationId;
        this.label = label;
        this.text = text;
    }
}
