package com.nasnav.dto.response.navbox;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SearchResult {
    private Long total;
    private List<String> suggestions;
    private Results results;


    @Data
    public static class Results{
        private List<Map<String,Object>> products;
        private List<Map<String,Object>> collections;
        private List<Map<String,Object>> tags;
    }
}



