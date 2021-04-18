package com.nasnav.test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.enumerations.ProductFeatureType;
import com.nasnav.persistence.*;
import com.nasnav.request.ProductSearchParam;
import net.jcip.annotations.NotThreadSafe;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONParser;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.IntStream;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.commons.utils.EntityUtils.allIsNull;
import static com.nasnav.commons.utils.EntityUtils.noneIsNull;
import static com.nasnav.enumerations.ExtraAttributeType.INVISIBLE;
import static com.nasnav.enumerations.ExtraAttributeType.STRING;
import static com.nasnav.persistence.ProductTypes.COLLECTION;
import static com.nasnav.persistence.ProductTypes.STOCK_ITEM;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static java.lang.Math.random;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static junit.framework.TestCase.assertEquals;
import static org.json.JSONObject.NULL;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
public class ProductServiceTest {

	private static final BigDecimal DISCOUNT = new BigDecimal("20");
	private static final String DUMMY_EXTRA_ATTR_VALUE = "Indeed";
	private static final String DUMMY_EXTRA_ATTR_NAME = "Very Cool Special feature";
	private static final String INVISIBLE_EXTRA_ATTR_NAME = "You cannot see me";
	private static final String DUMMY_EXTRA_ATTR_ICON = "cool_icon.png";
	private static final String PRODUCT_IMG_URL = "my_cool_img.jpg";
	private static final String PRODUCT_DESC = "Some description";
	public static final int BUNDLE_ITEM_MIN_QUANTITY = 1;
	public static final int BUNDLE_ITEM_NUM = 2;
	public static final int TEST_BUNDLE_ID = 200004;
	public static final int TEST_BUNDLE_SHOP_ID = 100001;
	public static final int TEST_BUNDLE_ORG_ID = 99001;
	public static final int TEST_BUNDLE_PRODUCTS_NUM = 4;
	public static final int TEST_BUNDLE_NUM = 2;
	public static final String PRODUCT_FEATURE_EXTRA_DATA = "{'extra_attribute_id': 7}";


	@Autowired
	private TestRestTemplate template;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ProductCollectionRepository productCollectionRepo;
	@Autowired
	private ProductCollectionItemRepository collectionItemRepo;

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

	@Autowired
	private FilesRepository fileRepository;

	@Autowired
	private ProductImagesRepository imgRepository;

	@Autowired
	private ExtraAttributesRepository extraAttributeRepository;

	@Autowired
	private ObjectMapper objectMapper;


	private final String PRODUCT_NAME = "LIPSTICK";
	private final String PRODUCT_P_NAME = "LIPSTICK PRODUCT";
	private final String PRODUCT_PRODUCT_BARCODE = "BBE3343222DDF";
	private final String PRODUCT_VARIANT_BARCODE = "11124988483838";
	private final String PRODUCT_VARIANT_NAME = "color";
	private final String PRODUCT_VARIANT_P_NAME = "lipstick color";
	private final String PRODUCT_FEATURE_1_VALUE = "red";
	private final String PRODUCT_FEATURE_2_VALUE = "strawberry";
	private final String PRODUCT_FEATURE_1_NAME = "Lispstick Color";
	private final String PRODUCT_FEATURE_1_P_NAME = "lipstick_color";
	private final String PRODUCT_FEATURE_2_NAME = "Lipstick flavour";
	private final String PRODUCT_FEATURE_2_P_NAME = "lipstick_flavour";
	private String FEATURE_SEPC_TEMPLATE = "{\"FEATURE_ID_1\":\"" + PRODUCT_FEATURE_1_VALUE + "\",\"FEATURE_ID_2\":\""
			+ PRODUCT_FEATURE_2_VALUE + "\"}";

	private final BigDecimal PRODUCT_PRICE = new BigDecimal("10");
	private final Integer QUANTITY = 100;


	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert.sql"})
	@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void getProductWithVariantsWithoutStock() {
		ProductTestData testData = createProductTestDataWithoutStocks();
		//-----------------------------------------
		ResponseEntity<String> response =
				template.getForEntity("/navbox/product?product_id=" + testData.productEntity.getId()+"&include_out_of_stock=true",
				String.class);
		//-----------------------------------------
		System.out.println("product without stocks >>> " + response.getBody());

		assertValidResponseWithoutStocks(testData, response);

		//-----------------------------------------
		cleanInsertedData(testData);
	}


	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert.sql"})
	@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void getProductWithVariantsWithStock() {

		ProductTestData testData = createProductTestData();

		//-----------------------------------------
		ResponseEntity<String> response = template.getForEntity(
				String.format("/navbox/product?product_id=%d&shop_id=%d", testData.productEntity.getId(), testData.shopEntities.get(0).getId()),
				String.class);
		//-----------------------------------------
		System.out.println("product with stocks >>> " + response.getBody());

		assertValidResponse(testData, response);

		//-----------------------------------------
		cleanInsertedData(testData);
	}


	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert.sql"})
	@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void getProductAndNoShopProvied() {

		ProductTestData testData = createProductTestDataWithMultipleStocks();

		//-----------------------------------------
		ResponseEntity<String> response = template.getForEntity(
				String.format("/navbox/product?product_id=%d", testData.productEntity.getId()),
				String.class);
		//-----------------------------------------
		System.out.println("product with stocks for all shops >>> " + response.getBody());

		assertValidResponse(testData, response);

		//-----------------------------------------
		cleanInsertedData(testData);
	}


	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert.sql"})
	@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void getProductAndStocksHasZeroDiscounts() {

		ProductTestData testData = createProductTestDataWithZeroDiscountStocks();

		//-----------------------------------------
		ResponseEntity<String> response = template.getForEntity(
				String.format("/navbox/product?product_id=%d", testData.productEntity.getId()),
				String.class);
		//-----------------------------------------
		System.out.println("product with stocks for all shops >>> " + response.getBody());

		assertValidResponse(testData, response);

		//-----------------------------------------
		cleanInsertedData(testData);
	}


	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert.sql"})
	@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void getProductAndFilterByShop() {

		ProductTestData testData = createProductTestDataWithMultipleStocks();
		Long shopId = testData.shopEntities.get(0).getId();
		//-----------------------------------------
		ResponseEntity<String> response = template.getForEntity(
				String.format("/navbox/product?product_id=%d&shop_id=%d", testData.productEntity.getId(), shopId),
				String.class);
		//-----------------------------------------
		System.out.println("product with stocks for all shops >>> " + response.getBody());

		assertValidResponseWithSingleStockReturned(testData, response, shopId);

		//-----------------------------------------
		cleanInsertedData(testData);
	}




	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert_6.sql"})
	@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void testProductWithZeroStocksExcluded() {
		// product #1001 with 1 variant and two stocks .. one with price 600 and the other 400 .. return lowest price info
		ResponseEntity<ProductDetailsDTO> response =
				template.getForEntity("/navbox/product?product_id=1001&include_out_of_stock=false",	ProductDetailsDTO.class);

		assertEquals(OK, response.getStatusCode());
		ProductDetailsDTO body = response.getBody();
		assertEquals("all variants are returned regardless of stock", 1, body.getVariants().size());
		assertNull("No price is returned because all stocks are zero", body.getPrice());
		assertNull("No price is returned because all stocks are zero", body.getPrices());
		assertNull("No default stock is returned because all stocks are zero", body.getStockId());
	}




	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert_6.sql"})
	@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void testProductWithZeroStocksIncluded() {
		// product #1001 with 1 variant and two stocks .. one with price 600 and the other 400 .. return lowest price info
		ResponseEntity<ProductDetailsDTO> response =
				template.getForEntity("/navbox/product?product_id=1001&include_out_of_stock=true",	ProductDetailsDTO.class);

		assertEquals(OK, response.getStatusCode());
		ProductDetailsDTO body = response.getBody();
		assertEquals("all variants are returned regardless of stock", 1, body.getVariants().size());
		assertEquals("Min price will be returned including zero stock prices", 0,  body.getPrice().compareTo(new BigDecimal("400")) );
		assertEquals("default stock is returned including zero stocks", 605L,  body.getStockId().longValue());
	}



