package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

public class CategoryDTO {
    @Getter
    @Schema(name = "Category creation or updating object")
    public static class CategoryModificationObject{
        @Schema(name = "Category id", example = "123", required = false)
        @JsonProperty("id")
        public Long id;

        @Schema(name = "Category's name", example = "Perfumes", required = false)
        @JsonProperty("name")
        public String name;

        @Schema(name = "Type of operation", example = "create", required = true)
        @JsonProperty("operation")
        public String operation;

        @Schema(name = "Category's parent ID", example = "123", required = false)
        @JsonProperty("parent_id")
        public Long parentId;

        @Schema(name = "Category's logo", example = "/categories/logos/564961451_56541.jpg", required = false)
        @JsonProperty("logo")
        public String logo;
    }
}
