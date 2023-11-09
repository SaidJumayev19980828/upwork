package com.nasnav.util;

import com.nasnav.commons.utils.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class MultipartFileUtils {
    public static MultipartFile convert(String base64Content, String fileName,String fileType) throws IOException {
        byte[] content = Base64.getDecoder().decode(base64Content);
        return new Base64MultipartFile(content, fileName , fileType);
    }

    private static class Base64MultipartFile implements MultipartFile {
        private final byte[] content;
        private final String fileName;
        private final String fileType;

        public Base64MultipartFile(byte[] content, String fileName,String fileType) {
            this.content = content;
            this.fileName = fileName;
            this.fileType = fileType;

        }

        @Override
        public String getName() {
            return StringUtils.cleanPath(fileName);
        }

        @Override
        public String getOriginalFilename() {
            return StringUtils.cleanPath(fileName);
        }

        @Override
        public String getContentType() {
            return fileType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return content;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            new FileOutputStream(dest).write(content);
        }
    }
}