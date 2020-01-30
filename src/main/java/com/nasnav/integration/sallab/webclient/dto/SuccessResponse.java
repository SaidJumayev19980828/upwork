package com.nasnav.integration.sallab.webclient.dto;

import lombok.Data;

import java.util.List;

@Data
public class SuccessResponse {

    private String id;
    private boolean success;
    private List<String> errors;
}
