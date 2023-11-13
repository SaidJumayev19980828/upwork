package com.nasnav.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;



@AllArgsConstructor
@Getter
@Setter
public class ContactUsFeedBackRequestDto {
    @NotBlank(message = "Mail content can not be empty")
    private String message;

    private Long formId;


}