	private void assertValidResponseWithSingleStockReturned(ProductTestData testData, ResponseEntity<String> response, Long shopId) {
		JSONObject productDetails = new JSONObject(response.getBody());
		JSONObject variant = productDetails.getJSONArray("variants").getJSONObject(0);
		List<StocksEntity> expectedStocks = testData.stocksEntities
				.stream()
				.filter(stock -> Objects.equals(stock.getShopsEntity().getId(), shopId))
				.collect(toList());
		JSONArray expectedStocksJSON = createExpectedStocks(expectedStocks);
		JSONArray stocks = getStocksJsonArray(variant);


		assertProductDetailsRetrieved(response, productDetails);
		assertVariantDetailRetrieved(variant);
		assertTrue(stocks.similar(expectedStocksJSON));
	}


	private void assertValidResponse(ProductTestData testData, ResponseEntity<String> response) {
		JSONObject productDetails = new JSONObject(response.getBody());
		JSONObject variant = productDetails.getJSONArray("variants").getJSONObject(0);
		JSONArray expectedStocks = createExpectedStocks(testData.stocksEntities);
		JSONArray stocks = getStocksJsonArray(variant);


		assertProductDetailsRetrieved(response, productDetails);
		assertVariantDetailRetrieved(variant);
		assertTrue(stocks.similar(expectedStocks));
	}


	private JSONArray getStocksJsonArray(JSONObject variant) {
		JSONArray stocks = variant.getJSONArray("stocks");
		for (int i = 0; i < stocks.length(); i++) {
			JSONObject stock = stocks.getJSONObject(i);
			String discountStr =
					ofNullable(stock.get("discount"))
							.map(Object::toString)
							.map(BigDecimal::new)
							.map(discount -> discount.setScale(10))
							.map(BigDecimal::toString)
							.orElse("0");
			stock.put("discount", discountStr);
		}
		return stocks;
	}


	private void assertValidResponseWithExtraAttr(ProductTestData testData, ResponseEntity<String> response) {
		JSONObject productDetails = new JSONObject(response.getBody());
		JSONObject variant = productDetails.getJSONArray("variants").getJSONObject(0);
		JSONArray expectedStocks = createExpectedStocks(testData.stocksEntities);
		JSONArray stocks = getStocksJsonArray(variant);


		assertProductDetailsRetrieved(response, productDetails);
		assertVariantDetailRetrievedWithExtraAttr(variant);
		assertTrue(stocks.similar(expectedStocks));
	}




	private void assertValidResponseWithInvisibleExtraAttr(ProductTestData testData, ResponseEntity<String> response) {
		JSONObject productDetails = new JSONObject(response.getBody());
		JSONObject variant = productDetails.getJSONArray("variants").getJSONObject(0);
		JSONArray expectedStocks = createExpectedStocks(testData.stocksEntities);
		JSONArray stocks = getStocksJsonArray(variant);


		assertProductDetailsWithFeaturesWithExtraDataRetrieved(response, productDetails);
		assertVariantDetailRetrievedWithInvisibleExtraAttr(variant);
		assertTrue(stocks.similar(expectedStocks));
	}




	private boolean featureHasExpectedData(JSONObject feature) {
		return ProductFeatureType.STRING.name().equals(feature.getString("type"))
				&& feature.getJSONObject("extra_data").similar(new JSONObject(PRODUCT_FEATURE_EXTRA_DATA));
	}


	private void assertValidResponseWithoutStocks(ProductTestData testData, ResponseEntity<String> response) {
		JSONObject productDetails = new JSONObject(response.getBody());
		JSONArray variantList = productDetails.getJSONArray("variants");

		assertProductDetailsRetrieved(response, productDetails);
		assertTrue("variants that have no returned stocks are not returned", variantList.isEmpty());
	}


	private ProductTestData createProductTestData() {
		ProductTestData testData = new ProductTestData();

		OrganizationEntity org = organizationRepository.findOneById(99001L);

		testData.productEntity = createDummyProduct();
		testData.imgFile = createProductImageFile(org);
		testData.img = createProductImage(testData.productEntity);
		testData.productFeaturesEntity_1 = createDummyFeature1(org);
		testData.productFeaturesEntity_2 = createDummyFeature2(org);
		testData.spec = createDummySpecValues(testData.productFeaturesEntity_1, testData.productFeaturesEntity_2);
		testData.productVariantsEntity = createDummyVariant(testData.productEntity, testData.spec);
		testData.shopEntities = createDummyShops(org, 1);
		testData.stocksEntities = createDummyStocks(testData.productVariantsEntity, org, testData.shopEntities);

		return testData;
	}



	private ProductTestData createProductTestDataWithExtraAttr() {
		ProductTestData testData = new ProductTestData();

		OrganizationEntity org = organizationRepository.findOneById(99001L);

		testData.productEntity = createDummyProduct();
		testData.imgFile = createProductImageFile(org);
		testData.img = createProductImage(testData.productEntity);
		testData.productFeaturesEntity_1 = createDummyFeature1(org);
		testData.productFeaturesEntity_2 = createDummyFeature2(org);
		testData.spec = createDummySpecValues(testData.productFeaturesEntity_1, testData.productFeaturesEntity_2);
		testData.productVariantsEntity = createDummyVariantWithExtraAttributes(testData.productEntity, testData.spec);
		testData.shopEntities = createDummyShops(org, 1);
		testData.stocksEntities = createDummyStocks(testData.productVariantsEntity, org, testData.shopEntities);

		return testData;
	}



