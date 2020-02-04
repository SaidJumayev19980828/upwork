package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Image {

    private String url;

    @JsonProperty("thumb_url")
    private String thumb;

    @JsonProperty("resized_url")
    private String resized;

    private String small;
}
