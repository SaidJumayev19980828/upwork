package com.nasnav.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.AppConfig;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.controller.UserController;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.UserTokenRepository;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.enumerations.Gender;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.response.BaseResponse;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.EmployeeUserService;
import com.nasnav.service.RoleService;
import com.nasnav.service.SecurityService;

import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.nasnav.test.commons.TestCommons.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.*;

@RunWith(SpringRunner.class)
@NotThreadSafe
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/EmpUsers_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
@Slf4j
public class EmployeeUserCreationTest extends AbstractTestWithTempBaseDir {

	@Mock
	private UserController userController;

	@Autowired
	private TestRestTemplate template;

	@Autowired
	private AppConfig config;
	@Autowired
	private ObjectMapper mapper;
	@Autowired
	EmployeeUserService employeeUserService;
	@Autowired
	private RoleService roleService;
	
	@Autowired
	EmployeeUserRepository empRepository;
	@Autowired
	private UserTokenRepository tokenRepo;
	@Autowired
	private SecurityService securityService;


	@Before
	public void setup() {
		config.mailDryRun = true;
		MockMvcBuilders.standaloneSetup(userController).build();		
	}




	@Test
	public void createEmployeeUserSuccessTest() {
		String body =
				json()
				.put("name", "Ahmed")
				.put("email", TestUserEmail)
				.put("org_id", 99001)
				.put("role", "NASNAV_ADMIN")
				.toString();
        HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);

