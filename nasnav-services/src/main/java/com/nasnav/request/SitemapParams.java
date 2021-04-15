package com.nasnav.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SitemapParams {
    private String url;
    private boolean include_products;
    private boolean include_collections;
    private boolean include_brands;
    private boolean include_tags;
    private boolean include_tags_tree;
}
