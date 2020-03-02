package com.nasnav.test.integration.sallab;

import static com.nasnav.integration.enums.IntegrationParam.IMG_SERVER_PASSWORD;
import static com.nasnav.integration.enums.IntegrationParam.IMG_SERVER_USERNAME;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.AUTH_GRANT_TYPE;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.CLIENT_ID;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.CLIENT_SECRET;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.PASSWORD;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.USERNAME;
import static com.nasnav.service.ProductImageService.PRODUCT_IMAGE;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.junit.MockServerRule;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.NavBox;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.IntegrationMappingRepository;
import com.nasnav.dao.ProductImagesRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.OrganizationIntegrationInfoDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.enums.IntegrationParam;
import com.nasnav.persistence.ProductVariantsEntity;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource("classpath:database.properties")
@AutoConfigureWebTestClient
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/El_sallab_integration_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
@DirtiesContext
public class ElSallabIntegrationTest {
	
	private static final String SALLAB_SERVER_URL = "https://azizsallab--DevSanbox.cs80.my.salesforce.com";    
    private static final String SALLAB_SEVER_URL_2 = "http://41.33.113.70";
    private static final String IMG_SERVER_URL = "https://azizsallab.my.salesforce.com";
    private static final String IMG_AUTH_SERVER_URL = "https://login.salesforce.com";
    private static final String AUTH_SERVER_URL = "https://test.salesforce.com";
    private static final String MOCK_SERVER_URL = "http://127.0.0.1";
    private static final String MOCK_SERVER_AUTH_TOKEN = "00D250000009BEF!AQcAQHE4mvVZ6hmXm7_4y1s26_FIG0yMMVvq58ecs1GshIRcQE2l5d40r_NR8AJA5g.gko2fNdCctisUWg4cOIGhqnK9xMma";
    
//    private static final String SERVER_URL = MOCK_SERVER_URL;
  private static final String SERVER_URL = SALLAB_SERVER_URL;
  
    private static final boolean usingMockServer = SERVER_URL.equals(MOCK_SERVER_URL);
	
    
	private static final Long ORG_ID = 99001L;
	
	
	@Autowired
	private IntegrationService integrationService;
	
	
	@Autowired
	private ElSallabIntegrationTestCommon testCommons;
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private ShopsRepository shopsRepo;
	
	@Autowired
    private TestRestTemplate template;
	
	@Autowired
	private BrandsRepository brandRepo;
	
	@Autowired
	private ProductVariantsRepository variantRepo;
	
	@Autowired
	private IntegrationMappingRepository mappingRepo;
		
	@Autowired
	private JdbcTemplate jdbc;
	
	@Autowired
	private StockRepository stocksRepo;
	
	@Autowired
	private ProductImagesRepository imgRepo;
	

	@Value("${files.basepath}")
	private String basePathStr;

	private Path basePath;

	
	@Rule
	 public MockServerRule mockServerRule = new MockServerRule(this);
	
	
	
	
	@Before
	public void init() throws Exception {			
		initIntegrationModules();		
	}








	private void printDummyImageSavePath() {
		this.basePath = Paths.get(basePathStr);
		
		System.out.println("Test Files Base Path  >>>> " + basePath.toAbsolutePath());
	}








