import static com.nasnav.constatnts.EmailConstants.ACTIVATION_ACCOUNT_EMAIL_SUBJECT;
import static com.nasnav.constatnts.EntityConstants.INITIAL_PASSWORD;
import static com.nasnav.enumerations.UserStatus.ACTIVATED;
import static com.nasnav.enumerations.UserStatus.NOT_ACTIVATED;
import static com.nasnav.response.ResponseStatus.ACTIVATION_SENT;
import static com.nasnav.response.ResponseStatus.EMAIL_EXISTS;
import static com.nasnav.response.ResponseStatus.EMAIL_NOT_EXIST;
import static com.nasnav.response.ResponseStatus.EXPIRED_TOKEN;
import static com.nasnav.response.ResponseStatus.INVALID_EMAIL;
import static com.nasnav.response.ResponseStatus.INVALID_NAME;
import static com.nasnav.response.ResponseStatus.INVALID_PARAMETERS;
import static com.nasnav.response.ResponseStatus.INVALID_PASSWORD;
import static com.nasnav.response.ResponseStatus.NEED_ACTIVATION;
import static com.nasnav.test.commons.TestCommons.TestUserEmail;
import static com.nasnav.test.commons.TestCommons.extractAuthTokenFromCookies;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.annotation.PreDestroy;
import javax.mail.MessagingException;

import com.nasnav.dao.AddressRepository;
import com.nasnav.persistence.AddressesEntity;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.RedirectView;

