import com.nasnav.AppConfig;
import com.nasnav.NavBox;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.controller.UserController;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.enumerations.Roles;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.UserApiResponse;
import com.nasnav.response.ResponseStatus;
import com.nasnav.service.EmployeeUserService;
import com.nasnav.service.UserService;
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
			persistentUser.setOrganizationId(1);
			persistentUser.setShopId((long) 10);
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
        HttpEntity<Object> employeeUserJson = getHttpEntity(
                "{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 2, , \"store_id\": 100, , \"role\": \"NASNAV_ADMIN\"}");
        ResponseEntity<UserApiResponse> response = template.postForEntity(
                "/user/create", employeeUserJson, UserApiResponse.class);
        //Delete this user
        //userService.deleteUser(response.getBody().getEntityId());
        Assert.assertTrue(response.getBody().isSuccess());
        Assert.assertEquals(200, response.getStatusCode().value());
    }
	
	@Test
    public void createEmployeeUserUnAuthorizedTest() {
        HttpEntity<Object> employeeUserJson = getHttpEntity(
                "{\"name\":\"Ahmed\",\"email\":\"" + TestCommons.TestUserEmail + "\", \"org_id\": 2, , \"store_id\": 100, , \"role\": \"CUSTOMER\"}");
        ResponseEntity<UserApiResponse> response = template.postForEntity(
                "/user/create", employeeUserJson, UserApiResponse.class);
        //Delete this user
        //userService.deleteUser(response.getBody().getEntityId());
        Assert.assertFalse(response.getBody().isSuccess());
        Assert.assertEquals(401, response.getStatusCode().value());
    }
}
