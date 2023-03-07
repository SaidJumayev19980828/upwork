package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
@AllArgsConstructor
@Data
public class AddonStockResponse {
	@JsonProperty(value = "success")
    private boolean success;

 @JsonProperty(value = "addonStock_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long addonStockId;

    public AddonStockResponse(Long addonStockId){
        this.success = true;
        this.addonStockId = addonStockId;
    }
}
