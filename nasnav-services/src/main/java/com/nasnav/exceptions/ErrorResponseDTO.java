package com.nasnav.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {

    private String description;
    private String message;
    private String error;

    public ErrorResponseDTO(String description,String message,String error){
        this.description = description;
        this.message = message;
        this.error = error;
    }

    public ErrorResponseDTO(String message,String error){
        this.message = message;
        this.error = error;
    }


    public ErrorResponseDTO(String message){
        this.message = message;
    }
}
