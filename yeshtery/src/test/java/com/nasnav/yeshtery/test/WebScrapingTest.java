package com.nasnav.yeshtery.test;

import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.WebScrapingLogRepository;
import com.nasnav.dto.response.RestResponsePage;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.WebScrapingLog;
import com.nasnav.service.WebScrapingService;
import com.nasnav.yeshtery.controller.v1.WebScrapingController;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static com.nasnav.yeshtery.test.commons.TestCommons.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@AutoConfigureWebTestClient
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Web_Scraping.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class WebScrapingTest  extends AbstractTestWithTempBaseDir {
    @Autowired
    private TestRestTemplate template;

    @Autowired
    private MockMvc mockMvc;


    private static final String TEST_FILE_DIR = "src/test/resources/files";
    private static final String TEST_FILE = "Master_File.csv";

    @Test
    public void getAllScrapped(){
        HttpEntity<Object> httpEntity = getHttpEntity("123");
        ParameterizedTypeReference<RestResponsePage<WebScrapingLog>> responseType = new ParameterizedTypeReference<>() {
        };

        ResponseEntity<RestResponsePage<WebScrapingLog>> response = template.exchange("/v1/scraping?orgId=" + 99001 , HttpMethod.GET, httpEntity, responseType);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void getAllScrappedWithError(){
        HttpEntity<Object> httpEntity = getHttpEntity("123");

        ResponseEntity<String> response = template.exchange("/v1/scraping?orgId=" + 9001 , HttpMethod.GET, httpEntity,String.class);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void fileScrappingException() throws Exception {
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.multipart("/v1/scraping/file?manualCollect=true&bootName=Tseppas Bot")
                .header(TOKEN_HEADER, "101112"));
        result.andExpect(status().is(400));
    }

    @Test
    public void test_successfully_scrapes_data_from_file() throws BusinessException, SQLException, IOException, InvocationTargetException, IllegalAccessException {
        Boolean manualCollect = true;
        String bootName = "boot";
        Long orgId = 99001L;
        Path fileValue = Paths.get(TEST_FILE_DIR).resolve(TEST_FILE).toAbsolutePath();
        byte[] fileData = Files.readAllBytes(fileValue);
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/plain", fileData);
        WebScrapingLog expectedLog = new WebScrapingLog();
        WebScrapingService mockScrapingService = Mockito.mock(WebScrapingService.class);
        when(mockScrapingService.scrapeDataFromFile(manualCollect, bootName, orgId, file)).thenReturn(expectedLog);
        WebScrapingController controller = new WebScrapingController(mockScrapingService);
        WebScrapingLog actualLog = controller.scrapeData(manualCollect, bootName, orgId, file);
        assertEquals(expectedLog, actualLog);
    }
}
