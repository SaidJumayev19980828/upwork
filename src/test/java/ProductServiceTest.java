import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.NavBox;
import com.nasnav.dao.BundleRepository;
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

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@NotThreadSafe
public class ProductServiceTest {

	public static final int BUNDLE_ITEM_MIN_QUANTITY = 1;
	public static final int BUNDLE_ITEM_NUM = 2;
	public static final int TEST_BUNDLE_ID = 200004;
	public static final int TEST_BUNDLE_SHOP_ID = 100001;
	public static final int TEST_BUNDLE_ORG_ID = 500001;
	public static final int TEST_BUNDLE_PRODUCTS_NUM = 4;
	public static final int TEST_BUNDLE_NUM = 2;
	@Value("classpath:sql/bundle_test_data_delete.sql")
	private Resource bundleDataDelete;

	@Value("classpath:sql/bundle_test_data_insert.sql")
	private Resource bundleDataInsert;

	@Value("classpath:sql/Products_Test_Data_Insert.sql")
	private Resource productsDataInsert;

	@Value("classpath:sql/Products_Test_Data_Delete.sql")
	private Resource productsDataDelete;

	@Autowired
	private DataSource datasource;

	@Autowired
	private TestRestTemplate template;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private BundleRepository bundleRepo;

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
	private String PRODUCT_VARIANT_FEATURE_SEPC = "{\"FEATURE_ID_1\":\"" + PRODUCT_FEATURE_1_VALUE + "\",\"FEATURE_ID_2\":\""
			+ PRODUCT_FEATURE_2_VALUE + "\"}";

	private final Double PRODUCT_PRICE = 10.5;
	private final Integer QUANTITY = 100;

	@Test
	public void getProductWithVariantsWithoutStock() {

		OrganizationEntity organizationEntity = new OrganizationEntity();
		organizationEntity.setCreatedAt(new Date());
		organizationEntity.setName("Fortune cosmitics");
		organizationEntity.setUpdatedAt(new Date());
		organizationEntity = organizationRepository.save(organizationEntity);


		ProductEntity productEntity = new ProductEntity();
		productEntity.setName(PRODUCT_NAME);
		productEntity.setPname(PRODUCT_P_NAME);
		productEntity.setCategoryId(CATEGORY_ID);
		productEntity.setCreationDate(new Date());
		productEntity.setUpdateDate(new Date());
		productEntity = productRepository.save(productEntity);

		ProductFeaturesEntity productFeaturesEntity_1 = new ProductFeaturesEntity();
		productFeaturesEntity_1.setName(PRODUCT_FEATURE_1_NAME);
		productFeaturesEntity_1.setPname(PRODUCT_FEATURE_1_P_NAME);
		productFeaturesEntity_1.setOrganizationId(organizationEntity.getId());
		productFeaturesEntity_1 = productFeaturesRepository.save(productFeaturesEntity_1);

		ProductFeaturesEntity productFeaturesEntity_2 = new ProductFeaturesEntity();
		productFeaturesEntity_2.setName(PRODUCT_FEATURE_2_NAME);
		productFeaturesEntity_2.setPname(PRODUCT_FEATURE_2_P_NAME);
		productFeaturesEntity_2.setOrganizationId(organizationEntity.getId());
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

		ResponseEntity<String> response = template.getForEntity("/navbox/product?product_id=" + productEntity.getId(),
				String.class);

		System.out.println("product without stocks >>> " + response.getBody());
		Assert.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
		assertTrue(response.getBody().toString().contains("\"name\":\"" + PRODUCT_NAME + "\""));
		assertTrue(response.getBody().toString().contains("\"p_name\":\"" + PRODUCT_P_NAME + "\""));
		assertTrue(response.getBody().toString().contains("\"category_id\":" + CATEGORY_ID));
		assertTrue(response.getBody().toString().contains("\"barcode\":\"" + PRODUCT_VARIANT_BARCODE + "\""));
		assertTrue(response.getBody().toString().contains("\"variant_features\""));
		assertTrue(response.getBody().toString().contains("\"variants\""));
		assertTrue(response.getBody().toString().contains("\"name\":\"" + PRODUCT_FEATURE_1_NAME + "\""));
		assertTrue(response.getBody().toString().contains("\"label\":\"" + PRODUCT_FEATURE_1_P_NAME + "\""));
		assertTrue(response.getBody().toString().contains("\"name\":\"" + PRODUCT_FEATURE_2_NAME + "\""));
		assertTrue(response.getBody().toString().contains("\"label\":\"" + PRODUCT_FEATURE_2_P_NAME + "\""));
		assertTrue(
				response.getBody().toString().contains("\"" + PRODUCT_FEATURE_1_NAME + "\":\"" + PRODUCT_FEATURE_1_VALUE + "\""));
		assertTrue(
				response.getBody().toString().contains("\"" + PRODUCT_FEATURE_2_NAME + "\":\"" + PRODUCT_FEATURE_2_VALUE + "\""));
		assertTrue(response.getBody().toString().contains("\"barcode\":\"" + PRODUCT_VARIANT_BARCODE + "\""));

		productVariantsRepository.delete(productVariantsEntity);
		productFeaturesRepository.delete(productFeaturesEntity_1);
		productFeaturesRepository.delete(productFeaturesEntity_2);
		productRepository.delete(productEntity);

	}

