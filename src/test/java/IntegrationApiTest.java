import static com.nasnav.integration.enums.IntegrationParam.INTEGRATION_MODULE;
import static com.nasnav.integration.enums.IntegrationParam.MAX_REQUEST_RATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.NavBox;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.IntegrationParamRepository;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.EventResult;
import com.nasnav.integration.exceptions.InvalidIntegrationEventException;
import com.nasnav.persistence.IntegrationParamEntity;
import com.nasnav.test.integration.event.TestEvent;

import net.jcip.annotations.NotThreadSafe;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@NotThreadSafe
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Integration_Api_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class IntegrationApiTest {
	
	private static final String DUMMY_VAL = "dummy_val";
	private static final String NASNAV_ADMIN_TOKEN = "abcdefg";
	private static final String ORG_ADMIN_TOKEN = "hijkllm";
	private static final Long ORG_ID = 99001L;
	private static final String DUMMY_PARAM = "DUMMY_PARAM";
	
	
	@Autowired
	private TestRestTemplate template;
	
	
	@Autowired
	private EmployeeUserRepository empUserRepo;
	
	
	@Autowired
	private IntegrationParamRepository paramRepo;
	
	
	
	@Autowired
	private IntegrationService integrationSrv;
	
	
	@Test
	public void testRegisterModuleAuthN(){
		String url = "/integration/module";
		HttpEntity request =  TestCommons.getHttpEntity("{}", "non-existing-token");
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void testRegisterModuleAuthZ(){
		String url = "/integration/module";
		HttpEntity request =  TestCommons.getHttpEntity("{}", ORG_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals(response.getStatusCode(), HttpStatus.FORBIDDEN);
	}
	
	
	
	
	@Test
	public void testRegisterModuleMissingOrgId(){
		String url = "/integration/module";
		JSONObject json = getIntegrationModuleRequestJson();
		json.remove("organization_id");
		
		HttpEntity request =  TestCommons.getHttpEntity(json.toString(), NASNAV_ADMIN_TOKEN);
		
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals( HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	
	private JSONObject getIntegrationModuleRequestJson() {
		JSONObject request = new JSONObject();
		request.put("organization_id", ORG_ID);
		request.put("integration_module", "com.nasnav.test.integration.modules.TestIntegrationModule");
		request.put("max_request_rate", 2);
		
		Map<String,String> params = new HashMap<>();
		params.put("DUMmY_paRam", DUMMY_VAL);
		request.put("integration_parameters", params);
		return request;
	}





	@Test
	public void testRegisterModuleMissingModuleName(){
		String url = "/integration/module";
		
		JSONObject json = getIntegrationModuleRequestJson();
		json.remove("integration_module");
		
		HttpEntity request =  TestCommons.getHttpEntity(json.toString(), NASNAV_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	
	@Test
	public void testRegisterModuleNonExistingOrg(){
		String url = "/integration/module";
		
		JSONObject json = getIntegrationModuleRequestJson();
		json.put("organization_id", 1111111111L);
		
		HttpEntity request =  TestCommons.getHttpEntity(json.toString(), NASNAV_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void testRegisterModuleNonExistingModule(){
		String url = "/integration/module";
		
		JSONObject json = getIntegrationModuleRequestJson();
		json.put("integration_module", "non.existing.module");
		
		HttpEntity request =  TestCommons.getHttpEntity(json.toString(), NASNAV_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void testRegisterModuleInvalidMaxRequestRate(){
		String url = "/integration/module";
		
		JSONObject json = getIntegrationModuleRequestJson();
		json.put("max_request_rate", -2000);
		
		HttpEntity request =  TestCommons.getHttpEntity(json.toString(), NASNAV_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void testRegisterModule() throws InvalidIntegrationEventException{
		assertFalse( isOrgAcceptEvents() );
		
		String url = "/integration/module";
		
		JSONObject json = getIntegrationModuleRequestJson();
		
		HttpEntity request =  TestCommons.getHttpEntity(json.toString(), NASNAV_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());		
		assertIntegrationParamInserted(json);
		
		
		assertTrue( isOrgAcceptEvents() );
	}





	private boolean isOrgAcceptEvents() throws InvalidIntegrationEventException {
		AtomicBoolean isCalled = new AtomicBoolean(false);
		TestEvent event = new TestEvent(ORG_ID, "hi", res -> isCalled.set(true));
		Mono<EventResult<String, String>> eventResult = integrationSrv.pushIntegrationEvent(event, (e, t) -> assertTrue(false));
		try {
			eventResult.block(Duration.ofMillis(20));
		}catch(Throwable t) {
			return false;
		}	
		
		return isCalled.get();
	}





	private void assertIntegrationParamInserted(JSONObject json) {
		//parameters inserted to database 
		//integration Module param inserted - max rate inserted - dummy param created and inserted
		Optional<IntegrationParamEntity> moduleName = 
				paramRepo.findByOrganizationIdAndType_typeName(
						json.getLong("organization_id")																
						, INTEGRATION_MODULE.getValue());
		
		Optional<IntegrationParamEntity> rate = 
				paramRepo.findByOrganizationIdAndType_typeName(
									json.getLong("organization_id")
									, MAX_REQUEST_RATE.getValue());
		
		Optional<IntegrationParamEntity> dummyParam = 
				paramRepo.findByOrganizationIdAndType_typeName(
									json.getLong("organization_id")
									, DUMMY_PARAM);
		
		assertTrue(moduleName.isPresent());
		assertTrue(rate.isPresent());
		assertTrue(dummyParam.isPresent());
		
		assertEquals(json.get("integration_module"), moduleName.get().getParamValue());
		assertTrue(moduleName.get().getType().isMandatory());
		
		assertEquals(json.getInt("max_request_rate"), getIntVal( rate.get().getParamValue()) );
		assertTrue(rate.get().getType().isMandatory());
		
		assertEquals(DUMMY_VAL, dummyParam.get().getParamValue());
		assertFalse(dummyParam.get().getType().isMandatory());
	}
	
	
	
	
	
	
	private int getIntVal(String asStr) {
		return Integer.valueOf(asStr).intValue() ;
	}
}
