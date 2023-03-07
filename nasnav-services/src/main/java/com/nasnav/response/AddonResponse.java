package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
@AllArgsConstructor
@Data
public class AddonResponse {
	 @JsonProperty(value = "success")
	    private boolean success;

	 @JsonProperty(value = "addon_id")
	    @JsonInclude(JsonInclude.Include.NON_NULL)
	    private Long addonId;

	    public AddonResponse(Long addonId){
	        this.success = true;
	        this.addonId = addonId;
	    }
}
