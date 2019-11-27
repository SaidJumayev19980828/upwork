package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TagResponse {
    @JsonProperty(value = "success")
    public boolean success;

    @JsonProperty(value = "tag_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Long tagId;

    public TagResponse(Long tagId){
        this.success = true;
        this.tagId = tagId;
    }
}
