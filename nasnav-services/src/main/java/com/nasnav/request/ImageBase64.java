package com.nasnav.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
public class ImageBase64 {
   @NotBlank(message = "\"base64\" must not be blank or null")
   private String base64;
   @NotBlank(message = "\"fileName\" must not be blank or null")
   private String fileName ;
   @NotBlank(message = "\"fileType\" must not be blank or null")
   private String fileType ;
}
