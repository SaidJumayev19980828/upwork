import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.NavBox;
import com.nasnav.dao.ProductImgsCustomRepository;
import com.nasnav.dto.VariantWithNoImagesDTO;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc
@PropertySource("classpath:test.database.properties")
@NotThreadSafe 
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Product_Imgs_Custom_Repo_Test_Data.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class ProductImageCustomRepoTest {

	private static Long ORG_ID = 99001L;
	
	@Autowired
	private ProductImgsCustomRepository customImgRepo;
	
	
	
	@Test
	public void getProductsWithNoImgsTest() {
		
		List<VariantWithNoImagesDTO> result = customImgRepo.getProductsWithNoImages(ORG_ID);
		
		List<VariantWithNoImagesDTO> expected = createExpectedResult();
		assertEquals(2, result.size());
		assertEquals(expected, result);
	}

	
	


	private List<VariantWithNoImagesDTO> createExpectedResult() {
		VariantWithNoImagesDTO variant1 = new VariantWithNoImagesDTO();
		variant1.setBarcode("barcody");
		variant1.setExternalId("ABC444ss");
		variant1.setProductId(1003L);
		variant1.setProductName("product_3");
		variant1.setVariantId(310003L);
		
		VariantWithNoImagesDTO variant2 = new VariantWithNoImagesDTO();
		variant2.setBarcode("ABC_barcodak");
		variant2.setExternalId("ABC123ss");
		variant2.setProductId(1005L);
		variant2.setProductName("product_5");
		variant2.setVariantId(310005L);
		
		return asList(variant1, variant2);
	}
}
