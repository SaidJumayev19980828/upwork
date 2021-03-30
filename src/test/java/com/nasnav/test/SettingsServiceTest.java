package com.nasnav.test;

import static com.nasnav.enumerations.Settings.HIDE_EMPTY_STOCKS;
import static com.nasnav.enumerations.Settings.SHOW_FREE_PRODUCTS;
import static com.nasnav.enumerations.SettingsType.PRIVATE;
import static com.nasnav.enumerations.SettingsType.PUBLIC;
import static com.nasnav.service.cart.optimizers.CartOptimizationStrategy.SAME_CITY;
import static com.nasnav.service.cart.optimizers.OptimizationStratigiesNames.SHOP_PER_SUBAREA;
import static com.nasnav.service.cart.optimizers.OptimizationStratigiesNames.WAREHOUSE;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;
import static org.json.JSONObject.NULL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.nasnav.enumerations.SettingsType;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dao.OrganizationCartOptimizationRepository;
import com.nasnav.dao.SettingRepository;
import com.nasnav.dto.request.organization.CartOptimizationSettingDTO;
import com.nasnav.dto.response.CartOptimizationStrategyDTO;
import com.nasnav.enumerations.Settings;
import com.nasnav.persistence.OrganizationCartOptimizationEntity;
import com.nasnav.persistence.SettingEntity;
import com.nasnav.service.cart.optimizers.CartOptimizationStrategy;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Organization_Test_Data_Insert_3.sql"})
@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
public class SettingsServiceTest {
	
	@Autowired
    private TestRestTemplate template;
	
	@Autowired
	private SettingRepository settingRepo;
	
	@Autowired
	private OrganizationCartOptimizationRepository optimizationRepo;
	
	@Autowired
	private ObjectMapper objectMapper;




	@Test
	public void postPrivateSettingsSuccessTest() {
		String settingName = "TEST_SETTING";
		String settingValue = "ON";
		String body = 
				json()
				.put("name", settingName)
				.put("value", settingValue)
				.toString();
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
	        ResponseEntity<String> res = 
	        		template
	        		.exchange("/organization/settings",POST, req, String.class);
	    assertEquals(200, res.getStatusCodeValue());
	    
	    Optional<SettingEntity> entity = settingRepo.findBySettingNameAndOrganization_Id(settingName, 99001L);
	    assertTrue(entity.isPresent());
	    assertEquals(settingName, entity.get().getSettingName());
	    assertEquals(settingValue, entity.get().getSettingValue());
		assertEquals(PRIVATE.getValue(), entity.get().getType().intValue());
	}




	@Test
	public void postPublicSettingsSuccessTest() {
		String settingName = "NOT_IN_SETTINGS_ENUM";
		String settingValue = "YEP";
		String body =
				json()
				.put("name", settingName)
				.put("value", settingValue)
				.put("type", PUBLIC.getValue())
				.toString();
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
		ResponseEntity<String> res =
				template
						.exchange("/organization/settings",POST, req, String.class);
		assertEquals(200, res.getStatusCodeValue());

		Optional<SettingEntity> entity = settingRepo.findBySettingNameAndOrganization_Id(settingName, 99001L);
		assertTrue(entity.isPresent());
		assertEquals(settingName, entity.get().getSettingName());
		assertEquals(settingValue, entity.get().getSettingValue());
		assertEquals(PUBLIC.getValue(), entity.get().getType().intValue());
	}
	
	
	
	
	@Test
	public void postSettingsInvalidParamTest() {
		String settingName = "TEST_SETTING";
		String body = 
				json()
				.put("name", settingName)
				.put("value", NULL)
				.toString();
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
	        ResponseEntity<String> res = 
	        		template
	        		.exchange("/organization/settings",POST, req, String.class);
	    assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
	    
	    Optional<SettingEntity> entity = settingRepo.findBySettingNameAndOrganization_Id(settingName, 99001L);
	    assertFalse(entity.isPresent());
	}
	
	
	
	
	@Test
	public void postSettingsInvalidSettingTest() {
		String settingName = "INVALID";
		String body = 
				json()
				.put("name", settingName)
				.put("value", "Bogy")
				.toString();
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
	        ResponseEntity<String> res = 
	        		template
	        		.exchange("/organization/settings",POST, req, String.class);
	    assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
	    
	    Optional<SettingEntity> entity = settingRepo.findBySettingNameAndOrganization_Id(settingName, 99001L);
	    assertFalse(entity.isPresent());
	}
	
	
	
