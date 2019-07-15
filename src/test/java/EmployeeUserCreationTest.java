import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.AppConfig;
import com.nasnav.NavBox;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.controller.UserController;
import com.nasnav.dao.*;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.Role;
import com.nasnav.persistence.RoleEmployeeUser;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.EmployeeUserService;
import org.apache.tomcat.jni.Local;
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
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
public class EmployeeUserCreationTest {

	private MockMvc mockMvc;
	private EmployeeUserEntity persistentUser;
	private OrganizationEntity organization;

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

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private RoleEmployeeUserRepository roleEmployeeUserRepository;

	@Before
	public void setup() {
		config.mailDryRun = true;
		mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
		cleanup();
	}

	@After
	public void cleanup() {
		EmployeeUserEntity user = employeeUserRepository.getByEmailAndOrganizationId(TestCommons.TestUserEmail, TestCommons.OrgId);
		if (user != null) {
			employeeUserRepository.delete(user);
		}
	}

	@PostConstruct
	public void setupLoginUser() {
		persistentUser = employeeUserRepository.getByEmailAndOrganizationId("unavailable@nasnav.com", (long)(15));
		if (persistentUser == null) {
			persistentUser = new EmployeeUserEntity();
			persistentUser.setName("John Smith");
			persistentUser.setEmail("unavailable@nasnav.com");
			persistentUser.setShopId((long) 10);
			persistentUser.setCreatedAt(LocalDateTime.now());
			persistentUser.setUpdatedAt(LocalDateTime.now());

			//create a new organization and save its id to the user entity
			organization = createOrganization();
			persistentUser.setOrganizationId(organization.getId());
		}
		persistentUser.setEncryptedPassword("---");
		employeeUserRepository.save(persistentUser);
	}


	@PreDestroy
	public void removeLoginUser() {
		if (persistentUser != null) {
			employeeUserRepository.delete(persistentUser);
		}
		// delete created roles
		deleteRoles(persistentUser.getId());
		deleteRoleEmployeeUsers(persistentUser.getId());

		//delete organization
		if(organization != null){
			organizationRepository.delete(organization);
		}
	}

	private void deleteRoleEmployeeUsers(Integer id) {
		List<RoleEmployeeUser> roleEmployees = roleEmployeeUserRepository.findRoleEmployeeUsersById(id);
		for(RoleEmployeeUser roleEmp : roleEmployees){
			roleEmployeeUserRepository.delete(roleEmp);
		}
	}

	private void deleteRoles(Integer id) {
		List<Role> roles = roleRepository.getRolesOfEmployeeUser(id);
		for(Role role : roles){
			roleRepository.delete(role);
		}
	}

	private OrganizationEntity createOrganization() {
		//create new organization
		OrganizationEntity org = new OrganizationEntity();
		org.setName("Test Organization");
		org.setCreatedAt(new Date());
		org.setUpdatedAt(new Date());
		org.setDescription("Test Organization Description");

		OrganizationEntity organization = organizationRepository.save(org);
		return organization;
	}

	 private HttpEntity<Object> getHttpEntity(Object body) {
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        return new HttpEntity<>(body, headers);
	    }
	
	@Test
    public void createEmployeeUserSuccessTest() {
		//create organization
		OrganizationEntity org = createOrganization();
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\":" + org.getId() + ", \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
        HttpEntity<Object> employeeUserJson = getHttpEntity(body);
		ResponseEntity<UserApiResponse> response = template.postForEntity(
                "/user/create", employeeUserJson, UserApiResponse.class);

		Long id = response.getBody().getEntityId();
		//Delete created user
		employeeUserService.deleteUser(id);

		// delete created roles
		deleteRoles(id.intValue());
		deleteRoleEmployeeUsers(id.intValue());

		//delete created organization
		organizationRepository.delete(org);
        Assert.assertTrue(response.getBody().isSuccess());
        Assert.assertEquals(200, response.getStatusCode().value());
    }

