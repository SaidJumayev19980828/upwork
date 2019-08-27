import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.commons.enums.SortOrder;
import com.nasnav.dao.BundleRepository;
import com.nasnav.dto.BundleDTO;
import com.nasnav.dto.ProductSortOptions;
import com.nasnav.persistence.BundleEntity;
import com.nasnav.response.BundleResponse;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@NotThreadSafe
public class BundlesApiTest {
	
	@Autowired
	private TestRestTemplate template;
	
	
	@Autowired
	private BundleRepository bundleRepo;
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Bundle_Test_Data_Insert.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/Bundle_Test_Data_Delete.sql"})
	public void getBundlesTest() throws JsonParseException, JsonMappingException, IOException{
		Long orgId = 99001L;
		int expectedCount = 2;
		ResponseEntity<String> response = template.getForEntity(
														"/product/bundles?"
																	+ "org_id=" + orgId
																	+ "&count=" + expectedCount
																	+ "&sort=" + ProductSortOptions.NAME.getValue()
																	+ "&order=" + SortOrder.DESC.getValue()
														, String.class); 		
		
		assertEquals( HttpStatus.OK,response.getStatusCode());
		
		String body = response.getBody();
		System.out.println("response >>>" + body);
		
		ObjectMapper mapper = new ObjectMapper();
		BundleResponse bundleRes = mapper.readValue(body, BundleResponse.class);
		
		assertEquals(expectedCount, bundleRes.getBundles().size());
		
		
		
		Long expectedTotal = bundleRepo.countByOrganizationId(orgId);
		assertEquals("expected number of all bundles for the organization, before limiting the result with 'count' parameter"
						, expectedTotal
						, bundleRes.getTotal() );
		
		
		BundleDTO firstBundle = bundleRes.getBundles().get(0);
		BundleEntity expectedFirstBundle = bundleRepo.findFirstByOrderByNameDesc();
		assertEquals("we order bundles desc by name, so , we expect this to be the first"
							, expectedFirstBundle.getName() 
							, firstBundle.getName());
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Bundle_Test_Data_Insert.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/Bundle_Test_Data_Delete.sql"})
	public void getSingleBundleTest() throws JsonParseException, JsonMappingException, IOException{
		Long bundleId = 200004L;
		ResponseEntity<String> response = template.getForEntity(
														"/product/bundles?"
																	+ "bundle_id=" + bundleId
																	+ "&sort=" + ProductSortOptions.NAME.getValue()
																	+ "&order=" + SortOrder.DESC.getValue()
														, String.class); 		
		
		assertEquals( HttpStatus.OK,response.getStatusCode());
		
		String body = response.getBody();
		System.out.println("response >>>" + body);
		
		ObjectMapper mapper = new ObjectMapper();
		BundleResponse bundleRes = mapper.readValue(body, BundleResponse.class);
		
		assertEquals(1, bundleRes.getBundles().size());
		
		
		
		
		assertEquals("expected number of all bundles that applies to the query"
						, Long.valueOf(1)
						, bundleRes.getTotal() );
		
		
		BundleDTO firstBundle = bundleRes.getBundles().get(0);
		BundleEntity expectedBundle = bundleRepo.findById(bundleId).get();
		assertEquals("we order bundles desc by name, so , we expect this to be the first"
							, expectedBundle.getName() 
							, firstBundle.getName());
	}
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Bundle_Test_Data_Insert.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/Bundle_Test_Data_Delete.sql"})
	public void getBundleMissingParamsTest() throws JsonParseException, JsonMappingException, IOException{
		ResponseEntity<String> response = template.getForEntity(
														"/product/bundles"
														, String.class); 		
		
		assertEquals( HttpStatus.NOT_ACCEPTABLE,response.getStatusCode());
		System.out.println("response >>>" + response);
	}
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Bundle_Test_Data_Insert.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/Bundle_Test_Data_Delete.sql"})
	public void getBundlesByCategoryTest() throws JsonParseException, JsonMappingException, IOException{
		Long orgId = 99001L;
		Long categoryId = 201L;
		int expectedCount = 2;		
		ResponseEntity<String> response = template.getForEntity(
														"/product/bundles?"
																	+ "org_id=" + orgId
																	+ "&category_id=" + categoryId
																	+ "&count=" + expectedCount
																	+ "&sort=" + ProductSortOptions.NAME.getValue()
														, String.class); 		
		
		assertEquals( HttpStatus.OK,response.getStatusCode());
		
		String body = response.getBody();
		System.out.println("response >>>" + body);
		
		ObjectMapper mapper = new ObjectMapper();
		BundleResponse bundleRes = mapper.readValue(body, BundleResponse.class);
		
		assertEquals(expectedCount, bundleRes.getBundles().size());
		
		
		
		Long expectedTotal = bundleRepo.countByCategoryId(categoryId);
		assertEquals("expected number of all bundles for the category, before limiting the result with 'count' parameter"
						, expectedTotal
						, bundleRes.getTotal() );
		
		
		BundleDTO firstBundle = bundleRes.getBundles().get(0);
		BundleEntity expectedFirstBundle = bundleRepo.findFirstByCategoryIdOrderByNameAsc(categoryId);
		assertEquals("we order bundles ASC by name, so , we expect this to be the first"
							, expectedFirstBundle.getName() 
							, firstBundle.getName());
	}
}
