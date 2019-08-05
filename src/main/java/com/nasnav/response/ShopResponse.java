package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.List;

@Data
public class ShopResponse implements Serializable {

    @JsonProperty(value = "success")
    private boolean success;

    @JsonProperty(value = "status")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ResponseStatus> responseStatus;

    @JsonIgnore
    private HttpStatus httpStatus;

    @JsonProperty(value = "store_id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Long storeId;

    public ShopResponse(){
        this.success = false;
    }

    public ShopResponse(Long storeId, HttpStatus httpStatus) {
        this.success = true;
        this.storeId = storeId;
        this.httpStatus = httpStatus;
    }

    public ShopResponse(List<ResponseStatus> responseStatus, HttpStatus httpStatus) {
        this.success = false;
        this.responseStatus = responseStatus;
        this.httpStatus = httpStatus;
    }
}
