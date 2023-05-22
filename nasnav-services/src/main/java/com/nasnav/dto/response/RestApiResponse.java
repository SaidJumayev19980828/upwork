package com.nasnav.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class RestApiResponse<T> {

    private boolean status;
    private ApiError error;
    private T result;


    public static <T> RestApiResponse<T> ok(T result) {
        return RestApiResponse.<T>builder()
                .status(true)
                .result(result)
                .build();
    }
}
