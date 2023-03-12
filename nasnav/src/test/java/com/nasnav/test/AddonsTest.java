package com.nasnav.test;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.HttpMethod.GET;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.NavBox;
import com.nasnav.service.AddonService;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Addons_Api_Test_Data.sql"})
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class AddonsTest {
	
	
	 @Autowired
	    private TestRestTemplate template;
	   @Autowired
		AddonService addonService;
	 

	   @Test
	    public void createAddon(){
	    	String requestJson =
	                json()
	                .put("name", "testAddon")
	                .put("operation", "create")
	                .put("type",1 )
	                .toString();
	    	

	        ResponseEntity<String> response = template.postForEntity("/addons",
	                getHttpEntity(requestJson, "123"), String.class);
	        assertEquals(200, response.getStatusCode().value());
	    }
	    @Test
	    public void updateAddon(){
	    	String requestJson =
	                json()
	                .put("id",1005 )
	                .put("name", "testAddon2")
	                .put("operation", "update")
	                .put("type",1 )
	                .toString();
	    	

	        ResponseEntity<String> response = template.postForEntity("/addons",
	                getHttpEntity(requestJson, "123"), String.class);
	        assertEquals(200, response.getStatusCode().value());
	    }
	    @Test
	    public void addAddonToProducts(){
	    	

	    	String requestJson =
	                json()
	                .put("products_ids",Arrays.asList(1001) )
	                .put("addons_ids",Arrays.asList(1005))
	                .toString();
	    	

	        ResponseEntity<String> response = template.postForEntity("/addons/product",
	                getHttpEntity(requestJson, "123"), String.class);
	        assertEquals(200, response.getStatusCode().value());
	    }
	    @Test
	    public void getOrganizationAddons(){
	    
	    	  HttpEntity<?> json = getHttpEntity("123");
	          ResponseEntity<String> response = template.exchange("/addons", GET, json, String.class);
	   
	          assertEquals(200, response.getStatusCodeValue());
	          assertNotNull(response.getBody());
	    }   
	     @Test
	    public void createAddonStock(){
	    	String requestJson =
	                json()
	                .put("addon_id",1009)
	                .put("operation", "create")
	                .put("quantity",20 )
	                .put("price",10 )
	                .put("shop_id",502 )
	                .toString();
	    	

	        ResponseEntity<String> response = template.postForEntity("/addons/stock",
	                getHttpEntity(requestJson, "123"), String.class);
	        assertEquals(200, response.getStatusCode().value());
	    }
	    
	     
	    @Test
		    public void updateAddonStock(){
		    	String requestJson =
		                json()
		                .put("id",1007)
		                .put("addon_id",1005)
		                .put("operation", "update")
		                .put("quantity",66 )
		                .put("price",8 )
		                .put("shop_id",502 )
		                .toString();
		    	

		        ResponseEntity<String> response = template.postForEntity("/addons/stock",
		                getHttpEntity(requestJson, "123"), String.class);
		        assertEquals(200, response.getStatusCode().value());
		    }
	    
	    
	    @Test
	    public void getItemAddons(){
	    
	    	  HttpEntity<?> json = getHttpEntity("123");
	          ResponseEntity<String> response = template.exchange("/addons/item/?item_id=99000", GET, json, String.class);
	   
	          assertEquals(200, response.getStatusCodeValue());
	          assertNotNull(response.getBody());
	    }   
}
