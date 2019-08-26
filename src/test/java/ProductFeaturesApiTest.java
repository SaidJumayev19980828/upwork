import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dto.ProductFeatureDTO;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc 
@PropertySource("classpath:database.properties")
@NotThreadSafe
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Product_Features_Test_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/Product_Features_Test_Delete.sql"})
public class ProductFeaturesApiTest {
	
	@Autowired
	private ProductFeaturesRepository featureRepo;
	
	
	@Autowired
	private TestRestTemplate template;
	
	
	@Test
	public void getProductFeaturesTest() throws JsonParseException, JsonMappingException, IOException {
		List<ProductFeatureDTO> expected = 
				Arrays.asList( 
						new ProductFeatureDTO(234, "Shoe size", "Size of the shoes", "s-size"),
						new ProductFeatureDTO(235, "Shoe color", "Color of the shoes", "s-color")
					);
		
		Map<String, Object> params = new HashMap<>();
		params.put("organization_id", 99001L);
		
		
		
		String json = template.getForEntity("/organization/products_features?organization_id={organization_id}"
														, String.class
														, params)
												.getBody();
		
		ObjectMapper mapper = new ObjectMapper();
		List<ProductFeatureDTO> fetched = mapper.readValue(json, new TypeReference<List<ProductFeatureDTO>>(){});
		
		assertTrue( expected.stream().allMatch(fetched::contains) );
	}
	
}
