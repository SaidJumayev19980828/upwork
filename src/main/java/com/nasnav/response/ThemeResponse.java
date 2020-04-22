package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ThemeResponse {
	
	//TODO: >>> this shouldn't be used for two responses, even if the response is identical now, they may change later , and at that time
	//we won't notice we changed two API's at the same time

    private Boolean success;	//TODO: >>> we don't need this, the http status is more robust indication of success

    @JsonProperty("id")
    private Integer id;

    public ThemeResponse(Integer id) {
        success = true;
        this.id = id;
    }
}
