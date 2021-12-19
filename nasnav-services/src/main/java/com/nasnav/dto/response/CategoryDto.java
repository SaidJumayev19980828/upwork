package com.nasnav.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CategoryDto {
    private Long id;
    private String name;
    @JsonProperty("p_name")
    private String pname;
    private String logo;
    private String cover;
    @JsonProperty("cover_small")
    private String coverSmall;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CategoryDto> children;

    private Long parent;
}
