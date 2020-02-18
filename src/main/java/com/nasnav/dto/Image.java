package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@Data
public class Image {

    private String url;

    private ImageUrl thumb;

    private ImageUrl resized;

    private ImageUrl small;

    public Image(String url) {
        this.url = url;
    }

}
