package com.nasnav.integration.sallab.webclient.dto;

import lombok.Data;

import java.util.List;

@Data
public class ErrorResponse {

    private String message;
    private String errorCode;
    private List<String> fields;
}
