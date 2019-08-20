package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

@Data
public class CategoryResponse implements Serializable {

    @JsonProperty(value = "success")
    private boolean success;

    @JsonProperty(value = "category_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long categoryId;

    @JsonProperty(value = "status")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String status;

    @JsonProperty(value = "description")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;

    public CategoryResponse(Long categoryId){
        this.success = true;
        this.categoryId = categoryId;
    }

    public CategoryResponse(String status, String description){
        this.success = false;
        this.status = status;
        this.description = description;
    }
}
