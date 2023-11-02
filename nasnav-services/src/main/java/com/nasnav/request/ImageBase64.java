package com.nasnav.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class ImageBase64 {
   @NotBlank(message = "\"base64\" must not be blank or null")
   private String base64;
   @NotBlank(message = "\"fileName\" must not be blank or null")
   private String fileName ;
}
