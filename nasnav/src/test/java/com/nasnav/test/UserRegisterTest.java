package com.nasnav.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.AppConfig;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.controller.UserController;
import com.nasnav.dao.*;
import com.nasnav.dto.AddressDTO;
import com.nasnav.dto.AddressRepObj;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.ActivateOtpDto;
import com.nasnav.dto.request.PackageRegisteredByUserDTO;
import com.nasnav.persistence.*;
import com.nasnav.response.RecoveryUserResponse;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.AdminService;
import com.nasnav.service.MailService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.UserService;
import com.nasnav.service.otp.OtpType;
import com.nasnav.test.commons.TestCommons;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.PreDestroy;
import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.nasnav.constatnts.EmailConstants.ACTIVATION_ACCOUNT_EMAIL_SUBJECT;
import static com.nasnav.enumerations.UserStatus.ACTIVATED;
import static com.nasnav.enumerations.UserStatus.NOT_ACTIVATED;
import static com.nasnav.response.ResponseStatus.EMAIL_EXISTS;
import static com.nasnav.response.ResponseStatus.INVALID_PARAMETERS;
import static com.nasnav.test.commons.TestCommons.*;
import static java.lang.String.format;
import static org.junit.Assert.*;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@RunWith(SpringRunner.class)
@NotThreadSafe
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/UserRegisterTest.sql"})
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class UserRegisterTest extends AbstractTestWithTempBaseDir {

	@SuppressWarnings("unused")
	private MockMvc mockMvc;
	private UserEntity persistentUser;
	private OrganizationEntity organization;

	@Mock
	private UserController userController;

	@Autowired
	private TestRestTemplate template;

	@Autowired
	private AppConfig config;

	@Autowired
	UserService userService;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private EmployeeUserRepository employeeUserRepo;
	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private AddressRepository addressRepo;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private UserTokenRepository userTokenRepo;
	@Autowired
	UserOtpRepository userOtpRepository;
	@Autowired
	private UserSubscriptionRepository subsRepo;

	@Autowired
	private ObjectMapper mapper;
	
	@MockBean
	private MailService mailService;

	@Autowired
	private AdminService adminService;

	@Autowired
	private SecurityService securityService;

	private String uniqueAddress = "630f3256-59bb-4b87-9600-60e64d028d68";

	@Before
	public void setup() {
		config.mailDryRun = true;
		mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
	}


	@Before
	public void clearCache(){
		adminService.invalidateCaches();
	}
	
	
	@Before
	public void setupLoginUser() {
		if (organization == null) {
			organization = organizationRepository.findOneById(99001L);
		}

		persistentUser = userRepository.getByEmailAndOrganizationId("unavailable@nasnav.com", organization.getId());
		if (persistentUser == null) {
			persistentUser = createUser();

			persistentUser = userRepository.save(persistentUser);
		}

	}




	private UserEntity createUser() {
		UserEntity persistentUser = new UserEntity();
		persistentUser.setName("John Smith");
		persistentUser.setEmail("unavailable@nasnav.com");
		persistentUser.setEncryptedPassword("---");
		persistentUser.setOrganizationId(organization.getId());
		persistentUser.setUserStatus(ACTIVATED.getValue());
		persistentUser.setAllowReward(false);
		return persistentUser;
	}
	
	

	private OrganizationEntity createOrganization() {
		OrganizationEntity org = new OrganizationEntity();
		org.setId(getNewDummyOrgId());
		org.setName("Test Organization");
		org.setDescription("Test Organization Description");	
		org.setThemeId(0);
		org.setEcommerce(1);

		OrganizationEntity organization = organizationRepository.saveAndFlush(org);
		return organization;
	}
	
	
	/**
	 * Dummy organizations that we can use in tests have id's between 99000 and 99999
	 * */
	private Long getNewDummyOrgId() {
		String sql = "SELECT MAX(ID) + 1 FROM PUBLIC.ORGANIZATIONS WHERE ID BETWEEN 99000 AND 99999";
		Long newId = jdbcTemplate.queryForObject(sql, Long.class);
		return newId != null? newId : 99000L;
	}




	@PreDestroy
	public void removeLoginUser() {
		if (persistentUser != null) {
			userRepository.delete(persistentUser);
		}

		if(organization != null){
			organizationRepository.delete(organization);
		}
	}

	
	
	public void testSameEmailAndOrgId() {
		HttpEntity<Object> userJson = getHttpEntity(
				"{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 5}", null);
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/register", userJson,
				UserApiResponse.class);
		// get userId for deletion after test
		Long userId = response.getBody().getEntityId();

		// try to re register with the same email and org_id
		response = template.postForEntity("/user/register", userJson, UserApiResponse.class);

		// response status should contain EMAIL_EXISTS
		log.debug("{}", response.getBody());
		Assert.assertTrue(response.getBody().getStatus().contains(EMAIL_EXISTS));
		Assert.assertEquals(406, response.getStatusCode().value());
		// Delete this user
		userService.deleteUser(userId);
	}


	@Test
	public void testSendResetPasswordTokenForInvalidMail() {
		ResponseEntity<String> response = getResponseFromGet("/user/recover?email=foo&org_id=" +
				organization.getId() + "&employee=false", String.class);
        log.debug("###############{}", response.getBody());
		Assert.assertTrue(response.getBody().contains("U$EMP$0004"));
		
		Assert.assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}

	@Test
	public void testChangePassword() {
		String body =
				json()
						.put("current_password", "12345678")
						.put("new_password", "newPassword")
						.put("confirm_password", "newPassword")
						.toString();
		HttpEntity<Object> userJson = getHttpEntity(body, "123");
		ResponseEntity<String> response = template.postForEntity("/user/change/password", userJson, String.class);
		assertEquals(200, response.getStatusCode().value());

		ResponseEntity<UserApiResponse> loginResponse = performLogin("user1@nasnav.com", "12345678", 99001L);

		assertEquals(HttpStatus.UNAUTHORIZED, loginResponse.getStatusCode());

		loginResponse = performLogin("user1@nasnav.com", "newPassword", 99001L);

		assertEquals(HttpStatus.OK, loginResponse.getStatusCode());

	}


	@Test
	public void testChangePasswordInvalidOldPassword() {
		String body =
				json()
						.put("current_password", "invalid_password")
						.put("new_password", "newPassword")
						.put("confirm_password", "newPassword")
						.toString();
		HttpEntity<Object> userJson = getHttpEntity(body, "123");
		ResponseEntity<String> response = template.postForEntity("/user/change/password", userJson, String.class);
		assertEquals(406, response.getStatusCode().value());

		ResponseEntity<UserApiResponse> loginResponse = performLogin("user1@nasnav.com", "12345678", 99001L);

		assertEquals(HttpStatus.OK, loginResponse.getStatusCode());

		loginResponse = performLogin("user1@nasnav.com", "newPassword", 99001L);

		assertEquals(HttpStatus.UNAUTHORIZED, loginResponse.getStatusCode());
	}

	private ResponseEntity<UserApiResponse> performLogin(String email, String password, Long orgId) {
		return performLogin(email, password, orgId, false, null);
	}

	private ResponseEntity<UserApiResponse> performLogin(String email, String password, Long orgId, boolean isEmployee, String notificationToken) {
		String request = json()
				.put("email", email)
				.put("password", password)
				.put("org_id", orgId)
				.put("employee", isEmployee)
				.put("notification_token", notificationToken)
				.toString();
		HttpEntity<Object> userJson = getHttpEntity(request, null);

		// ---------------------------------------------------------------------
		return template.postForEntity("/user/login", userJson,
				UserApiResponse.class);
	}

	@Test
	public void testSendResetPasswordTokenForValidButFakeMail() {
		ResponseEntity<String> response = getResponseFromGet("/user/recover?email=foo@foo.foo&org_id=" +
						organization.getId() + "&employee=false", String.class);
		Assert.assertTrue(response.getBody().contains("UXACTVX0001"));
		
		Assert.assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}

	
	
	
	
	@Test
	public void testSendResetPasswordTokenForNoPassedMail() {
		ResponseEntity<String> response = getResponseFromGet("/user/recover?email=&org_id=12&employee=false", String.class);
		Assert.assertTrue(response.getBody().contains("U$EMP$0004"));
		
		Assert.assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}

	
	private void assertResetTocken(String token) {
		HttpEntity<Object> userJson = getHttpEntity(
				"{\t\n" + "\t\"token\":\"" + token + "\",\n" + "\t\"password\":\"NewPassword\"\n" + "}", null);

		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/recover", userJson,
				UserApiResponse.class);

		
		Assert.assertEquals(OK.value(), response.getStatusCode().value());

		userJson = getHttpEntity(
				"{\"password\":\"" + "NewPassword" + "\"," + "\"email\":\""
						+ persistentUser.getEmail() + "\", \"org_id\": " +  organization.getId() + " }", null);

		response = template.postForEntity("/user/login", userJson, UserApiResponse.class);

		// Delete this user
		
		Assert.assertEquals(200, response.getStatusCode().value());
	}
	
	@Test
	public void testPasswordShouldBeReset() {

		persistentUser.setResetPasswordToken("ABCX");
		userService.update(persistentUser);

		getResponseFromGet("/user/recover?email=" + persistentUser.getEmail() + "&org_id=" + organization.getId()
				+ "&employee=false", UserApiResponse.class);
		// refresh the entity
		persistentUser = userRepository.findById((long)persistentUser.getId()).get();
		String token = persistentUser.getResetPasswordToken();
		Assert.assertNotEquals("ABCX", token);

		assertResetTocken(token);

	}

	@Test
	public void testPasswordResetWithOtp() {

		getResponseFromGet("/user/recover?email=" + persistentUser.getEmail() + "&org_id=" + organization.getId()
				+ "&employee=false&activation_method=OTP", UserApiResponse.class);
		String otp = userOtpRepository.findByUserAndType(persistentUser, OtpType.RESET_PASSWORD).map(BaseUserOtpEntity::getOtp).orElse(null);
		ActivateOtpDto activationBody = new ActivateOtpDto(persistentUser.getEmail(), otp, persistentUser.getOrganizationId());
		HttpEntity<ActivateOtpDto> request = new HttpEntity<ActivateOtpDto>(activationBody);
		
		ResponseEntity<RecoveryUserResponse> recoveryResponse = template.postForEntity("/user/recovery/otp-verify", request, RecoveryUserResponse.class);
		Assert.assertEquals(200, recoveryResponse.getStatusCodeValue());
		String token = recoveryResponse.getBody().getResetToken();

		assertResetTocken(token);
		recoveryResponse = template.postForEntity("/user/recovery/otp-verify", request, RecoveryUserResponse.class);
		Assert.assertEquals(400, recoveryResponse.getStatusCodeValue());
	}

	@Test
	public void testPasswordResetWithInvalidOtp() {

		getResponseFromGet("/user/recover?email=" + persistentUser.getEmail() + "&org_id=" + organization.getId()
				+ "&employee=false&activation_method=OTP", UserApiResponse.class);
		String otp = userOtpRepository.findByUserAndType(persistentUser, OtpType.RESET_PASSWORD).map(BaseUserOtpEntity::getOtp).orElse(null);
		ActivateOtpDto activationBody = new ActivateOtpDto(persistentUser.getEmail(), "invalid otp", persistentUser.getOrganizationId());
		HttpEntity<ActivateOtpDto> request = new HttpEntity<ActivateOtpDto>(activationBody);
		
		ResponseEntity<RecoveryUserResponse> recoveryResponse = template.postForEntity("/user/recovery/otp-verify", request, RecoveryUserResponse.class);
		Assert.assertEquals(400, recoveryResponse.getStatusCodeValue());

		for (int i = 0; i < config.otpMaxRetries; i++) {
			recoveryResponse = template.postForEntity("/user/recovery/otp-verify", request, RecoveryUserResponse.class);
			Assert.assertEquals(400, recoveryResponse.getStatusCodeValue());
		}

		activationBody = new ActivateOtpDto(persistentUser.getEmail(), otp, persistentUser.getOrganizationId());
		request = new HttpEntity<ActivateOtpDto>(activationBody);
		// otp is now deleted after max retries
		recoveryResponse = template.postForEntity("/user/recovery/otp-verify", request, RecoveryUserResponse.class);
		Assert.assertEquals(400, recoveryResponse.getStatusCodeValue());
	}



	@Test
	public void testInvalidJsonForPasswordRecovery() {

		getResponseFromGet("/user/recover?email=" + persistentUser.getEmail() + "&org_id=" + organization.getId(), UserApiResponse.class);

		HttpEntity<Object> userJson = getHttpEntity("{\t\n" + "\t\"token\": \"QWER\",, \"password\"}", null);

		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/recover", userJson,
				UserApiResponse.class);

		
		Assert.assertTrue(response.getBody().getStatus().contains(INVALID_PARAMETERS));
		Assert.assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}

	
	
	
	
	@Test
	public void testSetInvalidPassword() {

		getResponseFromGet("/user/recover?email=" + persistentUser.getEmail() + "&org_id=" + organization.getId(), UserApiResponse.class);

		// refresh user
		persistentUser = (UserEntity) userService.getUserById((long)persistentUser.getId());
		HttpEntity<Object> userJson = getHttpEntity(
				"{\"token\":\"" + persistentUser.getResetPasswordToken() + "\"," + "\"password\":\"123\"}", null);

		ResponseEntity<String> response = template.postForEntity("/user/recover", userJson, String.class);

		
		Assert.assertTrue(response.getBody().contains("U$LOG$0005"));
		Assert.assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}
	@Test
	public void testExpiredToken() throws IOException {
		persistentUser.setResetPasswordSentAt(LocalDateTime.now().minusHours(EntityConstants.TOKEN_VALIDITY + 1));
		String token = "ABC123XYZ";
		persistentUser.setResetPasswordToken(token);
		userService.update(persistentUser);

		HttpEntity<Object> userJson = getHttpEntity(
				"{\t\n" + "\t\"employee\":false," + "\n\t\"token\":\"" + token + "\",\n" +
						"\t\"password\":\"password\"\n" + "}", null);

		ResponseEntity<String> response = template.postForEntity("/user/recover", userJson,
				String.class);

		Assert.assertTrue(response.getBody().contains("U$LOG$0006"));
		Assert.assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}


	
	@Test
	public void testUserShouldLogin() {
		//try to get new password to use it for login
		String newPassword = "New_Password"; 		
		resetUserPassword(newPassword);
		
		String request = createLoginJson(newPassword);		
		HttpEntity<Object> userJson = getHttpEntity(request, null);
		
		//---------------------------------------------------------------------
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/login", userJson, UserApiResponse.class);
		
		Optional<String> login1Token = extractAuthTokenFromCookies(response);
		
		Assert.assertEquals(200, response.getStatusCode().value());
		assertTrue(response.getHeaders().get("Set-Cookie").get(0) != null);
		assertTrue(login1Token.isPresent());
		
		//---------------------------------------------------------------------
		ResponseEntity<UserApiResponse> response2 = template.postForEntity("/user/login", userJson, UserApiResponse.class);
		
		Optional<String> login2Token = extractAuthTokenFromCookies(response2);
		
		Assert.assertEquals(200, response2.getStatusCode().value());
		assertTrue(response2.getHeaders().get("Set-Cookie").get(0) != null);
		assertTrue(login2Token.isPresent());
		
		//---------------------------------------------------------------------
		assertNotEquals(login1Token.get(), login2Token.get());
	}
	
	
	
	
	
	@Test
	public void testUsingExpiredToken() {
		ResponseEntity<String> response = template.exchange("/admin/list_organizations", GET, getHttpEntity("", "889966"), String.class);
        Assert.assertEquals(401, response.getStatusCodeValue());
	}


	


	private String createLoginJson(String newPassword) {
		return json()
				.put("password", newPassword)
				.put("email", persistentUser.getEmail())
				.put("org_id", organization.getId())
				.toString();
	}


	

	@Test
	public void testUserLogout() {
		String token = "77";
		assertTrue(userTokenRepo.existsByToken(token));
		long userTokensCountBefore = userTokenRepo.countByUserEntity_Id(88005L);
		assertTrue("we assume the user has multiple tokens in the test", userTokensCountBefore > 1);
		
		//--------------------------------------------------
		HttpEntity<?> entity = getHttpEntity( token );
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/logout", entity, UserApiResponse.class);
		long userTokensCountAfter = userTokenRepo.countByUserEntity_Id(88005L);
		
		//--------------------------------------------------		
		Assert.assertEquals(200, response.getStatusCode().value());
		assertFalse(userTokenRepo.existsByToken(token));		
		assertEquals("other tokens should remain intact", 1L, userTokensCountBefore - userTokensCountAfter);
		log.debug(response.getHeaders().get("Set-Cookie").get(0));
	}
	
	
	
	
	
	
	@Test
	public void testUsingSemiExpiredToken() {
		String token = "875488";
		UserTokensEntity tokenEntityBefore = userTokenRepo.findByToken(token);
		assertNotNull(tokenEntityBefore);
		LocalDateTime tokenUpdateTimeBefore = tokenEntityBefore.getUpdateTime();
		
		ResponseEntity<String> response = template.exchange("/product/images?product_id=1234", GET, getHttpEntity("", token), String.class);
        Assert.assertEquals(406, response.getStatusCodeValue());
        
        UserTokensEntity tokenEntityAfter = userTokenRepo.findByToken(token);
		assertNotNull(tokenEntityAfter);
		LocalDateTime tokenUpdateTimeAfter = tokenEntityAfter.getUpdateTime();
		
		assertTrue("After using a token, and if it is nearly expired, its expiration should be renewed", tokenUpdateTimeAfter.isAfter(tokenUpdateTimeBefore));
	}




	private void resetUserPassword(String newPassword) {
		// send token to user
		getResponseFromGet("/user/recover?email=" + persistentUser.getEmail() + "&employee=false&org_id=" + organization.getId()
				, UserApiResponse.class);

		// refresh the user entity
		persistentUser = (UserEntity) userService.getUserById((long)persistentUser.getId());
		// use token to change password
		String token = persistentUser.getResetPasswordToken();
		HttpEntity<Object> userJson = getHttpEntity(
				"{\t\n" + "\t\"token\":\"" + token + "\",\n" + "\t\"password\":\"New_Password\",\n" +
						"\"employee\": false" +"}", null);
		log.debug("{}", userJson);
		template.postForEntity("/user/recover", userJson, UserApiResponse.class);

		// login using the new password
		userJson = getHttpEntity(
				"{\"password\":\"" + "New_Password" + "\", \"email\":\"" + persistentUser.getEmail() + "\", \"org_id\": " +
						organization.getId() +  "}", null);
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/login", userJson,
				UserApiResponse.class);

		
		Assert.assertEquals(200, response.getStatusCode().value());
	}
	
	
	
	@Test
	public void testUserShouldLoginUppercaseEmail() {
		//try to get new password to use it for login
		String newPassword = "New_Password"; 
		
		resetUserPassword(newPassword);

		// login using the new password and email with different character case
		String email = StringUtils.swapCase(persistentUser.getEmail());
		HttpEntity<Object> userJson = getHttpEntity(
				"{\"password\":\"" + newPassword + "\", \"email\":\"" + email + "\", \"org_id\": " +  organization.getId() + " }", null);
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/login", userJson,
				UserApiResponse.class);

		
		Assert.assertEquals(200, response.getStatusCode().value());
	}
	
	
	
	
	
	
	@Test
	public void testUserSameEmailDifferentOrgShouldLogin() {
		
		// login using the new password
		JSONObject request = new JSONObject();
		request.put("email", "user1@nasnav.com");
		request.put("password", "12345678");
		request.put("org_id", 99001L);
		
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/login"
																, getHttpEntity(request.toString(), null)
																, UserApiResponse.class);

		
		Assert.assertEquals(200, response.getStatusCode().value());
	}
	
	
	
	
	

	@Test
	public void testInvalidCredentialsLogin() {

		HttpEntity<Object> userJson = getHttpEntity("{\t\n" + "\t\"password\":\"" + "Invalid_Password" + "\",\n"
				+ "\t\"email\":\"" + persistentUser.getEmail() + "\", \"org_id\": " +  organization.getId() + "}", null);

		ResponseEntity<String> response = template.postForEntity("/user/login", userJson, String.class);
		
		Assert.assertTrue(response.getBody().contains("U$LOG$0002"));
		Assert.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode().value());
	}
	
	
	
	@Test
	public void testInvalidOrgIdLogin() {
		//try to get new password to use it for login
		String newPassword = "New_Password"; 		
		resetUserPassword(newPassword);

		// login using the new password
		HttpEntity<Object> userJson = getHttpEntity(
				"{\"password\":\"" + newPassword + "\", \"email\":\"" + persistentUser.getEmail() + "\", \"org_id\":null }", null);
		ResponseEntity<String> response = template.postForEntity("/user/login", userJson, String.class);

		Assert.assertTrue(response.getBody().contains("U$LOG$0002"));
		Assert.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode().value());
	}
	
	

	@Test
	public void testInvalidJsonForLogin() {
		// try to login using invalid json
		HttpEntity<Object> userJson = getHttpEntity("{\t\n" + "\t\"password" + "\t\"email\":\"\"\n" + "}", "");

		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/login", userJson,
				UserApiResponse.class);

		
		Assert.assertTrue(response.getBody().getStatus().contains(ResponseStatus.INVALID_PARAMETERS));
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}


	private <T> ResponseEntity<T> getResponseFromGet(String URL, Class<T> classRef) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		return template.exchange(URL, HttpMethod.GET, new HttpEntity<>(headers), classRef);
	}
	
	
	
	

	
	@Test
	public void testCustomerLoginByEmailUsedByCustomerAndEmployee() {
		//try to get new password to use it for login
		String email = "user2@nasnav.com";
		String password = "12345678"; 
		
		String request = new JSONObject()
								.put("password", password)
								.put("email", email)
								.put("org_id", 99001L)
								.put("employee", false)
								.toString();
		
		// login using the new password
		HttpEntity<Object> userJson = getHttpEntity(request, "DOESNOT-NEED-TOKEN");
		ResponseEntity<UserApiResponse> response = 
				template.postForEntity("/user/login", userJson,	UserApiResponse.class);
		
		//-------------------------------------------------------------------
		Assert.assertEquals(200, response.getStatusCode().value());
		
		String token = response.getBody().getToken();
		boolean userLoggedIn = userRepository.existsByAuthenticationToken( token);
		assertTrue("the logged in user should be the customer user, "
				+ "and its token should exists in USERS table", userLoggedIn );
	}


	@Test
	public void updateSelfUserTestSuccess() {
		// update self data test success
		String body = "{\"name\":\"John Doe\"}";
		HttpEntity<Object> userJson = getHttpEntity(body, "123");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", userJson, UserApiResponse.class);

		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void updateSelfUserInvalidDataTest() {
		// update self data test success
		String body = "{\"name\":\"123\", \"email\":\"gds\"}";
		HttpEntity<Object> userJson = getHttpEntity(body, "123");
		ResponseEntity<String> response = template.postForEntity("/user/update", userJson, String.class);

		log.debug("{}", response);
		Assert.assertEquals( 406, response.getStatusCodeValue());
	}


	@Test
	public void newUserRegisterTest() throws MessagingException, IOException {
		String redirectUrl = "https://nasnav.org/dummy_org/login?redirect=checkout";
		String body = createUserRegisterV2Request(redirectUrl).toString();   
		HttpEntity<Object> userJson = getHttpEntity((Object)body);
		ResponseEntity<String> response = template.postForEntity("/user/v2/register", userJson, String.class);

		Assert.assertEquals( 201, response.getStatusCodeValue());
		Mockito
			.verify(mailService)
			.send(
				  Mockito.eq("organization_1")
				, Mockito.eq("test@nasnav.com")
				, Mockito.eq("organization_1"+ACTIVATION_ACCOUNT_EMAIL_SUBJECT)
				, Mockito.anyString()
				, Mockito.anyMap());
	}
@Test
	public void newUserRegisterWithoutActivationMethodTest() throws MessagingException, IOException {
		String redirectUrl = "https://nasnav.org/dummy_org/login?redirect=checkout";
		String body = createUserRegisterV2RequestWithoutActivationMethod(redirectUrl).toString();
		HttpEntity<Object> userJson = getHttpEntity((Object)body);
		ResponseEntity<String> response = template.postForEntity("/user/v2/register", userJson, String.class);

		Assert.assertEquals( 201, response.getStatusCodeValue());
	}

	private JSONObject registerWithOtpAndAssert() throws MessagingException, IOException {
		JSONObject jsonBody = createUserRegisterV2Request(null).put("activation_method", "OTP");
		String body = jsonBody.toString();
		HttpEntity<Object> request = getHttpEntity((Object)body);
		ResponseEntity<String> response = template.postForEntity("/user/v2/register", request, String.class);

		Assert.assertEquals(201, response.getStatusCodeValue());
		Mockito
			.verify(mailService)
			.send(
				  Mockito.eq("organization_1")
				, Mockito.eq("test@nasnav.com")
				, Mockito.eq("organization_1"+ACTIVATION_ACCOUNT_EMAIL_SUBJECT)
				, Mockito.anyString()
				, Mockito.anyMap());
		return jsonBody;
	}

	@Test
	public void newUserRegisterWithOtpTest() throws MessagingException, IOException {
		JSONObject userJson = registerWithOtpAndAssert();
		String email = userJson.getString("email");
		Long orgId = userJson.getLong("org_id");
		UserEntity newUser = userRepository.getByEmailAndOrganizationId(email, orgId);
		String otp = userOtpRepository.findByUserAndType(newUser, OtpType.REGISTER).map(BaseUserOtpEntity::getOtp).orElse(null);
		ActivateOtpDto activationBody = new ActivateOtpDto(email, otp, orgId);
		HttpEntity<ActivateOtpDto> request = new HttpEntity<ActivateOtpDto>(activationBody);
		ResponseEntity<String> response = template.postForEntity("/user/v2/register/otp/activate", request, String.class);
		Assert.assertEquals(200, response.getStatusCodeValue());
		// otp is deleted request should fail
		response = template.postForEntity("/user/v2/register/otp/activate", request, String.class);
		Assert.assertEquals(400, response.getStatusCodeValue());
	}

	@Test
	public void newUserRegisterWithInvalidOtpTest() throws MessagingException, IOException {
		JSONObject userJson = registerWithOtpAndAssert();
		String email = userJson.getString("email");
		Long orgId = userJson.getLong("org_id");
		UserEntity newUser = userRepository.getByEmailAndOrganizationId(email, orgId);
		String otp = userOtpRepository.findByUserAndType(newUser, OtpType.REGISTER).map(BaseUserOtpEntity::getOtp).orElse(null);
		ActivateOtpDto activationBody = new ActivateOtpDto(email, "invalid otp", orgId);
		HttpEntity<ActivateOtpDto> request = new HttpEntity<ActivateOtpDto>(activationBody);
		ResponseEntity<String> response;

		for (int i = 0; i < config.otpMaxRetries; i++) {
			response = template.postForEntity("/user/v2/register/otp/activate", request, String.class);
			Assert.assertEquals(400, response.getStatusCodeValue());
		}

		// next request will fail as otp is deleted after max retries
		activationBody = new ActivateOtpDto("test@nasnav.com", otp, 99001L);
		request = new HttpEntity<ActivateOtpDto>(activationBody);
		response = template.postForEntity("/user/v2/register/otp/activate", request, String.class);
		Assert.assertEquals(400, response.getStatusCodeValue());
	}


	@Test
	public void newUserRegisterWithOrganizationRedirectDomainTest() throws MessagingException, IOException {
		String redirectUrl = "https://www.tooawsome.com/dummy_org/login?redirect=checkout";
		String body = createUserRegisterV2Request(redirectUrl).toString();
		HttpEntity<Object> userJson = getHttpEntity((Object)body);
		ResponseEntity<String> response = template.postForEntity("/user/v2/register", userJson, String.class);

		Assert.assertEquals( 201, response.getStatusCodeValue());
		Mockito
				.verify(mailService)
				.send(
						  Mockito.eq("organization_1")
						, Mockito.eq("test@nasnav.com")
						, Mockito.eq("organization_1"+ACTIVATION_ACCOUNT_EMAIL_SUBJECT)
						, Mockito.anyString()
						, Mockito.anyMap());
	}
	
	
	
	
	
	@Test
	public void activationEmailResendTest() throws MessagingException, IOException {
		String redirectUrl = "https://nasnav.org/dummy_org/login?redirect=checkout";
		
		JSONObject body = createActivationResendRequest(redirectUrl); 
		HttpEntity<Object> userJson = getHttpEntity(body.toString(), null);
		ResponseEntity<String> response = template.postForEntity("/user/v2/register/activate/resend", userJson, String.class);

		Assert.assertEquals( 200, response.getStatusCodeValue());
		Mockito
			.verify(mailService)
			.send(Mockito.eq("organization_1")
				, Mockito.eq("not.activated@nasnav.com")
				, Mockito.eq("organization_1"+ ACTIVATION_ACCOUNT_EMAIL_SUBJECT)
				, Mockito.anyString()
				, Mockito.anyMap());
	}

	@Test
	public void activationEmailResendOtpTest() throws MessagingException, IOException {
		
		JSONObject body = createActivationResendRequest(null);
		body.put("activation_method", "OTP");
		HttpEntity<Object> userJson = getHttpEntity(body.toString(), null);
		ResponseEntity<String> response = template.postForEntity("/user/v2/register/activate/resend", userJson, String.class);

		Assert.assertEquals( 200, response.getStatusCodeValue());
		Mockito
			.verify(mailService)
			.send(Mockito.eq("organization_1")
				, Mockito.eq("not.activated@nasnav.com")
				, Mockito.eq("organization_1"+ ACTIVATION_ACCOUNT_EMAIL_SUBJECT)
				, Mockito.anyString()
				, Mockito.anyMap());
	}
	
	
	
	@Test
	public void activationEmailResendDeactivatedButNotTokenTest() throws MessagingException, IOException {
		String redirectUrl = "https://nasnav.org/dummy_org/login?redirect=checkout";
		
		JSONObject body = createActivationResendRequest(redirectUrl); 
		body.put("email", "no.token.man@nasnav.com");
		
		HttpEntity<Object> userJson = getHttpEntity(body.toString(), null);
		ResponseEntity<String> response = template.postForEntity("/user/v2/register/activate/resend", userJson, String.class);

		Assert.assertEquals( 200, response.getStatusCodeValue());
		Mockito
			.verify(mailService)
			.send(Mockito.eq("organization_1")
				,Mockito.eq("no.token.man@nasnav.com")
				, Mockito.eq("organization_1" + ACTIVATION_ACCOUNT_EMAIL_SUBJECT)
				, Mockito.anyString()
				, Mockito.anyMap());
	}
	
	
	
	
	@Test
	public void activationEmailResendInvalidRedirectTest() throws MessagingException, IOException {
		String redirectUrl = "bla\bla? nasnav.org/dummy_org/login?redirect=checkout";
		
		JSONObject body = createActivationResendRequest(redirectUrl); 
		body.put("email", "no.token.man@nasnav.com");
		
		HttpEntity<Object> userJson = getHttpEntity(body.toString(), null);
		ResponseEntity<String> response = template.postForEntity("/user/v2/register/activate/resend", userJson, String.class);

		Assert.assertEquals( 500, response.getStatusCodeValue());
		Mockito
			.verify(mailService, never())
			.send(
				  Mockito.eq("organization_1")
				, Mockito.eq("no.token.man@nasnav.com")
				, Mockito.eq(ACTIVATION_ACCOUNT_EMAIL_SUBJECT)
				, Mockito.anyString()
				, Mockito.anyMap());
	}
	
	
	//TODO: test activation email resend : non-existing email for organization
	//TODO: test activation email resend : employee user
	//TODO: test activation email resend : too soon



	private JSONObject createActivationResendRequest(String redirectUrl) {
		return json()
				.put("email", "not.activated@nasnav.com")
				.put("org_id", 99001)
				.put("redirect_url", redirectUrl);
	}
	


	@Test
	public void newUserRegisterInvalidDataTest() {
		String body = "{\"name\":\"123\", \"email\":\"test.com\", \"password\":\"password\"," +
				"\"org_id\": 99001, \"confirmation_flag\":true}";
		HttpEntity<Object> userJson = getHttpEntity((Object)body);
		ResponseEntity<String> response = template.postForEntity("/user/v2/register", userJson, String.class);

		Assert.assertEquals( 406, response.getStatusCodeValue());


		body = "{\"name\":\"Ahmad\", \"email\":\"test@nasnav.com\", \"password\":\"password\"," +
				"\"org_id\": 0, \"confirmation_flag\":true}";
		userJson = getHttpEntity((Object)body);
		response = template.postForEntity("/user/v2/register", userJson, String.class);

		Assert.assertEquals( 406, response.getStatusCodeValue());


		body = "{\"name\":\"Ahmad\", \"email\":\"test@nasnav.com\", \"password\":\"password\"," +
				"\"org_id\": 99001, \"confirmation_flag\":fales}";
		userJson = getHttpEntity((Object)body);
		response = template.postForEntity("/user/v2/register", userJson, String.class);

		Assert.assertEquals( 406, response.getStatusCodeValue());
	}


	@Test
	public void newUserRegisterMissingDataTest() {
		String body = "{\"name\": null, \"email\":null, \"password\":\"password\"," +
				"\"org_id\": 99001, \"confirmation_flag\":true}";
		HttpEntity<Object> userJson = getHttpEntity((Object) body);
		ResponseEntity<String> response = template.postForEntity("/user/v2/register", userJson, String.class);
		Assert.assertEquals( 406, response.getStatusCodeValue());

		body = "{\"name\":\"Ahmad\", \"email\":\"test@nasnav.com\", \"password\":\"password\"," +
				"\"org_id\": null, \"confirmation_flag\":true}";
		userJson = getHttpEntity((Object)body);
		response = template.postForEntity("/user/v2/register", userJson, String.class);

		Assert.assertEquals( 406, response.getStatusCodeValue());
	}


	@Test
	public void newUserRegisterExistingUserTest() {
		String body = "{\"name\": \"name\", \"email\":\"user1@nasnav.com\", \"password\":\"password\"," +
				"\"org_id\": 99001, \"confirmation_flag\":true}";
		HttpEntity<Object> userJson = getHttpEntity((Object) body);
		ResponseEntity<String> response = template.postForEntity("/user/v2/register", userJson, String.class);
		Assert.assertEquals( 406, response.getStatusCodeValue());
	}


	@Test
	public void activateAccountTest() {
		//first create account
		String redirectUrl = "https://nasnav.org/dummy_org/login?redirect=checkout";
		String body = createUserRegisterV2Request(redirectUrl).toString();   
		HttpEntity<Object> userJson = getHttpEntity(body, null);
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/v2/register", userJson, UserApiResponse.class);
		Assert.assertEquals( 201, response.getStatusCodeValue());

		UserEntity user = userRepository.findById(response.getBody().getEntityId()).get();

		String activationUrl = format("/user/v2/register/activate?token=%s&redirect=%s", user.getResetPasswordToken(), redirectUrl);
		
		ResponseEntity<RedirectView> activationRes = template.getForEntity(activationUrl, RedirectView.class);

		user = userRepository.findById(response.getBody().getEntityId()).get();		
		String exepctedtUrl = format("https://nasnav.org/dummy_org/login?redirect=checkout&auth_token=%s", user.getAuthenticationToken());
		Assert.assertEquals(exepctedtUrl , activationRes.getHeaders().getLocation().toString());
		
		Assert.assertEquals(ACTIVATED.getValue(), user.getUserStatus());
		assertNull(user.getResetPasswordToken());
	}
	
	
	//TODO: activation email redirect is not in nasnav domain but in organization domains
	
	
	@Test
	public void activateAccountNoRedirectTest() {
		//first create account
		String redirectUrl = "https://nasnav.org/dummy_org/login?redirect=checkout";
		String body = createUserRegisterV2Request(redirectUrl).toString();   
		HttpEntity<Object> userJson = getHttpEntity(body, null);
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/v2/register", userJson, UserApiResponse.class);
		Assert.assertEquals( 201, response.getStatusCodeValue());

		UserEntity user = userRepository.findById(response.getBody().getEntityId()).get();

		String activationUrl = format("/user/v2/register/activate?token=%s", user.getResetPasswordToken(), redirectUrl);
		
		@SuppressWarnings("unused")
		ResponseEntity<String> activationRes = template.postForEntity(activationUrl, getHttpEntity(""), String.class);

		user = userRepository.findById(response.getBody().getEntityId()).get();		
		
		Assert.assertEquals(ACTIVATED.getValue(), user.getUserStatus());
		assertNull(user.getResetPasswordToken());
		
		//TODO: add assertion for returned login response 
	}
	
	
	
	
	
	
	@Test
	public void registerAccountTestInvalidRedirect() {
		String redirectUrl = "https://HACKER.BAD/hacking";
		String body = createUserRegisterV2Request(redirectUrl).toString();  
		
		HttpEntity<Object> userJson = getHttpEntity(body);
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/v2/register", userJson, UserApiResponse.class);
		
		Assert.assertEquals( 406, response.getStatusCodeValue());
	}
	
	
	
	
	@Test
	public void activateAccountTestInvalidRedirect() {
		//first create account
		String redirectUrl = "https://nasnav.org/dummy_org/login?redirect=checkout";
		String body = createUserRegisterV2Request(redirectUrl).toString();  
		HttpEntity<Object> userJson = getHttpEntity(body, null);
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/v2/register", userJson, UserApiResponse.class);
		Assert.assertEquals( 201, response.getStatusCodeValue());

		
		//try activating the account
		UserEntity user = userRepository.findById(response.getBody().getEntityId()).get();

		String invalidRedirectUrl = "https://HACKER.BAD/hacking";
		String activationUrl = format("/user/v2/register/activate?token=%s&redirect=%s", user.getResetPasswordToken(), invalidRedirectUrl);
		
		ResponseEntity<RedirectView> activationRes = template.getForEntity(activationUrl, RedirectView.class);
		Assert.assertEquals( 406, activationRes.getStatusCodeValue());
		
		user = userRepository.findById(response.getBody().getEntityId()).get();		
		
		Assert.assertEquals(NOT_ACTIVATED.getValue(), user.getUserStatus());
		Assert.assertNotNull(user.getResetPasswordToken());
	}


	@Test
	public void testGettingSameEmpUserInfoDifferentTokens() {
		//get user info by auth token 101112
		HttpEntity<?> entity = getHttpEntity("101112");
		ResponseEntity<UserRepresentationObject> res = template.exchange("/user/info?is_employee=true",
									HttpMethod.GET,
									entity,
									UserRepresentationObject.class);

		assertEquals(200, res.getStatusCodeValue());
		assertTrue(res.getBody().getId().equals(159L));

		//another auth token 131415 for the same user
		entity = getHttpEntity("131415");
		res = template.exchange("/user/info?is_employee=true",
				HttpMethod.GET,
				entity,
				UserRepresentationObject.class);

		assertEquals(200, res.getStatusCodeValue());
		assertTrue(res.getBody().getId().equals(159L));

		//another auth token 161718 for the same user
		entity = getHttpEntity("161718");
		res = template.exchange("/user/info?is_employee=true",
				HttpMethod.GET,
				entity,
				UserRepresentationObject.class);

		assertEquals(200, res.getStatusCodeValue());
		assertTrue(res.getBody().getId().equals(159L));
	}



	@Test
	public void testGettingSameUserInfoDifferentTokens() {
		//get user info by auth token 77
		HttpEntity<?> entity = getHttpEntity("77");
		ResponseEntity<UserRepresentationObject> res = template.exchange("/user/info?is_employee=false",
				HttpMethod.GET,
				entity,
				UserRepresentationObject.class);

		assertEquals(200, res.getStatusCodeValue());
		assertTrue(res.getBody().getId().equals(88005L));

		//another auth token 88 for the same user
		entity = getHttpEntity("88");
		res = template.exchange("/user/info?is_employee=false",
				HttpMethod.GET,
				entity,
				UserRepresentationObject.class);

		assertEquals(200, res.getStatusCodeValue());
		assertTrue(res.getBody().getId().equals(88005L));

		//another auth token 99 for the same user
		entity = getHttpEntity("99");
		res = template.exchange("/user/info?is_employee=false",
				HttpMethod.GET,
				entity,
				UserRepresentationObject.class);

		assertEquals(200, res.getStatusCodeValue());
		assertTrue(res.getBody().getId().equals(88005L));
	}



	@Test
	public void updateUserAddressTest() {
		JSONObject address =
				json()
				.put("phone_number", "01234567891")
				.put("area_id", 100001)
				.put("address_line_1", uniqueAddress)
				.put("sub_area_id", 888001);
		HttpEntity<?> request = getHttpEntity(address.toString(), "123");

		//adding address to user
		ResponseEntity<AddressDTO> response = template.exchange("/user/address", PUT, request, AddressDTO.class);
		assertEquals(200, response.getStatusCodeValue());

		Optional<AddressRepObj> entity = addressRepo.findByUserId(88001L).stream().findFirst();
		assertTrue(entity.isPresent());
		AddressRepObj addressResponse = entity.get();
		assertEquals(uniqueAddress, addressResponse.getAddressLine1());
		assertEquals(888001L, addressResponse.getSubAreaId().longValue());
		assertEquals("Badr city", addressResponse.getSubArea());



		//unlinking the address from user
		address = json()
					.put("phone_number", "01234567891")
					.put("area_id", 100001)
					.put("id", addressResponse.getId())
					.put("address_line_1", "Sesame street");
		request = getHttpEntity(address.toString(), "123");
		response = template.exchange("/user/address", PUT, request, AddressDTO.class);

		assertEquals(200, response.getStatusCodeValue());
		assertFalse(addressRepo.findByIdAndUserId(addressResponse.getId(), 88001L).isPresent());
		addressRepo.deleteById(addressResponse.getId());
	}



	@Test
	public void updateUserAddressSubAreaNotPerOrganizationTest() {
		JSONObject address =
				json()
				.put("phone_number", "01234567891")
				.put("area_id", 100001)
				.put("address_line_1", uniqueAddress)
				.put("sub_area_id", 888002);
		HttpEntity<?> request = getHttpEntity(address.toString(), "123");

		ResponseEntity<AddressDTO> response = template.exchange("/user/address", PUT, request, AddressDTO.class);
		assertEquals(406, response.getStatusCodeValue());
	}



	//@Test
	public void updateUserAddressSubAreaNotProvidingAreaTest() {
		JSONObject address =
				json()
				.put("area_id", "01234567891")
				.put("phone_number", "01234567891")
				.put("address_line_1", uniqueAddress)
				.put("sub_area_id", 888003);
		HttpEntity<?> request = getHttpEntity(address.toString(), "123");

		ResponseEntity<AddressDTO> response = template.exchange("/user/address", PUT, request, AddressDTO.class);
		assertEquals(200, response.getStatusCodeValue());
		Optional<AddressRepObj> entity = addressRepo.findByUserId(88001L).stream().findFirst();
		assertTrue(entity.isPresent());
		AddressRepObj addressResponse = entity.get();
		assertEquals(addressResponse.getSubAreaId().longValue(), 888003L);
		assertEquals("area will be assigned automatically", addressResponse.getAreaId().longValue(), 100002L);
	}




	@Test
	public void updateUserAddressUpdateBothAreaAndSubAreaSuccessTest() {
		JSONObject address =
				json()
				.put("phone_number", "01234567891")
				.put("address_line_1", uniqueAddress)
				.put("sub_area_id", 888003)
				.put("area_id", 100002);
		HttpEntity<?> request = getHttpEntity(address.toString(), "123");

		ResponseEntity<AddressDTO> response = template.exchange("/user/address", PUT, request, AddressDTO.class);
		assertEquals(200, response.getStatusCodeValue());
		Optional<AddressRepObj> entity = addressRepo.findByUserId(88001L).stream().findFirst();
		assertTrue(entity.isPresent());
		AddressRepObj addressResponse = entity.get();
		assertEquals(addressResponse.getSubAreaId().longValue(), 888003L);
		assertEquals(addressResponse.getAreaId().longValue(), 100002L);
	}




	@Test
	public void updateUserAddressUpdateBothAreaAndSubAreaFailedTest() {
		JSONObject address =
				json()
				.put("address_line_1", uniqueAddress)
				.put("sub_area_id", 888001)
				.put("phone_number", "01234567891")
				.put("area_id", 100002);
		HttpEntity<?> request = getHttpEntity(address.toString(), "123");

		ResponseEntity<AddressDTO> response = template.exchange("/user/address", PUT, request, AddressDTO.class);
		assertEquals(406, response.getStatusCodeValue());
	}




	@Test
	public void updateUserAddressNoSubAreaTest() {
		JSONObject address = json()
				.put("phone_number", "01234567891")
				.put("area_id", 100001)
				.put("address_line_1", uniqueAddress);
		HttpEntity<?> request = getHttpEntity(address.toString(), "123");

		ResponseEntity<AddressDTO> response = template.exchange("/user/address", PUT, request, AddressDTO.class);
		assertEquals(200, response.getStatusCodeValue());

		Optional<AddressRepObj> entity = addressRepo.findByUserId(88001L).stream().findFirst();
		assertTrue(entity.isPresent());
		AddressRepObj addressResponse = entity.get();
		assertEquals(uniqueAddress, addressResponse.getAddressLine1());
		assertNull(addressResponse.getSubAreaId());
		assertNull(addressResponse.getSubArea());
	}



	@Test
	public void logoutUserTest() {
		Long userTokensCount = userTokenRepo.countByUserEntity_Id(88005L);
		assertEquals(4 ,userTokensCount.intValue());

		HttpEntity req = getHttpEntity("77");
		template.postForEntity("/user/logout_all", req, UserApiResponse.class);

		Long userTokensCountAfter = userTokenRepo.countByUserEntity_Id(88005L);
		assertEquals(0 ,userTokensCountAfter.intValue());
	}



	@Test
	public void logoutEmployeeUserTest() {
		Long userTokensCount = userTokenRepo.countByEmployeeUserEntity_Id(159l);
		assertEquals(4 ,userTokensCount.intValue());

		HttpEntity req = getHttpEntity("101112");
		template.postForEntity("/user/logout_all", req, UserApiResponse.class);

		Long userTokensCountAfter = userTokenRepo.countByEmployeeUserEntity_Id(159l);
		assertEquals(0 ,userTokensCountAfter.intValue());
	}



	@Test
	public void suspendUserInvalidAuthZ() {
		HttpEntity req = getHttpEntity("invalid token");
		ResponseEntity<String> res = template.postForEntity("/user/suspend?user_id=88001&suspend=true", req, String.class);

		assertEquals(401, res.getStatusCodeValue());
	}



	@Test
	public void suspendUserInvalidAuthN() {
		HttpEntity req = getHttpEntity("192021");
		ResponseEntity<String> res = template.postForEntity("/user/suspend?user_id=88001&suspend=true", req, String.class);

		assertEquals(403, res.getStatusCodeValue());
	}



	@Test
	public void suspendUserInAnotherOrg() {
		HttpEntity req = getHttpEntity("101112");
		ResponseEntity<String> res = template.postForEntity("/user/suspend?user_id=88002&suspend=true", req, String.class);

		assertEquals(404, res.getStatusCodeValue());
	}



	@Test
	public void suspendUserTest() {
		HttpEntity req = getHttpEntity("101112");
		ResponseEntity<String> res = template.postForEntity("/user/suspend?user_id=88001&suspend=true", req, String.class);

		assertEquals(200, res.getStatusCodeValue());
		UserEntity user = userRepository.findById(88001L).get();
		assertEquals(202, user.getUserStatus().intValue());

		Long userTokensCountAfter = userTokenRepo.countByUserEntity_Id(88001L);
		assertEquals(0 ,userTokensCountAfter.intValue());
	}



	@Test
	public void unsuspendUserTest() {
		HttpEntity req = getHttpEntity("101112");
		ResponseEntity<String> res = template.postForEntity("/user/suspend?user_id=88006&suspend=false", req, String.class);

		assertEquals(200, res.getStatusCodeValue());
		UserEntity user = userRepository.findById(88006L).get();
		assertEquals(201, user.getUserStatus().intValue());
	}

	@Test
	public void suspendUserNotActivatedAccount() {
		HttpEntity req = getHttpEntity("101112");
		ResponseEntity<String> res = template.postForEntity("/user/suspend?user_id=88004&suspend=true", req, String.class);

		assertEquals(406, res.getStatusCodeValue());
		UserEntity user = userRepository.findById(88004L).get();
		assertEquals(200, user.getUserStatus().intValue());
	}

	@Test
	public void unsuspendUserNotActivatedAccount() {
		HttpEntity req = getHttpEntity("101112");
		ResponseEntity<String> res = template.postForEntity("/user/suspend?user_id=88004&suspend=false", req, String.class);

		assertEquals(406, res.getStatusCodeValue());
		UserEntity user = userRepository.findById(88004L).get();
		assertEquals(200, user.getUserStatus().intValue());
	}



	@Test
	public void loginSuspendedUser(){
		String email = "suspended.man@nasnav.com";
		String password = "963";

		String request = new JSONObject()
				.put("password", password)
				.put("email", email)
				.put("org_id", 99001L)
				.put("employee", false)
				.toString();

		HttpEntity<Object> userJson = getHttpEntity(request, null);
		ResponseEntity<UserApiResponse> response =
				template.postForEntity("/user/login", userJson,	UserApiResponse.class);
		assertEquals(423, response.getStatusCodeValue());
	}



	@Test
	public void userSubscribeTest(){
		ResponseEntity<String> response =
				template.exchange("/user/subscribe?email=email@nasnav.com&org_id=99001", HttpMethod.POST, null, String.class);
		assertEquals(200, response.getStatusCodeValue());
		UserSubscriptionEntity userSub = subsRepo.findByEmailAndOrganization_Id("email@nasnav.com", 99001L);
		assertNotNull(userSub.getToken());


		ResponseEntity<RedirectView> res =
				template.getForEntity("/user/subscribe/activate?org_id=99001&token=" + userSub.getToken(), RedirectView.class);
		assertEquals(302, res.getStatusCodeValue());
		userSub = subsRepo.findByEmailAndOrganization_Id("email@nasnav.com", 99001L);
		assertTrue(Objects.isNull(userSub.getToken()));
	}


	@Test
	public void getSubscribedUsers() throws IOException {
		HttpEntity req = getHttpEntity("101112");
		ResponseEntity<String> res = template.exchange("/organization/subscribed_users",GET, req, String.class);
		List<String> users = mapper.readValue(res.getBody(), List.class);
		assertEquals(200, res.getStatusCodeValue());
		assertTrue(!users.isEmpty());
	}


	@Test
	public void getSubscribedUsersAuthZ() {
		HttpEntity req = getHttpEntity("non exist");
		ResponseEntity<String> res = template.exchange("/organization/subscribed_users",GET, req, String.class);
		assertEquals(401, res.getStatusCodeValue());
	}


	@Test
	public void getSubscribedUsersAuthN() {
		HttpEntity req = getHttpEntity("222324");
		ResponseEntity<String> res = template.exchange("/organization/subscribed_users",GET, req, String.class);
		assertEquals(403, res.getStatusCodeValue());
	}


	@Test
	public void removeSubscribedUser() {
		assertTrue(subsRepo.existsByEmailAndOrganization_Id("sub@g.com", 99001L));
		HttpEntity req = getHttpEntity("101112");
		ResponseEntity<String> res = template.exchange("/organization/subscribed_users?email=sub@g.com",DELETE, req, String.class);
		assertEquals(200, res.getStatusCodeValue());
		assertFalse(subsRepo.existsByEmailAndOrganization_Id("sub@g.com", 99001L));
	}


	@Test
	public void removeSubscribedUserInvalidAuthZ() {
		HttpEntity req = getHttpEntity("nonexist");
		ResponseEntity<String> res = template.exchange("/organization/subscribed_users?email=sub@g.com",DELETE, req, String.class);
		assertEquals(401, res.getStatusCodeValue());
	}


	@Test
	public void removeSubscribedUserInvalidAuthN() {
		HttpEntity req = getHttpEntity("222324");
		ResponseEntity<String> res = template.exchange("/organization/subscribed_users?email=sub@g.com",DELETE, req, String.class);
		assertEquals(403, res.getStatusCodeValue());
	}


	@Test
	public void activateSubscribedUsersInvalidToken() {
		ResponseEntity<RedirectView> res =
				template.getForEntity("/user/subscribe/activate?org_id=99001&token=invalid", RedirectView.class);
		assertEquals(302, res.getStatusCodeValue());
	}


	@Test
	public void userSubscribeTestInvalidEmail(){
		ResponseEntity<String> response =
				template.exchange("/user/subscribe?email=invalidmail&org_id=99001", HttpMethod.POST, null, String.class);
		assertEquals(406, response.getStatusCodeValue());
	}

	@Test
	public void testRecoverUserRemovesOldTokens() {

		Long oldTokensCount = userTokenRepo.countByUserEntity_Id(88005L);
		assertTrue(oldTokensCount.intValue() == 4);
		String request = new JSONObject()
				.put("password", "12345678")
				.put("token", "d67438ac-f3a5-4939-9686-a1fc096f3f4f")
				.put("employee", false)
				.put("org_id", 99001)
				.toString();

		HttpEntity<Object> userJson = getHttpEntity(request, "DOESNOT-NEED-TOKEN");
		ResponseEntity<UserApiResponse> response =
				template.postForEntity("/user/recover", userJson,	UserApiResponse.class);

		Assert.assertEquals(200, response.getStatusCode().value());
		String token = response.getBody().getToken();
		boolean userLoggedIn = userTokenRepo.existsByToken(token);
		assertTrue("the recovered user should be logged in ", userLoggedIn );
		Long newTokensCount = userTokenRepo.countByUserEntity_Id(88005L);
		assertEquals(1, newTokensCount.intValue());
	}

	@Test
	public void listCustomersPageableSuccess() throws IOException {
		HttpEntity<?> req = getHttpEntity("101112");
		ResponseEntity<String> res = template.exchange("/user/list/customer?paging_start=0&paging_count=30&user_status=201", GET, req, String.class);
		assertEquals(200, res.getStatusCodeValue());
		List<UserRepresentationObject> userList = mapper.readValue(res.getBody(), new TypeReference<List<UserRepresentationObject>>(){});
		assertFalse(userList.isEmpty());
	}

	@Test
	public void listCustomersNoAuthN() throws IOException {
		HttpEntity<?> req = getHttpEntity("INVALID-TOKEN");
		ResponseEntity<String> res = template.exchange("/user/list/customer", GET, req, String.class);
		assertEquals(UNAUTHORIZED, res.getStatusCode());
	}



	@Test
	public void listCustomersNoAuthZ() throws IOException {
		HttpEntity<?> req = getHttpEntity("123");
		ResponseEntity<String> res = template.exchange("/user/list/customer", GET, req, String.class);
		assertEquals(FORBIDDEN, res.getStatusCode());
	}


	@Test
	public void recoverEmployeeUser() {
		Long userId = 162L;
		EmployeeUserEntity user = employeeUserRepo.findById(userId).get();
		assertEquals(NOT_ACTIVATED.getValue(), user.getUserStatus());

		String body = json()
				.put("token", "d67438ac-f3a5-4939-9686-a1fc096f3f4e")
				.put("password", "password")
				.put("org_id", 99001)
				.put("employee", true)
				.toString();
		HttpEntity request = getHttpEntity(body, null);
		ResponseEntity<String> res = template.postForEntity("/user/recover", request, String.class);
		assertEquals(200, res.getStatusCodeValue());
		user = employeeUserRepo.findById(userId).get();
		assertEquals(ACTIVATED.getValue(), user.getUserStatus());
	}

	@Test
	public void listUserNotificationTokens() {
		final String notificationToken = "SomeNotificationToken";
		var response = performLogin("user1@nasnav.com", "12345678", 99001L, false, notificationToken);
		assertEquals(OK, response.getStatusCode());

		UserEntity user = userRepository.findByEmailAndOrganizationId("user1@nasnav.com", 99001L).orElse(persistentUser);
		Set<String> notificationTokens = securityService.getValidNotificationTokens(user);
		Set<String> notificationTokensByUsers = securityService.getValidUserNotificationTokens(user);

		Set<String> expectedTokens = Set.of(notificationToken);
		assertEquals(expectedTokens, notificationTokens);
		assertEquals(expectedTokens, notificationTokensByUsers);
	}

	private JSONObject createUserRegisterV2Request(String redirectUrl) {
		return json()
				.put("activation_method", "VERIFICATION_LINK")
				.put("name", "Ahmad")
				.put("email", "test@nasnav.com")
				.put("password", "password")
				.put("org_id", 99001)
				.put("confirmation_flag", true)
				.put("redirect_url", redirectUrl);
	}
	private JSONObject createUserRegisterV2RequestWithoutActivationMethod(String redirectUrl) {
		return json()
				.put("name", "Ahmad")
				.put("email", "test@nasnav.com")
				.put("password", "password")
				.put("org_id", 99001)
				.put("confirmation_flag", true)
				.put("redirect_url", redirectUrl);
	}

}
