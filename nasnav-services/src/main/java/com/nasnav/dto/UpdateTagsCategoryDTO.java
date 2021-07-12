package com.nasnav.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(SnakeCaseStrategy.class)
public class UpdateTagsCategoryDTO {

    private Long categoryId;

    @JsonProperty("tags")
    private List<Long> tagsIds;
}
