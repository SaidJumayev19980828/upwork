package com.nasnav.test;
import static com.nasnav.test.commons.TestCommons.*;
import static org.junit.Assert.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.util.List;
import java.util.Optional;

import com.nasnav.dao.AddressRepository;
import com.nasnav.persistence.AddressesEntity;
import org.json.JSONObject;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import com.nasnav.AppConfig;
import com.nasnav.NavBox;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.controller.UserController;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.response.BaseResponse;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.EmployeeUserService;
import com.nasnav.test.commons.TestCommons;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@NotThreadSafe
@PropertySource("classpath:test.database.properties")
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/EmpUsers_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class EmployeeUserCreationTest {

	@Mock
	private UserController userController;

	@Autowired
	private TestRestTemplate template;

	@Autowired
	private AppConfig config;

	@Autowired
	EmployeeUserService employeeUserService;
	
	@Autowired
	EmployeeUserRepository empRepository;

	@Autowired
	private AddressRepository addressRepo;

	@Value("classpath:sql/EmpUsers_Test_Data_Insert.sql")
	private Resource userDataInsert;

	@Value("classpath:sql/database_cleanup.sql")
	private Resource databaseCleanup;

	@Before
	public void setup() {
		config.mailDryRun = true;
		MockMvcBuilders.standaloneSetup(userController).build();		
	}




	@Test
	public void createEmployeeUserSuccessTest() {
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + TestUserEmail + "\", \"org_id\": 99001, \"store_id\": 502, \"role\": \"NASNAV_ADMIN\"}";
        HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);

		response.getBody().getEntityId();

		Assert.assertEquals(200, response.getStatusCode().value());
	}
	
	
	
	@Test
	public void createEmployeeUserNoPrivelageTest() {
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + TestUserEmail + "\", \"org_id\": 99001, \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";

		//this user have the role CUSTOMER in the test data, it can't create other users
        HttpEntity<Object> employeeUserJson = getHttpEntity(body, "yuhjhu");
		ResponseEntity<BaseResponse> response = template.postForEntity("/user/create", employeeUserJson, BaseResponse.class);


		
		Assert.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
		
	}
	
	
	
	@Test
	public void createEmployeeUserByNonExistingUserTest() {
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + TestUserEmail + "\", \"org_id\": 99001, \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";

		//this user have the role CUSTOMER in the test data, it can't create other users
        HttpEntity<Object> employeeUserJson = getHttpEntity(body, "InvalidToken");
		ResponseEntity<BaseResponse> response = template.postForEntity("/user/create", employeeUserJson, BaseResponse.class);


		
		Assert.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode().value());
		
	}
	
	

	@Test
	public void createEmployeeUserInvalidNameTest() {
		// make an invalid employee user json without a name param
		String body = "{\"name\":\"Ahmed#&*\", \"email\":\"" + TestUserEmail + "\", \"org_id\": 99001, \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);

		
		
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_NAME));
		//delete created organization
	}

	@Test
	public void createEmployeeUserInvalidEmailTest() {
		// make an invalid employee user json without a name param
		String body = "{\"name\":\"Ahmed#&*\", \"email\":\"invalid_email\", \"org_id\": 99001, \"store_id\": 100, \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);

		
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_EMAIL));
	}

	@Test
	public void createEmployeeUserInvalidRoleTest() {
		// make an invalid employee user json without a name param
		String body = "{\"name\":\"Ahmed\", \"email\":\"" + TestUserEmail + "\", \"org_id\": 99001, \"store_id\": 100, \"role\": \"NASNAV_ADMIN, UNKNOWN_ROLE\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);

		
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_ROLE));
	}

	@Test
	public void createEmployeeUserOrganizationNoIdTest() {
		HttpEntity<Object> employeeUserJson = getHttpEntity(
                "{\"name\":\"Ahmed\",\"email\":\"" + TestUserEmail + "\", \"org_id\": 0, \"store_id\": 100, \"role\": \"ORGANIZATION_ADMIN\"}", "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);
		
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_ORGANIZATION));
	}

	// @Test - Unclear whether store_id can be 0
	public void createEmployeeUserStoreNoIdTest() {
		HttpEntity<Object> employeeUserJson = getHttpEntity(
				"{\"name\":\"Ahmed\",\"email\":\"" + TestUserEmail + "\", \"org_id\": 99001, \"store_id\": 0, \"role\": \"STORE_ADMIN\"}", "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity(
				"/user/create", employeeUserJson, UserApiResponse.class);

		
		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_STORE));
	}

	@Test
	public void createEmployeeUserEmailExistsTest() {
		// create employee user with an email
		String body = "{\"name\":\"Ahmed\", \"email\":\"" + TestUserEmail + "\", \"org_id\": 99001, \"store_id\": 502, \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);

		response.getBody().getEntityId();
		// try to create another employee user with the same email
		response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);

		
		Assert.assertEquals(NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.EMAIL_EXISTS));
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
		ResponseEntity<UserApiResponse> loginResponse = template.postForEntity(
				"/user/login", employeeUserLoginJson, UserApiResponse.class);

		Assert.assertEquals(HttpStatus.LOCKED.value(), loginResponse.getStatusCode().value());
		Assert.assertEquals(true, loginResponse.getBody().getResponseStatuses().contains(ResponseStatus.NEED_ACTIVATION));
	}

	@Test
	public void createEmployeeUserCreationSuccess() {
		// nasnav_admin role test
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + "Adminuser@nasnav.com" + "\", \"org_id\": 99001" + ", \"store_id\": 502, \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);
		response.getBody().getEntityId();

		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void createEmployeeUserAuthTestDifferentRoleSuccess() {
		// organization_admin role test success
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + "Adminuser@nasnav.com" + "\", \"org_id\": 99001" + ", \"store_id\": 502, \"role\": \"ORGANIZATION_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);
		response.getBody().getEntityId();

		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void createEmployeeUserAuthTestDifferentOrgFail() {
		// organization_admin role test fail (different organization)
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + "Adminuser@nasnav.com" + "\", \"org_id\": 99002" + ", \"store_id\": 10, \"role\": \"ORGANIZATION_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm");
		ResponseEntity<BaseResponse> response = template.postForEntity("/user/create", employeeUserJson, BaseResponse.class);
		
		Assert.assertEquals(406, response.getStatusCode().value());
	}

	@Test
	public void createEmployeeUserAuthTestFail() {
		// organization_admin role test fail (same organization but assigning NASNAV_ADMIN role without authority)
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + "Adminuser@nasnav.com" + "\", \"org_id\": 99001" + ", \"store_id\": 10, \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);
		
		Assert.assertEquals(401, response.getStatusCode().value());
	}

	@Test
	public void createEmployeeUserNoOrgIdTest() {
		String body = "{\"name\":\"Ahmed\",\"email\":\"" + "Adminuser5@nasnav.com" + "\" , \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/create", employeeUserJson, UserApiResponse.class);
		response.getBody().getEntityId();

		Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode().value());
		Assert.assertEquals(true, response.getBody().getResponseStatuses().contains(ResponseStatus.INVALID_ORGANIZATION));
	}

	@Test
	public void updateSelfEmployeeUserAuthTestSuccess() {
		// update self data test success
		String body = "{\"employee\":true,\"name\":\"Ahmad\",\"email\":\"" + "ahmad.user@nasnav.com" + "\" }";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void updateOtherEmployeeUserAuthTestSuccess() {
		// update user with id 158 success
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"name\":\"hussien\",\"email\":\"" + "hussien.Test@nasnav.com" + "\"," +
				" \"org_id\": 99002" + ", \"role\": \"ORGANIZATION_MANAGER\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);
		response.getBody().getEntityId();

		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void updateOtherEmployeeUserAuthTestFail() {
		// update user with id 158 fail(unauthorized assigning NASNAV_ADMIN role)
		String body = "{\"employee\":\"true\",\"updated_user_id\":\"158\", \"role\": \"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm");
		ResponseEntity<BaseResponse> response = template.postForEntity("/user/update", employeeUserJson, BaseResponse.class);

		
		Assert.assertEquals(406, response.getStatusCode().value());
	}

	@Test
	public void updateOtherEmployeeUserInvaildDataTestFail() {
		// update user with id 158 fail(invalid name and email)
		String body = "{\"employee\":\"true\",\"updated_user_id\":\"158\",\"name\":\"74man\",\"email\":\"" + "boda  Test@nasnav.com" + "\", \"org_id\": 99001" + "}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);
		response.getBody().getEntityId();

		
		Assert.assertEquals(406, response.getStatusCode().value());
	}

	//NASNAV_ADMIN,ORGANIZATION_ADMIN,ORGANIZATION_EMPLOYEE,STORE_EMPLOYEE

	//NASNAV_ADMIN changing roles to NASNAV_ADMIN,ORGANIZATION_ADMIN,ORGANIZATION_EMPLOYEE,STORE_EMPLOYEE
	@Test
	public void updateToNasnavAdminByNasnavAdminTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void updateToOrganizationAdminByNasnavAdminTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"ORGANIZATION_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void updateToOrganizationEmployeeByNasnavAdminTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"ORGANIZATION_EMPLOYEE\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void updateToStoreEmployeeByNasnavAdminTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"STORE_EMPLOYEE\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "abcdefg");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		Assert.assertEquals(200, response.getStatusCode().value());
	}
	//finish Nasnav_admin role test

	//ORGANIZATION_ADMIN changing roles to NASNAV_ADMIN,ORGANIZATION_ADMIN,ORGANIZATION_EMPLOYEE,STORE_EMPLOYEE
	@Test
	public void updateToNasnavAdminByOrganizationAdminTest() {
		String body = "{\"employee\":\"true\",\"updated_user_id\":\"158\",\"role\":\"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm");
		ResponseEntity<BaseResponse> response = template.postForEntity("/user/update", employeeUserJson, BaseResponse.class);

		
		Assert.assertEquals(406, response.getStatusCode().value());
	}

	@Test
	public void updateToOrganizationAdminByOrganizationAdminTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"ORGANIZATION_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm");
		ResponseEntity<String> response = template.postForEntity("/user/update", employeeUserJson, String.class);
		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void updateToOrganizationEmployeeByOrganizationAdminTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"ORGANIZATION_EMPLOYEE\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm");
		ResponseEntity<String> response = template.postForEntity("/user/update", employeeUserJson, String.class);

		Assert.assertEquals(200, response.getStatusCode().value());
	}

	@Test
	public void updateToStoreEmployeeByOrganizationAdminTest() {
		String body = "{\"employee\":\"true\",\"updated_user_id\":\"158\",\"role\":\"STORE_EMPLOYEE\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "hijkllm");
		ResponseEntity<BaseResponse> response = template.postForEntity("/user/update", employeeUserJson, BaseResponse.class);

		
		Assert.assertEquals(406, response.getStatusCode().value());
	}
	//finish ORGANIZATION_ADMIN role test

	//ORGANIZATION_EMPLOYEE changing roles to NASNAV_ADMIN,ORGANIZATION_ADMIN,ORGANIZATION_EMPLOYEE,STORE_EMPLOYEE
	@Test
	public void updateToNasnavAdminByOrganizationEmployeeTest() {
		String body = "{\"employee\":true,\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "123");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		
		Assert.assertEquals(401, response.getStatusCode().value());
	}

	@Test
	public void updateToOrganizationAdminByOrganizationEmployeeTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"ORGANIZATION_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "123");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		
		Assert.assertEquals(401, response.getStatusCode().value());
	}

	@Test
	public void updateToOrganizationEmployeeByOrganizationEmployeeTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"ORGANIZATION_EMPLOYEE\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "123");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		
		Assert.assertEquals(401, response.getStatusCode().value());
	}

	@Test
	public void updateToStoreEmployeeByOrganizationEmployeeTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"STORE_EMPLOYEE\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "123");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		
		Assert.assertEquals(401, response.getStatusCode().value());
	}
	//finish ORGANIZATION_EMPLOYEE role test


	//STORE_EMPLOYEE changing roles to NASNAV_ADMIN,ORGANIZATION_ADMIN,ORGANIZATION_EMPLOYEE,STORE_EMPLOYEE
	@Test
	public void updateToNasnavAdminByStoreEmployeeTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"NASNAV_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "456");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		
		Assert.assertEquals(401, response.getStatusCode().value());
	}

	@Test
	public void updateToOrganizationAdminByStoreEmployeeTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"ORGANIZATION_ADMIN\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "456");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		
		Assert.assertEquals(401, response.getStatusCode().value());
	}

	@Test
	public void updateToOrganizationEmployeeByStoreEmployeeTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"ORGANIZATION_EMPLOYEE\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "456");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		
		Assert.assertEquals(401, response.getStatusCode().value());
	}

	@Test
	public void updateToStoreEmployeeByStoreEmployeeTest() {
		String body = "{\"employee\":true,\"updated_user_id\":\"158\",\"role\":\"STORE_EMPLOYEE\"}";
		HttpEntity<Object> employeeUserJson = getHttpEntity(body, "456");
		ResponseEntity<UserApiResponse> response = template.postForEntity("/user/update", employeeUserJson, UserApiResponse.class);

		Assert.assertEquals(401, response.getStatusCode().value());
	}
	
	
	
	
	//finish STORE_EMPLOYEE role test

	@Test
	public void getUserOwnData() {
		HttpEntity<Object> header = getHttpEntity("yuhjhu");
		ResponseEntity<UserRepresentationObject> response = 
				template.exchange("/user/info", HttpMethod.GET, header, UserRepresentationObject.class);
		System.out.println(response.toString());
		
		Assert.assertEquals( 200, response.getStatusCodeValue());
		UserRepresentationObject user = response.getBody();
		Assert.assertEquals( 88L, user.getId().longValue());
		assertNotNull(user.roles);
		assertFalse(user.roles.isEmpty());
	}

	
	
	
	
	@Test
	public void getUserDataDifferentUsers() {
		// logged user is NASNAV_ADMIN so he can view all other users data
		HttpEntity<Object> header = getHttpEntity("abcdefg");
		ResponseEntity<UserRepresentationObject> response = template.exchange("/user/info?id=88", HttpMethod.GET,
				header, UserRepresentationObject.class);
		System.out.println(response.toString());
		Assert.assertEquals(200, response.getStatusCodeValue());

		//-------------------------------------------------------------------
		// logged user is ORGANIZATION_ADMIN ,so, he will just get his own data
		header = getHttpEntity("hijkllm");
		response = template.exchange("/user/info?id=88", HttpMethod.GET,
									header, UserRepresentationObject.class);
		
		System.out.println(response.toString());
		Assert.assertEquals( 200, response.getStatusCodeValue());
		UserRepresentationObject user = response.getBody();
		Assert.assertEquals( 69L, response.getBody().getId().longValue());
		assertNotNull(user.roles);
		assertFalse(user.roles.isEmpty());
	}
	
	
	
	

	@Test
	public void getNonExistUserData() {
		HttpEntity<Object> header = getHttpEntity("abcdefg");
		ResponseEntity<UserRepresentationObject> response = template.exchange("/user/info?id=526523", HttpMethod.GET,
				header, UserRepresentationObject.class);
		System.out.println(response.toString());
		Assert.assertEquals( 406, response.getStatusCodeValue());
	}
	
	
	

	// with NASNAV_ADIMN ACCOUNT
	@Test
	public void listEmpUsersDifferentFilters() {
		HttpEntity<Object> header = getHttpEntity( "abcdefg");

		// no filter
		ResponseEntity<List> response = template.exchange("/user/list", HttpMethod.GET,header, java.util.List.class);
		Assert.assertEquals(response.getStatusCodeValue(), 200);
		Assert.assertEquals(16, response.getBody().size());

		// org_id filter
		response = template.exchange("/user/list?org_id=99001", HttpMethod.GET,header, java.util.List.class);
		Assert.assertEquals(response.getStatusCodeValue(), 200);
		Assert.assertEquals(9 ,response.getBody().size());

		response = template.exchange("/user/list?org_id=99002", HttpMethod.GET,header, java.util.List.class);
		Assert.assertEquals(response.getStatusCodeValue(), 200);
		Assert.assertEquals(7, response.getBody().size());

		// store_id filter
		response = template.exchange("/user/list?store_id=501", HttpMethod.GET,header, java.util.List.class);
		Assert.assertEquals(200, response.getStatusCodeValue());
		Assert.assertEquals(7, response.getBody().size());

		response = template.exchange("/user/list?store_id=502", HttpMethod.GET,header, java.util.List.class);
		Assert.assertEquals(200, response.getStatusCodeValue());
		Assert.assertEquals(9, response.getBody().size());

		// role filter
		response = template.exchange("/user/list?role=NASNAV_ADMIN", HttpMethod.GET,header, java.util.List.class);
		System.out.println(response.getBody());
		Assert.assertEquals(200, response.getStatusCodeValue());
		Assert.assertEquals(2, response.getBody().size());

		ResponseEntity<String> failResponse = template.exchange("/user/list?role=invalid_role", HttpMethod.GET,header, String.class);
		Assert.assertEquals(failResponse.getStatusCode(), HttpStatus.NOT_ACCEPTABLE);

		response = template.exchange("/user/list?role=STORE_MANAGER", HttpMethod.GET,header, java.util.List.class);
		System.out.println(response.getBody());
		Assert.assertEquals(200, response.getStatusCodeValue());
		Assert.assertEquals(2, response.getBody().size());

		// role and org_id filter
		response = template.exchange("/user/list?role=STORE_MANAGER&org_id=99001", HttpMethod.GET,header, java.util.List.class);
		System.out.println(response.getBody());
		Assert.assertEquals(200, response.getStatusCodeValue());
		Assert.assertEquals(1, response.getBody().size());

		// role and store_id filter
		response = template.exchange("/user/list?role=STORE_ADMIN&store_id=501", HttpMethod.GET,header, java.util.List.class);
		System.out.println(response.getBody());
		Assert.assertEquals(200, response.getStatusCodeValue());
		Assert.assertEquals(1, response.getBody().size());

		// org_id and store_id filter
		response = template.exchange("/user/list?org_id=99001&store_id=502", HttpMethod.GET,header, java.util.List.class);
		System.out.println(response.getBody());
		Assert.assertEquals(200, response.getStatusCodeValue());
		Assert.assertEquals(2, response.getBody().size());

		// org_id and store_id and role filter
		response = template.exchange("/user/list?org_id=99001&store_id=502&role=STORE_MANAGER", HttpMethod.GET,header, java.util.List.class);
		System.out.println(response.getBody());
		Assert.assertEquals(200, response.getStatusCodeValue());
		Assert.assertEquals(0, response.getBody().size());

		response = template.exchange("/user/list?org_id=99001&store_id=501&role=STORE_MANAGER", HttpMethod.GET,header, java.util.List.class);
		System.out.println(response.getBody());
		Assert.assertEquals(200, response.getStatusCodeValue());
		Assert.assertEquals(1, response.getBody().size());
	}


	@Test
	public void listEmpUsersDifferentPrelivges() {
		// ORGANIZATION_ADMIN account with org_id = 99001
		HttpEntity<Object> header = getHttpEntity("hijkllm");
		ResponseEntity<List> response = template.exchange("/user/list", HttpMethod.GET, header, java.util.List.class);
		//returning EmpUsers within the same organization only
		System.out.println(response.getBody());
		Assert.assertEquals(response.getStatusCodeValue(), 200);
		Assert.assertEquals(response.getBody().size(), 6);

		// trying to filter with NASNAV_ADMIN role
		response = template.exchange("/user/list?role=NASNAV_ADMIN", HttpMethod.GET, header, java.util.List.class);
		//returning EmpUsers within the same organization only
		System.out.println(response.getBody());
		Assert.assertEquals(response.getStatusCodeValue(), 200);
		Assert.assertEquals(response.getBody().size(), 0);

		// trying to filter with different org_id
		response = template.exchange("/user/list?org_id=99002", HttpMethod.GET, header, java.util.List.class);
		//returning EmpUsers within the same organization only
		System.out.println(response.getBody());
		Assert.assertEquals(response.getStatusCodeValue(), 200);
		Assert.assertEquals(response.getBody().size(), 6);

		// trying to filter with store_id not exits in the organization
		response = template.exchange("/user/list?store_id=502", HttpMethod.GET, header, java.util.List.class);
		//returning EmpUsers within the same organization only
		System.out.println(response.getBody());
		Assert.assertEquals(response.getStatusCodeValue(), 200);
		Assert.assertEquals(response.getBody().size(), 0);

		// ORGANIZATION_MANAGER account
		header = getHttpEntity("123");
		response = template.exchange("/user/list", HttpMethod.GET, header, java.util.List.class);
		//returning EmpUsers within the same organization and roles below ORGANIZATION_MANAGER
		System.out.println(response.getBody());
		Assert.assertEquals(response.getStatusCodeValue(), 200);
		Assert.assertEquals(response.getBody().size(), 4);

		// ORGANIZATION_EMPLOYEE account
		header = getHttpEntity("456");
		response = template.exchange("/user/list", HttpMethod.GET, header, java.util.List.class);
		//returning EmpUsers within the same organization and roles below ORGANIZATION_EMPLOYEE
		System.out.println(response.getBody());
		Assert.assertEquals(response.getStatusCodeValue(), 200);
		Assert.assertEquals(response.getBody().size(), 2);
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
		
		Assert.assertEquals(200, response.getStatusCode().value());
		String token = response.getBody().getToken();
		boolean employeeUserLoggedIn = empRepository.existsByAuthenticationToken( token);
		assertTrue("the logged in user should be the employee user, "
				+ "and its token should exists in EMPLOYEE_USER table", employeeUserLoggedIn );
	}
}
