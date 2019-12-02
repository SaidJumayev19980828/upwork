import static com.nasnav.integration.enums.IntegrationParam.DISABLED;
import static com.nasnav.integration.enums.IntegrationParam.INTEGRATION_MODULE;
import static com.nasnav.integration.enums.IntegrationParam.MAX_REQUEST_RATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONObject;
import org.junit.Before;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.NavBox;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.IntegrationParamRepository;
import com.nasnav.dao.IntegrationParamTypeRepostory;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.EventResult;
import com.nasnav.integration.exceptions.InvalidIntegrationEventException;
import com.nasnav.persistence.IntegrationParamEntity;
import com.nasnav.persistence.IntegrationParamTypeEntity;
import com.nasnav.test.integration.event.TestEvent;

import net.jcip.annotations.NotThreadSafe;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@NotThreadSafe
@DirtiesContext
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Integration_Api_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class IntegrationApiTest {
	
	private static final String NEW_INTEGRATION_PARAM = "NEW_INTEGRATION_PARAM";
	private static final String DUMMY_VAL = "dummy_val";
	private static final String NASNAV_ADMIN_TOKEN = "abcdefg";
	private static final String ORG_EMP_TOKEN = "sfeesdfsdf";
	private static final String ORG_ADMIN_TOKEN = "hijkllm";
	private static final Long ORG_ID = 99001L;
	private static final String DUMMY_PARAM = "DUMMY_PARAM";
	private static final String EXISTING_INTEGRATION_PARAM = "EXISTING_PARAM";
	
	
	@Autowired
	private TestRestTemplate template;
	
	
	@Autowired
	private EmployeeUserRepository empUserRepo;
	
	
	@Autowired
	private IntegrationParamRepository paramRepo;
	
	
	
	@Autowired
	private IntegrationService integrationSrv;
	
	
	
	@Autowired
	private IntegrationParamTypeRepostory paramTypeRepo;
	
	
	@Before
	public void clearIntegrationModules() {
		integrationSrv.clearAllIntegrationModules();
	}
	
	
	
	
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
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
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
	public void testRegisterModuleInvalidModule(){
		String url = "/integration/module";
		
		JSONObject json = getIntegrationModuleRequestJson();
		json.put("integration_module", "com.nasnav.test.integration.modules.InvalidIntegrationModule");
		
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
		registerIntegrationModule();
	}





	private void registerIntegrationModule() throws InvalidIntegrationEventException {
		assertFalse( isOrgAcceptEvents() );
			
		JSONObject json = getIntegrationModuleRequestJson();		
		ResponseEntity<String> response = registerIntegrationModule(json);
		
		assertModuleRegistered(json, response);		
		assertTrue( isOrgAcceptEvents() );
	}
	
	
	
	
	
	private void registerDisabledIntegrationModule() throws InvalidIntegrationEventException {
		assertFalse( isOrgAcceptEvents() );
			
		JSONObject json = getIntegrationModuleRequestJson();
		json.getJSONObject("integration_parameters").put(DISABLED.getValue(), true);
		
		ResponseEntity<String> response = registerIntegrationModule(json);
		
		assertModuleRegistered(json, response);		
	}





	private ResponseEntity<String> registerIntegrationModule(JSONObject json) {
		String url = "/integration/module";
		HttpEntity request =  TestCommons.getHttpEntity(json.toString(), NASNAV_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.POST
						, request
						, String.class);
		return response;
	}





	private void assertModuleRegistered(JSONObject json, ResponseEntity<String> response) {
		assertEquals(HttpStatus.OK, response.getStatusCode());		
		assertIntegrationParamInserted(json);
	}





	private boolean isOrgAcceptEvents() throws InvalidIntegrationEventException {
		AtomicBoolean isCalled = new AtomicBoolean(false);
		TestEvent event = new TestEvent(ORG_ID, "hi", res -> isCalled.set(true));
		Mono<EventResult<String, String>> eventResult = integrationSrv.pushIntegrationEvent(event, (e, t) -> assertTrue(false));
		try {
			eventResult.block(Duration.ofMillis(200));
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
	
	
	
	
	@Test
	public void testDisableModuleAuthN(){
		String url = "/integration/module/disable";
		HttpEntity request =  TestCommons.getHttpEntity("{}", "non-existing-token");
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void testDisableModuleAuthZ(){
		String url = "/integration/module/disable";
		HttpEntity request =  TestCommons.getHttpEntity("{}", ORG_EMP_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void testDisableModuleForOrgWithNoIntegration(){
		String url = "/integration/module/disable";
		HttpEntity request =  TestCommons.getHttpEntity("", ORG_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url + "?organization_id=88859"
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals( HttpStatus.OK, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void testDisableModule() throws Exception{
		registerIntegrationModule();
		
		assertTrue( isOrgAcceptEvents() );
		//----------------------------------------------------------------	
		
		String url = "/integration/module/disable";
		HttpEntity request =  TestCommons.getHttpEntity("", ORG_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url + "?organization_id=" + ORG_ID
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals( HttpStatus.OK, response.getStatusCode());
		//----------------------------------------------------------------
		
		assertFalse( isOrgAcceptEvents() );
	}
	
	
	
	
	
	@Test
	public void testEnableModuleAuthN(){
		String url = "/integration/module/enable";
		HttpEntity request =  TestCommons.getHttpEntity("{}", "non-existing-token");
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void testEnableModuleAuthZ(){
		String url = "/integration/module/enable";
		HttpEntity request =  TestCommons.getHttpEntity("{}", ORG_EMP_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	
	
	@Test
	public void testEnableModule() throws Exception{
		registerDisabledIntegrationModule();
		
		assertFalse( isOrgAcceptEvents() );
		//----------------------------------------------------------------	
		
		String url = "/integration/module/enable";
		HttpEntity request =  TestCommons.getHttpEntity("", ORG_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url + "?organization_id=" + ORG_ID
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals( HttpStatus.OK, response.getStatusCode());
		//----------------------------------------------------------------
		
		assertTrue( isOrgAcceptEvents() );
	}
	
	
	
	
	
	
	@Test
	public void testEnableNonExistingModule() throws Exception{		
		//----------------------------------------------------------------	
		
		String url = "/integration/module/enable";
		HttpEntity request =  TestCommons.getHttpEntity("", ORG_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url + "?organization_id=" + ORG_ID
						, HttpMethod.POST
						, request
						, String.class);
		//----------------------------------------------------------------
		
		assertEquals( HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	
	@Test
	public void testRemoveModuleAuthN(){
		String url = "/integration/module";
		HttpEntity request =  TestCommons.getHttpEntity("{}", "non-existing-token");
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.DELETE
						, request
						, String.class);
		
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void testRemoveModuleAuthZ(){
		String url = "/integration/module";
		HttpEntity request =  TestCommons.getHttpEntity("{}", ORG_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.DELETE
						, request
						, String.class);
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	@Test
	public void testRemoveNonExistingModule(){
		String url = "/integration/module";
		HttpEntity request =  TestCommons.getHttpEntity("{}", NASNAV_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url + "?organization_id=" + ORG_ID
						, HttpMethod.DELETE
						, request
						, String.class);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void testRemoveModule() throws Exception{
		registerIntegrationModule();
		
		assertTrue( isOrgAcceptEvents() );
		//-------------------------------------------------
		
		String url = "/integration/module";
		HttpEntity request =  TestCommons.getHttpEntity("{}", NASNAV_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url + "?organization_id=" + ORG_ID
						, HttpMethod.DELETE
						, request
						, String.class);
		//-------------------------------------------------
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertFalse( isOrgAcceptEvents() );
		assertEquals("all parameters should be deleted for the organization", 0, paramRepo.findByOrganizationId(ORG_ID).size());
	}
	
	
	
	
	
	@Test
	public void testAddParamAuthN(){
		String url = "/integration/param";
		HttpEntity request =  TestCommons.getHttpEntity("{}", "non-existing-token");
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void testAddParamAuthZ(){
		String url = "/integration/param";
		HttpEntity request =  TestCommons.getHttpEntity("{}", ORG_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void testAddParamInvalidName(){
		JSONObject json = createIntegrationParamJson();
		json.put("param_name", "invalid$$#Param--Name");
		
		String url = "/integration/param";
		HttpEntity request =  TestCommons.getHttpEntity(json.toString(), NASNAV_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void testAddParamNonExistingName(){
		JSONObject json = createIntegrationParamJson();
		json.remove("param_name");
		
		String url = "/integration/param";
		HttpEntity request =  TestCommons.getHttpEntity(json.toString(), NASNAV_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void testAddParamNonExistingOrg(){
		JSONObject json = createIntegrationParamJson();
		json.put("organization_id", 5588);
		
		String url = "/integration/param";
		HttpEntity request =  TestCommons.getHttpEntity(json.toString(), NASNAV_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
	}


	
	
	
	
	@Test
	public void testAddNewParam(){
		JSONObject json = createIntegrationParamJson();		
		
		assertParameterNotExists(json);
		//----------------------------------------------------------
		String url = "/integration/param";
		HttpEntity request =  TestCommons.getHttpEntity(json.toString(), NASNAV_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		//-------------------------------------------------------------
		
		Optional<IntegrationParamEntity> parameterOptAfter = getParamByNameAndOrgId(json);
		assertTrue( parameterOptAfter.isPresent());
		
		IntegrationParamEntity parameter = parameterOptAfter.get();
		assertParameterSaved(json, parameter);
	}
	
	
	
	
	
	@Test
	public void testUpdateExistingParam(){
		JSONObject json = createIntegrationParamJson();		
		json.put("param_name", EXISTING_INTEGRATION_PARAM);
		
		assertParameterExists(json);
		//----------------------------------------------------------
		String url = "/integration/param";
		HttpEntity request =  TestCommons.getHttpEntity(json.toString(), NASNAV_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.POST
						, request
						, String.class);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		//-------------------------------------------------------------
		
		Optional<IntegrationParamEntity> parameterOptAfter = getParamByNameAndOrgId(json);
		assertTrue( parameterOptAfter.isPresent());
		
		IntegrationParamEntity parameter = parameterOptAfter.get();
		assertParameterSaved(json, parameter);
	}



	
	
	@Test
	public void testDeleteParamAuthN(){
		String url = "/integration/param";
		HttpEntity request =  TestCommons.getHttpEntity("{}", "non-existing-token");
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.DELETE
						, request
						, String.class);
		
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	
	
	
	@Test
	public void testDeleteParamAuthZ(){
		String url = "/integration/param";
		HttpEntity request =  TestCommons.getHttpEntity("{}", ORG_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.DELETE
						, request
						, String.class);
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	
	
	@Test
	public void testDeleteParamInvalidName(){
		JSONObject json = createIntegrationParamJson();
		json.put("param_name", "invalid$$#Param--Name");
		
		String url = "/integration/param";
		HttpEntity request =  TestCommons.getHttpEntity(json.toString(), NASNAV_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.DELETE
						, request
						, String.class);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}
	
	
	
	
	
	
	
	@Test
	public void testDeleteParamNonExistingOrg(){
		JSONObject json = createIntegrationParamJson();
		json.put("organization_id", 5588);
		
		String url = "/integration/param";
		HttpEntity request =  TestCommons.getHttpEntity(json.toString(), NASNAV_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.DELETE
						, request
						, String.class);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}
	
	
	
	
	

	
	@Test
	public void testDeleteParam(){
		JSONObject json = createIntegrationParamDeleteJson();		
		json.put("param_name", EXISTING_INTEGRATION_PARAM);
		
		assertParameterExists(json);
		//----------------------------------------------------------
		String url = "/integration/param";
		HttpEntity request =  TestCommons.getHttpEntity(json.toString(), NASNAV_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.DELETE
						, request
						, String.class);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		//-------------------------------------------------------------
		
		Optional<IntegrationParamEntity> parameterOptAfter = getParamByNameAndOrgId(json);
		assertFalse( parameterOptAfter.isPresent());		
	}
	
	
	
	
	

	private JSONObject createIntegrationParamDeleteJson() {
		JSONObject json = new JSONObject();
		json.put("organization_id", ORG_ID);
		json.put("param_name", NEW_INTEGRATION_PARAM);
		return json;
	}




	private void assertParameterNotExists(JSONObject json) {
		Optional<IntegrationParamEntity> parameterOptBefore = getParamByNameAndOrgId(json);
		Optional<IntegrationParamTypeEntity> parameterTypeBefore = 
				paramTypeRepo.findByTypeName(json.getString("param_name"));
		
		assertFalse( parameterOptBefore.isPresent());
		assertFalse(parameterTypeBefore.isPresent());
	}
	
	
	
	
	
	
	private void assertParameterExists(JSONObject json) {
		Optional<IntegrationParamEntity> parameterOptBefore = getParamByNameAndOrgId(json);
		Optional<IntegrationParamTypeEntity> parameterTypeBefore = 
				paramTypeRepo.findByTypeName(json.getString("param_name"));
		
		assertTrue( parameterOptBefore.isPresent());
		assertTrue( parameterTypeBefore.isPresent());
	}



	
	


	private Optional<IntegrationParamEntity> getParamByNameAndOrgId(JSONObject json) {
		Optional<IntegrationParamEntity> parameterOpt = paramRepo.findByOrganizationIdAndType_typeName(
																	json.getLong("organization_id")
																	, json.getString("param_name")) ;
		return parameterOpt;
	}


	
	


	private void assertParameterSaved(JSONObject json, IntegrationParamEntity parameter) {
		assertEquals(json.getLong("organization_id"),  parameter.getOrganizationId().longValue() );
		assertEquals(json.getString("param_value"), parameter.getParamValue());
		assertEquals(json.getString("param_name"), parameter.getType().getTypeName());
		assertNotNull(parameter.getCreatedAt());
		assertNotNull(parameter.getUpdatedAt());
	}

	
	
	

	private JSONObject createIntegrationParamJson() {
		JSONObject json = new JSONObject();
		json.put("organization_id", ORG_ID);
		json.put("param_name", NEW_INTEGRATION_PARAM);
		json.put("param_value", "doesn't matter");
		return json;
	}
	
	
	
	
	
	
	@Test
	public void testGetIntegrationModuleAuthN(){
		String url = "/integration/module/all";
		HttpEntity request =  TestCommons.getHttpEntity("{}", "non-existing-token");
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.GET
						, request
						, String.class);
		
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	
	
	
	@Test
	public void testGetIntegrationModuleAuthZ(){
		String url = "/integration/module/all";
		HttpEntity request =  TestCommons.getHttpEntity("{}", ORG_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.GET
						, request
						, String.class);
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	
	
	
	@Test
	public void testGetIntegrationModule() throws Exception{
		registerIntegrationModule();
		
		assertTrue( isOrgAcceptEvents() );
		//----------------------------------------------------------------			
		
		String url = "/integration/module/all";
		HttpEntity request =  TestCommons.getHttpEntity("", NASNAV_ADMIN_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange(url
						, HttpMethod.GET
						, request
						, String.class);
		//----------------------------------------------------------------	
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}
	
	
}