	@Test
	public void getProductWithVariantsWitStock() {

		OrganizationEntity organizationEntity = new OrganizationEntity();
		organizationEntity.setCreatedAt(new Date());
		organizationEntity.setName("Fortune cosmitics");
		organizationEntity.setUpdatedAt(new Date());
		organizationEntity = organizationRepository.save(organizationEntity);

		ProductEntity productEntity = new ProductEntity();
		productEntity.setName(PRODUCT_NAME);
		productEntity.setPname(PRODUCT_P_NAME);
		productEntity.setCategoryId(CATEGORY_ID);
		productEntity.setCreationDate(new Date());
		productEntity.setUpdateDate(new Date());
		productEntity = productRepository.save(productEntity);

		ProductFeaturesEntity productFeaturesEntity_1 = new ProductFeaturesEntity();
		productFeaturesEntity_1.setName(PRODUCT_FEATURE_1_NAME);
		productFeaturesEntity_1.setPname(PRODUCT_FEATURE_1_P_NAME);
		productFeaturesEntity_1.setOrganizationId(organizationEntity.getId());
		productFeaturesEntity_1 = productFeaturesRepository.save(productFeaturesEntity_1);

		ProductFeaturesEntity productFeaturesEntity_2 = new ProductFeaturesEntity();
		productFeaturesEntity_2.setName(PRODUCT_FEATURE_2_NAME);
		productFeaturesEntity_2.setPname(PRODUCT_FEATURE_2_P_NAME);
		productFeaturesEntity_2.setOrganizationId(organizationEntity.getId());
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

		ResponseEntity<String> response = template.getForEntity(
				"/navbox/product?product_id=" + productEntity.getId() + "&shop_id=" + shopsEntity.getId(),
				String.class);

		System.out.println( "product with stocks >>> " +response.getBody());

		Assert.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
		assertTrue(response.getBody().toString().contains("\"name\":\"" + PRODUCT_NAME + "\""));
		assertTrue(response.getBody().toString().contains("\"p_name\":\"" + PRODUCT_P_NAME + "\""));
		assertTrue(response.getBody().toString().contains("\"category_id\":" + CATEGORY_ID ));
		assertTrue(response.getBody().toString().contains("\"barcode\":\"" + PRODUCT_VARIANT_BARCODE + "\""));
		assertTrue(response.getBody().toString().contains("\"variant_features\""));
		assertTrue(response.getBody().toString().contains("\"variants\":"));
		assertTrue(response.getBody().toString().contains("\"name\":\"" + PRODUCT_FEATURE_1_NAME + "\""));
		assertTrue(response.getBody().toString().contains("\"label\":\"" + PRODUCT_FEATURE_1_P_NAME + "\""));
		assertTrue(response.getBody().toString().contains("\"name\":\"" + PRODUCT_FEATURE_2_NAME + "\""));
		assertTrue(response.getBody().toString().contains("\"label\":\"" + PRODUCT_FEATURE_2_P_NAME + "\""));
		assertTrue(
				response.getBody().toString().contains("\"" + PRODUCT_FEATURE_1_NAME + "\":\"" + PRODUCT_FEATURE_1_VALUE + "\""));
		assertTrue(
				response.getBody().toString().contains("\"" + PRODUCT_FEATURE_2_NAME + "\":\"" + PRODUCT_FEATURE_2_VALUE + "\""));
		assertTrue(response.getBody().toString().contains("\"barcode\":\"" + PRODUCT_VARIANT_BARCODE + "\""));
		assertTrue(response.getBody().toString().contains("\"shop_id\":" + shopsEntity.getId() ));
		assertTrue(response.getBody().toString().contains("\"quantity\":" + QUANTITY ));
		assertTrue(response.getBody().toString().contains("\"price\":" + PRODUCT_PRICE ));
		assertTrue(response.getBody().toString().contains("\"discount\":" + 0 ));

		stockRepository.delete(stocksEntity);
		shopsRepository.delete(shopsEntity);

		productVariantsRepository.delete(productVariantsEntity);
		productFeaturesRepository.delete(productFeaturesEntity_1);
		productFeaturesRepository.delete(productFeaturesEntity_2);
		productRepository.delete(productEntity);

		organizationRepository.delete(organizationEntity);

	}

