package com.nasnav.commons.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FilesUtils {
    protected final static Log logger = LogFactory.getLog(FilesUtils.class);
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
            if(detectedType != null && detectedType.length() > 0) {
                return excelFileType.contains(detectedType);
            }
            return false;

        } catch (IOException e) {
            logger.error("===== failed to access uploaded file");
            return false;
        }
    }

    public static boolean isCsv(MultipartFile file) {
        Tika tika = new Tika();
        String detectedType;
        try {
            detectedType = tika.detect(file.getBytes());
            if(detectedType != null && detectedType.length() > 0) {
                return csvFileType.contains(detectedType);
            }
            return false;

        } catch (IOException e) {
            logger.error("===== failed to access uploaded file");
            return false;
        }
    }
}
