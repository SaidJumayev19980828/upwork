package com.nasnav.commons.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
@Slf4j
public class FilesUtils {
    private static Set<String> excelFileType = new HashSet<>();
    private static Set<String> csvFileType = new HashSet<>();
    static {
        excelFileType.add("application/x-tika-msoffice");
        excelFileType.add("application/x-tika-ooxml");
        // csv
        csvFileType.add("text/csv");
        csvFileType.add("text/plain");

    }
    public static boolean isExcel(MultipartFile file) {
        Tika tika = new Tika();
        String detectedType;
        try {
            detectedType = tika.detect(file.getBytes());
            if(detectedType != null && detectedType.length() > 6) {
                return excelFileType.contains(detectedType);
            }
            return false;

        } catch (IOException e) {
            log.error("===== failed to access uploaded file");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isCsv(MultipartFile file) {
        Tika tika = new Tika();
        String detectedType;
        try {
            detectedType = tika.detect(file.getBytes());
            if(detectedType != null && detectedType.length() > 6) {
                return csvFileType.contains(detectedType);
            }
            return false;

        } catch (IOException e) {
            log.error("===== failed to access uploaded file");
            e.printStackTrace();
            return false;
        }
    }
}
