package com.nasnav.integration.sallab.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;


@Data
public class ProductsResponse {
    @JsonProperty("totalSize")
    private Integer totalSize;
    
    @JsonProperty("done")
    private Boolean done;
    
    @JsonProperty("nextRecordsUrl")
    private String nextRecordsUrl;
    private List<Record> records;
}
