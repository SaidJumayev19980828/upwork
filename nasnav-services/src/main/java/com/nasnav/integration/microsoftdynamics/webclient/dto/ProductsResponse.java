package com.nasnav.integration.microsoftdynamics.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;


@Data
public class ProductsResponse {
    @JsonProperty("CurrentPage")
    private int currentPage;
    
    @JsonProperty("TotalPages")
    private int totalPages;
    
    private List<Product> products;
}
