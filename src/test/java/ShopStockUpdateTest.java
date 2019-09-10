import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nasnav.NavBox;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.persistence.BaseUserEntity;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@NotThreadSafe
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Stock_Update_API_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class ShopStockUpdateTest {
	
	private static final Long TEST_SHOP_ID = null;
	
	private static final Long TEST_VARIANT_ID = null;

	private static final long VALID_ORG_AMDIN = 69L;

	private static final long OTHER_ORG_ADMIN = 70L;

	private static final long NOT_ORG_ADMIN = 68L;


	@Autowired
	private TestRestTemplate template;


	@Autowired
	private EmployeeUserRepository empUserRepo;
	

	@Test
	public void createStockNoAuthNTest() throws JsonProcessingException{
		
		JSONObject updateReq = createStockUpdateReq();
		
		HttpEntity request =  TestCommons.getHttpEntity(updateReq.toString() , 0L, "non-existing-token");
		
		ResponseEntity<String> response = 
				template.exchange("/shop/stock"
						, HttpMethod.POST
						, request
						, String.class);		
		
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	
	
	@Test
	public void createStockUnAuthZTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(NOT_ORG_ADMIN); //this user is not an organization admin
		
		JSONObject updateReq = createStockUpdateReq();
		
		HttpEntity request =  TestCommons.getHttpEntity(updateReq.toString() , user.getId(), user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/shop/stock"
						, HttpMethod.POST
						, request
						, String.class);	
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	
	@Test
	public void createStockOtherOrgAdminTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(OTHER_ORG_ADMIN); 
		
		JSONObject updateReq = createStockUpdateReq();
		
		HttpEntity request =  TestCommons.getHttpEntity(updateReq.toString() , user.getId(), user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/shop/stock"
						, HttpMethod.POST
						, request
						, String.class);	
		
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
	
	

	
	
	private JSONObject createStockUpdateReq() {
		JSONObject json = new JSONObject();
		
		json.put("shop_id", TEST_SHOP_ID);
		json.put("variant_id", TEST_VARIANT_ID);
		json.put("price", 10.5);
		json.put("currency", 0);
		json.put("quantity", 101);
		
		return json;
	}
}
