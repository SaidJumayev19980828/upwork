import java.time.LocalDateTime;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.nasnav.AppConfig;
import com.nasnav.NavBox;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.controller.UserController;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.UserService;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@NotThreadSafe
public class UserRegisterTest {

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


	@Before
	public void setup() {
		config.mailDryRun = true;
		mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
		cleanup();
	}

	@After
	public void cleanup() {
		UserEntity user = userRepository.getByEmailAndOrganizationId("unavailable@nasnav.com", (long)15);
		if (user != null) {
			userRepository.delete(user);
		}
	}

	@PostConstruct
	public void setupLoginUser() {
		if (organization == null) {
			organization = createOrganization();
		}

		persistentUser = userRepository.getByEmailAndOrganizationId("unavailable@nasnav.com", organization.getId());
		if (persistentUser == null) {
			persistentUser = new UserEntity();
			persistentUser.setName("John Smith");
			persistentUser.setEmail("unavailable@nasnav.com");
			persistentUser.setCreatedAt(LocalDateTime.now());
			persistentUser.setUpdatedAt(LocalDateTime.now());

			//create a new organization and save its id to the user entity
			persistentUser.setOrganizationId(organization.getId());
		}
		persistentUser.setEncPassword("---");
		userRepository.save(persistentUser);
	}

