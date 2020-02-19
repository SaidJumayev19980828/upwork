import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.nasnav.NavBox;
import com.nasnav.dao.FilesRepository;
import com.nasnav.dao.ProductImagesRepository;
import com.nasnav.security.AuthenticationFilter;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc
@PropertySource("classpath:database.properties")
@NotThreadSafe 
@ContextConfiguration(initializers = BaseDirInitialzer.class) //overrides the property "files.basepath" to use temp dir 
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD) //creates a new context with new temp dir for each test method
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Products_image_bulk_API_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class ProductImageBulkUrlUpload {
	private static final String PRODUCT_IMG_BULK_URL = "/product/image/bulk/url";

	private static final String USER_TOKEN = "101112";
	
	private static final String STORE_ADMIN_TOKEN = "ssErf33";
	private static final String OTHER_ORG_ADMIN_TOKEN = "131415";
	private static final String TEST_ZIP_DIR = "src/test/resources/img_bulk_zip";
	private static final String TEST_CSV = "img_bulk_barcode.csv";
	
	
	@Value("${files.basepath}")
	private String basePathStr;

	private Path basePath;
	
	
	@Autowired
	private FilesRepository filesRepo;
	
	@Autowired
	private ProductImagesRepository imgRepo;
	
	
	
	@Autowired
	private  MockMvc mockMvc;
	
	
	
	
	@Test
	public void updateImgBulkNoAuthz() throws IOException, Exception {
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
		performFileUpload(TEST_CSV, jsonBytes, "NON-EXISTING-TOKEN")
	             .andExpect(status().is(401));
		
		assertNoImgsImported();
	}
	
	
	
	
	
	
	@Test
	public void updateImgBulkNoAuthN() throws IOException, Exception {
		
		byte[] jsonBytes = createDummyUploadRequest().toString().getBytes();
		
		performFileUpload(TEST_CSV, jsonBytes, STORE_ADMIN_TOKEN)
	             .andExpect(status().is(403));
		
		assertNoImgsImported();
	}
	
	
	
	
	private JSONObject createDummyUploadRequest() {
		JSONObject metaData = new JSONObject();		
		
		metaData.put("type", 7);
		metaData.put("priority", 1);
		
		return metaData;
	}
	
	
	
	
	private ResultActions performFileUpload(String csvFileName, byte[] json, String userToken) throws IOException, Exception {
		
		MockMultipartFile csvPart = createCsvPart(csvFileName);				
		MockPart jsonPart = createJsonPart(json);
		
		ResultActions result = 
		    mockMvc.perform(MockMvcRequestBuilders
		    					.multipart(PRODUCT_IMG_BULK_URL)
				                 .file(csvPart)
				                 .part(jsonPart)
				                 .header(AuthenticationFilter.TOKEN_HEADER, userToken));
		return result;
	}
	
	
	
	
	private void assertNoImgsImported() throws IOException {
		assertEquals(0L, imgRepo.count());
		assertEquals(0L, filesRepo.count());
		try(Stream<Path> files = Files.walk(basePath)){
			Long cnt = files.filter(path -> !Files.isDirectory(path) ).count();
			assertEquals("no files should exist in the save directory", 0L, cnt.longValue());
		}
	}
	
	
	

	private MockPart createJsonPart(byte[] json) {
		MockPart jsonPart = new MockPart("properties", "properties",  json);		
		jsonPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		return jsonPart;
	}
	
	
	
	
	private MockMultipartFile createZipPart(String zipFileName) throws IOException {
		Path zip = Paths.get(TEST_ZIP_DIR).resolve(zipFileName).toAbsolutePath();
		assertTrue(Files.exists(zip));
		byte[] zipData = Files.readAllBytes(zip);		
		MockMultipartFile zipPart = new MockMultipartFile("imgs_zip", zipFileName, "application/zip", zipData);
		return zipPart;
	}
	
	
	
	
	
	private MockMultipartFile createCsvPart(String csvFileName) throws IOException {
		Path csv = Paths.get(TEST_ZIP_DIR).resolve(csvFileName).toAbsolutePath();
		assertTrue(Files.exists(csv));
		byte[] csvData = Files.readAllBytes(csv);		
		MockMultipartFile csvPart = new MockMultipartFile("imgs_barcode_csv", csvFileName, "text/csv", csvData);
		return csvPart;
	}

}
