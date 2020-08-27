package com.nasnav.test;

import static com.nasnav.enumerations.Settings.CART_OPTIMIZATION_STARTEGY;
import static com.nasnav.service.cart.optimizers.CartOptimizationStrategy.SAME_CITY;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.json.JSONObject.NULL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.NavBox;
import com.nasnav.dao.OrganizationCartOptimizationRepository;
import com.nasnav.dao.SettingRepository;
import com.nasnav.persistence.OrganizationCartOptimizationEntity;
import com.nasnav.persistence.SettingEntity;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Organization_Test_Data_Insert.sql"})
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
	
	
	@Test
	public void postSettingsSuccessTest() {
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
		String settingName = CART_OPTIMIZATION_STARTEGY.name();
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
	    
	    Optional<SettingEntity> entity = settingRepo.findBySettingNameAndOrganization_Id(settingName, 99001L);
	    assertFalse(entity.isPresent());
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
}
