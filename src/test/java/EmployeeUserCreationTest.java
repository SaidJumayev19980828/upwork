import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.nasnav.AppConfig;
import com.nasnav.NavBox;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.controller.UserController;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.RoleEmployeeUserRepository;
import com.nasnav.dao.RoleRepository;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.response.BaseResponse;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.EmployeeUserService;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@NotThreadSafe 
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
		org.setId(99001L);

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
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 99001, \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
        HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);

		Long id = response.getBody().getEntityId();

		Assert.assertTrue(response.getBody().isSuccess());
		Assert.assertEquals(200, response.getStatusCode().value());
	}
	
	
	
	@Test
	public void createEmployeeUserNoPrivelageTest() {
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 99001, \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";

		//this user have the role CUSTOMER in the test data, it can't create other users
        HttpEntity<Object> employeeUserJson = getHttpEntity(body, "123", "70");
		ResponseEntity<BaseResponse> response = template.postForEntity("/user/create", employeeUserJson, BaseResponse.class);


		
		Assert.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
		Assert.assertFalse(response.getBody().isSuccess());
	}
	
	
	
	@Test
	public void createEmployeeUserByNonExistingUserTest() {
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 99001, \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";

		//this user have the role CUSTOMER in the test data, it can't create other users
        HttpEntity<Object> employeeUserJson = getHttpEntity(body, "InvalidToken", "70");
		ResponseEntity<BaseResponse> response = template.postForEntity("/user/create", employeeUserJson, BaseResponse.class);


		
		Assert.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode().value());
		Assert.assertFalse(response.getBody().isSuccess());
	}
	
	

	@Test
	public void createEmployeeUserInvalidNameTest() {
		// make an invalid employee user json without a name param
		String body = "{\"name\":\"Ahmed#&*\", \"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 99001, \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
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
		String body = "{\"name\":\"Ahmed#&*\", \"email\":\"invalid_email\", \"org_id\": 99001, \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_EMAIL));
	}

	@Test
	public void createEmployeeUserInvalidRoleTest() {
		// make an invalid employee user json without a name param
		String body = "{\"name\":\"Ahmed\", \"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 99001, \"store_id\": 100, \"role\": \"NASNAV_ADMIN, UNKNOWN_ROLE\"}";
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
				"{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 99001, \"store_id\": 0, \"role\": \"STORE_ADMIN\"}", "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity(
				"/user/create", employeeUserJson, UserApiResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_STORE));
	}




	@Test
	public void createEmployeeUserEmailExistsTest() {
		// create employee user with an email
		String body = "{\"name\":\"Ahmed\", \"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 99001, \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
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
				"{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": " + "99001" + ", \"store_id\": 0, \"role\": \"STORE_ADMIN\"}",
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
		String userBody = "{\"name\":\"Ahmed\", \"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 99001, \"store_id\": 100, \"role\": \"ORGANIZATION_EMPLOYEE\"}";
		String loginBody = "{\"email\":\"" + TestCommons.TestUserEmail + "\", \"password\": \"" + EntityConstants.INITIAL_PASSWORD + "\", \"org_id\": 99001, \"employee\": true}";
		//create a new employee user
		HttpEntity<Object> employeeUserJson = getHttpEntity(userBody, "abcdefg", "68");
		HttpEntity<Object> employeeUserLoginJson = getHttpEntity(loginBody, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);
		Long id = response.getBody().getEntityId();
		
		// try to login with this user email before activation
		ResponseEntity<UserApiResponse> loginResponse = template.postForEntity(
				"/user/login", employeeUserLoginJson, UserApiResponse.class);

		Assert.assertFalse(loginResponse.getBody().isSuccess());
		Assert.assertEquals(HttpStatus.LOCKED.value(), loginResponse.getStatusCode().value());
		Assert.assertEquals(true, loginResponse.getBody().getResponseStatuses().contains(ResponseStatus.NEED_ACTIVATION));
	}

	@Test
	public void createEmployeeUserCreationSuccess() {
		// nasnav_admin role test
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + "Adminuser@nasnav.com" + "\", \"org_id\": 99001" + ", \"store_id\": 10, \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);
		Long id = response.getBody().getEntityId();

		Assert.assertTrue(response.getBody().isSuccess());
		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void createEmployeeUserAuthTestDifferentRoleSuccess() {
		// organization_admin role test success
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + "Adminuser@nasnav.com" + "\", \"org_id\": 99001" + ", \"store_id\": 10, \"role\": \"ORGANIZATION_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);
		Long id = response.getBody().getEntityId();

		Assert.assertTrue(response.getBody().isSuccess());
		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void createEmployeeUserAuthTestDifferentOrgFail() {
		// organization_admin role test fail (different organization)
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + "Adminuser@nasnav.com" + "\", \"org_id\": 99002" + ", \"store_id\": 10, \"role\": \"ORGANIZATION_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm", "69");
		ResponseEntity<BaseResponse> response = template.postForEntity("/user/create", employeeUserJson, BaseResponse.class);
		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(406, response.getStatusCode().value());
	}

	@Test
	public void createEmployeeUserAuthTestFail() {
		// organization_admin role test fail (same organization but assigning NASNAV_ADMIN role without authority)
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + "Adminuser@nasnav.com" + "\", \"org_id\": 99001" + ", \"store_id\": 10, \"role\": \"NASNAV_ADMIN\"}";
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
		String body = "{\"employee\":true,\"name\":\"Ahmad\",\"email\":\"" + "ahmad.user@nasnav.com" + "\" }";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		Assert.assertTrue(response.getBody().isSuccess());
		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void updateOtherEmployeeUserAuthTestSuccess() {
		// update user with id 158 success
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"name\":\"hussien\",\"email\":\"" + "hussien.Test@nasnav.com" + "\"," +
				" \"org_id\": 99002" + ", \"role\": \"ORGANIZATION_MANAGER\"}";
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
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm", "69");
		ResponseEntity<BaseResponse> response = template.postForEntity("/user/update", employeeUserJson, BaseResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(406, response.getStatusCode().value());
	}

	@Test
	public void updateOtherEmployeeUserInvaildDataTestFail() {
		// update user with id 158 fail(invalid name and email)
		String body = "{\"updated_user_id\":\"158\",\"name\":\"74man\",\"email\":\"" + "boda  Test@nasnav.com" + "\", \"org_id\": 99001" + "}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);
		Long id = response.getBody().getEntityId();

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(406, response.getStatusCode().value());
	}

	//NASNAV_ADMIN,ORGANIZATION_ADMIN,ORGANIZATION_EMPLOYEE,STORE_EMPLOYEE

	//NASNAV_ADMIN changing roles to NASNAV_ADMIN,ORGANIZATION_ADMIN,ORGANIZATION_EMPLOYEE,STORE_EMPLOYEE
	@Test
	public void updateToNasnavAdminByNasnavAdminTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		Assert.assertTrue(response.getBody().isSuccess());
		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void updateToOrganizationAdminByNasnavAdminTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"ORGANIZATION_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		Assert.assertTrue(response.getBody().isSuccess());
		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void updateToOrganizationEmployeeByNasnavAdminTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"ORGANIZATION_EMPLOYEE\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		Assert.assertTrue(response.getBody().isSuccess());
		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void updateToStoreEmployeeByNasnavAdminTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"STORE_EMPLOYEE\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg", "68");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		Assert.assertTrue(response.getBody().isSuccess());
		Assert.assertEquals(200, response.getStatusCode().value());
	}
	//finish Nasnav_admin role test

	//ORGANIZATION_ADMIN changing roles to NASNAV_ADMIN,ORGANIZATION_ADMIN,ORGANIZATION_EMPLOYEE,STORE_EMPLOYEE
	@Test
	public void updateToNasnavAdminByOrganizationAdminTest() {
		String body = "{\"updated_user_id\":\"158\",\"role\":\"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm", "69");
		ResponseEntity<BaseResponse> response = template.postForEntity("/user/update", employeeUserJson, BaseResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(406, response.getStatusCode().value());
	}

	@Test
	public void updateToOrganizationAdminByOrganizationAdminTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"ORGANIZATION_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm", "69");
		ResponseEntity<BaseResponse> response = template.postForEntity("/user/update", employeeUserJson, BaseResponse.class);

		Assert.assertTrue(response.getBody().isSuccess());
		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void updateToOrganizationEmployeeByOrganizationAdminTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"ORGANIZATION_EMPLOYEE\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm", "69");
		ResponseEntity<BaseResponse> response = template.postForEntity("/user/update", employeeUserJson, BaseResponse.class);

		
		Assert.assertEquals(200, response.getStatusCode().value());
		Assert.assertTrue(response.getBody().isSuccess());
		
	}

	@Test
	public void updateToStoreEmployeeByOrganizationAdminTest() {
		String body = "{\"updated_user_id\":\"158\",\"role\":\"STORE_EMPLOYEE\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm", "69");
		ResponseEntity<BaseResponse> response = template.postForEntity("/user/update", employeeUserJson, BaseResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(406, response.getStatusCode().value());
	}
	//finish ORGANIZATION_ADMIN role test

	//ORGANIZATION_EMPLOYEE changing roles to NASNAV_ADMIN,ORGANIZATION_ADMIN,ORGANIZATION_EMPLOYEE,STORE_EMPLOYEE
	@Test
	public void updateToNasnavAdminByOrganizationEmployeeTest() {
		String body = "{\"employee\":true,\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "123", "70");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(401, response.getStatusCode().value());
	}

	@Test
	public void updateToOrganizationAdminByOrganizationEmployeeTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"ORGANIZATION_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "123", "70");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(401, response.getStatusCode().value());
	}

	@Test
	public void updateToOrganizationEmployeeByOrganizationEmployeeTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"ORGANIZATION_EMPLOYEE\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "123", "70");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(401, response.getStatusCode().value());
	}

	@Test
	public void updateToStoreEmployeeByOrganizationEmployeeTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"STORE_EMPLOYEE\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "123", "70");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(401, response.getStatusCode().value());
	}
	//finish ORGANIZATION_EMPLOYEE role test


	//STORE_EMPLOYEE changing roles to NASNAV_ADMIN,ORGANIZATION_ADMIN,ORGANIZATION_EMPLOYEE,STORE_EMPLOYEE
	@Test
	public void updateToNasnavAdminByStoreEmployeeTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "456", "71");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(401, response.getStatusCode().value());
	}

	@Test
	public void updateToOrganizationAdminByStoreEmployeeTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"ORGANIZATION_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "456", "71");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(401, response.getStatusCode().value());
	}

	@Test
	public void updateToOrganizationEmployeeByStoreEmployeeTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"ORGANIZATION_EMPLOYEE\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "456", "71");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(401, response.getStatusCode().value());
	}

	@Test
	public void updateToStoreEmployeeByStoreEmployeeTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"STORE_EMPLOYEE\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "456", "71");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		Assert.assertFalse(response.getBody().isSuccess());
		Assert.assertEquals(401, response.getStatusCode().value());
	}
	//finish STORE_EMPLOYEE role test
}
