package com.nasnav.service.model.mail;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;

import java.io.InputStream;

@Data
@AllArgsConstructor
public class MailAttachment {
    private String fileName;
    private ByteArrayResource data;
}