	private ProductTestData createProductTestDataWithInvisibleExtraAttr() {
		ProductTestData testData = new ProductTestData();

		OrganizationEntity org = organizationRepository.findOneById(99001L);

		testData.productEntity = createDummyProduct();
		testData.imgFile = createProductImageFile(org);
		testData.img = createProductImage(testData.productEntity);
		testData.productFeaturesEntity_1 = createDummyFeature1WithExtraData(org);
		testData.productFeaturesEntity_2 = createDummyFeature2WithExtraData(org);
		testData.spec = createDummySpecValues(testData.productFeaturesEntity_1, testData.productFeaturesEntity_2);
		testData.productVariantsEntity = createDummyVariantWithInvisibleExtraAttributes(testData.productEntity, testData.spec);
		testData.shopEntities = createDummyShops(org, 1);
		testData.stocksEntities = createDummyStocks(testData.productVariantsEntity, org, testData.shopEntities);

		return testData;
	}


	private ProductTestData createProductTestDataWithoutStocks() {
		ProductTestData testData = new ProductTestData();

		OrganizationEntity org = organizationRepository.findOneById(99001L);

		testData.productEntity = createDummyProduct();
		testData.imgFile = createProductImageFile(org);
		testData.img = createProductImage(testData.productEntity);
		testData.productFeaturesEntity_1 = createDummyFeature1(org);
		testData.productFeaturesEntity_2 = createDummyFeature2(org);
		testData.spec = createDummySpecValues(testData.productFeaturesEntity_1, testData.productFeaturesEntity_2);
		testData.productVariantsEntity = createDummyVariant(testData.productEntity, testData.spec);
		testData.shopEntities = createDummyShops(org, 1);
		return testData;
	}


	private ProductTestData createProductTestDataWithMultipleStocks() {
		ProductTestData testData = new ProductTestData();

		OrganizationEntity org = organizationRepository.findOneById(99001L);

		testData.productEntity = createDummyProduct();
		testData.imgFile = createProductImageFile(org);
		testData.img = createProductImage(testData.productEntity);
		testData.productFeaturesEntity_1 = createDummyFeature1(org);
		testData.productFeaturesEntity_2 = createDummyFeature2(org);
		testData.spec = createDummySpecValues(testData.productFeaturesEntity_1, testData.productFeaturesEntity_2);
		testData.productVariantsEntity = createDummyVariant(testData.productEntity, testData.spec);
		testData.shopEntities = createDummyShops(org, 2);
		testData.stocksEntities = createDummyStocks(testData.productVariantsEntity, org, testData.shopEntities);
		return testData;
	}



	private ProductTestData createProductTestDataWithZeroDiscountStocks() {
		ProductTestData testData = new ProductTestData();

		OrganizationEntity org = organizationRepository.findOneById(99001L);

		testData.productEntity = createDummyProduct();
		testData.imgFile = createProductImageFile(org);
		testData.img = createProductImage(testData.productEntity);
		testData.productFeaturesEntity_1 = createDummyFeature1(org);
		testData.productFeaturesEntity_2 = createDummyFeature2(org);
		testData.spec = createDummySpecValues(testData.productFeaturesEntity_1, testData.productFeaturesEntity_2);
		testData.productVariantsEntity = createDummyVariant(testData.productEntity, testData.spec);
		testData.shopEntities = createDummyShops(org, 2);
		testData.stocksEntities = createDummyStocks(testData.productVariantsEntity, org, testData.shopEntities, ZERO);
		return testData;
	}



	private void assertFeatureArrayRetrieved(JSONObject body) {
		JSONArray features = body.getJSONArray("variant_features");
		JSONArray expectedFeatures = createExpectedFeaturesJson();
		assertTrue(features.similar(expectedFeatures));
	}



	private void assertFeatureWithExtraDataArrayRetrieved(JSONObject body) {
		JSONArray features = body.getJSONArray("variant_features");
		JSONArray expectedFeatures = createExpectedFeaturesWithExtraDataJson();
		assertTrue(features.similar(expectedFeatures));
	}


	private ProductEntity createDummyProduct() {
		ProductEntity productEntity = new ProductEntity();
		productEntity.setName(PRODUCT_NAME);
		productEntity.setPname(PRODUCT_P_NAME);
		productEntity.setOrganizationId(99001L);
		productEntity.setDescription(PRODUCT_DESC);
		productEntity.setBarcode(PRODUCT_PRODUCT_BARCODE);
		productEntity.setBrandId(101L);
		productEntity = productRepository.save(productEntity);

		return productEntity;
	}


	private ProductImagesEntity createProductImage(ProductEntity productEntity) {


		ProductImagesEntity img = new ProductImagesEntity();
		img.setPriority(0);        //product cover images has priority zero
		img.setProductEntity(productEntity);
		img.setType(7);
		img.setUri(PRODUCT_IMG_URL);

		return imgRepository.save(img);
	}


	private FileEntity createProductImageFile(OrganizationEntity org) {
		FileEntity file = null;
		file = fileRepository.findByUrl(PRODUCT_IMG_URL);

		if (file == null) {
			file = new FileEntity();
			file.setLocation("/img.jpg");
			file.setMimetype("image/jpeg");
			file.setOrganization(org);
			file.setUrl(PRODUCT_IMG_URL);
			file.setOriginalFileName("img.jpg");
		}

		return fileRepository.save(file);
	}


	private ProductFeaturesEntity createDummyFeature1(OrganizationEntity org) {
		ProductFeaturesEntity productFeaturesEntity_1 = new ProductFeaturesEntity();
		productFeaturesEntity_1.setName(PRODUCT_FEATURE_1_NAME);
		productFeaturesEntity_1.setPname(PRODUCT_FEATURE_1_P_NAME);
		productFeaturesEntity_1.setOrganization(org);
		productFeaturesEntity_1.setLevel(0);
		productFeaturesEntity_1 = productFeaturesRepository.save(productFeaturesEntity_1);
		return productFeaturesEntity_1;
	}


	private ProductFeaturesEntity createDummyFeature2(OrganizationEntity org) {
		ProductFeaturesEntity productFeaturesEntity_2 = new ProductFeaturesEntity();
		productFeaturesEntity_2.setName(PRODUCT_FEATURE_2_NAME);
		productFeaturesEntity_2.setPname(PRODUCT_FEATURE_2_P_NAME);
		productFeaturesEntity_2.setOrganization(org);
		productFeaturesEntity_2.setLevel(0);
		productFeaturesEntity_2 = productFeaturesRepository.save(productFeaturesEntity_2);
		return productFeaturesEntity_2;
	}



