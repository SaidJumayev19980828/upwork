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
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
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

	@Value("classpath:sql/EmpUsers_Test_Data_Insert.sql")
	private Resource userDataInsert;

	@Value("classpath:sql/database_cleanup.sql")
	private Resource databaseCleanup;

	@Autowired
	private DataSource datasource;

	@Before
	public void setup() {
		config.mailDryRun = true;
		mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
		cleanup();
		try (Connection con = datasource.getConnection()) {
			ScriptUtils.executeSqlScript(con, userDataInsert);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@After
	public void cleanup() {
		try (Connection con = datasource.getConnection()) {
			ScriptUtils.executeSqlScript(con, databaseCleanup);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

/*
	private void deleteRoleEmployeeUsers(Integer id) {
		List<RoleEmployeeUser> roleEmployees = roleEmployeeUserRepository.findRoleEmployeeUsersById(id);
		for(RoleEmployeeUser roleEmp : roleEmployees){
			roleEmployeeUserRepository.delete(roleEmp);
		}
	}

	private void deleteRoles(Integer id) {
		List<Role> roles = roleRepository.getRolesOfEmployeeUser(id);
		for(Role role : roles){
			if (role.getId() != 1 && role.getId() != 2) {
				roleRepository.delete(role);
			}
		}
	}
*/

/*
	private OrganizationEntity createOrganization() {
		//create new organization
		OrganizationEntity org = new OrganizationEntity();
		org.setName("Test Organization");
		org.setCreatedAt(new Date());
		org.setUpdatedAt(new Date());
		org.setDescription("Test Organization Description");
		org.setId(801L);

		OrganizationEntity organization = organizationRepository.save(org);
		return organization;
	}
*/

	private HttpEntity<Object> getHttpEntity(Object body, String token, String id) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("User-Token", token);
		headers.add("User-ID", id);
		return new HttpEntity<>(body, headers);
	}

	@Test
	public void createEmployeeUserSuccessTest() {
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 801, \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
        HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);

		Long id = response.getBody().getEntityId();

		Assert.assertTrue(response.getBody().isSuccess());
		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void createEmployeeUserInvalidNameTest() {
		// make an invalid employee user json without a name param
		String body = "{\"name\":\"Ahmed#&*\", \"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 801, \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_NAME));
		//delete created organization
	}

	@Test
	public void createEmployeeUserInvalidEmailTest() {
		// make an invalid employee user json without a name param
		String body = "{\"name\":\"Ahmed#&*\", \"email\":\"invalid_email\", \"org_id\": 801, \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_EMAIL));
	}

	@Test
	public void createEmployeeUserInvalidRoleTest() {
		// make an invalid employee user json without a name param
		String body = "{\"name\":\"Ahmed\", \"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 801, \"store_id\": 100, \"role\": \"NASNAV_ADMIN, UNKNOWN_ROLE\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_ROLE));
	}

	@Test
	public void createEmployeeUserOrganizationNoIdTest() {
		HttpEntity<Object> employeeUserJson = getHttpEntity(
                "{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 0, \"store_id\": 100, \"role\": \"ORGANIZATION_ADMIN\"}", "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);
		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_ORGANIZATION));
	}

	// @Test - Unclear whether store_id can be 0
	public void createEmployeeUserStoreNoIdTest() {
		HttpEntity<Object> employeeUserJson = getHttpEntity(
				"{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 801, \"store_id\": 0, \"role\": \"STORE_ADMIN\"}", "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity(
				"/user/create", employeeUserJson, UserApiResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_STORE));
	}




	@Test
	public void createEmployeeUserEmailExistsTest() {
		// create employee user with an email
		String body = "{\"name\":\"Ahmed\", \"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 801, \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);

		Long id = response.getBody().getEntityId();
		// try to create another employee user with the same email
		response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.EMAIL_EXISTS));
	}


