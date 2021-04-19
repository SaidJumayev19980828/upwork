package com.nasnav.service.model.mail;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.io.ByteArrayResource;

@Data
@AllArgsConstructor
public class MailAttachment {
    private String fileName;
    private ByteArrayResource data;
}
