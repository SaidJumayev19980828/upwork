package com.nasnav.test.integration.sallab;

import static com.nasnav.integration.sallab.ElSallabIntegrationParams.AUTH_GRANT_TYPE;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.CLIENT_ID;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.CLIENT_SECRET;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.PASSWORD;
import static com.nasnav.integration.sallab.ElSallabIntegrationParams.USERNAME;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.springframework.http.HttpMethod.POST;

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
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.NavBox;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.IntegrationMappingRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dto.OrganizationIntegrationInfoDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.sallab.ElSallabIntegrationParams;
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
    private static final String AUTH_SERVER_URL = "https://test.salesforce.com";
    private static final String MOCK_SERVER_URL = "http://127.0.0.1";
    
  private static final String SERVER_URL = MOCK_SERVER_URL;
//  private static final String SERVER_URL = SALLAB_SERVER_URL;
  
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
	
	
	@Rule
	 public MockServerRule mockServerRule = new MockServerRule(this);
	
	
	
	
	@Before
	public void init() throws Exception {			
		String serverFullUrl = SALLAB_SERVER_URL;
		String server2FullUrl = SALLAB_SEVER_URL_2;
		String authServerUrl = AUTH_SERVER_URL;
		
		if(usingMockServer) {
			serverFullUrl = testCommons.initElSallabMockServer(mockServerRule);
			server2FullUrl = serverFullUrl;
			authServerUrl = serverFullUrl;
		}
		
		registerIntegrationModule(serverFullUrl, server2FullUrl, authServerUrl);
	}
	
	
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/El_sallab_integration_products_import_test_data.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void importProductsTest() throws Throwable {
		int productCount = 2;
		int variantCount = 4;
		long countVariantsBefore = variantRepo.count();
		long countProductsBefore = productRepo.count();
		long countShopsBefore = shopsRepo.count();
		
		//------------------------------------------------		
		//call product import api
		JSONObject requestJson = 
				json()
					.put("dryrun", false)
					.put("update_product", true)
					.put("update_stocks", true)
					.put("currency", 1)
					.put("encoding", "UTF-8")
					.put("page_count", 100);
		
		HttpEntity<Object> request = getHttpEntity(requestJson.toString(), "hijkllm");
        ResponseEntity<Integer> response = template.exchange("/integration/import/products", POST, request, Integer.class);       

		//------------------------------------------------

        Thread.sleep(2000);
		//------------------------------------------------
		//test the mock api was called
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
				        .withPath("/services/data/v44.0/query/.+"),
				      VerificationTimes.exactly(1)
				    );
			
			mockServerRule.getClient().verify(
				      request()
				        .withMethod("GET")
				        .withPath("/ElSallab.Webservice/SallabService.svc/getItemPriceBreakdown"),
				      VerificationTimes.exactly(4)
				    );
			
			mockServerRule.getClient().verify(
				      request()
				        .withMethod("GET")
				        .withPath("/ElSallab.Webservice/SallabService.svc/getItemStockBalance"),
				      VerificationTimes.exactly(4)
				    );
			
		}
		//------------------------------------------------
		//test imported brands were created
		//test the imported products were created
		
		long countProductsAfter = productRepo.count();
		long countVariantsAfter = variantRepo.count();
		long countShopsAfter = shopsRepo.count();
		long shopsCount = 1;
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotEquals("products were imported", 0L, countProductsAfter - countProductsBefore);
		if(usingMockServer) {
			assertEquals("assert brands were imported", 1L, brandRepo.count());
			assertTrue("all imported products have integration mapping" , allProductHaveMapping());
			assertEquals("check number of remaining pages to import", 0, response.getBody().intValue());
			assertEquals("check number of imported products" , productCount, countProductsAfter - countProductsBefore);
			assertEquals("check number of imported variants" , variantCount, countVariantsAfter - countVariantsBefore);
			assertEquals("check number of imported shops" , shopsCount, countShopsAfter - countShopsBefore);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	private void registerIntegrationModule(String serverFullUrl, String server2FullUrl, String authServerUrl) throws BusinessException {
		Map<String,String> params = new HashMap<>();
		params.put("SERVER_URL", serverFullUrl);
		params.put("SERVER_2_URL", server2FullUrl);
		params.put("AUTH_SERVER_URL", authServerUrl);
		params.put(AUTH_GRANT_TYPE.getValue(), "password");
		params.put(CLIENT_ID.getValue(), "3MVG98_Psg5cppyZgL4kzqXARpsy8tyvcM1d8DwhODOxPiDTnqaf71BGU2cmzBpvf8l_myMTql31bhVa.ar8V");
		params.put(CLIENT_SECRET.getValue(), "4085100268240543918");
		params.put(USERNAME.getValue(), "mzaklama@elsallab.com.devsanbox");
		params.put(PASSWORD.getValue(), "CloudzLab001tBHMDjhBGvDRsmWMrfog0oHG7");
		
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
