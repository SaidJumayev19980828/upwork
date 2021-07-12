package com.nasnav.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.enumerations.SearchType;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SearchParameters {
    public Long org_id;
    public String keyword;
    public Integer start;
    public Integer count;
    public List<SearchType> type;
}