	private ProductFeaturesEntity createDummyFeature1WithExtraData(OrganizationEntity org) {
		ProductFeaturesEntity productFeaturesEntity_1 = new ProductFeaturesEntity();
		productFeaturesEntity_1.setName(PRODUCT_FEATURE_1_NAME);
		productFeaturesEntity_1.setPname(PRODUCT_FEATURE_1_P_NAME);
		productFeaturesEntity_1.setOrganization(org);
		productFeaturesEntity_1.setLevel(0);
		productFeaturesEntity_1.setExtraData(PRODUCT_FEATURE_EXTRA_DATA);
		productFeaturesEntity_1 = productFeaturesRepository.save(productFeaturesEntity_1);
		return productFeaturesEntity_1;
	}



	private ProductFeaturesEntity createDummyFeature2WithExtraData(OrganizationEntity org) {
		ProductFeaturesEntity productFeaturesEntity_2 = new ProductFeaturesEntity();
		productFeaturesEntity_2.setName(PRODUCT_FEATURE_2_NAME);
		productFeaturesEntity_2.setPname(PRODUCT_FEATURE_2_P_NAME);
		productFeaturesEntity_2.setOrganization(org);
		productFeaturesEntity_2.setLevel(0);
		productFeaturesEntity_2.setExtraData(PRODUCT_FEATURE_EXTRA_DATA);
		productFeaturesEntity_2 = productFeaturesRepository.save(productFeaturesEntity_2);
		return productFeaturesEntity_2;
	}


	private String createDummySpecValues(ProductFeaturesEntity productFeaturesEntity_1,
										 ProductFeaturesEntity productFeaturesEntity_2) {
		return FEATURE_SEPC_TEMPLATE
				.replace("FEATURE_ID_1", productFeaturesEntity_1.getId() + "")
				.replace("FEATURE_ID_2", productFeaturesEntity_2.getId() + "");
	}


	private ProductVariantsEntity createDummyVariant(ProductEntity productEntity, String spec) {
		ProductVariantsEntity productVariantsEntity = new ProductVariantsEntity();
		productVariantsEntity.setBarcode(PRODUCT_VARIANT_BARCODE);
		productVariantsEntity.setName(PRODUCT_VARIANT_NAME);
		productVariantsEntity.setFeatureSpec(spec);
		productVariantsEntity.setPname(PRODUCT_VARIANT_P_NAME);
		productVariantsEntity.setProductEntity(productEntity);
		productVariantsEntity = productVariantsRepository.save(productVariantsEntity);

		return productVariantsEntity;
	}


	private ProductVariantsEntity createDummyVariantWithExtraAttributes(ProductEntity productEntity, String spec) {
		ProductVariantsEntity variant = createDummyVariant(productEntity, spec);
		Set<ProductExtraAttributesEntity> extraAttributes = createDummyExtraAttr(variant);
		extraAttributes.forEach(variant::addExtraAttribute);
		return productVariantsRepository.save(variant);
	}


	private ProductVariantsEntity createDummyVariantWithInvisibleExtraAttributes(ProductEntity productEntity, String spec) {
		ProductVariantsEntity variant = createDummyVariant(productEntity, spec);
		Set<ProductExtraAttributesEntity> extraAttributes = createInvisibleDummyExtraAttr(variant);
		extraAttributes.forEach(variant::addExtraAttribute);
		return productVariantsRepository.save(variant);
	}



	private Set<ProductExtraAttributesEntity> createDummyExtraAttr(ProductVariantsEntity variant) {
		ProductExtraAttributesEntity productExtraAttr = new ProductExtraAttributesEntity();
		ExtraAttributesEntity extraAttr = createDummyExtraAttrDef(variant);
		productExtraAttr.setExtraAttribute(extraAttr);
		productExtraAttr.setVariant(variant);
		productExtraAttr.setValue(DUMMY_EXTRA_ATTR_VALUE);
		return setOf(productExtraAttr);
	}




	private Set<ProductExtraAttributesEntity> createInvisibleDummyExtraAttr(ProductVariantsEntity variant) {
		ProductExtraAttributesEntity productExtraAttr = new ProductExtraAttributesEntity();
		ExtraAttributesEntity extraAttr = createInvisibleDummyExtraAttrDef(variant);
		productExtraAttr.setExtraAttribute(extraAttr);
		productExtraAttr.setVariant(variant);
		productExtraAttr.setValue(DUMMY_EXTRA_ATTR_VALUE);
		return setOf(productExtraAttr);
	}



	private ExtraAttributesEntity createDummyExtraAttrDef(ProductVariantsEntity variant) {
		ExtraAttributesEntity extraAttr = new ExtraAttributesEntity();
		extraAttr.setIconUrl(DUMMY_EXTRA_ATTR_ICON);
		extraAttr.setName(DUMMY_EXTRA_ATTR_NAME);
		extraAttr.setOrganizationId(variant.getProductEntity().getOrganizationId());
		extraAttr.setType(null);
		return extraAttributeRepository.save(extraAttr);
	}



	private ExtraAttributesEntity createInvisibleDummyExtraAttrDef(ProductVariantsEntity variant) {
		ExtraAttributesEntity extraAttr = new ExtraAttributesEntity();
		extraAttr.setIconUrl(DUMMY_EXTRA_ATTR_ICON);
		extraAttr.setName(INVISIBLE_EXTRA_ATTR_NAME);
		extraAttr.setOrganizationId(variant.getProductEntity().getOrganizationId());
		extraAttr.setType(INVISIBLE.getValue());
		return extraAttributeRepository.save(extraAttr);
	}


	private List<ShopsEntity> createDummyShops(OrganizationEntity org, int shopsNum) {
		return IntStream.range(0, shopsNum)
				.mapToObj(i -> createDummyShop(org))
				.collect(toList());
	}


	private ShopsEntity createDummyShop(OrganizationEntity organizationEntity) {
		ShopsEntity shopsEntity = new ShopsEntity();
		shopsEntity.setName("Fortune - #" + getRandomInt());
		shopsEntity.setOrganizationEntity(organizationEntity);
		shopsEntity.setRemoved(0);
		shopsEntity = shopsRepository.save(shopsEntity);
		return shopsEntity;
	}


	private int getRandomInt() {
		return (int) (random() * Integer.MAX_VALUE);
	}


