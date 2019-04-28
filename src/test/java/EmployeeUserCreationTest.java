import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.AppConfig;
import com.nasnav.NavBox;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.controller.UserController;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.EmployeeUserService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
public class EmployeeUserCreationTest {

	private MockMvc mockMvc;
	private EmployeeUserEntity persistentUser;

	@Mock
	private UserController userController;

	@Autowired
	private TestRestTemplate template;

	@Autowired
	private AppConfig config;

	@Autowired
	EmployeeUserService employeeUserService;

	@Autowired
	private EmployeeUserRepository employeeUserRepository;

	@Before
	public void setup() {
		config.mailDryRun = true;
		mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
		cleanup();
	}

	@After
	public void cleanup() {
		EmployeeUserEntity user = employeeUserRepository.getByEmail(TestCommons.TestUserEmail);
		if (user != null) {
			employeeUserRepository.delete(user);
		}
	}

	@PostConstruct
	public void setupLoginUser() {
		persistentUser = employeeUserRepository.getByEmail("unavailable@nasnav.com");
		if (persistentUser == null) {
			persistentUser = new EmployeeUserEntity();
			persistentUser.setName("John Smith");
			persistentUser.setEmail("unavailable@nasnav.com");
			persistentUser.setOrganizationId(1L);
			persistentUser.setShopId((long) 10);
			persistentUser.setCreatedAt(LocalDateTime.now());
			persistentUser.setUpdatedAt(LocalDateTime.now());
		}
		persistentUser.setEncryptedPassword("---");
		employeeUserRepository.save(persistentUser);
	}

	@PreDestroy
	public void removeLoginUser() {
		if (persistentUser != null) {
			employeeUserRepository.delete(persistentUser);
		}
	}
	
	 private HttpEntity<Object> getHttpEntity(Object body) {
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        return new HttpEntity<>(body, headers);
	    }
	
	@Test
    public void createEmployeeUserSuccessTest() {
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 2, \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
        HttpEntity<Object> employeeUserJson = getHttpEntity(body);
		ResponseEntity<UserApiResponse> response = template.postForEntity(
                "/user/create", employeeUserJson, UserApiResponse.class);
        //Delete created user
        employeeUserService.deleteUser(response.getBody().getEntityId());
        Assert.assertTrue(response.getBody().isSuccess());
        Assert.assertEquals(200, response.getStatusCode().value());
    }

	@Test
	public void createEmployeeUserInvalidNameTest() {
		// make an invalid employee user json without a name param
		String body = "{\"name\":\"Ahmed#&*\", \"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 2, \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body);
		ResponseEntity<UserApiResponse> response = template.postForEntity(
				"/user/create", employeeUserJson, UserApiResponse.class);
		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_NAME));
	}

	@Test
	public void createEmployeeUserInvalidEmailTest() {
		// make an invalid employee user json without a name param
		String body = "{\"name\":\"Ahmed#&*\", \"email\":\"invalid_email\", \"org_id\": 2, \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body);
		ResponseEntity<UserApiResponse> response = template.postForEntity(
				"/user/create", employeeUserJson, UserApiResponse.class);
		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_EMAIL));
	}

	@Test
	public void createEmployeeUserInvalidRoleTest() {
		// make an invalid employee user json without a name param
		String body = "{\"name\":\"Ahmed\", \"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 2, \"store_id\": 100, \"role\": \"NASNAV_ADMIN, UNKNOWN_ROLE\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body);
		ResponseEntity<UserApiResponse> response = template.postForEntity(
				"/user/create", employeeUserJson, UserApiResponse.class);
		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_ROLE));
	}
	
	@Test
    public void createEmployeeUserUnAuthorizedRoleTest() {
        HttpEntity<Object> employeeUserJson = getHttpEntity(
                "{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 0, \"store_id\": 100, \"role\": \"ORGANIZATION_ADMIN\"}");
        ResponseEntity<UserApiResponse> response = template.postForEntity(
                "/user/create", employeeUserJson, UserApiResponse.class);
        Assert.assertFalse(response.getBody().isSuccess());
        Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
        System.out.println(response.getBody().getResponseStatuses());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_ORGANIZATION));
	}


	@Test
	public void createEmployeeUserStoreRoleNoIdTest() {
		HttpEntity<Object> employeeUserJson = getHttpEntity(
				"{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 10, \"store_id\": 0, \"role\": \"STORE_ADMIN\"}");
		ResponseEntity<UserApiResponse> response = template.postForEntity(
				"/user/create", employeeUserJson, UserApiResponse.class);
		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_STORE));
		System.out.println(response.getBody().getResponseStatuses());
	}

	@Test
	public void createEmployeeUserEmailExistsTest() {
		// create employee user with an email
		String body = "{\"name\":\"Ahmed\", \"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 2, \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body);
		ResponseEntity<UserApiResponse> response = template.postForEntity(
				"/user/create", employeeUserJson, UserApiResponse.class);
		Long id = response.getBody().getEntityId();
		// try to create another employee user with the same email
		response = template.postForEntity(
				"/user/create", employeeUserJson, UserApiResponse.class);

		//Delete created user
		employeeUserService.deleteUser(id);
		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.EMAIL_EXISTS));
	}

	@Test
	public void employeeUserLoginNeedsActivationTest() {
		// create employee user with an email
		String userBody = "{\"name\":\"Ahmed\", \"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 2, \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
		String loginBody = "{\"email\":\"" + TestCommons.TestUserEmail + "\", \"password\": \"" + EntityConstants.INITIAL_PASSWORD + "\", \"org_id\": 2, \"employee\": true}";
		//create a new employee user
		HttpEntity<Object> employeeUserJson = getHttpEntity(userBody);
		HttpEntity<Object> employeeUserLoginJson = getHttpEntity(loginBody);
		ResponseEntity<UserApiResponse> response = template.postForEntity(
				"/user/create", employeeUserJson, UserApiResponse.class);
		Long id = response.getBody().getEntityId();
		// try to login with this user email before activation
		response = template.postForEntity(
				"/user/login", employeeUserLoginJson, UserApiResponse.class);

		//Delete created user
		employeeUserService.deleteUser(id);
		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.LOCKED.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.NEED_ACTIVATION));
	}
}