import com.nasnav.AppConfig;
import com.nasnav.NavBox;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.controller.UserController;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dao.UserTokenRepository;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.UserTokensEntity;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.MailService;
import com.nasnav.service.UserService;
import com.nasnav.test.commons.TestCommons;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/UserRegisterTest.sql"})
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class UserRegisterTest {

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
	private OrganizationRepository organizationRepository;

	@Autowired
	private AddressRepository addressRepo;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private UserTokenRepository userTokenRepo;
	
	
	@MockBean
	private MailService mailService;

	@Before
	public void setup() {
		config.mailDryRun = true;
		mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
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
		
		return persistentUser;
	}
	
	

	private OrganizationEntity createOrganization() {
		OrganizationEntity org = new OrganizationEntity();
		org.setId(getNewDummyOrgId());
		org.setName("Test Organization");
		org.setDescription("Test Organization Description");	
		org.setThemeId(0);

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

	@Test
	public void testUserShouldBeRegistered() {
		//create new organization
		OrganizationEntity org = createOrganization();

		HttpEntity<Object> userJson = getHttpEntity(
				"{\"name\":\"Ahmed\",\"email\":\"" + TestUserEmail + "\", \"org_id\":" + org.getId() + "}", null);
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/register", userJson,
				UserApiResponse.class);
		// Delete this user
		userService.deleteUser(response.getBody().getEntityId());
		//delete created organization
		organizationRepository.delete(org);
		
		Assert.assertEquals(201, response.getStatusCode().value());
	}

	@Test
	public void testSameEmailDifferentOrgId() {
		//create new organization
		OrganizationEntity org = createOrganization();
		HttpEntity<Object> userJson = getHttpEntity(
				"{\"name\":\"Ahmed\",\"email\":\"" + TestUserEmail + "\", \"org_id\":" + org.getId() + "}", null);

		//create new organization
		OrganizationEntity newOrg = createOrganization();
		HttpEntity<Object> userJsonNewOrgId = getHttpEntity(
				"{\"name\":\"Ahmed\",\"email\":\"" + TestUserEmail + "\", \"org_id\": " + newOrg.getId() + "}",
				null);

		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/register", userJson,
				UserApiResponse.class);
		// get userId for deletion after test
		Long userId = response.getBody().getEntityId();

		
		// try to re register with the same email and different org_id
		response = template.postForEntity("/user/register", userJsonNewOrgId, UserApiResponse.class);
		
		Long newUserId = response.getBody().getEntityId();
		// response status should contain ACTIVATION_SENT
		Assert.assertTrue(response.getBody().getResponseStatuses().contains(ACTIVATION_SENT));
		Assert.assertEquals(201, response.getStatusCode().value());
		
		// Delete this user
		userService.deleteUser(userId);
		userService.deleteUser(newUserId);

		//delete created organizations
		organizationRepository.delete(org);
		organizationRepository.delete(newOrg);
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
		System.out.println(response.getBody());
		Assert.assertTrue(response.getBody().getResponseStatuses().contains(EMAIL_EXISTS));
		Assert.assertEquals(406, response.getStatusCode().value());
		// Delete this user
		userService.deleteUser(userId);
	}

	
	
	
	
	
	@Test
	public void testInvalidEmailRegistration() {
		HttpEntity<Object> userJson = getHttpEntity("{\"name\":\"Ahmed\",\"email\":\"Foo.bar.com\"}", null);
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/register", userJson,
				UserApiResponse.class);
		// success should be false
		
		// response status should contain INVALID_EMAIL
		Assert.assertTrue(response.getBody().getResponseStatuses().contains(INVALID_EMAIL));
		Assert.assertEquals(406, response.getStatusCode().value());
	}

	
	
	
	@Test
	public void testInvalidNameRegistration() {
		HttpEntity<Object> userJson = getHttpEntity(
				"{\"name\":\"Ahmed234#\",\"email\":\"" + TestCommons.TestUserEmail + "\"}", null);
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/register", userJson,
				UserApiResponse.class);
		// success should be false
		
		// response status should contain INVALID_NAME
		Assert.assertTrue(response.getBody().getResponseStatuses().contains(INVALID_NAME));
		Assert.assertEquals(406, response.getStatusCode().value());
	}

	
	
	
	
	@Test
	public void testInvalidJsonForUserRegisteration() {
		HttpEntity<Object> userJson = getHttpEntity("{,,}");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/register", userJson,
				UserApiResponse.class);
		// success should be false
		
		Assert.assertEquals(406, response.getStatusCode().value());
	}

	
	
	

	@Test
	public void testSendResetPasswordTokenForInvalidMail() {
		ResponseEntity<UserApiResponse> response = getResponseFromGet("/user/recover?email=foo&org_id=" +
				organization.getId() + "&employee=false", UserApiResponse.class);
        System.out.println("###############" + response.getBody().getMessages());
		Assert.assertTrue(response.getBody().getMessages().contains(INVALID_EMAIL.name()));
		
		Assert.assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}

	
	
	
	
	@Test
	public void testSendResetPasswordTokenForValidButFakeMail() {
		ResponseEntity<UserApiResponse> response = getResponseFromGet("/user/recover?email=foo@foo.foo&org_id=" +
						organization.getId() + "&employee=false",
				UserApiResponse.class);
		Assert.assertTrue(response.getBody().getMessages().contains(EMAIL_NOT_EXIST.name()));
		
		Assert.assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}

	
	
	
	
	@Test
	public void testSendResetPasswordTokenForNoPassedMail() {
		ResponseEntity<UserApiResponse> response = getResponseFromGet("/user/recover?email=&org_id=12&employee=false", UserApiResponse.class);
		Assert.assertTrue(response.getBody().getMessages().contains(INVALID_EMAIL.name()));
		
		Assert.assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
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
	public void testInvalidJsonForPasswordRecovery() {

		getResponseFromGet("/user/recover?email=" + persistentUser.getEmail() + "&org_id=" + organization.getId(), UserApiResponse.class);

		HttpEntity<Object> userJson = getHttpEntity("{\t\n" + "\t\"token\": \"QWER\",, \"password\"}", null);

		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/recover", userJson,
				UserApiResponse.class);

		
		Assert.assertTrue(response.getBody().getResponseStatuses().contains(INVALID_PARAMETERS));
		Assert.assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}

	
	
	
	
	@Test
	public void testSetInvalidPassword() {

		getResponseFromGet("/user/recover?email=" + persistentUser.getEmail() + "&org_id=" + organization.getId(), UserApiResponse.class);

		// refresh user
		persistentUser = (UserEntity) userService.getUserById((long)persistentUser.getId());
		HttpEntity<Object> userJson = getHttpEntity(
				"{\"token\":\"" + persistentUser.getResetPasswordToken() + "\"," + "\"password\":\"123\"}", null);

		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/recover", userJson,
				UserApiResponse.class);

		
		Assert.assertTrue(response.getBody().getResponseStatuses().contains(INVALID_PASSWORD));
		Assert.assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}

	
	
	
	@Test
	public void testExpiredToken() {
		persistentUser.setResetPasswordSentAt(LocalDateTime.now().minusHours(EntityConstants.TOKEN_VALIDITY + 1));
		String token = "ABC123XYZ";
		persistentUser.setResetPasswordToken(token);
		userService.update(persistentUser);

		HttpEntity<Object> userJson = getHttpEntity(
				"{\t\n" + "\t\"employee\":false," + "\n\t\"token\":\"" + token + "\",\n" +
						"\t\"password\":\"password\"\n" + "}", null);

		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/recover", userJson,
				UserApiResponse.class);

		
		Assert.assertTrue(response.getBody().getResponseStatuses().contains(EXPIRED_TOKEN));
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
		System.out.println(response.getHeaders().get("Set-Cookie").get(0));
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
		System.out.println(userJson);
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

		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/login", userJson,
				UserApiResponse.class);

		
		Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_CREDENTIALS));
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
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/login", userJson,
				UserApiResponse.class);

		Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_CREDENTIALS));
		Assert.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode().value());
	}
	
	

	@Test
	public void testInvalidJsonForLogin() {
		// try to login using invalid json
		HttpEntity<Object> userJson = getHttpEntity("{\t\n" + "\t\"password" + "\t\"email\":\"\"\n" + "}");

		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/login", userJson,
				UserApiResponse.class);

		
		Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_PARAMETERS));
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}

	@Test
	public void testLoginForNeedActivationUser() {
		// registe new user
		HttpEntity<Object> userJson = getHttpEntity(
				"{\"name\":\"" + "Some New Name" + "\"," + "\"email\":\"" + "another_email@nasnav.com"
						+ "\", \"org_id\": " +  organization.getId() + " }", null);
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/register", userJson,
				UserApiResponse.class);
		Long userId = response.getBody().getEntityId();

		// directly login without changing his passwrod
		userJson = getHttpEntity("{\t\n" + "\t\"password\":\"" + INITIAL_PASSWORD + "\",\n"
				+ "\t\"email\":\"" + "another_email@nasnav.com" + "\", \"org_id\": " +  organization.getId() + " }", null);

		response = template.postForEntity("/user/login", userJson, UserApiResponse.class);
		// Delete this user
		userService.deleteUser(userId);
		
		Assert.assertTrue(response.getBody().getResponseStatuses().contains(NEED_ACTIVATION));
		Assert.assertEquals(HttpStatus.LOCKED.value(), response.getStatusCode().value());
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

		System.out.println(response.toString());
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
				  Mockito.eq("test@nasnav.com")
				, Mockito.eq(ACTIVATION_ACCOUNT_EMAIL_SUBJECT)
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
			.send(
				  Mockito.eq("not.activated@nasnav.com")
				, Mockito.eq(ACTIVATION_ACCOUNT_EMAIL_SUBJECT)
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
			.send(
				  Mockito.eq("no.token.man@nasnav.com")
				, Mockito.eq(ACTIVATION_ACCOUNT_EMAIL_SUBJECT)
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
				  Mockito.eq("no.token.man@nasnav.com")
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
		Assert.assertNull(user.getResetPasswordToken());
	}
	
	
	
	
	
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
		Assert.assertNull(user.getResetPasswordToken());
		
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
	public void updateEmployeeAddressTest() {
		JSONObject address = json().put("address_line_1", "address line");
		JSONObject body = json().put("employee", false)
				.put("address", address);
		HttpEntity request = getHttpEntity(body.toString(), "123");

		//adding address to user
		ResponseEntity<String> response = template.postForEntity("/user/update", request, String.class);
		assertEquals(200, response.getStatusCodeValue());

		Optional<AddressesEntity> entity = addressRepo.findOneByUserId(88001L);
		assertTrue(entity.isPresent());
		AddressesEntity addressesEntity = entity.get();
		assertEquals("address line", addressesEntity.getAddressLine1());




		//unlinking the address from user
		address = json().put("id", addressesEntity.getId());
		body = body.put("address", address);
		request = getHttpEntity(body.toString(), "123");
		response = template.postForEntity("/user/update", request, String.class);

		assertEquals(200, response.getStatusCodeValue());
		assertFalse(addressRepo.findByIdAndUserId(addressesEntity.getId(), 88001L).isPresent());
		addressRepo.delete(addressesEntity);

	}



	private JSONObject createUserRegisterV2Request(String redirectUrl) {
		return json()
				.put("name", "Ahmad")
				.put("email", "test@nasnav.com")
				.put("password", "password")
				.put("org_id", 99001)
				.put("confirmation_flag", true)
				.put("redirect_url", redirectUrl);
	}

}
