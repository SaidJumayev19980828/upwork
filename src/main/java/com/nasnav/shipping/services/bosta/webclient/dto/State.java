package com.nasnav.shipping.services.bosta.webclient.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
//can be used as State or Type object (has the same naming and structure)
public class State {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long code;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String value;

    private String before;
    private String after;
}