	private OrganizationEntity createOrganization() {
		//create new organization
		OrganizationEntity org = new OrganizationEntity();
		org.setName("Test Organization");
		org.setCreatedAt(new Date());
		org.setUpdatedAt(new Date());
		org.setDescription("Test Organization Description");

		OrganizationEntity organization = organizationRepository.saveAndFlush(org);
		return organization;
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
				"{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\":" + org.getId() + "}");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/register", userJson,
				UserApiResponse.class);
		// Delete this user
		userService.deleteUser(response.getBody().getEntityId());
		//delete created organization
		organizationRepository.delete(org);
		Assert.assertTrue(response.getBody().isSuccess());
		Assert.assertEquals(201, response.getStatusCode().value());
	}

	@Test
	public void testSameEmailDifferentOrgId() {
		//create new organization
		OrganizationEntity org = createOrganization();
		HttpEntity<Object> userJson = getHttpEntity(
				"{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\":" + org.getId() + "}");

		//create new organization
		OrganizationEntity newOrg = createOrganization();
		HttpEntity<Object> userJsonNewOrgId = getHttpEntity(
				"{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": " + newOrg.getId() + "}");

		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/register", userJson,
				UserApiResponse.class);
		// get userId for deletion after test
		Long userId = response.getBody().getEntityId();

		
		// try to re register with the same email and different org_id
		response = template.postForEntity("/user/register", userJsonNewOrgId, UserApiResponse.class);
		Assert.assertTrue(response.getBody().isSuccess());
		Long newUserId = response.getBody().getEntityId();
		// response status should contain ACTIVATION_SENT
		Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.ACTIVATION_SENT));
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
				"{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 5}");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/register", userJson,
				UserApiResponse.class);
		// get userId for deletion after test
		Long userId = response.getBody().getEntityId();

		// try to re register with the same email and org_id
		response = template.postForEntity("/user/register", userJson, UserApiResponse.class);
		Assert.assertFalse(response.getBody().isSuccess());
		// response status should contain EMAIL_EXISTS
		System.out.println(response.getBody());
		Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.EMAIL_EXISTS));
		Assert.assertEquals(406, response.getStatusCode().value());
		// Delete this user
		userService.deleteUser(userId);
	}

	
	@Test
	public void testInvalidEmailRegistration() {
		HttpEntity<Object> userJson = getHttpEntity("{\"name\":\"Ahmed\",\"email\":\"Foo.bar.com\"}");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/register", userJson,
				UserApiResponse.class);
		// success should be false
		Assert.assertFalse(response.getBody().isSuccess());
		// response status should contain INVALID_EMAIL
		Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_EMAIL));
		Assert.assertEquals(406, response.getStatusCode().value());
	}

	@Test
	public void testInvalidNameRegistration() {
		HttpEntity<Object> userJson = getHttpEntity(
				"{\"name\":\"Ahmed234\",\"email\":\"" + TestCommons.TestUserEmail + "\"}");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/register", userJson,
				UserApiResponse.class);
		// success should be false
		Assert.assertFalse(response.getBody().isSuccess());
		// response status should contain INVALID_NAME
		Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_NAME));
		Assert.assertEquals(406, response.getStatusCode().value());
	}

	@Test
	public void testInvalidJsonForUserRegisteration() {
		HttpEntity<Object> userJson = getHttpEntity("{,,}");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/register", userJson,
				UserApiResponse.class);
		// success should be false
		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(406, response.getStatusCode().value());
	}

	/*
	 * @Test public void testSendResetPasswordTokenEmail() { HttpEntity<Object>
	 * userJson = getHttpEntity( "{\"name\":\"" + persistentUser.getName() + "\"," +
	 * "\"email\":\"" + persistentUser.getEmail() + "\"}");
	 * ResponseEntity<UserApiResponse> response = template.postForEntity(
	 * "/user/register", userJson, UserApiResponse.class);
	 * 
	 * long userId = response.getBody().getEntityId(); response =
	 * getResponseFromGet("/user/recover?email=" + persistentUser.getEmail(),
	 * UserApiResponse.class); //Delete this user
	 * Assert.assertTrue(response.getBody().isSuccess());
	 * Assert.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
	 * }
	 */

	@Test
	public void testSendResetPasswordTokenForInvalidMail() {

		ResponseEntity<UserApiResponse> response = getResponseFromGet("/user/recover?email=foo" + "&org_id=" + organization.getId(), UserApiResponse.class);
System.out.println("###############" + response.getBody().getMessages());
		Assert.assertTrue(response.getBody().getMessages().contains(ResponseStatus.INVALID_EMAIL.name()));
		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}

	@Test
	public void testSendResetPasswordTokenForValidButFakeMail() {
		ResponseEntity<UserApiResponse> response = getResponseFromGet("/user/recover?email=foo@foo.foo" + "&org_id=" + organization.getId(),
				UserApiResponse.class);
		Assert.assertTrue(response.getBody().getMessages().contains(ResponseStatus.EMAIL_NOT_EXIST.name()));
		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}

	@Test
	public void testSendResetPasswordTokenForNoPassedMail() {
		ResponseEntity<UserApiResponse> response = getResponseFromGet("/user/recover?email=&org_id=12", UserApiResponse.class);
		Assert.assertTrue(response.getBody().getMessages().contains(ResponseStatus.INVALID_EMAIL.name()));
		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}

	@Test
	public void testPasswordShouldBeReset() {

		persistentUser.setResetPasswordToken("ABCX");
		userService.update(persistentUser);

		getResponseFromGet("/user/recover?email=" + persistentUser.getEmail() + "&org_id=" + organization.getId(), UserApiResponse.class);
		// refresh the entity
		persistentUser = userRepository.findById(persistentUser.getId()).get();
		String token = persistentUser.getResetPasswordToken();
		Assert.assertNotEquals("ABCX", token);

		HttpEntity<Object> userJson = getHttpEntity(
				"{\t\n" + "\t\"token\":\"" + token + "\",\n" + "\t\"password\":\"NewPassword\"\n" + "}");

		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/recover", userJson,
				UserApiResponse.class);

		Assert.assertTrue(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());

		userJson = getHttpEntity(
				"{\"password\":\"" + "NewPassword" + "\"," + "\"email\":\"" + persistentUser.getEmail() + "\", \"org_id\": " +  organization.getId() + " }");

		response = template.postForEntity("/user/login", userJson, UserApiResponse.class);

		// Delete this user
		Assert.assertTrue(response.getBody().isSuccess());
		Assert.assertEquals(200, response.getStatusCode().value());

	}

	@Test
	public void testInvalidJsonForPasswordRecovery() {

		getResponseFromGet("/user/recover?email=" + persistentUser.getEmail() + "&org_id=" + organization.getId(), UserApiResponse.class);

		HttpEntity<Object> userJson = getHttpEntity("{\t\n" + "\t\"token\": \"QWER\",, \"password\"}");

		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/recover", userJson,
				UserApiResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_PARAMETERS));
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}

	@Test
	public void testSetInvalidPassword() {

		getResponseFromGet("/user/recover?email=" + persistentUser.getEmail() + "&org_id=" + organization.getId(), UserApiResponse.class);

		// refresh user
		persistentUser = (UserEntity) userService.getUserById(persistentUser.getId());
		HttpEntity<Object> userJson = getHttpEntity(
				"{\"token\":\"" + persistentUser.getResetPasswordToken() + "\"," + "\"password\":\"123\"}");

		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/recover", userJson,
				UserApiResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_PASSWORD));
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}

	@Test
	public void testExpiredToken() {
		persistentUser.setResetPasswordSentAt(LocalDateTime.now().minusHours(EntityConstants.TOKEN_VALIDITY + 1));
		String token = "ABC123XYZ";
		persistentUser.setResetPasswordToken(token);
		userService.update(persistentUser);

		HttpEntity<Object> userJson = getHttpEntity(
				"{\t\n" + "\t\"token\":\"" + token + "\",\n" + "\t\"password\":\"password\"\n" + "}");

		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/recover", userJson,
				UserApiResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.EXPIRED_TOKEN));
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}

	@Test
	public void testUserShouldLogin() {
		// send token to user
		getResponseFromGet("/user/recover?email=" + persistentUser.getEmail() + "&org_id=" + organization.getId(), UserApiResponse.class);

		// refresh the user entity
		persistentUser = (UserEntity) userService.getUserById(persistentUser.getId());
		// use token to change password
		String token = persistentUser.getResetPasswordToken();
		HttpEntity<Object> userJson = getHttpEntity(
				"{\t\n" + "\t\"token\":\"" + token + "\",\n" + "\t\"password\":\"New_Password\"\n" + "}");

		template.postForEntity("/user/recover", userJson, UserApiResponse.class);

		// login using the new password
		userJson = getHttpEntity(
				"{\"password\":\"" + "New_Password" + "\", \"email\":\"" + persistentUser.getEmail() + "\", \"org_id\": " +  organization.getId() + " }");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/login", userJson,
				UserApiResponse.class);

		Assert.assertTrue(response.getBody().isSuccess());
		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void testInvalidCredentialsLogin() {

		HttpEntity<Object> userJson = getHttpEntity("{\t\n" + "\t\"password\":\"" + "Invalid_Password" + "\",\n"
				+ "\t\"email\":\"" + persistentUser.getEmail() + "\", \"org_id\": " +  organization.getId() + "}");

		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/login", userJson,
				UserApiResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_CREDENTIALS));
		Assert.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode().value());
	}

	@Test
	public void testInvalidJsonForLogin() {
		// try to login using invalid json
		HttpEntity<Object> userJson = getHttpEntity("{\t\n" + "\t\"password" + "\t\"email\":\"\"\n" + "}");

		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/login", userJson,
				UserApiResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_PARAMETERS));
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}

	@Test
	public void testLoginForNeedActivationUser() {
		// registe new user
		HttpEntity<Object> userJson = getHttpEntity(
				"{\"name\":\"" + "Some New Name" + "\"," + "\"email\":\"" + "another_email@nasnav.com"
						+ "\", \"org_id\": " +  organization.getId() + " }");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/register", userJson,
				UserApiResponse.class);
		Long userId = response.getBody().getEntityId();

		// directly login without changing his passwrod
		userJson = getHttpEntity("{\t\n" + "\t\"password\":\"" + EntityConstants.INITIAL_PASSWORD + "\",\n"
				+ "\t\"email\":\"" + "another_email@nasnav.com" + "\", \"org_id\": " +  organization.getId() + " }");

		response = template.postForEntity("/user/login", userJson, UserApiResponse.class);
		// Delete this user
		userService.deleteUser(userId);
		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertTrue(response.getBody().getResponseStatuses().contains(ResponseStatus.NEED_ACTIVATION));
		Assert.assertEquals(HttpStatus.LOCKED.value(), response.getStatusCode().value());
	}
	

	private HttpEntity<Object> getHttpEntity(Object body) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<>(body, headers);
	}

	private <T> ResponseEntity<T> getResponseFromGet(String URL, Class<T> classRef) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return template.exchange(URL, HttpMethod.GET, new HttpEntity<>(headers), classRef);
	}
}
