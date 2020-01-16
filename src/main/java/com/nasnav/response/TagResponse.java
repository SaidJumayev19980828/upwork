package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class TagResponse implements Serializable {

    @JsonProperty(value = "success")
    private boolean success;

    @JsonProperty(value = "tag_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long tagId;

    public TagResponse(Long tagId){
        this.success = true;
        this.tagId = tagId;
    }

}