		assertEquals(200, response.getStatusCode().value());
		assertUserCreated(response.getBody());
	}
	
	
	
	@Test
	public void createEmployeeUserNoPrivelageTest() {
		String body =
				json()
				.put("name", "Ahmed")
				.put("email", TestUserEmail)
				.put("org_id", 99001)
				.put("role", "NASNAV_ADMIN")
				.toString();

		//this user have the role CUSTOMER in the test data, it can't create other users
        HttpEntity<Object> employeeUserJson = getHttpEntity(body, "yuhjhu");
		ResponseEntity<BaseResponse> response = template.postForEntity("/user/create", employeeUserJson, BaseResponse.class);

		assertEquals(FORBIDDEN, response.getStatusCode());
	}
	
	
	
	@Test
	public void createEmployeeUserByNonExistingUserTest() {
		String body =
				json()
				.put("name", "Ahmed")
				.put("email", TestUserEmail)
				.put("org_id", 99001)
				.put("role", "NASNAV_ADMIN")
				.toString();

		//this user have the role CUSTOMER in the test data, it can't create other users
        HttpEntity<Object> employeeUserJson = getHttpEntity(body, "InvalidToken");
		ResponseEntity<BaseResponse> response = template.postForEntity("/user/create", employeeUserJson, BaseResponse.class);

		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode().value());
	}
	
	

	@Test
	public void createEmployeeUserInvalidNameTest() {
		String body =
				json()
				.put("name", "Ahmed#&*")
				.put("email", TestUserEmail)
				.put("org_id", 99001)
				.put("role", "NASNAV_ADMIN")
				.toString();
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<String> response = template.postForEntity("/user/create", employeeUserJson, String.class);
		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
		assertTrue(response.getBody().contains("U$EMP$0003"));
	}



	@Test
	public void createEmployeeUserInvalidEmailTest() {
		// make an invalid employee user json without a name param
		String body = "{\"name\":\"Ahmed#&*\", \"email\":\"invalid_email\", \"org_id\": 99001, \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<String> response = template.postForEntity("/user/create", employeeUserJson, String.class);

		
		assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		assertTrue(response.getBody().contains("U$EMP$0003"));
	}



	@Test
	public void createEmployeeUserInvalidRoleTest() {
		// make an invalid employee user json without a name param
		String body = "{\"name\":\"Ahmed\", \"email\":\"" + TestUserEmail + "\", \"org_id\": 99001, \"store_id\": 100, \"role\": \"NASNAV_ADMIN, UNKNOWN_ROLE\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<String> response = template.postForEntity("/user/create", employeeUserJson, String.class);

		
		assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		assertTrue(response.getBody().contains("U$EMP$0007"));
	}



	@Test
	public void createEmployeeUserOrganizationNotExistsTest() {
		String body =
				json()
				.put("name", "Ahmed")
				.put("email", TestUserEmail)
				.put("org_id", -1)
				.put("store_id", 502)
				.put("role", "ORGANIZATION_ADMIN")
				.toString();
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<String> response = template.postForEntity("/user/create", employeeUserJson, String.class);
		
		assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		assertTrue(response.getBody().contains("U$EMP$0005"));
	}




	@Test
	public void createEmployeeUserStoreNotExistsTest() {
		String body =
				json()
				.put("name", "Ahmed")
				.put("email", TestUserEmail)
				.put("org_id", 99001)
				.put("store_id", -1)
				.put("role", "STORE_MANAGER")
				.toString();
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity(
				"/user/create", employeeUserJson, UserApiResponse.class);

		assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}



	@Test
	public void createEmployeeUserStoreNoIdTest() {
		String body =
				json()
				.put("name", "Ahmed")
				.put("email", TestUserEmail)
				.put("org_id", 99001)
				.put("role", "STORE_MANAGER")
				.toString();
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity(
				"/user/create", employeeUserJson, UserApiResponse.class);

		assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
	}



	@Test
	public void createEmployeeUserStoreEmployeeTest() {
		String body =
				json()
				.put("name", "Ahmed")
				.put("email", TestUserEmail)
				.put("org_id", 99001)
				.put("shop_id", 502)
				.put("role", "STORE_EMPLOYEE")
				.toString();
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "131415");
		ResponseEntity<UserApiResponse> response = template.postForEntity(
				"/user/create", employeeUserJson, UserApiResponse.class);

		assertEquals(OK, response.getStatusCode());
		assertUserCreated(response.getBody());
	}



	@Test
	public void createEmployeeUserStoreEmployeeForAnotherStoreTest() {
		String body =
				json()
				.put("name", "Ahmed")
				.put("email", TestUserEmail)
				.put("org_id", 99001)
				.put("store_id", 502)
				.put("role", "STORE_EMPLOYEE")
				.toString();
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "161718");
		ResponseEntity<UserApiResponse> response = template.postForEntity(
				"/user/create", employeeUserJson, UserApiResponse.class);

		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}



	@Test
	public void createEmployeeUserEmailExistsForSameOrgTest() {
		// create employee user with an email
		String body = "{\"name\":\"Ahmed\", \"email\":\"" + TestUserEmail + "\", \"org_id\": 99001, \"store_id\": 502, \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);

		response.getBody().getEntityId();
		// try to create another employee user with the same email
		ResponseEntity<String> anotherResponse = template.postForEntity("/user/create", employeeUserJson, String.class);

		
		assertEquals(NOT_ACCEPTABLE.value(), anotherResponse.getStatusCode().value());
		assertEquals(true, anotherResponse.getBody().contains("U$EMP$0006"));
	}



	@Test
	public void createEmployeeUserEmailExistsForDifferentOrgTest() {
		// create employee user with an email
		JSONObject bodyJson =
				json()
				.put("name", "Ahmed")
				.put("email", TestUserEmail)
				.put("org_id", 99001L)
				.put("store_id", 502)
				.put("role", "NASNAV_ADMIN");
		String body = bodyJson.toString();
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);

		response.getBody().getEntityId();
		// try to create another employee user with the same email but on another organization
		String bodyNextRequest =
				bodyJson
					.put("org_id", 99002)
					.put("store_id", 501)
					.toString();
		HttpEntity<Object> nextUserJson = getHttpEntity(bodyNextRequest, "abcdefg");
		ResponseEntity<String> anotherResponse = template.postForEntity("/user/create", nextUserJson, String.class);

		assertEquals(NOT_ACCEPTABLE.value(), anotherResponse.getStatusCode().value());
		assertEquals(true, anotherResponse.getBody().contains("U$EMP$0006"));
	}




	@Test
	public void createEmployeeUserEmailExistsDifferentCaseTest() {
		// create employee user with an email
		String body =
				json()
				.put("name", "Ahmed")
				.put("email", TestUserEmail.toUpperCase())
				.put("org_id", 99001L)
				.put("store_id", 502)
				.put("role", "NASNAV_ADMIN")
				.toString();
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);

		// try to create another employee user with the same email
		ResponseEntity<String> anotherResponse = template.postForEntity("/user/create", employeeUserJson, String.class);


		assertEquals(NOT_ACCEPTABLE.value(), anotherResponse.getStatusCode().value());
		assertTrue(anotherResponse.getBody().contains("U$EMP$0006"));
	}



	@Test
	public void createEmployeeUserStoreForDifferentOrgTest() {
		String body =
				json()
				.put("name", "Ahmed")
				.put("email", TestUserEmail)
				.put("org_id", 99001L)
				.put("store_id", 501)
				.put("role", "STORE_MANAGER")
				.toString();
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<String> response = template.postForEntity("/user/create", employeeUserJson, String.class);

		assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		assertEquals(true, response.getBody().contains("U$EMP$0012"));
	}




	@Test
	public void employeeUserLoginNeedsActivationTest() {
		// create employee user with an email
		String userBody = "{\"name\":\"Ahmed\", \"email\":\"" + TestUserEmail + "\", \"org_id\": 99001, \"store_id\": 502, \"role\": \"ORGANIZATION_EMPLOYEE\"}";
		String loginBody = "{\"email\":\"" + TestUserEmail + "\", \"password\": \"" + EntityConstants.INITIAL_PASSWORD + "\", \"org_id\": 99001, \"employee\": true}";
		//create a new employee user
		HttpEntity<Object> employeeUserJson = getHttpEntity(userBody, "abcdefg");
		HttpEntity<Object> employeeUserLoginJson = getHttpEntity(loginBody, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);
		response.getBody().getEntityId();

		// try to login with this user email before activation
		ResponseEntity<String> loginResponse = template.postForEntity(
				"/user/login", employeeUserLoginJson, String.class);

		assertEquals(HttpStatus.LOCKED.value(), loginResponse.getStatusCode().value());
		assertEquals(true, loginResponse.getBody().contains("U$LOG$0003"));
	}





	@Test
	public void createEmployeeUserAuthTestDifferentRoleSuccess() {
		// organization_admin role test success
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + "Adminuser@nasnav.com" + "\", \"org_id\": 99001" + ", \"store_id\": 502, \"role\": \"ORGANIZATION_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);
		response.getBody().getEntityId();

		assertEquals(200, response.getStatusCode().value());
	}



	@Test
	public void createEmployeeUserAuthTestDifferentOrgFail() {
		// organization_admin role test fail (different organization)
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + "Adminuser@nasnav.com" + "\", \"org_id\": 99002" + ", \"store_id\": 10, \"role\": \"ORGANIZATION_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm");
		ResponseEntity<BaseResponse> response = template.postForEntity("/user/create", employeeUserJson, BaseResponse.class);
		
		assertEquals(406, response.getStatusCode().value());
	}



	@Test
	public void createEmployeeUserAuthTestFail() {
		// organization_admin role test fail (same organization but assigning NASNAV_ADMIN role without authority)
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + "Adminuser@nasnav.com" + "\", \"org_id\": 99001" + ", \"store_id\": 10, \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);
		
		assertEquals(406, response.getStatusCode().value());
	}



	@Test
	public void createEmployeeUserNoOrgIdTest() {
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + "Adminuser5@nasnav.com" + "\" , \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<String> response = template.postForEntity("/user/create", employeeUserJson, String.class);

		assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		assertEquals(true, response.getBody().contains("U$EMP$0005"));
	}




	@Test
	public void createStoreManagerByOrganizationManagerFail() {
		// NASNAV_ADMIN role test
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + "Adminuser@nasnav.com" + "\", \"org_id\": 99001" + ", \"store_id\": 502, \"role\": \"STORE_MANAGER\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "123");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);
		response.getBody().getEntityId();

		assertEquals(403, response.getStatusCode().value());
	}




	@Test
	public void createStoreManagerByOrganizationAdminSuccess() {
		Long anotherStoreThanAdminStore = 503L;
		String body =
				json()
				.put("name", "Ahmed")
				.put("email", "Adminuser@nasnav.com")
				.put("org_id", 99001)
				.put("shop_id", anotherStoreThanAdminStore)
				.put("role", "STORE_MANAGER")
				.toString();
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);
		response.getBody().getEntityId();

		assertEquals(200, response.getStatusCode().value());
	}



	@Test
	public void updateSelfEmployeeUserAuthTestSuccess() {
		// update self data test success
		String body = json()
				.put("employee",true)
				.put("name","Ahmad")
				.put("email","ahmad.user@nasnav.com")
				.put("gender", Gender.MALE)
				.toString();
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		assertEquals(200, response.getStatusCode().value());
		EmployeeUserEntity user = empRepository.findById(68L).get();
		assertEquals("Ahmad", user.getName());
		assertEquals("ahmad.user@nasnav.com", user.getEmail());
		assertEquals(Gender.MALE, user.getGender());
	}
	@Test
	public void updateSelfEmployeeUserWithAvatarSuccess() {
		// update self data test success
		String body = json()
				.put("employee",true)
				.put("name","Ahmad")
				.put("email","ahmad.user@nasnav.com")
				.put("avatar","test.png")
				.toString();
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);
		assertEquals(200, response.getStatusCode().value());
		EmployeeUserEntity user = empRepository.findById(68L).get();
		assertEquals("Ahmad", user.getName());
		assertEquals("test.png", user.getImage());
		assertEquals("ahmad.user@nasnav.com", user.getEmail());
	}

	@Test
	public void updateOtherEmployeeUserAuthByNasNavAdminTestSuccess() {
		// update user with id 158 success
		String body = json()
				.put("employee",true)
				.put("updated_user_id","158")
				.put("name","hussien")
				.put("email","hussien.Test@nasnav.com")
				.put("role","ORGANIZATION_MANAGER")
				.toString();
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);
		response.getBody().getEntityId();

		assertEquals(200, response.getStatusCode().value());
		EmployeeUserEntity user = empRepository.findById(158L).get();
		assertEquals("hussien", user.getName());
		assertEquals("hussien.Test@nasnav.com", user.getEmail());
	}




	@Test
	public void updateOtherEmployeeUserAuthTestFail() {
		// update user with id 158 fail(unauthorized assigning NASNAV_ADMIN role)
		String body = createUserUpdateJson("NASNAV_ADMIN");
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm");
		ResponseEntity<BaseResponse> response = template.postForEntity("/user/update", employeeUserJson, BaseResponse.class);
		
		assertEquals(406, response.getStatusCode().value());
	}



	@Test
	public void updateOtherEmployeeUserInvaildDataTestFail() {
		// update user with id 158 fail(invalid name and email)
		String body = json()
				.put("employee",true)
				.put("updated_user_id","158")
				.put("name","74man")
				.put("email","boda  Test@nasnav.com")
				.toString();
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);
		response.getBody().getEntityId();
		
		assertEquals(406, response.getStatusCode().value());
	}

	//NASNAV_ADMIN,ORGANIZATION_ADMIN,ORGANIZATION_EMPLOYEE,STORE_EMPLOYEE

	//NASNAV_ADMIN changing roles to NASNAV_ADMIN,ORGANIZATION_ADMIN,ORGANIZATION_EMPLOYEE,STORE_EMPLOYEE
	@ParameterizedTest
	@MethodSource("provideTestData")
	public void updateRoleByNasnavAdminTest(String roles, Set<String> expectedEmpRoles) {
		String body = createUserUpdateJson(roles);
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		assertEquals(200, response.getStatusCode().value());
		Set<String> newEmpRoles = new HashSet<>(roleService.getRolesNamesOfEmployeeUser(158L));
		assertTrue(expectedEmpRoles.equals(newEmpRoles));
	}

	private static Stream<Arguments> provideTestData() {
		return Stream.of(
				Arguments.of("NASNAV_ADMIN", new HashSet<>(asList("NASNAV_ADMIN"))),
				Arguments.of("ORGANIZATION_EMPLOYEE,STORE_MANAGER", new HashSet<>(asList("ORGANIZATION_EMPLOYEE", "STORE_MANAGER"))),
				Arguments.of("ORGANIZATION_EMPLOYEE", new HashSet<>(asList("ORGANIZATION_EMPLOYEE"))),
				Arguments.of("STORE_EMPLOYEE", new HashSet<>(asList("STORE_EMPLOYEE")))
		);
	}
	//finish NASNAV_ADMIN role test

	//ORGANIZATION_ADMIN changing roles to NASNAV_ADMIN,ORGANIZATION_ADMIN,ORGANIZATION_EMPLOYEE,STORE_EMPLOYEE
	@Test
	public void updateToNasnavAdminByOrganizationAdminTest() {
		String body = createUserUpdateJson("NASNAV_ADMIN");
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm");
		ResponseEntity<BaseResponse> response = template.postForEntity("/user/update", employeeUserJson, BaseResponse.class);
		
		assertEquals(406, response.getStatusCode().value());
	}

	@Test
	public void updateToOrganizationAdminByOrganizationAdminTest() {
		String body = createUserUpdateJson("ORGANIZATION_ADMIN");
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm");
		ResponseEntity<String> response = template.postForEntity("/user/update", employeeUserJson, String.class);

		assertEquals(200, response.getStatusCode().value());

		Set<String> expectedEmpRoles = new HashSet<>(asList("ORGANIZATION_ADMIN"));
		Set<String> newEmpRoles = new HashSet<>(roleService.getRolesNamesOfEmployeeUser(158L));
		assertTrue(expectedEmpRoles.equals(newEmpRoles));
	}

	@Test
	public void updateToOrganizationEmployeeByOrganizationAdminTest() {
		String body = createUserUpdateJson("ORGANIZATION_EMPLOYEE");
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm");
		ResponseEntity<String> response = template.postForEntity("/user/update", employeeUserJson, String.class);

		assertEquals(200, response.getStatusCode().value());

		Set<String> expectedEmpRoles = new HashSet<>(asList("ORGANIZATION_EMPLOYEE"));
		Set<String> newEmpRoles = new HashSet<>(roleService.getRolesNamesOfEmployeeUser(158L));
		assertTrue(expectedEmpRoles.equals(newEmpRoles));
	}

	@Test
	public void updateToStoreEmployeeByOrganizationAdminTest() {
		String body = createUserUpdateJson("STORE_EMPLOYEE");
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm");
		ResponseEntity<BaseResponse> response = template.postForEntity("/user/update", employeeUserJson, BaseResponse.class);

		
		assertEquals(200, response.getStatusCode().value());

		Set<String> expectedEmpRoles = new HashSet<>(asList("STORE_EMPLOYEE"));
		Set<String> newEmpRoles = new HashSet<>(roleService.getRolesNamesOfEmployeeUser(158L));
		assertTrue(expectedEmpRoles.equals(newEmpRoles));
	}


	@Test
	public void updateToStoreManagerByOrganizationAdminTest() {
		String body = createUserUpdateJson("STORE_MANAGER");
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm");
		ResponseEntity<BaseResponse> response = template.postForEntity("/user/update", employeeUserJson, BaseResponse.class);


		assertEquals(200, response.getStatusCode().value());

		Set<String> expectedEmpRoles = new HashSet<>(asList("STORE_MANAGER"));
		Set<String> newEmpRoles = new HashSet<>(roleService.getRolesNamesOfEmployeeUser(158L));
		assertTrue(expectedEmpRoles.equals(newEmpRoles));
	}
	//finish ORGANIZATION_ADMIN role test

	//ORGANIZATION_EMPLOYEE changing roles to NASNAV_ADMIN,ORGANIZATION_ADMIN,ORGANIZATION_EMPLOYEE,STORE_EMPLOYEE
	@ParameterizedTest
	@MethodSource("provideTestDataForOrganizationEmployee")
	public void updateRoleByOrganizationEmployeeTest(String roles) {
		String body = createUserUpdateJson(roles);
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "123");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		assertEquals(406, response.getStatusCode().value());
	}

	private static Stream<Arguments> provideTestDataForOrganizationEmployee() {
		return Stream.of(
				Arguments.of("NASNAV_ADMIN"),
				Arguments.of("ORGANIZATION_ADMIN"),
				Arguments.of("ORGANIZATION_EMPLOYEE"),
				Arguments.of("STORE_EMPLOYEE")
		);
	}
	//finish ORGANIZATION_EMPLOYEE role test


	//STORE_EMPLOYEE changing roles to NASNAV_ADMIN,ORGANIZATION_ADMIN,ORGANIZATION_EMPLOYEE,STORE_EMPLOYEE
	@ParameterizedTest
	@MethodSource("provideTestDataForStoreEmployee")
	public void updateRoleByStoreEmployeeTest(String roles) {
		String body = createUserUpdateJson(roles);
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "111444");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		assertEquals(406, response.getStatusCode().value());
	}

	private static Stream<Arguments> provideTestDataForStoreEmployee() {
		return Stream.of(
				Arguments.of("NASNAV_ADMIN"),
				Arguments.of("ORGANIZATION_ADMIN"),
				Arguments.of("ORGANIZATION_EMPLOYEE"),
				Arguments.of("STORE_EMPLOYEE")
		);
	}
	
	private String createUserUpdateJson(String roles) {
		return json()
				.put("employee",true)
				.put("updated_user_id","158")
				.put("role",roles)
				.toString();
	}
	
	
	//finish STORE_EMPLOYEE role test

	@Test
	public void getUserOwnData() {
		HttpEntity<Object> header = getHttpEntity("yuhjhu");
		ResponseEntity<UserRepresentationObject> response = 
				template.exchange("/user/info", GET, header, UserRepresentationObject.class);
		log.debug("{}", response);
		
		assertEquals( 200, response.getStatusCodeValue());
		UserRepresentationObject user = response.getBody();
		assertEquals( 88L, user.getId().longValue());
		assertNotNull(user.getRoles());
		assertFalse(user.getRoles().isEmpty());
	}

	
	
	
	
	@Test
	public void getUserDataDifferentUsers() {
		// logged user is NASNAV_ADMIN so he can view all other users data
		HttpEntity<Object> header = getHttpEntity("abcdefg");
		ResponseEntity<UserRepresentationObject> response = template.exchange("/user/info?id=88", GET,
				header, UserRepresentationObject.class);
		log.debug("{}", response);
		assertEquals(200, response.getStatusCodeValue());

		//-------------------------------------------------------------------
		// logged user is ORGANIZATION_ADMIN ,so, he will get  his own data
		header = getHttpEntity("hijkllm");
		response = template.exchange("/user/info?id=88", GET,
									header, UserRepresentationObject.class);
		
		log.debug("{}", response);
		assertEquals( 200, response.getStatusCodeValue());
		UserRepresentationObject user = response.getBody();
		assertEquals( 88, response.getBody().getId().longValue());
		assertNotNull(user.getRoles());
		assertFalse(user.getRoles().isEmpty());
		assertNotNull(user.getLastLogin());
		assertEquals(Gender.MALE, user.getGender());
	}
	
	
	
	

	@Test
	public void getNonExistUserData() {
		HttpEntity<Object> header = getHttpEntity("abcdefg");
		ResponseEntity<UserRepresentationObject> response = template.exchange("/user/info?id=526523", GET,
				header, UserRepresentationObject.class);
		log.debug("{}", response);
		assertEquals( 406, response.getStatusCodeValue());
	}
	
	@SneakyThrows
	private List<UserRepresentationObject> parseResponse(String response) {
		return mapper.readValue(response, new TypeReference<List<UserRepresentationObject>>(){});
	}

	private void assertSizeCorrect(int expectedSize, ResponseEntity<String> response) {
		assertEquals(response.getStatusCodeValue(), 200);
		List<UserRepresentationObject> parsedResponse = parseResponse(response.getBody());
		assertEquals(expectedSize, parsedResponse.size());
	}

	// with MEETUSVR_ADIMN ACCOUNT
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/EmpUsers_Test_Data_Insert_3.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void listEmpUsersDifferentFilters() {
		HttpEntity<Object> header = getHttpEntity( "abcdefg");

		// no filter
		ResponseEntity<String> response = template.exchange("/user/list", GET,header, String.class);
		assertSizeCorrect(17,response);

		// org_id filter
		response = template.exchange("/user/list?org_id=99001", GET,header, String.class);
		assertEquals(response.getStatusCodeValue(), 200);
		assertSizeCorrect(10,response);

		response = template.exchange("/user/list?org_id=99002", GET,header, String.class);
		assertSizeCorrect(7,response);
		// store_id filter
		response = template.exchange("/user/list?shop_id=501", GET,header, String.class);
		assertSizeCorrect(7,response);

		response = template.exchange("/user/list?shop_id=502", GET,header, String.class);
		assertSizeCorrect(10,response);

		// role filter
		response = template.exchange("/user/list?role=NASNAV_ADMIN", GET,header, String.class);
		assertSizeCorrect(2,response);


		ResponseEntity<String> failResponse = template.exchange("/user/list?role=invalid_role", GET,header, String.class);
		assertEquals(failResponse.getStatusCode(), NOT_ACCEPTABLE);

		response = template.exchange("/user/list?role=STORE_MANAGER", GET,header, String.class);
		assertSizeCorrect(4,response);


		// role and org_id filter
		response = template.exchange("/user/list?role=STORE_MANAGER&org_id=99001", GET,header, String.class);
		assertSizeCorrect(2,response);


		// role and store_id filter
		response = template.exchange("/user/list?role=STORE_MANAGER&shop_id=501", GET,header, String.class);
		assertSizeCorrect(2,response);


		// org_id and store_id filter
		response = template.exchange("/user/list?org_id=99001&shop_id=502", GET,header, String.class);
		assertSizeCorrect(10,response);


		// org_id and store_id and role filter
		response = template.exchange("/user/list?org_id=99001&shop_id=502&role=STORE_MANAGER", GET,header, String.class);
		assertSizeCorrect(2,response);


		response = template.exchange("/user/list?org_id=99002&shop_id=501&role=STORE_MANAGER", GET,header, String.class);
		assertSizeCorrect(2,response);

	}




	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/EmpUsers_Test_Data_Insert_3.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void listEmpUsersDifferentPrelivages() {
		// ORGANIZATION_ADMIN account with org_id = 99001
		HttpEntity<Object> header = getHttpEntity("hijkllm");
		ResponseEntity<List> response = template.exchange("/user/list", GET, header, List.class);
		//returning EmpUsers within the same organization only
		log.debug("{}", response.getBody());
		assertEquals(response.getStatusCodeValue(), 200);
		assertEquals(6, response.getBody().size());

		// trying to filter with different org_id
		response = template.exchange("/user/list?org_id=99002", GET, header, List.class);
		//returning EmpUsers within the same organization only
		log.debug("{}", response.getBody());
		assertEquals(response.getStatusCodeValue(), 200);
		assertEquals(response.getBody().size(), 6);

		// trying to filter with store_id not exits in the organization
		response = template.exchange("/user/list?shop_id=501", GET, header, List.class);
		//returning EmpUsers within the same organization only
		log.debug("{}", response.getBody());
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(0, response.getBody().size());

		// ORGANIZATION_MANAGER account
		header = getHttpEntity("123");
		response = template.exchange("/user/list", GET, header, List.class);
		//returning EmpUsers within the same organization and roles below ORGANIZATION_MANAGER
		log.debug("{}", response.getBody());
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(5, response.getBody().size());

		// ORGANIZATION_EMPLOYEE account
		header = getHttpEntity("hhhkkk");
		response = template.exchange("/user/list", GET, header, List.class);
		//returning EmpUsers within the same organization and roles below ORGANIZATION_EMPLOYEE
		log.debug("{}", response.getBody());
		assertEquals(response.getStatusCodeValue(), 200);
		assertEquals(1, response.getBody().size());

		// trying to filter with NASNAV_ADMIN role
		ResponseEntity<String> res = template.exchange("/user/list?role=NASNAV_ADMIN", GET, header, String.class);
		assertEquals(res.getStatusCodeValue(), 406);
		assertTrue(res.getBody().contains("U$EMP$0013"));
	}
	
	
	
	
	
	@Test
	public void testLoginByEmailUsedByCustomerAndEmployee() {
		//try to get new password to use it for login
		String email = "user1@nasnav.com";
		String password = "12345678"; 
		
		String request = new JSONObject()
								.put("password", password)
								.put("email", email)
								.put("org_id", 99001L)
								.put("employee", true)
								.toString();
		
		// login using the new password
		HttpEntity<Object> userJson = getHttpEntity(request, "DOESNOT-NEED-TOKEN");
		ResponseEntity<UserApiResponse> response = 
				template.postForEntity("/user/login", userJson,	UserApiResponse.class);
		
		assertEquals(200, response.getStatusCode().value());
		String token = response.getBody().getToken();
		boolean employeeUserLoggedIn = empRepository.existsByAuthenticationToken( token);
		assertTrue("the logged in user should be the employee user, "
				+ "and its token should exists in EMPLOYEE_USER table", employeeUserLoggedIn );
	}

	@Test
	public void listUserNotificationTokens() {
		final String notificationToken = "SomeNotificationToken";
		String email = "user1@nasnav.com";
		String password = "12345678"; 
		
		String request = new JSONObject()
								.put("password", password)
								.put("email", email)
								.put("org_id", 99001L)
								.put("employee", true)
								.put("notification_token", notificationToken)
								.toString();
		HttpEntity<Object> userJson = getHttpEntity(request, "DOESNOT-NEED-TOKEN");
		ResponseEntity<Void> response = 
				template.postForEntity("/user/login", userJson,	Void.class);
		assertEquals(OK, response.getStatusCode());

		EmployeeUserEntity user = empRepository.findByEmailIgnoreCaseAndOrganizationId("user1@nasnav.com", 99001L).orElse(null);
		Set<String> notificationTokens = securityService.getValidNotificationTokens(user);
		Set<String> notificationTokensByUsers = securityService.getValidNotificationTokensForOrgEmployees(99001L);

		Set<String> expectedTokens = Set.of(notificationToken);
		assertEquals(expectedTokens, notificationTokens);
		assertEquals(expectedTokens, notificationTokensByUsers);
	}

	@Test
	public void testEmployeeLoginWithoutOrgId() {
		String request = new JSONObject()
				.put("password", "12345678")
				.put("email", "user1@nasnav.com")
				.put("employee", true)
				.toString();

		HttpEntity<Object> userJson = getHttpEntity(request, "DOESNOT-NEED-TOKEN");
		ResponseEntity<UserApiResponse> response =
				template.postForEntity("/user/login", userJson,	UserApiResponse.class);

		assertEquals(200, response.getStatusCode().value());
		String token = response.getBody().getToken();
		boolean employeeUserLoggedIn = empRepository.existsByAuthenticationToken( token);
		assertTrue("the logged in user should be the employee user, "
				+ "and its token should exists in EMPLOYEE_USER table", employeeUserLoggedIn );
	}



	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/EmpUsers_Test_Data_Insert.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void createOtherEmployeeUserWithMultipleHigherLevelFail() {
		//admin has multiple roles including organization manager, but none of them can create organization employee
		String body =
				json()
				.put("name", "Ahmed")
				.put("email", "ahmed.mail@mail.com")
				.put("org_id", 99001)
				.put("store_id", 502)
				.put("role", "ORGANIZATION_EMPLOYEE,STORE_EMPLOYEE")
				.toString();

		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "161718");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);
		response.getBody().getEntityId();

		assertEquals(406, response.getStatusCode().value());
	}



	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/EmpUsers_Test_Data_Insert.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void updateOtherEmployeeUserIntoMultipleHigherLevelFail() {
		//admin has multiple roles including organization manager, but none of them can create organization employee
		String body =
				json()
					.put("updated_user_id", 81)
					.put("employee", true)
					.put("role", "ORGANIZATION_ADMIN,STORE_EMPLOYEE")
					.toString();

		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "161718");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);
		response.getBody().getEntityId();

		assertEquals(406, response.getStatusCode().value());
	}



	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/EmpUsers_Test_Data_Insert.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void updateOtherEmployeeUserAlreadyWithMultipleHigherLevelFail() {
		//admin has multiple roles including organization manager, but none of them can create organization employee
		String body =
				json()
						.put("updated_user_id", 80)
						.put("employee", true)
						.put("role", "STORE_EMPLOYEE")
						.put("org_id", 99001)
						.toString();

		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "161718");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);
		response.getBody().getEntityId();

		assertEquals(406, response.getStatusCode().value());
	}




	@Test
	public void updateOtherEmployeeUserChangeOrgByOrgAdminFail() {
		//admin tries to change aen employee organization
		Long id = 74L;
		Long orgBefore = empRepository.findById(id).get().getOrganizationId();
		String body =
				json()
					.put("updated_user_id", id)
					.put("employee", true)
					.put("role", "STORE_EMPLOYEE")
					.put("org_id", 99002)
					.toString();

		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);
		response.getBody().getEntityId();

		assertEquals(200, response.getStatusCode().value());
		Long orgAfter = empRepository.findById(id).get().getOrganizationId();
		assertEquals("organization parameter is used only by NASNAV_ADMIN users"
				, orgBefore, orgAfter);
 	}


	@Test
	public void updateOtherEmployeeUserChangeStoreOfOtherOrgByOrgAdminFail() {
		//admin tries to change aen employee into other org store
		Long id = 74L;
		Long shopBefore = empRepository.findById(id).get().getShopId();
		String body =
				json()
					.put("updated_user_id", id)
					.put("employee", true)
					.put("shop_id", 501)
					.toString();

		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);
		response.getBody().getEntityId();

		assertEquals(406, response.getStatusCode().value());
		Long shopAfter = empRepository.findById(id).get().getShopId();
		assertEquals( shopBefore, shopAfter);
	}



	@Test
	public void updateOtherEmployeeUserChangeStoreByStoreManagerFail() {
		//store manager tries to change aen employee into other org store
		Long id = 74L;
		Long shopBefore = empRepository.findById(id).get().getShopId();
		String body =
				json()
				.put("updated_user_id", id)
				.put("employee", true)
				.put("store_id", 503)
				.toString();

		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "131415");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);
		response.getBody().getEntityId();

		assertEquals(200, response.getStatusCode().value());
		Long shopAfter = empRepository.findById(id).get().getShopId();
		assertEquals( shopBefore, shopAfter);
	}




	@Test
	public void updateOtherEmployeeUserChangeStoreByOrganizationAdminSuccess() {
		//organziation admin tries to change an employee into other org store
		Long id = 74L;
		Long newShop = 503L;
		Long shopBefore = empRepository.findById(id).get().getShopId();
		String body =
				json()
						.put("updated_user_id", id)
						.put("employee", true)
						.put("shop_id", newShop)
						.toString();

		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);
		response.getBody().getEntityId();

		assertEquals(200, response.getStatusCode().value());
		Long shopAfter = empRepository.findById(id).get().getShopId();
		assertNotEquals( shopBefore, shopAfter);
		assertEquals( newShop, shopAfter);
	}


	@Test
	public void recoverEmployeeUserTest() {
		ResponseEntity<String> response =
				template.getForEntity("/user/recover?email=testuser1@nasnav.com&employee=true&org_id=99001", String.class);

		assertEquals(200, response.getStatusCode().value());
		EmployeeUserEntity emp = empRepository.findById(68L).get();
		assertNotNull( emp.getResetPasswordToken());
	}

	@Test
	public void testRecoverEmployeeRemovesOldTokens() {

		Long oldTokensCount = tokenRepo.countByEmployeeUserEntity_Id(159L);
		assertTrue(oldTokensCount.intValue() == 2);
		String request = new JSONObject()
				.put("password", "12345678")
				.put("token", "d67438ac-f3a5-4939-9686-a1fc096f3f4f")
				.put("employee", true)
				.put("org_id", 99001)
				.toString();

		HttpEntity<Object> userJson = getHttpEntity(request, "DOESNOT-NEED-TOKEN");
		ResponseEntity<UserApiResponse> response =
				template.postForEntity("/user/recover", userJson,	UserApiResponse.class);

		Assert.assertEquals(200, response.getStatusCode().value());
		String token = response.getBody().getToken();
		boolean employeeUserLoggedIn = tokenRepo.existsByToken(token);
		assertTrue("the recovered user should be logged in ", employeeUserLoggedIn );
		Long newTokensCount = tokenRepo.countByEmployeeUserEntity_Id(159L);
		assertTrue(newTokensCount.intValue() == 1);
	}


	@Test
	public void suspendEmpAccountByNasnavAdmin() {
		assertEquals(201, employeeUserService.getUserById(69L).getUserStatus().intValue());
		HttpEntity request = getHttpEntity("", "abcdefg");
		String params = "user_id=69&is_employee=true&suspend=true";
		ResponseEntity<String> response = template.postForEntity("/user/suspend?"+params, request, String.class);
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(202, employeeUserService.getUserById(69L).getUserStatus().intValue());

		params = "user_id=69&is_employee=true&suspend=false";
		response = template.postForEntity("/user/suspend?"+params, request, String.class);
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(201, employeeUserService.getUserById(69L).getUserStatus().intValue());
	}

	@Test
	public void suspendEmpAccountByOrgAdmin() {
		HttpEntity request = getHttpEntity("", "hijkllm");
		String params = "user_id=70&is_employee=true&suspend=true";
		ResponseEntity<String> response = template.postForEntity("/user/suspend?"+params, request, String.class);
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(202, employeeUserService.getUserById(70L).getUserStatus().intValue());
	}

	@Test
	public void suspendEmpAccountInvalidStatus() {
		HttpEntity request = getHttpEntity("", "hijkllm");
		String params = "user_id=71&is_employee=true&suspend=true";
		ResponseEntity<String> response = template.postForEntity("/user/suspend?"+params, request, String.class);
		assertEquals(406, response.getStatusCodeValue());
		assertEquals(0, employeeUserService.getUserById(71L).getUserStatus().intValue());
	}

	@Test
	public void suspendSelfAccount() {
		HttpEntity request = getHttpEntity("", "hijkllm");
		String params = "user_id=69&is_employee=true&suspend=true";
		ResponseEntity<String> response = template.postForEntity("/user/suspend?"+params, request, String.class);
		assertEquals(406, response.getStatusCodeValue());
	}

	@Test
	public void suspendNasnavAdminByOrgAdmin() {
		HttpEntity request = getHttpEntity("", "hijkllm");
		String params = "user_id=68&is_employee=true&suspend=true";
		ResponseEntity<String> response = template.postForEntity("/user/suspend?"+params, request, String.class);
		assertEquals(406, response.getStatusCodeValue());
	}

	@Test
	public void suspendEmpAccountCustomer() {
		HttpEntity request = getHttpEntity("", "yuhjhu");
		String params = "user_id=69&is_employee=true&suspend=true";
		ResponseEntity<String> response = template.postForEntity("/user/suspend?"+params, request, String.class);
		assertEquals(403, response.getStatusCodeValue());
	}

	@Test
	public void suspendEmpAccountNotActivatedAccount() {
		HttpEntity request = getHttpEntity("", "abcdefg");
		String params = "user_id=82&is_employee=true&suspend=true";
		ResponseEntity<String> response = template.postForEntity("/user/suspend?"+params, request, String.class);
		assertEquals(406, response.getStatusCodeValue());
		assertEquals(200, employeeUserService.getUserById(82L).getUserStatus().intValue());
	}

	@Test
	public void unsuspendEmpAccountNotActivatedAccount() {
		HttpEntity request = getHttpEntity("", "abcdefg");
		String params = "user_id=82&is_employee=true&suspend=false";
		ResponseEntity<String> response = template.postForEntity("/user/suspend?"+params, request, String.class);
		assertEquals(406, response.getStatusCodeValue());
		assertEquals(200, employeeUserService.getUserById(82L).getUserStatus().intValue());
	}


	private void assertUserCreated(UserApiResponse apiResponse) {
		assertNotNull(apiResponse);
		boolean exists = empRepository.existsById(apiResponse.getEntityId());
		assertTrue(exists);
	}

}
