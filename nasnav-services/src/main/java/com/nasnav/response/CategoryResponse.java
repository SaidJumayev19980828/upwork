package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class CategoryResponse implements Serializable {

    @JsonProperty(value = "success")
    public boolean success;

    @JsonProperty(value = "category_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Long categoryId;

    public CategoryResponse(Long categoryId){
        this.success = true;
        this.categoryId = categoryId;
    }
}
