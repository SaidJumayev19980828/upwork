package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

public class CategoryDTO {
    @Getter
    @ApiModel(value = "Category creation or updating object")
    public static class CategoryModificationObject{
        @ApiModelProperty(value = "Category id", example = "123", required = false)
        @JsonProperty("id")
        public Long id;

        @ApiModelProperty(value = "Category's name", example = "Perfumes", required = false)
        @JsonProperty("name")
        public String name;

        @ApiModelProperty(value = "Type of operation", example = "create", required = true)
        @JsonProperty("operation")
        public String operation;

        @ApiModelProperty(value = "Category's parent ID", example = "123", required = false)
        @JsonProperty("parent_id")
        public Integer parentId;

        @ApiModelProperty(value = "Category's logo", example = "/categories/logos/564961451_56541.jpg", required = false)
        @JsonProperty("logo")
        public String logo;
    }
}
