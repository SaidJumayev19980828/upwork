package com.nasnav.yeshtery.test;

import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.WebScrapingLogRepository;
import com.nasnav.dto.response.RestResponsePage;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.AddressesEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.WebScrapingLog;
import com.nasnav.service.*;
import com.nasnav.service.impl.WebScrapingServiceImplementation;
import com.nasnav.service.notification.NotificationService;
import com.nasnav.yeshtery.controller.v1.WebScrapingController;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;
import net.jcip.annotations.NotThreadSafe;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static com.nasnav.yeshtery.test.commons.TestCommons.getHttpEntity;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureWebTestClient
@NotThreadSafe
@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Web_Scraping.sql"})
@Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
class WebScrapingTest extends AbstractTestWithTempBaseDir {
    @Autowired
    private TestRestTemplate template;

    @Autowired
    private MockMvc mockMvc;


    @Mock
    private WebScrapingLogRepository scrapingLogRepository;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private CsvExcelDataExportService csvExportService;
    @Mock
    private SecurityService securityService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private FileService fileService;
    @Mock
    private MultipartFile file;

    @InjectMocks
    private WebScrapingServiceImplementation webScrapingService;


    private static final String TEST_FILE_DIR = "src/test/resources/files";
    private static final String TEST_FILE = "Master_File.csv";


    @Test
    public void getAllScrapped() {
        HttpEntity<Object> httpEntity = getHttpEntity("123");
        ParameterizedTypeReference<RestResponsePage<WebScrapingLog>> responseType = new ParameterizedTypeReference<>() {
        };

        ResponseEntity<RestResponsePage<WebScrapingLog>> response = template.exchange("/v1/scraping?type=URL_BASED&orgId=" + 99001, HttpMethod.GET, httpEntity, responseType);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void getAllScrappedWithError() {
        HttpEntity<Object> httpEntity = getHttpEntity("123");

        ResponseEntity<String> response = template.exchange("/v1/scraping?orgId=" + 9001, HttpMethod.GET, httpEntity, String.class);
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
        WebScrapingService mockScrapingService = mock(WebScrapingService.class);
        when(mockScrapingService.scrapeDataFromFile(manualCollect, bootName, orgId, file)).thenReturn(expectedLog);
        WebScrapingController controller = new WebScrapingController(mockScrapingService);
        WebScrapingLog actualLog = controller.scrapeData("123", manualCollect, bootName, orgId, file);
        assertEquals(expectedLog, actualLog);
    }


    @Test
    public void test_deleteScrapingLog_nonExistingId() {
        WebScrapingService webScrapingService = mock(WebScrapingService.class);
        WebScrapingController webScrapingController = new WebScrapingController(webScrapingService);
        Long id = 100L;
        doThrow(new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.SCRAPPING$002, id)).when(webScrapingService).deleteScrapingLog(id);

        assertThrows(RuntimeBusinessException.class, () -> webScrapingController.deleteScrapingLog("123", id));

        verify(webScrapingService).deleteScrapingLog(id);
    }

    @Test
    public void test_deleteScrapingLog_validId() {
        WebScrapingService webScrapingService = mock(WebScrapingService.class);
        WebScrapingController webScrapingController = new WebScrapingController(webScrapingService);
        Long id = 1L;
        webScrapingController.deleteScrapingLog("123", id);
        verify(webScrapingService).deleteScrapingLog(id);
    }

    @Test
    @Disabled("The test method is extremelly slow. TO BE ENHANCED.")
    public void test_scrapeDataFromFile_manualCollectValidFile() throws Exception {
        String testFileDir = TEST_FILE_DIR;
        Path file = Paths.get(testFileDir).resolve(TEST_FILE).toAbsolutePath();

        byte[] fileData = Files.readAllBytes(file);
        MockMultipartFile imgsPart = new MockMultipartFile("file", TEST_FILE, "text/csv", fileData);
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.multipart("/v1/scraping/file")
                .file(imgsPart)
                .param("manualCollect", "true")
                .param("bootName", "bootName")
                .param("orgId", "99001")

                .header(TOKEN_HEADER, "101112"));

        result.andExpect(status().is(200));

        assertNotNull(result);
    }

    @Test
    public void test_scrapeDataFromFile_saveWebScrapingLog() throws IOException, BusinessException, SQLException, IllegalAccessException, InvocationTargetException {

        // Create mock objects
        Boolean manualCollect = true;
        String bootName = "bootName";
        Long orgId = 99001L;
        MultipartFile file = Mockito.mock(MultipartFile.class);

        // Mock method calls
        Mockito.when(file.getBytes()).thenReturn(new byte[]{});
        Mockito.when(organizationService.getOrganizationById(orgId)).thenReturn(this.buildOrg());
        Mockito.when(csvExportService.generateProductsFile(Mockito.any(), Mockito.anyBoolean())).thenReturn(new ByteArrayOutputStream());
        Mockito.when(scrapingLogRepository.save(Mockito.any())).thenReturn(new WebScrapingLog());

        assertThrows(RuntimeBusinessException.class, () -> webScrapingService.scrapeDataFromFile(manualCollect, bootName, orgId, file));
    }

    public OrganizationEntity buildOrg() {
        AddressesEntity addresses = new AddressesEntity();
        addresses.setId(99001L);
        addresses.setAddressLine1("addressLine1");
        addresses.setAddressLine2("addressLine2");
        addresses.setFirstName("city");
        addresses.setBuildingNumber("state");
        ShopsEntity shops = new ShopsEntity();
        shops.setId(99001L);

        Set<ShopsEntity> shopsList = new HashSet<>();
        shopsList.add(shops);
        shops.setAddressesEntity(addresses);
        OrganizationEntity org = new OrganizationEntity();
        org.setId(99001L);
        org.setShops(shopsList);
        org.setDescription("test");

        return org;
    }
}
