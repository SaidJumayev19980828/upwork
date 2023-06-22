package com.nasnav.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.OrganizationCartOptimizationRepository;
import com.nasnav.dao.SettingRepository;
import com.nasnav.dto.CountriesRepObj;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

import static com.nasnav.enumerations.Settings.*;
import static com.nasnav.enumerations.SettingsType.PRIVATE;
import static com.nasnav.enumerations.SettingsType.PUBLIC;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static junit.framework.TestCase.*;
import static org.json.JSONObject.NULL;
import static org.junit.Assert.assertFalse;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Organization_Test_Data_Insert_3.sql"})
@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class SettingsServiceTest extends AbstractTestWithTempBaseDir {
	
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
		var settingName = "TEST_SETTING";
		var settingValue = "ON";
		var body =
				json()
				.put("name", settingName)
				.put("value", settingValue)
				.toString();
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
		var res =
	        		template
	        		.exchange("/organization/settings",POST, req, String.class);
	    assertEquals(200, res.getStatusCodeValue());

		var entity = settingRepo.findBySettingNameAndOrganization_Id(settingName, 99001L);
	    assertTrue(entity.isPresent());
	    assertEquals(settingName, entity.get().getSettingName());
	    assertEquals(settingValue, entity.get().getSettingValue());
		assertEquals(PRIVATE.getValue(), entity.get().getType().intValue());
	}




	@Test
	public void postPublicSettingsSuccessTest() {
		var settingName = "NOT_IN_SETTINGS_ENUM";
		var settingValue = "YEP";
		var body =
				json()
				.put("name", settingName)
				.put("value", settingValue)
				.put("type", PUBLIC.getValue())
				.toString();
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
		var res =
				template
						.exchange("/organization/settings",POST, req, String.class);
		assertEquals(200, res.getStatusCodeValue());

		var entity = settingRepo.findBySettingNameAndOrganization_Id(settingName, 99001L);
		assertTrue(entity.isPresent());
		assertEquals(settingName, entity.get().getSettingName());
		assertEquals(settingValue, entity.get().getSettingValue());
		assertEquals(PUBLIC.getValue(), entity.get().getType().intValue());
	}
	
	
	
	
	@Test
	public void postSettingsInvalidParamTest() {
		var settingName = "TEST_SETTING";
		var body =
				json()
				.put("name", settingName)
				.put("value", NULL)
				.toString();
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
		var res =
	        		template
	        		.exchange("/organization/settings",POST, req, String.class);
	    assertEquals(NOT_ACCEPTABLE, res.getStatusCode());

		var entity = settingRepo.findBySettingNameAndOrganization_Id(settingName, 99001L);
	    assertFalse(entity.isPresent());
	}
	
	
	
	
	@Test
	public void postSettingsInvalidSettingTest() {
		var settingName = "INVALID";
		var body =
				json()
				.put("name", settingName)
				.put("value", "Bogy")
				.toString();
		HttpEntity<?> req = getHttpEntity(body, "hijkllm");
		var res =
	        		template
	        		.exchange("/organization/settings",POST, req, String.class);
	    assertEquals(NOT_ACCEPTABLE, res.getStatusCode());

		var entity = settingRepo.findBySettingNameAndOrganization_Id(settingName, 99001L);
	    assertFalse(entity.isPresent());
	}
	
	
	
	@Test
	public void postSettingsNoAuthZTest() {
		HttpEntity<?> req = getHttpEntity("eereeee");
		var res =
        		template
        		.exchange("/organization/settings",POST, req, String.class);
        assertEquals(FORBIDDEN, res.getStatusCode());
	}
	
	
	
	
	@Test
	public void postSettingsNoAuthNTest() {
		HttpEntity<?> req = getHttpEntity("NOT EXIST");
		var res =
        		template
        		.exchange("/organization/settings",POST, req, String.class);
        assertEquals(UNAUTHORIZED, res.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void deleteSettingsNoAuthZTest() {
		HttpEntity<?> req = getHttpEntity("eereeee");
		var res =
        		template
        		.exchange("/organization/settings",DELETE, req, String.class);
        assertEquals(FORBIDDEN, res.getStatusCode());
	}
	
	
	
	
	@Test
	public void deleteSettingsNoAuthNTest() {
		HttpEntity<?> req = getHttpEntity("NOT EXIST");
		var res =
        		template
        		.exchange("/organization/settings",DELETE, req, String.class);
        assertEquals(UNAUTHORIZED, res.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void deleteSettingsSuccessTest() {
		var settingName = "TEST_SETTING";
		HttpEntity<?> req = getHttpEntity("hijkllm");
		var res =
	        		template
	        		.exchange("/organization/settings?name="+settingName,DELETE, req, String.class);
	    assertEquals(200, res.getStatusCodeValue());

		var entity = settingRepo.findBySettingNameAndOrganization_Id(settingName, 99001L);
	    assertFalse(entity.isPresent());
	}
	

	
	
	@Test
	@Sql(executionPhase = BEFORE_TEST_METHOD , scripts = {"/sql/Products_Test_Data_Insert.sql"})
	@Sql(executionPhase = AFTER_TEST_METHOD , scripts = {"/sql/database_cleanup.sql"})
	public void showFreeProductsEnabled() {
		setOrganizationSetting(SHOW_FREE_PRODUCTS.name(), true);

		var response = template.getForEntity("/navbox/products?org_id=99001", String.class);
		System.out.println(response.getBody());
		var json = (JSONObject) JSONParser.parseJSON(response.getBody());
		var total = json.getLong("total");
		assertEquals("there are total 3 products with with org_id = 99001 and single product with zero price",4L , total);
	}
	
	
	
	
	@Test
	@Sql(executionPhase = BEFORE_TEST_METHOD , scripts = {"/sql/Products_Test_Data_Insert.sql"})
	@Sql(executionPhase = AFTER_TEST_METHOD , scripts = {"/sql/database_cleanup.sql"})
	public void hideEmptyStocksEnabled() {
		setOrganizationSetting(HIDE_EMPTY_STOCKS.name(), true);

		var response = template.getForEntity("/navbox/products?org_id=99001", String.class);
		System.out.println(response.getBody());
		var json = (JSONObject) JSONParser.parseJSON(response.getBody());
		var total = json.getLong("total");
		assertEquals("there are total 3 products with with org_id = 99001 and single product with zero stock, which should be ignored"
						,2L , total);
	}




	private void setOrganizationSetting(String settingName, Object settingValue) {
		var body =
				json()
				.put("name", settingName)
				.put("value", settingValue)
				.toString();
		HttpEntity<?> req = getHttpEntity(body, "161718");
		var res =
	        		template
	        		.exchange("/organization/settings",POST, req, String.class);
	    assertEquals(200, res.getStatusCodeValue());
	}


	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Settings_Test_Data_Insert.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void hideAreasWithNoSubAreasEnabled() throws IOException {
		var response = template.getForEntity("/navbox/countries?org_id=99001", String.class);
		var body =
				objectMapper.readValue(response.getBody(), new TypeReference<Map<String, CountriesRepObj>>(){});
		assertEquals(200, response.getStatusCodeValue());
		var egypt = body.get("Egypt");
		assertNotNull(egypt);
		var cairo = egypt.getCities().get("Cairo");
		assertEquals("Cairo", cairo.getName());
		assertEquals("only one Area has sub-areas", 1, cairo.getAreas().keySet().size());
		assertTrue(cairo.getAreas().containsKey("new cairo"));
	}

	@Test
	@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Organization_Test_Data_Insert_2.sql"})
	@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void deleteOptimizationStrategyTest() {
		HttpEntity<?> req = getHttpEntity("hijkllm");
		ResponseEntity<String> res =
				template
						.exchange("/organization/settings/cart_optimization/strategy?strategy_name=SAME_CITY&shipping_service=TEST",DELETE, req, String.class);
		assertEquals(OK, res.getStatusCode());
		assertFalse(optimizationRepo.findByOptimizationStrategyAndOrganization_Id("SAME_CITY", 99001L).isPresent());
	}

	@Test
	@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Organization_Test_Data_Insert_2.sql"})
	@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void deleteOptimizationStrategyInvalidAuthZ() {
		HttpEntity<?> req = getHttpEntity("eereeee");
		ResponseEntity<String> res =
				template
						.exchange("/organization/settings/cart_optimization/strategy?strategy_name=SAME_CITY",DELETE, req, String.class);
		assertEquals(403, res.getStatusCodeValue());
	}

	@Test
	@Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Organization_Test_Data_Insert_2.sql"})
	@Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void deleteOptimizationStrategyInvalidAuthN() {
		HttpEntity<?> req = getHttpEntity("Invalid");
		ResponseEntity<String> res =
				template
						.exchange("/organization/settings/cart_optimization/strategy?strategy_name=SAME_CITY",DELETE, req, String.class);
		assertEquals(401, res.getStatusCodeValue());
	}

}