/*
	@Test
	public void createEmployeeUserStoreRoleNoIdTest() {
		HttpEntity<Object> employeeUserJson = getHttpEntity(
				"{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": " + "801" + ", \"store_id\": 0, \"role\": \"STORE_ADMIN\"}",
				"abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity(
				"/user/create", employeeUserJson, UserApiResponse.class);
		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_STORE));
	}
*/

	@Test
	public void employeeUserLoginNeedsActivationTest() {
		// create employee user with an email
		String userBody = "{\"name\":\"Ahmed\", \"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 801, \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
		String loginBody = "{\"email\":\"" + TestCommons.TestUserEmail + "\", \"password\": \"" + EntityConstants.INITIAL_PASSWORD + "\", \"org_id\": 801, \"employee\": true}";
		//create a new employee user
		HttpEntity<Object> employeeUserJson = getHttpEntity(userBody, "abcdefg", "68");
		HttpEntity<Object> employeeUserLoginJson = getHttpEntity(loginBody, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);
		Long id = response.getBody().getEntityId();
		// try to login with this user email before activation
		response = template.postForEntity(
				"/user/login", employeeUserLoginJson, UserApiResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.LOCKED.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.NEED_ACTIVATION));
	}

	@Test
	public void createEmployeeUserCreationSuccess() {
		// nasnav_admin role test
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + "Adminuser@nasnav.com" + "\", \"org_id\": 801" + ", \"store_id\": 10, \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);
		Long id = response.getBody().getEntityId();

		Assert.assertTrue(response.getBody().isSuccess());
		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void createEmployeeUserAuthTestDifferentRoleSuccess() {
		// organization_admin role test success
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + "Adminuser@nasnav.com" + "\", \"org_id\": 801" + ", \"store_id\": 10, \"role\": \"ORGANIZATION_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);
		Long id = response.getBody().getEntityId();

		Assert.assertTrue(response.getBody().isSuccess());
		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void createEmployeeUserAuthTestDifferentOrgFail() {
		// organization_admin role test fail (different organization)
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + "Adminuser@nasnav.com" + "\", \"org_id\": 802" + ", \"store_id\": 10, \"role\": \"ORGANIZATION_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm", "69");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);
		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(406, response.getStatusCode().value());
	}

	@Test
	public void createEmployeeUserAuthTestFail() {
		// organization_admin role test fail (same organization but assigning NASNAV_ADMIN role without authority)
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + "Adminuser@nasnav.com" + "\", \"org_id\": 801" + ", \"store_id\": 10, \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm", "69");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);
		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(401, response.getStatusCode().value());
	}

	@Test
	public void createEmployeeUserNoOrgIdTest() {
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + "Adminuser5@nasnav.com" + "\" , \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);
		Long id = response.getBody().getEntityId();

		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_ORGANIZATION));
	}

	@Test
	public void updateSelfEmployeeUserAuthTestSuccess() {
		// update self data test success
		String body = "{\"name\":\"Ahmad\",\"email\":\"" + "ahmad.user@nasnav.com" + "\" }";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);
		Long id = response.getBody().getEntityId();

System.out.println(response.getBody());
		Assert.assertTrue(response.getBody().isSuccess());
		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void updateOtherEmployeeUserAuthTestSuccess() {
		// update user with id 158 success
		String body = "{\"updated_user_id\":\"158\",\"name\":\"hussien\",\"email\":\"" + "hussien.Test@nasnav.com" + "\", \"org_id\": 802" + ", \"role\": \"ORGANIZATION_MANAGER\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);
		Long id = response.getBody().getEntityId();

		Assert.assertTrue(response.getBody().isSuccess());
		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void updateOtherEmployeeUserAuthTestFail() {
		// update user with id 158 fail(unauthorized assigning NASNAV_ADMIN role)
		String body = "{\"updated_user_id\":\"158\", \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijklm", "69");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);
		Long id = response.getBody().getEntityId();

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(406, response.getStatusCode().value());
	}

	@Test
	public void updateOtherEmployeeUserInvaildDataTestFail() {
		// update user with id 158 fail(invalid name and email)
		String body = "{\"updated_user_id\":\"158\",\"name\":\"74man\",\"email\":\"" + "boda  Test@nasnav.com" + "\", \"org_id\": 801" + "}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);
		Long id = response.getBody().getEntityId();

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(406, response.getStatusCode().value());
	}
}
