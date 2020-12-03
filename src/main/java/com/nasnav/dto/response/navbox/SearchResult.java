package com.nasnav.dto.response.navbox;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.ProductRepresentationObject;
import com.nasnav.dto.TagsRepresentationObject;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SearchResult {
    private Long total;
    private List<String> suggestions;
    private Results results;


    @Data
    static class Results{
        private List<ProductRepresentationObject> products;
        private List<ProductRepresentationObject> collections;
        private List<TagsRepresentationObject> tags;
    }
}



