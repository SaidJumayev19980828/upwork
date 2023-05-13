package com.nasnav.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiError {

    private String code;
    private String message_En;
    private String message_Ar;

}