	private List<StocksEntity> createDummyStocks(ProductVariantsEntity variant, OrganizationEntity org
			, List<ShopsEntity> shopsEntities) {
		BigDecimal discountAmount =
				DISCOUNT
						.divide(new BigDecimal("100"), 10, HALF_EVEN)
						.multiply(new BigDecimal(PRODUCT_PRICE.toString()));
		return shopsEntities.stream()
				.map(shop -> createDummyStock(variant, org, shop, discountAmount))
				.collect(toList());
	}


	private List<StocksEntity> createDummyStocks(ProductVariantsEntity variant, OrganizationEntity org
			, List<ShopsEntity> shopsEntities, BigDecimal discountAmount) {
		return shopsEntities.stream()
				.map(shop -> createDummyStock(variant, org, shop, discountAmount))
				.collect(toList());
	}


	private StocksEntity createDummyStock(ProductVariantsEntity productVariantsEntity,
										  OrganizationEntity organizationEntity, ShopsEntity shopsEntity, BigDecimal discountAmount) {

		StocksEntity stocksEntity = new StocksEntity();
		stocksEntity.setDiscount(new BigDecimal(0));
		stocksEntity.setPrice(PRODUCT_PRICE);
		stocksEntity.setProductVariantsEntity(productVariantsEntity);
		stocksEntity.setQuantity(QUANTITY);
		stocksEntity.setOrganizationEntity(organizationEntity);
		stocksEntity.setShopsEntity(shopsEntity);
		stocksEntity.setDiscount(discountAmount);
		stocksEntity = stockRepository.save(stocksEntity);
		return stocksEntity;
	}


	private void cleanInsertedData(ProductTestData testData) {
		imgRepository.delete(testData.img);
		fileRepository.delete(testData.imgFile);
		testData.stocksEntities.forEach(stockRepository::delete);
		testData.shopEntities.forEach(shopsRepository::delete);
		productVariantsRepository.delete(testData.productVariantsEntity);
		productFeaturesRepository.delete(testData.productFeaturesEntity_1);
		productFeaturesRepository.delete(testData.productFeaturesEntity_2);
		productRepository.delete(testData.productEntity);
	}


	private void assertVariantDetailRetrieved(JSONObject variant) {
		assertEquals(PRODUCT_FEATURE_1_VALUE, variant.getString(PRODUCT_FEATURE_1_P_NAME));
		assertEquals(PRODUCT_FEATURE_2_VALUE, variant.getString(PRODUCT_FEATURE_2_P_NAME));
		assertEquals(PRODUCT_VARIANT_BARCODE, variant.getString("barcode"));
	}



	private void assertVariantDetailRetrievedWithExtraAttr(JSONObject variant) {
		assertEquals(PRODUCT_FEATURE_1_VALUE, variant.getString(PRODUCT_FEATURE_1_P_NAME));
		assertEquals(PRODUCT_FEATURE_2_VALUE, variant.getString(PRODUCT_FEATURE_2_P_NAME));
		assertEquals(PRODUCT_VARIANT_BARCODE, variant.getString("barcode"));

		JSONObject extraAttr = variant.getJSONArray("extra_attributes").getJSONObject(0);
		assertEquals(DUMMY_EXTRA_ATTR_NAME, extraAttr.getString("name"));
		assertEquals(STRING.name(), extraAttr.get("type"));
		assertEquals(DUMMY_EXTRA_ATTR_VALUE, extraAttr.getString("value"));
		assertEquals(DUMMY_EXTRA_ATTR_ICON, extraAttr.getString("icon_url"));
	}



	private void assertVariantDetailRetrievedWithInvisibleExtraAttr(JSONObject variant) {
		assertEquals(PRODUCT_FEATURE_1_VALUE, variant.getString(PRODUCT_FEATURE_1_P_NAME));
		assertEquals(PRODUCT_FEATURE_2_VALUE, variant.getString(PRODUCT_FEATURE_2_P_NAME));
		assertEquals(PRODUCT_VARIANT_BARCODE, variant.getString("barcode"));

		JSONObject extraAttr = variant.getJSONArray("extra_attributes").getJSONObject(0);
		assertEquals(INVISIBLE_EXTRA_ATTR_NAME, extraAttr.getString("name"));
		assertEquals(INVISIBLE.name(), extraAttr.get("type"));
		assertEquals(true, extraAttr.get("invisible"));
		assertEquals(DUMMY_EXTRA_ATTR_VALUE, extraAttr.getString("value"));
		assertEquals(DUMMY_EXTRA_ATTR_ICON, extraAttr.getString("icon_url"));
	}



	private void assertProductDetailsRetrieved(ResponseEntity<String> response, JSONObject product) {
		assertEquals(OK, response.getStatusCode());
		assertEquals(PRODUCT_NAME, product.getString("name"));
		assertEquals(PRODUCT_P_NAME, product.getString("p_name"));
		assertEquals(PRODUCT_PRODUCT_BARCODE, product.getString("barcode"));
		assertEquals(PRODUCT_DESC, product.getString("description"));
		assertEquals(ProductTypes.DEFAULT, product.getInt("product_type"));
		assertTrue(product.has("variant_features"));
		assertTrue(product.has("variants"));
		assertFeatureArrayRetrieved(product);
	}


	private void assertProductDetailsWithFeaturesWithExtraDataRetrieved(ResponseEntity<String> response, JSONObject product) {
		assertEquals(OK, response.getStatusCode());
		assertEquals(PRODUCT_NAME, product.getString("name"));
		assertEquals(PRODUCT_P_NAME, product.getString("p_name"));
		assertEquals(PRODUCT_PRODUCT_BARCODE, product.getString("barcode"));
		assertEquals(PRODUCT_DESC, product.getString("description"));
		assertEquals(ProductTypes.DEFAULT, product.getInt("product_type"));
		assertTrue(product.has("variant_features"));
		assertTrue(product.has("variants"));
		assertFeatureWithExtraDataArrayRetrieved(product);
	}




	private JSONArray createExpectedStocks(List<StocksEntity> expectedStocks) {
		JSONArray expectedStocksJson = new JSONArray();

		expectedStocks.stream()
				.map(this::createStockJSONObj)
				.forEach(expectedStocksJson::put);

		return expectedStocksJson;
	}


