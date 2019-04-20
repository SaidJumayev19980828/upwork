import java.math.BigDecimal;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.NavBox;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.StocksEntity;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
public class ProductServiceTest {

	@Autowired
	private TestRestTemplate template;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ProductVariantsRepository productVariantsRepository;

	@Autowired
	private ProductFeaturesRepository productFeaturesRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private ShopsRepository shopsRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	private final String PRODUCT_NAME = "LIPSTICK";
	private final String PRODUCT_P_NAME = "LIPSTICK PRODUCT";
	private final Long CATEGORY_ID = 1l;
	private final String PRODUCT_VARIANT_BARCODE = "11124988483838";
	private final String PRODUCT_VARIANT_NAME = "color";
	private final String PRODUCT_VARIANT_P_NAME = "lipstick color";
	private final String PRODUCT_FEATURE_1_VALUE = "red";
	private final String PRODUCT_FEATURE_2_VALUE = "strawberry";
	private final String PRODUCT_FEATURE_1_NAME = "color";
	private final String PRODUCT_FEATURE_1_P_NAME = "lipstick color";
	private final String PRODUCT_FEATURE_2_NAME = "flavour";
	private final String PRODUCT_FEATURE_2_P_NAME = "lipstick flavour";
	private String PRODUCT_VARIANT_FEATURE_SEPC = "{FEATURE_ID_1:" + PRODUCT_FEATURE_1_VALUE + ",FEATURE_ID_2:"
			+ PRODUCT_FEATURE_2_VALUE + "}";

	private final Double PRODUCT_PRICE = 10.5;
	private final Integer QUANTITY = 100;

	@Test
	public void getProductWithVariantsWithoutStock() {

		ProductEntity productEntity = new ProductEntity();
		productEntity.setName(PRODUCT_NAME);
		productEntity.setPname(PRODUCT_P_NAME);
		productEntity.setCategoryId(CATEGORY_ID);
		productEntity.setCreationdDate(new Date());
		productEntity.setUpdateDate(new Date());
		productEntity = productRepository.save(productEntity);

		ProductFeaturesEntity productFeaturesEntity_1 = new ProductFeaturesEntity();
		productFeaturesEntity_1.setName(PRODUCT_FEATURE_1_NAME);
		productFeaturesEntity_1.setPname(PRODUCT_FEATURE_1_P_NAME);
		productFeaturesEntity_1 = productFeaturesRepository.save(productFeaturesEntity_1);

		ProductFeaturesEntity productFeaturesEntity_2 = new ProductFeaturesEntity();
		productFeaturesEntity_2.setName(PRODUCT_FEATURE_2_NAME);
		productFeaturesEntity_2.setPname(PRODUCT_FEATURE_2_P_NAME);
		productFeaturesEntity_2 = productFeaturesRepository.save(productFeaturesEntity_2);

		PRODUCT_VARIANT_FEATURE_SEPC = PRODUCT_VARIANT_FEATURE_SEPC
				.replace("FEATURE_ID_1", productFeaturesEntity_1.getId() + "")
				.replace("FEATURE_ID_2", productFeaturesEntity_2.getId() + "");
		ProductVariantsEntity productVariantsEntity = new ProductVariantsEntity();
		productVariantsEntity.setBarcode(PRODUCT_VARIANT_BARCODE);
		productVariantsEntity.setName(PRODUCT_VARIANT_NAME);
		productVariantsEntity.setFeatureSpec(PRODUCT_VARIANT_FEATURE_SEPC);
		productVariantsEntity.setPname(PRODUCT_VARIANT_P_NAME);
		productVariantsEntity.setProductEntity(productEntity);
		productVariantsEntity = productVariantsRepository.save(productVariantsEntity);

		ResponseEntity<Object> response = template.getForEntity("/navbox/product?product_id=" + productEntity.getId(),
				Object.class);

		Assert.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
		Assert.assertTrue(response.getBody().toString().contains("name=" + PRODUCT_NAME));
		Assert.assertTrue(response.getBody().toString().contains("p_name=" + PRODUCT_P_NAME));
		Assert.assertTrue(response.getBody().toString().contains("category_id=" + CATEGORY_ID));
		Assert.assertTrue(response.getBody().toString().contains("barcode=" + PRODUCT_VARIANT_BARCODE));
		Assert.assertTrue(response.getBody().toString().contains("variant_features"));
		Assert.assertTrue(response.getBody().toString().contains("variants"));
		Assert.assertTrue(response.getBody().toString().contains("name=" + PRODUCT_FEATURE_1_NAME));
		Assert.assertTrue(response.getBody().toString().contains("label=" + PRODUCT_FEATURE_1_P_NAME));
		Assert.assertTrue(response.getBody().toString().contains("name=" + PRODUCT_FEATURE_2_NAME));
		Assert.assertTrue(response.getBody().toString().contains("label=" + PRODUCT_FEATURE_2_P_NAME));
		Assert.assertTrue(
				response.getBody().toString().contains(PRODUCT_FEATURE_1_NAME + "=" + PRODUCT_FEATURE_1_VALUE));
		Assert.assertTrue(
				response.getBody().toString().contains(PRODUCT_FEATURE_2_NAME + "=" + PRODUCT_FEATURE_2_VALUE));
		Assert.assertTrue(response.getBody().toString().contains("barcode=" + PRODUCT_VARIANT_BARCODE));

		productVariantsRepository.delete(productVariantsEntity);
		productFeaturesRepository.delete(productFeaturesEntity_1);
		productFeaturesRepository.delete(productFeaturesEntity_2);
		productRepository.delete(productEntity);

	}