	private void initIntegrationModules() throws Exception, BusinessException {
		String serverFullUrl = SALLAB_SERVER_URL;
		String server2FullUrl = SALLAB_SEVER_URL_2;
		String authServerUrl = AUTH_SERVER_URL;
		String imgServerUrl = IMG_SERVER_URL;
		String imgAuthServerUrl = IMG_AUTH_SERVER_URL;
		
		if(usingMockServer) {
			serverFullUrl = testCommons.initElSallabMockServer(mockServerRule);
			server2FullUrl = serverFullUrl;
			authServerUrl = serverFullUrl;
			imgServerUrl = serverFullUrl;
			imgAuthServerUrl = serverFullUrl;
		}
		
		registerIntegrationModule(serverFullUrl, server2FullUrl, authServerUrl, imgServerUrl, imgAuthServerUrl);
	}
	
	
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/El_sallab_integration_products_import_test_data.sql","/sql/el_sallab_tags_insert.sql", "/sql/el_sallab_brands_insert.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void importProductsTest() throws Throwable {
		Count expected = new Count();
		expected.products = 2L;
		expected.variants = 5L;
		expected.tags = expected.products*3;
		expected.extraAttr = expected.variants*5;
		expected.stocks = expected.variants;
		expected.pages = 1;
		
		Count countBefore = new Count();
		countBefore.variants = variantRepo.count();
		countBefore.products = productRepo.count();
		countBefore.shops = shopsRepo.count();
		countBefore.brands = brandRepo.count();		
		countBefore.tags = countProductTags();		
		countBefore.extraAttr = countProductExtraAttr();
		countBefore.stocks = stocksRepo.count();
		
		//------------------------------------------------		
		//call product import api
		JSONObject requestJson = createImportProductRequest();
		
		HttpEntity<Object> request = getHttpEntity(requestJson.toString(), "hijkllm");
        ResponseEntity<Integer> response = template.exchange("/integration/import/products", POST, request, Integer.class);       

		//------------------------------------------------

        Thread.sleep(2000);
		//------------------------------------------------
		//test the mock api was called
		verifyMockServerCalls((int)expected.variants, 1);
		
		//------------------------------------------------
		//test imported brands were created
		//test the imported products were created
		
		assertDataImported(expected, countBefore, response);
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/El_sallab_integration_products_import_test_data.sql","/sql/el_sallab_tags_insert.sql", "/sql/el_sallab_brands_insert.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void importProductsWithPaginationTest() throws Throwable {
		Count expected = new Count();
		expected.products = 1L;
		expected.variants = 2L;
		expected.tags = expected.products*3;
		expected.extraAttr = expected.variants*5;
		expected.stocks = expected.variants;
		expected.pages = 3;
		
		Count countBefore = new Count();
		countBefore.variants = variantRepo.count();
		countBefore.products = productRepo.count();
		countBefore.shops = shopsRepo.count();
		countBefore.brands = brandRepo.count();		
		countBefore.tags = countProductTags();		
		countBefore.extraAttr = countProductExtraAttr();
		countBefore.stocks = stocksRepo.count();
		
		//------------------------------------------------		
		//call product import api
		JSONObject requestJson = createImportProductRequest();
		requestJson.put("page_count", "2");
		requestJson.put("page_num", "1");
		
		HttpEntity<Object> request = getHttpEntity(requestJson.toString(), "hijkllm");
        ResponseEntity<Integer> response = template.exchange("/integration/import/products", POST, request, Integer.class);       

		//------------------------------------------------

        Thread.sleep(2000);
		//------------------------------------------------
		//test the mock api was called
		verifyMockServerCalls((int)expected.variants, 0);
		
		//------------------------------------------------
		//test imported brands were created
		//test the imported products were created
		
		assertDataImported(expected, countBefore, response);
	}


	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/El_sallab_integration_Test_Images_Insert.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void imagesImportTest() throws InterruptedException {
		printDummyImageSavePath();
		
		Integer variantNum = 2;
		Long imgsCountBefore  = imgRepo.count();
		//------------------------------------------------		
		//call product import api
		JSONObject requestJson = createImportImagesRequest();
		requestJson.put("page_count", variantNum.toString());
		requestJson.put("page_num", "1");
		
		HttpEntity<Object> request = getHttpEntity(requestJson.toString(), "hijkllm");
        ResponseEntity<String> response = template.exchange("/integration/import/product_images", POST, request, String.class);       
        
		//------------------------------------------------

        Thread.sleep(2000);
		//------------------------------------------------
        assertEquals(OK, response.getStatusCode());
		//test the mock api was called
        verifyImageImportMockServerCalls(variantNum,1);
		
		//------------------------------------------------
		//test imported brands were created
		//test the imported products were created
		
        Long imgsCountAfter  = imgRepo.count();
        assertEquals(variantNum.longValue(), imgsCountAfter - imgsCountBefore);
	}
	
	
	
	
	
	private JSONObject createImportImagesRequest() {
		return json()
				.put("page_num", 1)
				.put("page_count", 500)
				.put("type", PRODUCT_IMAGE)
				.put("priority", 0);
	}








	private void verifyImageImportMockServerCalls(Integer variantCount, Integer additionalPageCount) {
		if(usingMockServer) {
			mockServerRule.getClient().verify(
				      request()
				        .withMethod("GET")
				        .withPath("/services/data/v44.0/query"),
				      VerificationTimes.exactly(1)
				    );
			
//			mockServerRule.getClient().verify(
//				      request()
//				        .withMethod("GET")
//				        .withPath("/services/data/v44.0/query/.+")
//				        .withHeader("Authorization", "Bearer "+MOCK_SERVER_AUTH_TOKEN),
//				      VerificationTimes.exactly(additionalPageCount)
//				    );
			
			mockServerRule.getClient().verify(
				      request()
				        .withMethod("GET")
				        .withPath("/services/data/v36.0/sobjects/Attachment/.+/Body")
				        .withHeader("Authorization", "Bearer "+MOCK_SERVER_AUTH_TOKEN),
				      VerificationTimes.exactly(variantCount)
				    );
	   }
	}
	
	





	private void verifyMockServerCalls(Integer variantCount, Integer additionalPageCount) throws AssertionError {
		if(usingMockServer) {
			mockServerRule.getClient().verify(
				      request()
				        .withMethod("GET")
				        .withPath("/services/data/v44.0/query"),
				      VerificationTimes.exactly(1)
				    );
			
			mockServerRule.getClient().verify(
				      request()
				        .withMethod("GET")
				        .withPath("/services/data/v44.0/query/.+")
				        .withHeader("Authorization", "Bearer "+MOCK_SERVER_AUTH_TOKEN),
				      VerificationTimes.exactly(additionalPageCount)
				    );
			
			mockServerRule.getClient().verify(
				      request()
				        .withMethod("GET")
				        .withPath("/ElSallab.Webservice/SallabService.svc/getItemPriceBreakdown"),
				      VerificationTimes.exactly(variantCount)
				    );
			
			mockServerRule.getClient().verify(
				      request()
				        .withMethod("GET")
				        .withPath("/ElSallab.Webservice/SallabService.svc/getItemStockBalance"),
				      VerificationTimes.exactly(variantCount)
				    );
			
		}
	}








	private JSONObject createImportProductRequest() {
		JSONObject requestJson = 
				json()
					.put("dryrun", false)
					.put("update_product", true)
					.put("update_stocks", true)
					.put("currency", 1)
					.put("encoding", "UTF-8")
					.put("page_count", 100);
		return requestJson;
	}








	private void assertDataImported(Count expected, Count countBefore, ResponseEntity<Integer> response) {
		long countProductsAfter = productRepo.count();
		long countVariantsAfter = variantRepo.count();
		long countShopsAfter = shopsRepo.count();
		long shopsCount = 1L;
		long countBrandsAfter = brandRepo.count();
		long productTagsAfter = countProductTags();		
		long extraAttrAfter = countProductExtraAttr();
		long stockCountAfter = stocksRepo.count();
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotEquals("products were imported", 0L, countProductsAfter - countBefore.products);
		if(usingMockServer) {
			assertEquals("assert brands were imported", 0L, countBrandsAfter - countBefore.brands);
			assertTrue("all imported products have integration mapping" , allProductHaveMapping());
			assertEquals("check total number of pages to import", expected.pages, response.getBody().intValue());
			assertEquals("check number of imported products" , expected.products, countProductsAfter - countBefore.products);
			assertEquals("check number of imported variants" , expected.variants, countVariantsAfter - countBefore.variants);
			assertEquals("check number of imported shops" , shopsCount, countShopsAfter - countBefore.shops);
			assertEquals("check the number of added product tags", expected.tags , productTagsAfter - countBefore.tags);
			assertEquals("check the number of added extra-attributes", expected.extraAttr , extraAttrAfter- countBefore.extraAttr);
			assertEquals("check the number of added stocks", expected.stocks , stockCountAfter- countBefore.stocks);
		}
	}




	private Long countProductExtraAttr() {
		return 	jdbc.queryForObject("select count(*) from public.products_extra_attributes", Long.class);
	}




	private long countProductTags() {
		return 	jdbc.queryForObject("select count(*) from public.product_tags", Long.class); 
	}
	
	
	
	
	
	
	
	private void registerIntegrationModule(String serverFullUrl, String server2FullUrl, String authServerUrl, String imgServerUrl, String imgAuthServerUrl) throws BusinessException {
		Map<String,String> params = new HashMap<>();
		params.put("SERVER_URL", serverFullUrl);
		params.put("SERVER_2_URL", server2FullUrl);
		params.put("AUTH_SERVER_URL", authServerUrl);
		params.put("IMG_SERVER_URL", imgServerUrl);
		params.put(IntegrationParam.IMG_AUTH_SERVER_URL.getValue(), imgAuthServerUrl);
		params.put(AUTH_GRANT_TYPE.getValue(), "password");
		params.put(CLIENT_ID.getValue(), "3MVG98_Psg5cppyZgL4kzqXARpsy8tyvcM1d8DwhODOxPiDTnqaf71BGU2cmzBpvf8l_myMTql31bhVa.ar8V");
		params.put(CLIENT_SECRET.getValue(), "4085100268240543918");
		params.put(USERNAME.getValue(), "mzaklama@elsallab.com.devsanbox");
		params.put(PASSWORD.getValue(), "CloudzLab001tBHMDjhBGvDRsmWMrfog0oHG7");
		params.put(IMG_SERVER_USERNAME.getValue(), "mzaklama@elsallab.com");
		params.put(IMG_SERVER_PASSWORD.getValue(), "CloudzLab001rMVQgFDbydbFQ4PLEUYycvn0");
		
		OrganizationIntegrationInfoDTO integrationInfo = new OrganizationIntegrationInfoDTO();
		integrationInfo.setIntegrationModule("com.nasnav.integration.sallab.ElSallabIntegrationModule");
		integrationInfo.setMaxRequestRate(1);
		integrationInfo.setOrganizationId(99001L);
		integrationInfo.setIntegrationParameters(params);
		
		integrationService.registerIntegrationModule(integrationInfo);
	}
	
	
	

	private boolean allProductHaveMapping() {
		return variantRepo
				.findByOrganizationId(ORG_ID)
				.stream()
				.map(ProductVariantsEntity::getId)
				.map(id -> id.toString())
				.map(id -> mappingRepo.findByOrganizationIdAndMappingType_typeNameAndLocalValue(ORG_ID, "PRODUCT_VARIANT", id))
				.allMatch(Optional::isPresent);			
	}
	

}


class Count{
	public long variants;
	public long products;
	public long shops;
	public long brands;		
	public long tags;		
	public long extraAttr;
	public long stocks;
	public long pages;
}