	private JSONObject createStockJSONObj(StocksEntity stock) {
		//please note the Types of expected, so it matches the types of retrieved json fields
		//ex: if discount is integer here and double in the response JSONObject, it won't match
		String discountStr =
				ofNullable(stock.getDiscount())
						.map(discount -> discount.setScale(10))
						.map(discount -> discount.divide(stock.getPrice(), 10, HALF_EVEN))
						.map(discount -> discount.multiply(new BigDecimal("100")))
						.map(discount -> discount.setScale(10))
						.map(BigDecimal::toString)
						.orElse("0");
		JSONObject stockJson = new JSONObject();
		stockJson.put("id", stock.getId().intValue());
		stockJson.put("shop_id", stock.getShopsEntity().getId().intValue());
		stockJson.put("quantity", stock.getQuantity());
		stockJson.put("price", stock.getPrice().doubleValue());
		stockJson.put("discount", discountStr);
		stockJson.put("unit", NULL);
		stockJson.put("currency_value", "EGP");

		return stockJson;
	}


	private JSONArray createExpectedFeaturesJson() {
		JSONObject expectedFeature1 = new JSONObject();
		expectedFeature1.put("name", PRODUCT_FEATURE_1_NAME);
		expectedFeature1.put("label", PRODUCT_FEATURE_1_P_NAME);
		expectedFeature1.put("type", ProductFeatureType.STRING.name());
		expectedFeature1.put("extra_data", new JSONObject());

		JSONObject expectedFeature2 = new JSONObject();
		expectedFeature2.put("name", PRODUCT_FEATURE_2_NAME);
		expectedFeature2.put("label", PRODUCT_FEATURE_2_P_NAME);
		expectedFeature2.put("type", ProductFeatureType.STRING.name());
		expectedFeature2.put("extra_data", new JSONObject());

		return new JSONArray(Arrays.asList(expectedFeature1, expectedFeature2));
	}



	private JSONArray createExpectedFeaturesWithExtraDataJson() {
		JSONObject expectedFeature1 = new JSONObject();
		expectedFeature1.put("name", PRODUCT_FEATURE_1_NAME);
		expectedFeature1.put("label", PRODUCT_FEATURE_1_P_NAME);
		expectedFeature1.put("type", ProductFeatureType.STRING.name());
		expectedFeature1.put("extra_data", new JSONObject(PRODUCT_FEATURE_EXTRA_DATA));

		JSONObject expectedFeature2 = new JSONObject();
		expectedFeature2.put("name", PRODUCT_FEATURE_2_NAME);
		expectedFeature2.put("label", PRODUCT_FEATURE_2_P_NAME);
		expectedFeature2.put("type", ProductFeatureType.STRING.name());
		expectedFeature2.put("extra_data", new JSONObject(PRODUCT_FEATURE_EXTRA_DATA));

		return new JSONArray(Arrays.asList(expectedFeature1, expectedFeature2));
	}


	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Product_Bundle_Test_Data_Insert.sql"})
	@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void getSingleProductBundle() {
		ResponseEntity<String> response =
				template.getForEntity(
						"/navbox/product?product_id=" + TEST_BUNDLE_ID + "&shop_id=" + TEST_BUNDLE_SHOP_ID,
						String.class);
		System.out.println(response.getBody());
		JSONObject json = (JSONObject) JSONParser.parseJSON(response.getBody());

		assertEquals(200, response.getStatusCodeValue());
		assertEquals(BUNDLE_ITEM_NUM, json.getJSONArray("bundle_items").length());
		assertEquals(BUNDLE_ITEM_MIN_QUANTITY,
				json.getJSONArray("variants")
						.getJSONObject(0)
						.getJSONArray("stocks")
						.getJSONObject(0)
						.getInt("quantity"));
	}


	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Product_Bundle_Test_Data_Insert.sql"})
	@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void getAllProductsIncludingBundle() {
		ResponseEntity<String> response = template.getForEntity(
				"/navbox/products?org_id=" + TEST_BUNDLE_ORG_ID + "&shop_id=" + TEST_BUNDLE_SHOP_ID,
				String.class);
		System.out.println(response.getBody());
		JSONObject json = (JSONObject) JSONParser.parseJSON(response.getBody());
		JSONArray products = json.getJSONArray("products");

		assertEquals(TEST_BUNDLE_PRODUCTS_NUM, products.length());
		assertEquals(TEST_BUNDLE_NUM, getBundleItemsNum(products));

	}


	/**
	 * in the test bundle test data , bundle names starts with "#Bundle"
	 */
	private int getBundleItemsNum(JSONArray products) {
		return (int) IntStream.range(0, products.length())
				.mapToObj(products::getJSONObject)
				.filter(obj -> obj.getString("name").toLowerCase().startsWith("#bundle"))
				.count();
	}


	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Product_Bundle_Test_Data_Insert.sql"})
	@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void testProductResponseTotal() {
		ResponseEntity<String> response = template.getForEntity(
				"/navbox/products?org_id=" + TEST_BUNDLE_ORG_ID + "&shop_id=" + TEST_BUNDLE_SHOP_ID,
				String.class);
		System.out.println(response.getBody());
		JSONObject json = (JSONObject) JSONParser.parseJSON(response.getBody());
		long total = json.getLong("total");


		assertEquals("all products are counted including bundles and services", 4L, total);
	}


	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert.sql"})
	@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void testProductResponse() throws Throwable {
		performTestProductResponseByFilters();
		productBarcodeTest();
	}


	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert.sql"})
	@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void testProductWithMultipVariantResponse() {
		// product #1002 with 2 variants .. return multiple_variants = true
		ResponseEntity<ProductsResponse> response = template.getForEntity("/navbox/products?shop_id=501", ProductsResponse.class);

		Boolean isMultipleVariants = getProductFromResponse(response, 1002L).isMultipleVariants();

		Assert.assertTrue(isMultipleVariants);
	}


	private ProductRepresentationObject getProductFromResponse(ResponseEntity<ProductsResponse> response, Long productId) {
		return response.getBody()
				.getProducts()
				.stream()
				.filter(p -> Objects.equals(p.getId().longValue(), productId))
				.findAny()
				.get();
	}


	private ProductRepresentationObject getProductFromStringResponse(ResponseEntity<String> response, Long productId) throws Throwable {
		ObjectMapper mapper = new ObjectMapper();
		ProductsResponse body = mapper.readValue(response.getBody(), ProductsResponse.class);
		return body.getProducts()
				.stream()
				.filter(p -> Objects.equals(p.getId().longValue(), productId))
				.findAny()
				.get();
	}


	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert.sql"})
	@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void testProductReturnedMinimumPrice() {
		// product #1001 with 1 variant and two stocks .. one with price 600 and the other 400 .. return lowest price info
		ResponseEntity<ProductsResponse> response =
				template.getForEntity("/navbox/products?org_id=99001&category_id=201&brand_id=101", ProductsResponse.class);

		ProductRepresentationObject product = getProductFromResponse(response, 1001L);

		Assert.assertEquals(new BigDecimal("400.00"), product.getPrice());
	}


