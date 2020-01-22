package com.nasnav.integration.microsoftdynamics.webclient.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;


@Data
public class ProductsResponse {
    @JsonProperty("CurrentPage")
    private int currentPage;
    
    @JsonProperty("TotalPages")
    private int totalPages;
    
    private List<Product> products;
}
