package com.nasnav.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;


@AllArgsConstructor
@Getter
@Setter
public class ContactUsRequestDto {
    @NotBlank(message = "Customer name can not be empty")
    private String customerName;
    @Email(message = "Invalid email format")
    @NotBlank(message = "Customer email can not be empty")
    private String customerEmail;
    @NotBlank(message = "Mail content can not be empty")
    private String message;


}