	private void performTestProductResponseByFilters() throws Throwable {
		//// testing brand_id filter ////
		ResponseEntity<String> response = template.getForEntity("/navbox/products?org_id=99001", String.class);
		System.out.println(response.getBody());
		JSONObject json = (JSONObject) JSONParser.parseJSON(response.getBody());
		long total = json.getLong("total");
		assertEquals("there are total 3 products with with org_id = 99001 and no brand_id filter", 3, total);


		response = template.getForEntity("/navbox/products?org_id=99001&brand_id=101", String.class);
		System.out.println(response.getBody());
		json = (JSONObject) JSONParser.parseJSON(response.getBody());
		total = json.getLong("total");
		assertEquals("there are 2 products with brand_id = 101", 2, total);


		response = template.getForEntity("/navbox/products?org_id=99001&brand_id=102", String.class);
		System.out.println(response.getBody());
		json = (JSONObject) JSONParser.parseJSON(response.getBody());
		total = json.getLong("total");
		assertEquals("there are 1 product with brand_id = 102", 1, total);
		//// finish test

		//// test fields existance in both "product" and "products" apis
		response = template.getForEntity("/navbox/products?org_id=99001", String.class);

		assertJsonFieldExists(response);
		getProductFromStringResponse(response, 1005L);

		response = template.getForEntity("/navbox/product?product_id=1001", String.class);
		System.out.println("response JSON >>>  " + response.getBody().toString());
		assertTrue(response.getBody().toString().contains("brand_id"));
		//// finish test
	}


