package com.nasnav.integration.sallab.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ProductsResponse {
    @JsonProperty("totalSize")
    public Integer totalSize;
    @JsonProperty("done")
    public Boolean done;
    @JsonProperty("nextRecordsUrl")
    public String nextRecordsUrl;
    public List<Record> records;
}