	@Test
	public void getProductWithVariantsWitStock() {

		ProductEntity productEntity = new ProductEntity();
		productEntity.setName(PRODUCT_NAME);
		productEntity.setPname(PRODUCT_P_NAME);
		productEntity.setCategoryId(CATEGORY_ID);
		productEntity.setCreationdDate(new Date());
		productEntity.setUpdateDate(new Date());
		productEntity = productRepository.save(productEntity);

		ProductFeaturesEntity productFeaturesEntity_1 = new ProductFeaturesEntity();
		productFeaturesEntity_1.setName(PRODUCT_FEATURE_1_NAME);
		productFeaturesEntity_1.setPname(PRODUCT_FEATURE_1_P_NAME);
		productFeaturesEntity_1 = productFeaturesRepository.save(productFeaturesEntity_1);

		ProductFeaturesEntity productFeaturesEntity_2 = new ProductFeaturesEntity();
		productFeaturesEntity_2.setName(PRODUCT_FEATURE_2_NAME);
		productFeaturesEntity_2.setPname(PRODUCT_FEATURE_2_P_NAME);
		productFeaturesEntity_2 = productFeaturesRepository.save(productFeaturesEntity_2);

		PRODUCT_VARIANT_FEATURE_SEPC = PRODUCT_VARIANT_FEATURE_SEPC
				.replace("FEATURE_ID_1", productFeaturesEntity_1.getId() + "")
				.replace("FEATURE_ID_2", productFeaturesEntity_2.getId() + "");
		ProductVariantsEntity productVariantsEntity = new ProductVariantsEntity();
		productVariantsEntity.setBarcode(PRODUCT_VARIANT_BARCODE);
		productVariantsEntity.setName(PRODUCT_VARIANT_NAME);
		productVariantsEntity.setFeatureSpec(PRODUCT_VARIANT_FEATURE_SEPC);
		productVariantsEntity.setPname(PRODUCT_VARIANT_P_NAME);
		productVariantsEntity.setProductEntity(productEntity);
		productVariantsEntity = productVariantsRepository.save(productVariantsEntity);

		OrganizationEntity organizationEntity = new OrganizationEntity();
		organizationEntity.setCreatedAt(new Date());
		organizationEntity.setName("Fortune cosmitics");
		organizationEntity.setUpdatedAt(new Date());
		organizationEntity = organizationRepository.save(organizationEntity);

		ShopsEntity shopsEntity = new ShopsEntity();
		shopsEntity.setName("Fortune");
		shopsEntity.setCreatedAt(new Date());
		shopsEntity.setUpdatedAt(new Date());
		shopsEntity.setOrganizationEntity(organizationEntity);
		shopsEntity = shopsRepository.save(shopsEntity);

		StocksEntity stocksEntity = new StocksEntity();
		stocksEntity.setDiscount(new BigDecimal(0));
		stocksEntity.setPrice(new BigDecimal(PRODUCT_PRICE));
		stocksEntity.setProductEntity(productEntity);
		stocksEntity.setProductVariantsEntity(productVariantsEntity);
		stocksEntity.setQuantity(QUANTITY);
		stocksEntity.setCreationDate(new Date());
		stocksEntity.setUpdateDate(new Date());
		stocksEntity.setShopsEntity(shopsEntity);
		stocksEntity = stockRepository.save(stocksEntity);

		ResponseEntity<Object> response = template.getForEntity(
				"/navbox/product?product_id=" + productEntity.getId() + "&shop_id=" + shopsEntity.getId(),
				Object.class);

		Assert.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
		Assert.assertTrue(response.getBody().toString().contains("name=" + PRODUCT_NAME));
		Assert.assertTrue(response.getBody().toString().contains("p_name=" + PRODUCT_P_NAME));
		Assert.assertTrue(response.getBody().toString().contains("category_id=" + CATEGORY_ID));
		Assert.assertTrue(response.getBody().toString().contains("barcode=" + PRODUCT_VARIANT_BARCODE));
		Assert.assertTrue(response.getBody().toString().contains("variant_features"));
		Assert.assertTrue(response.getBody().toString().contains("variants"));
		Assert.assertTrue(response.getBody().toString().contains("name=" + PRODUCT_FEATURE_1_NAME));
		Assert.assertTrue(response.getBody().toString().contains("label=" + PRODUCT_FEATURE_1_P_NAME));
		Assert.assertTrue(response.getBody().toString().contains("name=" + PRODUCT_FEATURE_2_NAME));
		Assert.assertTrue(response.getBody().toString().contains("label=" + PRODUCT_FEATURE_2_P_NAME));
		Assert.assertTrue(
				response.getBody().toString().contains(PRODUCT_FEATURE_1_NAME + "=" + PRODUCT_FEATURE_1_VALUE));
		Assert.assertTrue(
				response.getBody().toString().contains(PRODUCT_FEATURE_2_NAME + "=" + PRODUCT_FEATURE_2_VALUE));
		Assert.assertTrue(response.getBody().toString().contains("barcode=" + PRODUCT_VARIANT_BARCODE));
		Assert.assertTrue(response.getBody().toString().contains("shop_id=" + shopsEntity.getId()));
		Assert.assertTrue(response.getBody().toString().contains("quantity=" + QUANTITY));
		Assert.assertTrue(response.getBody().toString().contains("price=" + PRODUCT_PRICE));
		Assert.assertTrue(response.getBody().toString().contains("discount=" + 0));

		stockRepository.delete(stocksEntity);
		shopsRepository.delete(shopsEntity);
		organizationRepository.delete(organizationEntity);

		productVariantsRepository.delete(productVariantsEntity);
		productFeaturesRepository.delete(productFeaturesEntity_1);
		productFeaturesRepository.delete(productFeaturesEntity_2);
		productRepository.delete(productEntity);

	}

}