	private void assertJsonFieldExists(ResponseEntity<String> response) {
		System.out.println("response JSON >>>  " + response.getBody().toString());
		assertTrue(response.getBody().toString().contains("brand_id"));
		assertTrue(response.getBody().toString().contains("p_name"));
		assertTrue(response.getBody().toString().contains("image_url"));
		assertTrue(response.getBody().toString().contains("default_variant_features"));
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


	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert_2.sql"})
	@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void getProductWithMultipleVariantsTest() {
		ResponseEntity<String> response = template.getForEntity("/navbox/product?product_id=1001", String.class);

		JSONArray expectedVariantFeatures = createExpectedFeaturesJson();
		JSONObject product = new JSONObject(response.getBody());
		JSONArray variantFeatures = product.getJSONArray("variant_features");
		JSONArray variants = product.getJSONArray("variants");

		assertEquals("Product 1001 has 5 variants, only the 4 with stock records will be returned", 4, variants.length());
		assertEquals("The product have only 2 variant features", 2, variantFeatures.length());
		assertTrue(variantFeatures.similar(expectedVariantFeatures));
	}


	@Test
	@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert_2.sql"})
	@Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void testGetProductFilters() throws IOException {
		ProductSearchParam param = new ProductSearchParam();
		param.org_id = 99001L;
		param.brand_id = 101L;

		ResponseEntity<ProductsFiltersResponse> response =
				template.getForEntity("/navbox/filters?" + param.toString(), ProductsFiltersResponse.class);
		assertEquals(200, response.getStatusCodeValue());

		JSONObject res = new JSONObject(response.getBody());
		assertEquals(4, res.length());
		JSONObject prices = res.getJSONObject("prices");
		assertEquals(new BigDecimal(200).setScale(2), prices.getBigDecimal("minPrice"));
		assertEquals(new BigDecimal(1200).setScale(2), prices.getBigDecimal("maxPrice"));
		JSONArray brands = res.getJSONArray("brands");
		assertTrue(!brands.isEmpty());

		JSONArray tags = res.getJSONArray("tags");
		assertTrue(!tags.isEmpty());
		assertEquals(1, tags.length());

		TagsRepresentationObject tag = objectMapper.readValue(tags.get(0).toString(), TagsRepresentationObject.class);
		assertEquals("squishy things", tag.getName());

		JSONObject variantFeatures = res.getJSONObject("variantFeatures");
		assertTrue(!variantFeatures.isEmpty());
	}


	@Test
	@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert_2.sql"})
	@Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void testGetProductFiltersSearchByProductCode() throws JsonParseException, JsonMappingException, IOException {
		ProductSearchParam param = new ProductSearchParam();
		param.org_id = 99001L;
		param.name = "code gens";

		ResponseEntity<String> response =
				template.getForEntity("/navbox/products?org_id=99001&name=gens", String.class);
		assertEquals(OK, response.getStatusCode());

		System.out.println(response.getBody());
		ProductsResponse products = objectMapper.readValue(response.getBody(), new TypeReference<ProductsResponse>() {
		});
		assertEquals("there is 1 products with product code like the given", 1L, products.getTotal().longValue());
		long expectedProductId =
				products
						.getProducts()
						.stream()
						.map(ProductRepresentationObject::getId)
						.findFirst()
						.get();
		assertEquals("only product 1001 has product code like the given", 1001L, expectedProductId);
	}


	@Test
	@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert_2.sql"})
	@Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void testGetProductFiltersSearchBySku() throws JsonParseException, JsonMappingException, IOException {
		ResponseEntity<String> response =
				template.getForEntity("/navbox/products?org_id=99001&name=114", String.class);
		assertEquals(OK, response.getStatusCode());

		System.out.println(response.getBody());
		ProductsResponse products = objectMapper.readValue(response.getBody(), new TypeReference<ProductsResponse>() {
		});
		assertEquals("there is 1 products with sku like the given", 1L, products.getTotal().longValue());
		long expectedProductId =
				products
						.getProducts()
						.stream()
						.map(ProductRepresentationObject::getId)
						.findFirst()
						.get();
		assertEquals("only product 1001 has product code like the given", 1001L, expectedProductId);
	}


	@Test
	@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert_4.sql"})
	@Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void getProductWithMultipleIndenticalImagesTest() {
		ResponseEntity<String> response = template.getForEntity("/navbox/product?product_id=1001", String.class);

		JSONObject product = new JSONObject(response.getBody());
		JSONArray images = product.getJSONArray("images");

		assertEquals("product 1001 and its variant both share the same image, it shoudn't be duplicated in images array"
				, 1, images.length());
	}


	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert.sql"})
	@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void getProductWithVariantsWithExtraAttributes() {

		ProductTestData testData = createProductTestDataWithExtraAttr();

		//-----------------------------------------
		ResponseEntity<String> response = template.getForEntity(
				format("/navbox/product?product_id=%d&shop_id=%d"
						, testData.productEntity.getId(), testData.shopEntities.get(0).getId()),
				String.class);
		//-----------------------------------------
		System.out.println("product with extra attributes >>> " + response.getBody());

		assertValidResponseWithExtraAttr(testData, response);
	}



	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert.sql"})
	@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void getProductWithVariantsWithInvisibleExtraAttributes() {

		ProductTestData testData = createProductTestDataWithInvisibleExtraAttr();

		//-----------------------------------------
		ResponseEntity<String> response = template.getForEntity(
				format("/navbox/product?product_id=%d&shop_id=%d"
						, testData.productEntity.getId(), testData.shopEntities.get(0).getId()),
				String.class);
		//-----------------------------------------
		System.out.println("product with extra attributes >>> " + response.getBody());

		assertValidResponseWithInvisibleExtraAttr(testData, response);
	}



	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Shop_360_Test_Data.sql"})
	@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void deleteCollectionSuccess() {
		HttpEntity request = getHttpEntity("131415");

		assertTrue(productCollectionRepo.existsById(1004L));
		ResponseEntity<String> response = template.exchange("/product/collection?id=1004",
																DELETE, request, String.class);
		assertEquals(200, response.getStatusCodeValue());
		assertFalse(productCollectionRepo.existsById(1004L));
		assertFalse(collectionItemRepo.existsById(1004L));
	}


	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Shop_360_Test_Data.sql"})
	@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void deleteCollectionsSuccess() {
		HttpEntity request = getHttpEntity("131415");

		assertTrue(productCollectionRepo.existsById(1004L));
		assertTrue(productCollectionRepo.existsById(1008L));
		ResponseEntity<String> response = template.exchange("/product/collection?id=1004,1008",
				DELETE, request, String.class);
		assertEquals(200, response.getStatusCodeValue());
		assertFalse(productCollectionRepo.existsById(1004L));
		assertFalse(productCollectionRepo.existsById(1008L));
		assertFalse(collectionItemRepo.existsById(1004L));
		assertFalse(collectionItemRepo.existsById(1008L));
	}


	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Shop_360_Test_Data.sql"})
	@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void deleteCollectionsBelongToDifferentOrg() {
		HttpEntity request = getHttpEntity("131415");

		assertTrue(productCollectionRepo.existsById(1004L));
		assertTrue(productCollectionRepo.existsById(1007L));
		ResponseEntity<String> response = template.exchange("/product/collection?id=1004,1007",
				DELETE, request, String.class);
		assertEquals(404, response.getStatusCodeValue());
		assertTrue(productCollectionRepo.existsById(1004L));
		assertTrue(productCollectionRepo.existsById(1007L));
		assertTrue(collectionItemRepo.existsById(1004L));
		assertTrue(collectionItemRepo.existsById(1007L));
	}



	@Test
	@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert_5.sql"})
	@Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void testGetProductsOutOfStockProductsHasEmptyPrices() throws JsonParseException, JsonMappingException, IOException {
		ProductSearchParam param = new ProductSearchParam();
		param.org_id = 99001L;
		param.name = "code gens";

		ResponseEntity<String> response =
				template.getForEntity("/navbox/products?org_id=99001", String.class);
		assertEquals(OK, response.getStatusCode());

		System.out.println(response.getBody());
		ProductsResponse prodResponse = objectMapper.readValue(response.getBody(), new TypeReference<ProductsResponse>() {
		});
		List<ProductRepresentationObject> allProducts = prodResponse.getProducts();
		List<ProductRepresentationObject> products =
				allProducts
						.stream()
						.filter(p -> p.getProductType() == STOCK_ITEM)
						.collect(toList());
		List<ProductRepresentationObject> collections =
				allProducts
						.stream()
						.filter(p -> p.getProductType() == COLLECTION)
						.collect(toList());

		assertTrue( hasEmptyPrices(collections));
		assertTrue( isOutOfStkProductsHasEmptyPrices(products) );
		assertTrue( isProductsWithStockHavePrices(products) );
	}

	@Test
	@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Shop_360_Test_Data.sql"})
	@Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void deleteProductsWithVariantsLinkedToCollection() {
		HttpEntity<?> request =  getHttpEntity("131415");

		ResponseEntity<String> response =
				template.exchange("/product?product_id=1006", DELETE, request, String.class);
		assertEquals(406, response.getStatusCodeValue());
	}

	@Test
	@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/Shop_360_Test_Data.sql"})
	@Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
	public void deleteProductsWithVariantsLinkedToCollectionAndForceFlagTrue() {
		ProductCollectionEntity collection = productCollectionRepo.findByIdAndOrganizationId(1008L, 99002L).get();
		assertFalse(collection.getVariants().isEmpty());

		HttpEntity<?> request =  getHttpEntity("131415");

		ResponseEntity<String> response =
				template.exchange("/product?product_id=1006&force_delete_collection_items=true", DELETE, request, String.class);
		assertEquals(200, response.getStatusCodeValue());

		collection = productCollectionRepo.findByIdAndOrganizationId(1008L, 99002L).get();
		assertTrue(collection.getVariants().isEmpty());
	}


	private boolean isProductsWithStockHavePrices(List<ProductRepresentationObject> products) {
		return products
				.stream()
				.filter(p -> p.getId() == 1001L)
				.anyMatch(p -> noneIsNull(p.getPrices(), p.getPrices().getMinPrice(), p.getPrices().getMaxPrice()));
	}




	private boolean isOutOfStkProductsHasEmptyPrices(List<ProductRepresentationObject> products) {
		return products.
				stream()
				.filter(p -> p.getId() == 1005L)
				.map(ProductRepresentationObject::getPrices)
				.filter(Objects::nonNull)
				.anyMatch(prices -> allIsNull(prices.getMaxPrice(), prices.getMinPrice()));
	}



	private boolean hasEmptyPrices(List<ProductRepresentationObject> collections) {
		return collections.
				stream()
				.map(ProductRepresentationObject::getPrices)
				.filter(Objects::nonNull)
				.anyMatch(prices -> allIsNull(prices.getMaxPrice(), prices.getMinPrice()));
	}
}



class ProductTestData{
	public String spec;
	public BrandsEntity brand;
	ProductEntity productEntity;		
	ProductFeaturesEntity productFeaturesEntity_1;
	ProductFeaturesEntity productFeaturesEntity_2;
	ProductVariantsEntity productVariantsEntity;
	List<ShopsEntity> shopEntities ;
	List<StocksEntity> stocksEntities;
	ProductImagesEntity img;
	FileEntity imgFile;
	
	ProductTestData(){
		shopEntities = new ArrayList<>();
		stocksEntities = new ArrayList<>();
	}
	
	
	//TODO case: GET /navbox/product : provide a shop that the product has no stocks in
	//TODO case: GET /navbox/product : provide non-existing shop
	//TODO case: GET /navbox/product : get product with images , check the returned "imgs" array and "image_url" in response
}