	@Test
	public void getSingleProductBundle(){
		prepareBundleTestData();
		performBundleResponseTest();
		RemoveBundleTestData();
	}

	private void performBundleResponseTest() {
		ResponseEntity<String> response =
				template.getForEntity(
					"/navbox/product?product_id=" + TEST_BUNDLE_ID + "&shop_id=" + TEST_BUNDLE_SHOP_ID,
				String.class);
		System.out.println(response.getBody());
		JSONObject  json = (JSONObject) JSONParser.parseJSON(response.getBody());

		assertEquals(BUNDLE_ITEM_NUM, json.getJSONArray("bundle_items").length());
		assertEquals(BUNDLE_ITEM_MIN_QUANTITY,
							json.getJSONArray("variants")
								.getJSONObject(0)
								.getJSONArray("stocks")
								.getJSONObject(0)
								.getInt("quantity"));
	}

	private void prepareBundleTestData() {
		try (Connection con = datasource.getConnection()) {
			ScriptUtils.executeSqlScript(con, this.bundleDataInsert);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void RemoveBundleTestData() {
		try (Connection con = datasource.getConnection()) {
			ScriptUtils.executeSqlScript(con, this.bundleDataDelete);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}



	@Test
	public void getAllProductsIncludingBundle(){
		prepareBundleTestData();
		performAllProductsWithBundleTest();
		RemoveBundleTestData();
	}

	private void performAllProductsWithBundleTest() {
		ResponseEntity<String> response = template.getForEntity(
				"/navbox/products?org_id=" + TEST_BUNDLE_ORG_ID + "&shop_id=" + TEST_BUNDLE_SHOP_ID,
				String.class);
		System.out.println(response.getBody());
		JSONObject  json = (JSONObject) JSONParser.parseJSON(response.getBody());
		JSONArray products = json.getJSONArray("products");

		assertEquals(TEST_BUNDLE_PRODUCTS_NUM, products.length());
		assertEquals(TEST_BUNDLE_NUM, getBundleItemsNum(products));
	}


	/**in the test bundle test data , bundle names starts with "#Bundle"*/
	private int getBundleItemsNum(JSONArray products) {
		return (int)IntStream.range(0,products.length())
							.mapToObj(products::getJSONObject)
							.filter( obj -> obj.getString("name").toLowerCase().startsWith("#bundle"))
							.count();
	}



	@Test
	public void testProductResponseTotal(){
		prepareBundleTestData();
		performTestProductResponseTotal();
		RemoveBundleTestData();
	}

	private void performTestProductResponseTotal() {
		ResponseEntity<String> response = template.getForEntity(
				"/navbox/products?org_id=" + TEST_BUNDLE_ORG_ID + "&shop_id=" + TEST_BUNDLE_SHOP_ID,
				String.class);
		System.out.println(response.getBody());
		JSONObject  json = (JSONObject) JSONParser.parseJSON(response.getBody());
		long total = json.getLong("total");


		assertEquals("only the total of actual products should be counted, bundles and services are not counted"
						,51L , total);
	}

	@Test
	public void testProductResponse(){
		prepareProductsTestData();
		performTestProductResponseByFilters();
		productBarcodeTest();
		RemoveProductsTestData();
	}

	private void prepareProductsTestData() {
		try (Connection con = datasource.getConnection()) {
			ScriptUtils.executeSqlScript(con, this.productsDataInsert);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	private void RemoveProductsTestData() {
		try (Connection con = datasource.getConnection()) {
			ScriptUtils.executeSqlScript(con, this.productsDataDelete);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void performTestProductResponseByFilters() {
		//// testing brand_id filter ////
		ResponseEntity<String> response = template.getForEntity("/navbox/products?org_id=99001", String.class);
		System.out.println(response.getBody());
		JSONObject  json = (JSONObject) JSONParser.parseJSON(response.getBody());
		long total = json.getLong("total");
		assertEquals("there are total 16 products with with org_id = 99001 and no brand_id filter"
				,16 , total);


		response = template.getForEntity("/navbox/products?org_id=99001&brand_id=101", String.class);
		System.out.println(response.getBody());
		json = (JSONObject) JSONParser.parseJSON(response.getBody());
		total = json.getLong("total");
		assertEquals("there are 10 products with brand_id = 101"
				,10 , total);


		response = template.getForEntity("/navbox/products?org_id=99001&brand_id=102", String.class);
		System.out.println(response.getBody());
		json = (JSONObject) JSONParser.parseJSON(response.getBody());
		total = json.getLong("total");
		assertEquals("there are 6 products with brand_id = 102"
				,6 , total);
		//// finish test

		//// test fields existance in both "product" and "products" apis
		response = template.getForEntity("/navbox/products?org_id=99001", String.class);

		assertJsonFieldExists(response);

		response = template.getForEntity("/navbox/product?product_id=1001", String.class);
		System.out.println("response JSON >>>  "+ response.getBody().toString());
		assertTrue(response.getBody().toString().contains("brand_id"));
		assertTrue(response.getBody().toString().contains("category_id"));
		//// finish test
	}

	private void assertJsonFieldExists(ResponseEntity<String> response) {
		System.out.println("response JSON >>>  "+ response.getBody().toString());
		assertTrue(response.getBody().toString().contains("brand_id"));
		assertTrue(response.getBody().toString().contains("category_id"));
		assertTrue(response.getBody().toString().contains("p_name"));
		assertTrue(response.getBody().toString().contains("image_url"));
	}

	public void productBarcodeTest() {
		// product 1001 doesn't have barcode
		ResponseEntity<String> response = template.getForEntity("/navbox/product?product_id=1001", String.class);
		System.out.println(response.getBody());
		Assert.assertTrue(response.getBody().contains("barcode\":null"));

		// product 1002 has barcode = 123456789
		response = template.getForEntity("/navbox/product?product_id=1002", String.class);
		System.out.println(response.getBody());
		Assert.assertTrue(response.getBody().contains("barcode\":\"123456789"));
	}
}
