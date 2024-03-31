package com.nasnav.test;


import com.nasnav.service.impl.FileServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;


import static org.assertj.core.api.Assertions.assertThatCode;

class FileServiceImplTest {

    @Test
    void saveFileFor3DModel() {
        FileServiceImpl fileService = new FileServiceImpl();

        MultipartFile file = null;
        Long modelId = null;
        String uniqeFileName = null;
        assertThatCode(() -> fileService.saveFileFor3DModel(file, uniqeFileName, modelId))
                .isInstanceOf(IllegalArgumentException.class);
    }

}