	@Test
	public void postSettingsNoAuthZTest() {
		HttpEntity<?> req = getHttpEntity("eereeee");
        ResponseEntity<String> res = 
        		template
        		.exchange("/organization/settings",POST, req, String.class);
        assertEquals(FORBIDDEN, res.getStatusCode());
	}
	
	
	
	
	@Test
	public void postSettingsNoAuthNTest() {
		HttpEntity<?> req = getHttpEntity("NOT EXIST");
        ResponseEntity<String> res = 
        		template
        		.exchange("/organization/settings",POST, req, String.class);
        assertEquals(UNAUTHORIZED, res.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void deleteSettingsNoAuthZTest() {
		HttpEntity<?> req = getHttpEntity("eereeee");
        ResponseEntity<String> res = 
        		template
        		.exchange("/organization/settings",DELETE, req, String.class);
        assertEquals(FORBIDDEN, res.getStatusCode());
	}
	
	
	
	
	@Test
	public void deleteSettingsNoAuthNTest() {
		HttpEntity<?> req = getHttpEntity("NOT EXIST");
        ResponseEntity<String> res = 
        		template
        		.exchange("/organization/settings",DELETE, req, String.class);
        assertEquals(UNAUTHORIZED, res.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void deleteSettingsSuccessTest() {
		String settingName = "TEST_SETTING";
		HttpEntity<?> req = getHttpEntity("hijkllm");
	        ResponseEntity<String> res = 
	        		template
	        		.exchange("/organization/settings?name="+settingName,DELETE, req, String.class);
	    assertEquals(200, res.getStatusCodeValue());
	    
	    Optional<SettingEntity> entity = settingRepo.findBySettingNameAndOrganization_Id(settingName, 99001L);
	    assertFalse(entity.isPresent());
	}
	
	
	
	
	
	@Test
	public void postCartOptimizationSettingNoAuthZTest() {
		HttpEntity<?> req = getHttpEntity("eereeee");
        ResponseEntity<String> res = 
        		template
        		.exchange("/organization/settings/cart_optimization/strategy",POST, req, String.class);
        assertEquals(FORBIDDEN, res.getStatusCode());
	}
	
	
	
	
	@Test
	public void postCartOptimizationSettingNoAuthNTest() {
		HttpEntity<?> req = getHttpEntity("NOT EXIST");
        ResponseEntity<String> res = 
        		template
        		.exchange("/organization/settings/cart_optimization/strategy",DELETE, req, String.class);
        assertEquals(UNAUTHORIZED, res.getStatusCode());
	}
	
	
	
	
	@Test
	public void postCartOptimizationSettingInvalidParamTest() {
		String strategy = "INVALID";
		String body = 
				json()
				.put("strategy_name", strategy)
				.toString();
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
	        ResponseEntity<String> res = 
	        		template
	        		.exchange("/organization/settings/cart_optimization/strategy",POST, req, String.class);
	    assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
	    
	    Optional<OrganizationCartOptimizationEntity> optimizationParamsEntity = 
	    		optimizationRepo.findByOptimizationStrategyAndOrganization_Id(strategy, 99001L);
	    assertFalse(optimizationParamsEntity.isPresent());
	}
	
	
	
	
	@Test
	public void postCartOptimizationSettingSuccessTest() {
		String strategy = SAME_CITY.name();
		String optimizationParams = 
				json()
				.toString();
		String body = 
				json()
				.put("strategy_name", strategy)
				.toString();
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
	        ResponseEntity<String> res = 
	        		template
	        		.exchange("/organization/settings/cart_optimization/strategy",POST, req, String.class);
	    assertEquals(OK, res.getStatusCode());
	    
	    Optional<OrganizationCartOptimizationEntity> optimizationParamsEntity = 
	    		optimizationRepo.findByOptimizationStrategyAndOrganization_Id(strategy, 99001L);
	    assertTrue(optimizationParamsEntity.isPresent());
	    assertEquals(strategy, optimizationParamsEntity.get().getOptimizationStrategy());
	    assertEquals(optimizationParams, optimizationParamsEntity.get().getParameters());
	}
	
	
	
	
	
	@Test
	public void postWarehouseCartOptimizationSettingInvalidParamTest() {
		String strategy = WAREHOUSE;
		String body = 
				json()
				.put("strategy_name", strategy)
				.put("parameters", json())
				.toString();
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
	        ResponseEntity<String> res = 
	        		template
	        		.exchange("/organization/settings/cart_optimization/strategy",POST, req, String.class);
	    assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
	    
	    Optional<OrganizationCartOptimizationEntity> optimizationParamsEntity = 
	    		optimizationRepo.findByOptimizationStrategyAndOrganization_Id(strategy, 99001L);
	    assertFalse(optimizationParamsEntity.isPresent());
	}
	
	
	
	
	
	@Test
	public void postWarehouseCartOptimizationSettingNoExistingWareHouseTest() {
		String strategy = WAREHOUSE;
		String body = 
				json()
				.put("strategy_name", strategy)
				.put("parameters", json().put("warehouse_id", -1))
				.toString();
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
	        ResponseEntity<String> res = 
	        		template
	        		.exchange("/organization/settings/cart_optimization/strategy",POST, req, String.class);
	    assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
	    
	    Optional<OrganizationCartOptimizationEntity> optimizationParamsEntity = 
	    		optimizationRepo.findByOptimizationStrategyAndOrganization_Id(strategy, 99001L);
	    assertFalse(optimizationParamsEntity.isPresent());
	}


	
	@Test
	public void postWarehouseCartOptimizationSettingWarehouseFromAnotherOrgTest() {
		String strategy = WAREHOUSE;
		String body = 
				json()
				.put("strategy_name", strategy)
				.put("parameters", json().put("warehouse_id", 501))
				.toString();
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
	        ResponseEntity<String> res = 
	        		template
	        		.exchange("/organization/settings/cart_optimization/strategy",POST, req, String.class);
	    assertEquals(NOT_ACCEPTABLE, res.getStatusCode());
	    
	    Optional<OrganizationCartOptimizationEntity> optimizationParamsEntity = 
	    		optimizationRepo.findByOptimizationStrategyAndOrganization_Id(strategy, 99001L);
	    assertFalse(optimizationParamsEntity.isPresent());
	}



	@Test
	public void postSubAreaShopCartOptimizationSettingDefaultShopFromAnotherOrgTest() {
		String strategy = SHOP_PER_SUBAREA;
		String body =
				json()
					.put("strategy_name", strategy)
					.put("parameters",
							json()
							.put("default_shop", 501)
							.put("sub_area_shop_mapping", json().put("77001", 502)))
					.toString();
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
		ResponseEntity<String> res =
				template
						.exchange("/organization/settings/cart_optimization/strategy",POST, req, String.class);
		assertEquals(NOT_ACCEPTABLE, res.getStatusCode());

		Optional<OrganizationCartOptimizationEntity> optimizationParamsEntity =
				optimizationRepo.findByOptimizationStrategyAndOrganization_Id(strategy, 99001L);
		assertFalse(optimizationParamsEntity.isPresent());
	}



	@Test
	public void postSubAreaShopCartOptimizationSettingSubAreaShopFromAnotherOrgTest() {
		String strategy = SHOP_PER_SUBAREA;
		String body =
				json()
					.put("strategy_name", strategy)
					.put("parameters",
							json()
							.put("default_shop", 502)
							.put("sub_area_shop_mapping", json().put("77001", 501)))
					.toString();
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
		ResponseEntity<String> res =
				template
						.exchange("/organization/settings/cart_optimization/strategy",POST, req, String.class);
		assertEquals(NOT_ACCEPTABLE, res.getStatusCode());

		Optional<OrganizationCartOptimizationEntity> optimizationParamsEntity =
				optimizationRepo.findByOptimizationStrategyAndOrganization_Id(strategy, 99001L);
		assertFalse(optimizationParamsEntity.isPresent());
	}



	@Test
	public void postSubAreaShopCartOptimizationSettingSubAreaFromAnotherOrgTest() {
		String strategy = SHOP_PER_SUBAREA;
		String body =
				json()
					.put("strategy_name", strategy)
					.put("parameters",
							json()
							.put("default_shop", 502)
							.put("sub_area_shop_mapping", json().put("77002", 502)))
					.toString();
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
		ResponseEntity<String> res =
				template
						.exchange("/organization/settings/cart_optimization/strategy",POST, req, String.class);
		assertEquals(NOT_ACCEPTABLE, res.getStatusCode());

		Optional<OrganizationCartOptimizationEntity> optimizationParamsEntity =
				optimizationRepo.findByOptimizationStrategyAndOrganization_Id(strategy, 99001L);
		assertFalse(optimizationParamsEntity.isPresent());
	}



	@Test
	public void postSubAreaShopCartOptimizationSettingSuccessTest() {
		String strategy = SHOP_PER_SUBAREA;
		String body =
				json()
					.put("strategy_name", strategy)
					.put("parameters",
							json()
							.put("default_shop", 502)
							.put("sub_area_shop_mapping", json().put("77001", 502)))
					.toString();
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
		ResponseEntity<String> res =
				template
						.exchange("/organization/settings/cart_optimization/strategy",POST, req, String.class);
		assertEquals(OK, res.getStatusCode());

		Optional<OrganizationCartOptimizationEntity> optimizationParamsEntity =
				optimizationRepo.findByOptimizationStrategyAndOrganization_Id(strategy, 99001L);
		assertTrue(optimizationParamsEntity.isPresent());
		assertEquals(strategy, optimizationParamsEntity.get().getOptimizationStrategy());
	}
	


	@Test
	public void postWarehouseCartOptimizationSettingSuccessTest() {
		String strategy = WAREHOUSE;
		JSONObject optimizationParams = json().put("warehouse_id", 502);
		String body = 
				json()
				.put("strategy_name", strategy)
				.put("parameters", optimizationParams)
				.toString();
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
	        ResponseEntity<String> res = 
	        		template
	        		.exchange("/organization/settings/cart_optimization/strategy",POST, req, String.class);
	    assertEquals(OK, res.getStatusCode());
	    
	    Optional<OrganizationCartOptimizationEntity> optimizationParamsEntity = 
	    		optimizationRepo.findByOptimizationStrategyAndOrganization_Id(strategy, 99001L);
	    assertTrue(optimizationParamsEntity.isPresent());
	    assertEquals(strategy, optimizationParamsEntity.get().getOptimizationStrategy());
	    assertEquals(optimizationParams.toString(), optimizationParamsEntity.get().getParameters());
	}
	
	
	
	
	@Test
	@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Organization_Test_Data_Insert_2.sql"})
	@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getOptimizationStrategyTest() throws JsonParseException, Exception {
		HttpEntity<?> req = getHttpEntity("hijkllm");
        ResponseEntity<String> res = 
        		template
        		.exchange("/organization/settings/cart_optimization/strategy",GET, req, String.class);
        assertEquals(OK, res.getStatusCode());
        
        List<CartOptimizationSettingDTO> strategyConfigs = 
        		objectMapper.readValue(res.getBody(), new TypeReference<List<CartOptimizationSettingDTO>>(){});
        
        assertEquals(2, strategyConfigs.size());
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Organization_Test_Data_Insert_2.sql"})
	@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getOptimizationStrategyNoAuthzTest() throws JsonParseException, Exception {
		HttpEntity<?> req = getHttpEntity("eereeee");
        ResponseEntity<String> res = 
        		template
        		.exchange("/organization/settings/cart_optimization/strategy",GET, req, String.class);
        assertEquals(FORBIDDEN, res.getStatusCode());
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Organization_Test_Data_Insert_2.sql"})
	@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void getOptimizationStrategyNoAuthNTest() throws JsonParseException, Exception {
		HttpEntity<?> req = getHttpEntity("Non existing");
        ResponseEntity<String> res = 
        		template
        		.exchange("/organization/settings/cart_optimization/strategy",GET, req, String.class);
        assertEquals(UNAUTHORIZED, res.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void listOptimizationStrategiesTest() throws JsonParseException, Exception {
		HttpEntity<?> req = getHttpEntity("hijkllm");
        ResponseEntity<String> res = 
        		template
        		.exchange("/organization/settings/cart_optimization/strategies",GET, req, String.class);
        assertEquals(OK, res.getStatusCode());
        
        List<CartOptimizationStrategyDTO> strategies = 
        		objectMapper.readValue(res.getBody(), new TypeReference<List<CartOptimizationStrategyDTO>>(){});
       
        
        Set<String> strategiesNames = 
        		stream(CartOptimizationStrategy.values())
        		.map(CartOptimizationStrategy::getValue)
        		.collect(toSet());
        
        boolean allStrategiesReturned =
        		strategies
        		.stream()
        		.map(CartOptimizationStrategyDTO::getName)
        		.allMatch(strategiesNames::contains);
        
        
        assertEquals(strategiesNames.size(), strategies.size());
        assertTrue(allStrategiesReturned);
	}
	
	
	
	
	
	@Test
	public void listOptimizationStrategiesNoAuthzTest() throws JsonParseException, Exception {
		HttpEntity<?> req = getHttpEntity("eereeee");
        ResponseEntity<String> res = 
        		template
        		.exchange("/organization/settings/cart_optimization/strategies",GET, req, String.class);
        assertEquals(FORBIDDEN, res.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void listOptimizationStrategiesAuthNTest() throws JsonParseException, Exception {
		HttpEntity<?> req = getHttpEntity("Non existing");
        ResponseEntity<String> res = 
        		template
        		.exchange("/organization/settings/cart_optimization/strategies",GET, req, String.class);
        assertEquals(UNAUTHORIZED, res.getStatusCode());
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase = BEFORE_TEST_METHOD , scripts = {"/sql/Products_Test_Data_Insert.sql"})
	@Sql(executionPhase = AFTER_TEST_METHOD , scripts = {"/sql/database_cleanup.sql"})
	public void showFreeProductsEnabled() {
		setOrganizationSetting(SHOW_FREE_PRODUCTS.name(), true);
		
		ResponseEntity<String> response = template.getForEntity("/navbox/products?org_id=99001", String.class);
		System.out.println(response.getBody());
		JSONObject  json = (JSONObject) JSONParser.parseJSON(response.getBody());
		long total = json.getLong("total");
		assertEquals("there are total 3 products with with org_id = 99001 and single product with zero price",4L , total);
	}
	
	
	
	
	@Test
	@Sql(executionPhase = BEFORE_TEST_METHOD , scripts = {"/sql/Products_Test_Data_Insert.sql"})
	@Sql(executionPhase = AFTER_TEST_METHOD , scripts = {"/sql/database_cleanup.sql"})
	public void hideEmptyStocksEnabled() {
		setOrganizationSetting(HIDE_EMPTY_STOCKS.name(), true);
		
		ResponseEntity<String> response = template.getForEntity("/navbox/products?org_id=99001", String.class);
		System.out.println(response.getBody());
		JSONObject  json = (JSONObject) JSONParser.parseJSON(response.getBody());
		long total = json.getLong("total");
		assertEquals("there are total 3 products with with org_id = 99001 and single product with zero stock, which should be ignored"
						,2L , total);
	}




	private void setOrganizationSetting(String settingName, Object settingValue) {
		String body = 
				json()
				.put("name", settingName)
				.put("value", settingValue)
				.toString();
		HttpEntity<?> req = getHttpEntity(body, "161718");
	        ResponseEntity<String> res = 
	        		template
	        		.exchange("/organization/settings",POST, req, String.class);
	    assertEquals(200, res.getStatusCodeValue());
	}
}