	@Test
	public void createEmployeeUserInvalidNameTest() {
		//create organization
		OrganizationEntity org = createOrganization();
		// make an invalid employee user json without a name param
		String body = "{\"name\":\"Ahmed#&*\", \"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\":" + org.getId() + ", \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body);
		ResponseEntity<UserApiResponse> response = template.postForEntity(
				"/user/create", employeeUserJson, UserApiResponse.class);
		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_NAME));
		//delete created organization
		organizationRepository.delete(org);
	}

	@Test
	public void createEmployeeUserInvalidEmailTest() {
		//create organization
		OrganizationEntity org = createOrganization();
		// make an invalid employee user json without a name param
		String body = "{\"name\":\"Ahmed#&*\", \"email\":\"invalid_email\", \"org_id\": " + org.getId() + ", \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body);
		ResponseEntity<UserApiResponse> response = template.postForEntity(
				"/user/create", employeeUserJson, UserApiResponse.class);
		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_EMAIL));
		//delete created organization
		organizationRepository.delete(org);
	}

	@Test
	public void createEmployeeUserInvalidRoleTest() {
		//create organization
		OrganizationEntity org = createOrganization();
		// make an invalid employee user json without a name param
		String body = "{\"name\":\"Ahmed\", \"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": " + org.getId() + ", \"store_id\": 100, \"role\": \"NASNAV_ADMIN, UNKNOWN_ROLE\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body);
		ResponseEntity<UserApiResponse> response = template.postForEntity(
				"/user/create", employeeUserJson, UserApiResponse.class);
		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_ROLE));
		//delete created organization
		organizationRepository.delete(org);
	}
	
	@Test
    public void createEmployeeUserOrganizationRoleNoIdTest() {
		//create organization
		OrganizationEntity org = createOrganization();
        HttpEntity<Object> employeeUserJson = getHttpEntity(
                "{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 0, \"store_id\": 100, \"role\": \"ORGANIZATION_ADMIN\"}");
        ResponseEntity<UserApiResponse> response = template.postForEntity(
                "/user/create", employeeUserJson, UserApiResponse.class);
        Assert.assertFalse(response.getBody().isSuccess());
        Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_ORGANIZATION));
		//delete created organization
		organizationRepository.delete(org);
	}


	@Test
	public void createEmployeeUserStoreRoleNoIdTest() {
		//create organization
		OrganizationEntity org = createOrganization();
		HttpEntity<Object> employeeUserJson = getHttpEntity(
				"{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": " + org.getId() + ", \"store_id\": 0, \"role\": \"STORE_ADMIN\"}");
		ResponseEntity<UserApiResponse> response = template.postForEntity(
				"/user/create", employeeUserJson, UserApiResponse.class);
		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_STORE));
		//delete created organization
		organizationRepository.delete(org);

	}

	@Test
	public void createEmployeeUserEmailExistsTest() {
		//create organization
		OrganizationEntity org = createOrganization();
		// create employee user with an email
		String body = "{\"name\":\"Ahmed\", \"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": " + org.getId() + ", \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body);
		ResponseEntity<UserApiResponse> response = template.postForEntity(
				"/user/create", employeeUserJson, UserApiResponse.class);
		Long id = response.getBody().getEntityId();
		// try to create another employee user with the same email
		response = template.postForEntity(
				"/user/create", employeeUserJson, UserApiResponse.class);

		//Delete created user
		employeeUserService.deleteUser(id);

		// delete created roles
		deleteRoles(id.intValue());
		deleteRoleEmployeeUsers(id.intValue());

		//delete created organization
		organizationRepository.delete(org);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.EMAIL_EXISTS));
	}

	@Test
	public void employeeUserLoginNeedsActivationTest() {
		//create organization
		OrganizationEntity org = createOrganization();
		// create employee user with an email
		String userBody = "{\"name\":\"Ahmed\", \"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": " + org.getId() + ", \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
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

		// delete created roles
		deleteRoles(id.intValue());
		deleteRoleEmployeeUsers(id.intValue());

		//delete created organization
		organizationRepository.delete(org);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.LOCKED.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.NEED_ACTIVATION));
	}
}
