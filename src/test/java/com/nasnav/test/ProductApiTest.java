package com.nasnav.test;
import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.enumerations.OrderStatus.NEW;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.json.JSONObject;
import org.junit.Assert;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.constatnts.EntityConstants.Operation;
import com.nasnav.dao.BasketRepository;
import com.nasnav.dao.BundleRepository;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.ProductImagesRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dao.TagsRepository;
import com.nasnav.dto.ProductRepresentationObject;
import com.nasnav.dto.ProductSortOptions;
import com.nasnav.dto.ProductsFiltersResponse;
import com.nasnav.dto.ProductsResponse;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.persistence.TagsEntity;
import com.nasnav.request.ProductSearchParam;
import com.nasnav.response.ProductUpdateResponse;
import com.nasnav.response.ProductsDeleteResponse;
import com.nasnav.test.commons.TestCommons;

import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Products_API_Test_Data_Insert.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class ProductApiTest {
	
	private static String USER_TOKEN = "123";
	
	
	@Autowired
	private TestRestTemplate template;


	@Autowired
	private EmployeeUserRepository empUserRepo;

	@Autowired
	private ProductRepository productRepository;


	@Autowired
	private ProductVariantsRepository variantRepo;


	@Autowired
	private StockRepository stockRepo;


	@Autowired
	private ProductImagesRepository imgRepo;
	
	
	@Autowired
	private OrdersRepository orderRepo;
	
	@Autowired
	private BundleRepository bundleRepo;


	@Autowired
	private JdbcTemplate jdbc;


	@Autowired
	private BasketRepository basketRepo;

	@Autowired
	private TagsRepository tagsRepo;


	@Test
	public void createProductUserWithNoRightsTest() throws JsonProcessingException {
		BaseUserEntity user = empUserRepo.getById(68L);

		JSONObject productJson = createNewDummyProduct();

		HttpEntity<?> request =  getHttpEntity(productJson.toString() , user.getAuthenticationToken());

		ResponseEntity<String> response =
				template.exchange("/product/info"
						, POST
						, request
						, String.class);

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

		JSONObject body = new JSONObject(response.getBody());
		assertFalse(body.getBoolean("success"));
	}




	@Test
	public void createProductTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);

		JSONObject product = createNewDummyProduct();

		ResponseEntity<ProductUpdateResponse> response = postProductData(user, product);

		validateCreatedProductResponse(response);

		Long id = response.getBody().getProductId();
		ProductEntity saved  = productRepository.findById(id).get();

		validateCreatedProductData(product, saved, id, user.getOrganizationId());
	}



	private void validateCreatedProductResponse(ResponseEntity<ProductUpdateResponse> response) {
		assertEquals(HttpStatus.OK, response.getStatusCode());
		ProductUpdateResponse body = response.getBody();
		assertNotEquals(body.getProductId() , Long.valueOf(0L));
		assertTrue(productRepository.existsById(body.getProductId()));
	}




	@Test
	public void updateProductTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);
		Long id = 1001L;
		ProductEntity originalProduct =  productRepository.findById(id).get();


		//modify some fields for existing product in the test data, only the fields added to the JSON will be changed
		JSONObject product = new JSONObject();
		product.put("operation", Operation.UPDATE.getValue());
		product.put("product_id",id);
		product.put("barcode","UPDATEDbARcODE");
		product.put("name","updated product");
		product.put("brand_id", JSONObject.NULL);

		HttpEntity<?> request =  TestCommons.getHttpEntity(product.toString() , user.getAuthenticationToken());

		ResponseEntity<ProductUpdateResponse> response =
				template.exchange("/product/info"
						, POST
						, request
						, ProductUpdateResponse.class);

		validateCreatedProductResponse(response);

		Long savedId = response.getBody().getProductId();
		ProductEntity saved  = productRepository.findById(savedId).get();

		//modified properties should equal to new values
		assertEquals(id , saved.getId());
		assertEquals(product.getString("name"), saved.getName());
		assertEquals(product.getString("barcode"), saved.getBarcode());
		assertNull(saved.getBrandId());

		//original values should remain the same
		assertEquals("updated-product", saved.getPname());
		assertEquals(originalProduct.getDescription(), saved.getDescription());
		assertEquals(user.getOrganizationId() , saved.getOrganizationId());
	}





	private ResponseEntity<ProductUpdateResponse> postProductData(BaseUserEntity user, JSONObject productJson)
			throws JsonProcessingException {

		HttpEntity<?> request =  TestCommons.getHttpEntity(productJson.toString() , user.getAuthenticationToken());

		ResponseEntity<ProductUpdateResponse> response =
				template.exchange("/product/info"
						, POST
						, request
						, ProductUpdateResponse.class);
		return response;
	}




	private void validateCreatedProductData(JSONObject product, ProductEntity saved, Long id, Long userOrgId) {
		assertEquals(id , saved.getId());
		assertEquals(product.get("name"), saved.getName());
		assertEquals(product.get("p_name"), saved.getPname());
		assertEquals(product.get("description"), saved.getDescription());
		assertEquals(product.get("barcode"), saved.getBarcode());
		assertEquals(product.get("brand_id"), saved.getBrandId());
		assertEquals(userOrgId, saved.getOrganizationId()); //the new product takes the organization of the user 
	}



	private JSONObject createNewDummyProduct() {
		JSONObject product = new JSONObject();
		product.put("operation", Operation.CREATE.getValue());
		product.put("product_id", JSONObject.NULL);
		product.put("name", "Test Product");
		product.put("p_name", "test_product");
		product.put("description", "Testing creating/updating product");
		product.put("barcode", "BAR12345CODE");
		product.put("brand_id", 101L);

		return product;
	}




	@Test
	public void createProdcutDefaultPnameTest() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);

		JSONObject product = createNewDummyProduct();
		product.put("p_name",JSONObject.NULL);  //set pname to null, so that it will be auto-generated from product name

		ResponseEntity<ProductUpdateResponse> response = postProductData(user, product);

		validateCreatedProductResponse(response);

		Long id = response.getBody().getProductId();
		ProductEntity saved  = productRepository.findById(id).get();

		product.put("p_name","test-product"); //set the expected pname , it will be compared against the generated one in the entity

		validateCreatedProductData(product, saved, id, user.getOrganizationId());
	}




	@Test
	public void createProdcutNullBrand() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);

		JSONObject product = createNewDummyProduct();
		product.put("brand_id", JSONObject.NULL);

		ResponseEntity<String> response = postInvalidProductData(user, product);

		Long id = new JSONObject(response.getBody()).getLong("product_id");
		ProductEntity saved  = productRepository.findById(id).get();

		validateCreatedProductData(product, saved, id, user.getOrganizationId());
	}




	@Test
	public void createProdcutNonExistingBrand() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);

		JSONObject product = createNewDummyProduct();
		product.put("brand_id", 99999L);

		ResponseEntity<String> response = postInvalidProductData(user, product);

		validateErrorResponse(response);
	}



	@Test
	public void createProdcutInvalidBrand() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);

		JSONObject product = createNewDummyProduct();
		product.put("brand_id", 102L); // this brand is for another organization 

		ResponseEntity<String> response = postInvalidProductData(user, product);

		validateErrorResponse(response);
	}



	@Test
	public void createProdcutNullName() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);

		JSONObject product = createNewDummyProduct();
		product.put("name", JSONObject.NULL);

		ResponseEntity<String> response = postInvalidProductData(user, product);

		validateErrorResponse(response);
	}




	@Test
	public void updateProdcutNonExistingOperationField() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);

		JSONObject product = createNewDummyProduct();
		product.remove("operation");

		ResponseEntity<String> response = postInvalidProductData(user, product);

		validateErrorResponse(response);
	}



	@Test
	public void createProdcutNonExistingNameField() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);

		JSONObject product = createNewDummyProduct();
		product.remove("name");

		ResponseEntity<String> response = postInvalidProductData(user, product);

		validateErrorResponse(response);
	}






	@Test
	public void createProdcutNonExistingBrandIdField() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);

		JSONObject product = createNewDummyProduct();
		product.remove("brand_id");

		ResponseEntity<String> response = postInvalidProductData(user, product);

		validateErrorResponse(response);
	}




	@Test
	public void updateProdcutNonExistingIdField() throws JsonProcessingException{
		BaseUserEntity user = empUserRepo.getById(69L);

		JSONObject product = createNewDummyProduct();
		product.put("operation", Operation.UPDATE.getValue());
		product.remove("product_id");

		ResponseEntity<String> response = postInvalidProductData(user, product);

		validateErrorResponse(response);
	}



	private void validateErrorResponse(ResponseEntity<String> response) {
		JSONObject body = new JSONObject(response.getBody());

		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
		assertTrue(body.has("message"));
		assertTrue(body.has("error"));
	}



	private ResponseEntity<String> postInvalidProductData(BaseUserEntity user, JSONObject productJson)
			throws JsonProcessingException {

		HttpEntity<?> request =  getHttpEntity(productJson.toString() , user.getAuthenticationToken());

		return template
				.exchange("/product/info"
					, POST
					, request
					, String.class);
	}






	@Test
	public void deleteProductTest() throws JsonParseException, JsonMappingException, IOException {
		BaseUserEntity user = empUserRepo.getById(69L);
		Long productId = 1008L;

		assertTrue(productRepository.existsById(productId)); //assert product exists before delete
		assertNotEquals("product had images", 0, imgRepo.findByProductEntity_Id(productId).size());

		ResponseEntity<String> response = deleteProduct(user, productId);

		assertExpectedResponse(productId, response);
		assertFalse(productRepository.existsById(productId));

		assertProductImagesNotDeleted(productId);
	}


	@Test
	public void deleteMultipleProductsTest() throws JsonParseException, JsonMappingException, IOException {
		BaseUserEntity user = empUserRepo.getById(69L);
		List<Long> productIds = new ArrayList<>();
		productIds.add(1008L);
		productIds.add(1007L);

		ResponseEntity<String> response = deleteMultipleProducts(user, productIds);

		assertEquals(200, response.getStatusCodeValue());
	}




	@Test
	public void deleteNonExistingProductTest() throws JsonParseException, JsonMappingException, IOException {
		BaseUserEntity user = empUserRepo.getById(69L);

		Long productId = 77771008L;

		assertFalse(productRepository.existsById(productId)); //assert product doesn't exists before delete

		ResponseEntity<String> response = deleteProduct(user, productId);

		assertExpectedResponse(productId, response);
	}








	@Test
	public void softDeleteProductTest() throws JsonParseException, JsonMappingException, IOException {
		BaseUserEntity user = empUserRepo.getById(69L);

		Long productId = 1008L;
		List<Long> variantIds = getVariantsIdList(productId);

		preDeleteAssertions(productId, variantIds);
		//---------------------------------------------------------------------------------------
		ResponseEntity<String> response = deleteProduct(user, productId);

		//---------------------------------------------------------------------------------------
		postDeleteAssertions(productId, variantIds, response);
	}






	@Test
	public void softDeleteBundleTest() throws JsonParseException, JsonMappingException, IOException {
		BaseUserEntity user = empUserRepo.getById(69L);

		Long productId = 1010L;
		List<Long> variantIds = getVariantsIdList(productId);

		preDeleteAssertions(productId, variantIds);
		//---------------------------------------------------------------------------------------
		ResponseEntity<String> response = deleteBundle(user, productId);

		//---------------------------------------------------------------------------------------
		postDeleteAssertions(productId, variantIds, response);
	}






	private ResponseEntity<String> deleteBundle(BaseUserEntity user, Long bundleId) {
		HttpEntity<?> request =  TestCommons.getHttpEntity("" ,user.getAuthenticationToken());
		ResponseEntity<String> response =
				template.exchange("/product/bundle?product_id=" + bundleId
						, DELETE
						, request
						, String.class);
		return response;
	}





	private void preDeleteAssertions(Long productId, List<Long> variantIds) {
		assertProductExists(productId);
		assertProductImagesExists(productId);
		assertVariantsExists(variantIds);
	}




	private void assertProductImagesExists(Long productId) {
		assertNotEquals("assert product had images before delete", 0, imgRepo.findByProductEntity_Id(productId).size());
	}




	private void assertProductExists(Long productId) {
		assertFalse( productHasRemovedFlag(productId) );
		assertTrue(productRepository.existsById(productId)); //assert product exists before delete
	}




	private void assertVariantsExists(List<Long> variantIds) {
		boolean allNotRemoved = variantIds.stream().allMatch(id -> !variantsHasRemovedFlag(id));
		assertNotEquals(0L, variantIds.stream().count());
		assertTrue(allNotRemoved);
	}




	private void postDeleteAssertions(Long productId, List<Long> variantIds, ResponseEntity<String> response)
			throws IOException, JsonParseException, JsonMappingException {
		assertExpectedResponse(productId, response);

		assertProductEntityIgnoredByHibernate(productId);

		assertProductHasRemovedFlag(productId);

		assertVariantsHaveRemovedFlags(variantIds);

		assertProductImagesNotDeleted(productId);
	}




	private void assertProductImagesNotDeleted(Long productId) {
		Long imgsCount = jdbc.queryForObject("select count(*) from product_images where product_id = "+ productId, Long.class);
		assertNotEquals("product images still exists", 0L, imgsCount.longValue());
	}




	private void assertExpectedResponse(Long productId, ResponseEntity<String> response)
			throws IOException, JsonParseException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		ProductsDeleteResponse body = mapper.readValue(response.getBody(), ProductsDeleteResponse.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(body.isSuccess());
		assertEquals(productId, body.getProductIds().get(0));
	}




	private void assertVariantsHaveRemovedFlags(List<Long> variantIds) {
		boolean allRemoved = variantIds.stream().allMatch(this::variantsHasRemovedFlag);
		assertTrue(allRemoved);
	}




	private void assertProductHasRemovedFlag(Long productId) {
		assertEquals(1L , countProductWithId(productId).longValue());
		assertTrue( productHasRemovedFlag(productId) );
	}




	private void assertProductEntityIgnoredByHibernate(Long productId) {
		assertFalse(productRepository.existsById(productId));
		assertFalse(productRepository.findById(productId).isPresent());
		Boolean exists = isProductReturnedByFindAll(productId);
		assertFalse(exists);
	}




	private List<Long> getVariantsIdList(Long productId) {
		List<Long> variantIds = variantRepo.findByProductEntity_Id(productId)
										.stream()
										.map(ProductVariantsEntity::getId)
										.collect(toList());
		return variantIds;
	}




	private Boolean isProductReturnedByFindAll(Long productId) {
		List<ProductEntity> products = (List<ProductEntity>) productRepository.findAll();
		Boolean exists = products.stream()
								.map(ProductEntity::getId)
								.anyMatch(id -> Objects.equals(id, productId));
		return exists;
	}





	private Boolean productHasRemovedFlag(Long productId) {
		Integer removed = jdbc.queryForObject("select removed from public.products where id = " + productId, Integer.class);
		return removed != 0;
	}




	private Boolean variantsHasRemovedFlag(Long variantId) {
		Integer removed = jdbc.queryForObject("select removed from public.product_variants where id = " + variantId, Integer.class);
		return removed != 0;
	}




	private Long countProductWithId(Long productId) {
		return jdbc.queryForObject("select count(*) from public.products where id = " + productId, Long.class);
	}



	@Test
	public void deleteProductInvalidUserRoleTest() {
		BaseUserEntity user = empUserRepo.getById(68L); //this user has NASNAV_ADMIN Role

		Long productId = 1006L;

		assertTrue(productRepository.existsById(productId)); //assert product exists before delete

		ResponseEntity<String> response = deleteProduct(user, productId);

		JSONObject body = new JSONObject(response.getBody());

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		assertFalse(body.getBoolean("success"));
	}



	@Test
	public void deleteProductInvalidUserOrgTest() {
		EmployeeUserEntity user = empUserRepo.getById(69L); //set another organization for the user
		user.setOrganizationId(99001L);
		empUserRepo.save(user);

		Long productId = 1006L;

		assertTrue(productRepository.existsById(productId)); //assert product exists before delete

		ResponseEntity<String> response = deleteProduct(user, productId);

		assertEquals(FORBIDDEN, response.getStatusCode());
	}



	@Test
	public void deleteProductNonExistingUserTest() {

		Long productId = 1006L;

		assertTrue(productRepository.existsById(productId)); //assert product exists before delete

		HttpEntity<?> request =  TestCommons.getHttpEntity("" ,"InvalidToken");

		ResponseEntity<String> response =
				template.exchange("/product?product_id=" + productId
						, DELETE
						, request
						, String.class);

		JSONObject body = new JSONObject(response.getBody());

		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertFalse(body.getBoolean("success"));
	}


	@Test
	public void deleteProductHasStocksTest() throws JsonParseException, JsonMappingException, IOException {
		BaseUserEntity user = empUserRepo.getById(69L);

		Long productId = 1006L;
		List<StocksEntity> stocks = stockRepo.findByProductIdAndShopsId(productId, 502L);

		assertNotEquals("assert product had stocks", 0L, stocks.size());
		assertTrue("assert product exists before delete", productRepository.existsById(productId));
		//---------------------------------------------------------------------
		ResponseEntity<String> response = deleteProduct(user, productId);

		//---------------------------------------------------------------------
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertFalse("assert product was soft deleted", productRepository.existsById(productId));
		assertNotEquals("assert stocks were not deleted", 0L, stocks.size());
	}




	@Test
	public void deleteProductInConfirmedOrdersTest() throws JsonParseException, JsonMappingException, IOException {
		BaseUserEntity user = empUserRepo.getById(69L);

		Long productId = 1014L;
		long basketItemsCountBefore = basketRepo.countByProductIdAndOrderEntity_status(productId, 1);

		assertNotEquals("assert product had confirmed order items", 0L, basketItemsCountBefore);
		assertTrue("assert product exists before delete", productRepository.existsById(productId));
		//---------------------------------------------------------------------
		ResponseEntity<String> response = deleteProduct(user, productId);

		//---------------------------------------------------------------------
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertFalse("assert product was soft deleted", productRepository.existsById(productId));

		long basketItemsCountAfter = basketRepo.countByProductIdAndOrderEntity_status(productId, 1);
		assertNotEquals("assert product item where not deleted", 0L, basketItemsCountAfter);
	}





	@Test
	public void deleteProductInBundleTest() throws JsonParseException, JsonMappingException, IOException {
		BaseUserEntity user = empUserRepo.getById(69L);

		Long productId = 1003L;

		assertTrue("assert product exists before delete", productRepository.existsById(productId));
		//---------------------------------------------------------------------
		ResponseEntity<String> response = deleteProduct(user, productId);

		//---------------------------------------------------------------------
		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
		assertTrue("assert product was NOT deleted", productRepository.existsById(productId));
	}







	@Test
	public void deleteProductInRemovedBundleTest() throws JsonParseException, JsonMappingException, IOException {
		BaseUserEntity user = empUserRepo.getById(69L);

		Long productId = 1012L;

		assertTrue("assert product exists before delete", productRepository.existsById(productId));
		//---------------------------------------------------------------------
		ResponseEntity<String> response = deleteProduct(user, productId);

		//---------------------------------------------------------------------
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertFalse("assert product was deleted", productRepository.existsById(productId));
	}




	private ResponseEntity<String> deleteMultipleProducts(BaseUserEntity user, List<Long> productIds) {
		HttpEntity<?> request =  getHttpEntity("" , user.getAuthenticationToken());

		String queryParams = "";
		for(Long id : productIds) {
			queryParams += "&product_id="+id;
		}

		ResponseEntity<String> response =
				template.exchange("/product?"+ queryParams.substring(1)
						, DELETE
						, request
						, String.class);
		return response;
	}

	private ResponseEntity<String> deleteProduct(BaseUserEntity user, Long productId) {
		HttpEntity<?> request =  getHttpEntity(user.getAuthenticationToken());

		ResponseEntity<String> response =
				template.exchange("/product?product_id=" + productId
						, DELETE
						, request
						, String.class);
		return response;
	}
	
	
	
	//tests that hibernate is actually using GenerationType.IDENTITY strategy for generating product 
	//id's. But the test recreates HIBERNATE_SEQUENCE and set its start value to 1.
	//so, if we later used HIBERNATE_SEQUENCE, it can be dangerous if it ran by accident on 
	//the production database.
	//The SQL script for re-creating HIBERNATE_SEQUENCE will be commented as well.
	
	//@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Products_Test_Data_Insert_3.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void testIdGeneration() {
		
		Long maxProductId = jdbc.queryForObject("select max(id) from public.products", Long.class); 
		
		ProductEntity newProduct = new ProductEntity();
		newProduct.setName("new Product");
		newProduct.setBrandId(101L);
		newProduct.setOrganizationId(99001L);
		
		newProduct = productRepository.save(newProduct);
		
		assertTrue("24 products were inserted directly into the database, with their id's assigned from a sequence."
				+ "Hibernate sequence was set to 1, so if hibernate uses it instead and generated the id, it will be "
				+ "less than max PRODUCTS.ID "
				, newProduct.getId() > maxProductId);
	}


	
	
	
	@Test
	public void testGetProductsByDifferentFilters() {
		/* try to get products by different filters */

		// get by org_id only
		ProductSearchParam param = new ProductSearchParam();
		param.org_id = 99001L;

		ResponseEntity<ProductsResponse> response = template.getForEntity("/navbox/products?"+param.toString(), ProductsResponse.class);
		JSONObject res = new JSONObject(response.getBody());
		Assert.assertEquals(3, res.getInt("total"));

		// filter by name
		param.name = "product_1";
		response = template.getForEntity("/navbox/products?"+param.toString(), ProductsResponse.class);
		res = new JSONObject(response.getBody());
		Assert.assertEquals(1, res.getInt("total"));

		// filter by brand_id
		param.name = null;
		param.brand_id = 101L;
		response = template.getForEntity("/navbox/products?"+param.toString(), ProductsResponse.class);
		res = new JSONObject(response.getBody());
		Assert.assertEquals(1, res.getInt("total"));

		// filter by tag
		param.brand_id = null;
		param.tags = setOf(5001L);
		response = template.getForEntity("/navbox/products?"+param.toString(), ProductsResponse.class);
		res = new JSONObject(response.getBody());
		Assert.assertEquals(2, res.getInt("total"));

		// filter by start
		param.tags = null;
		param.start = 2;
		response = template.getForEntity("/navbox/products?"+param.toString(), ProductsResponse.class);
		res = new JSONObject(response.getBody());
		Assert.assertEquals(3, res.getInt("total"));
		Assert.assertEquals(1, res.getJSONArray("products").length());

		// filter by count
		param.start = null;
		param.count = 1;
		response = template.getForEntity("/navbox/products?"+param.toString(), ProductsResponse.class);
		res = new JSONObject(response.getBody());
		Assert.assertEquals(3, res.getInt("total"));
		Assert.assertEquals(1, res.getJSONArray("products").length());
	}

    @Test
    public void testGetProductsSortingFilters() {
        ProductSearchParam param = new ProductSearchParam();
        param.shop_id = 502L;

        // sorting by id ASC
        param.sort = ProductSortOptions.ID;
        param.setOrder("ASC");
        ResponseEntity<ProductsResponse> response = template.getForEntity("/navbox/products?"+param.toString(), ProductsResponse.class);
        ProductsResponse productsRes = response.getBody();

        List<ProductRepresentationObject> productRepObj = productsRes.getProducts();
        for(int i=0;i<productRepObj.size()-1;i++)
            Assert.assertTrue(productRepObj.get(i).getId() < productRepObj.get(i+1).getId());

        // sorting by name ASC
        param.sort = ProductSortOptions.NAME;
        response = template.getForEntity("/navbox/products?"+param.toString(), ProductsResponse.class);
        productsRes = response.getBody();

        productRepObj = productsRes.getProducts();
        for(int i=0;i<productRepObj.size()-1;i++)
            Assert.assertTrue(productRepObj.get(i).getName().compareTo(productRepObj.get(i+1).getName()) <= 0);

		// sorting by p_name ASC
		param.sort = ProductSortOptions.P_NAME;
		response = template.getForEntity("/navbox/products?"+param.toString(), ProductsResponse.class);
		productsRes = response.getBody();

		productRepObj = productsRes.getProducts();
		for(int i=0;i<productRepObj.size()-1;i++)
			if (productRepObj.get(i).getPname() != null && productRepObj.get(i+1).getPname() != null)
				Assert.assertTrue(productRepObj.get(i).getPname().compareTo(productRepObj.get(i+1).getPname()) <= 0);


		// sorting by price ASC
		param.sort = ProductSortOptions.PRICE;
		response = template.getForEntity("/navbox/products?"+param.toString(), ProductsResponse.class);
		productsRes = response.getBody();

		productRepObj = productsRes.getProducts();
		for(int i=0;i<productRepObj.size()-1;i++)
			Assert.assertTrue(productRepObj.get(i).getPrice().compareTo(productRepObj.get(i+1).getPrice()) <= 0);




		param.sort = ProductSortOptions.ID;
		param.setOrder("DESC");
		response = template.getForEntity("/navbox/products?"+param.toString(), ProductsResponse.class);
		productsRes = response.getBody();

		productRepObj = productsRes.getProducts();
		for(int i=0;i<productRepObj.size()-1;i++)
			Assert.assertTrue(productRepObj.get(i).getId() > productRepObj.get(i+1).getId());

		// sorting by name ASC
		param.sort = ProductSortOptions.NAME;
		response = template.getForEntity("/navbox/products?"+param.toString(), ProductsResponse.class);
		productsRes = response.getBody();

		productRepObj = productsRes.getProducts();
		for(int i=0;i<productRepObj.size()-1;i++)
			Assert.assertTrue(productRepObj.get(i).getName().compareTo(productRepObj.get(i+1).getName()) >= 0);

		// sorting by p_name ASC
		param.sort = ProductSortOptions.P_NAME;
		response = template.getForEntity("/navbox/products?"+param.toString(), ProductsResponse.class);
		productsRes = response.getBody();

		productRepObj = productsRes.getProducts();
		for(int i=0;i<productRepObj.size()-1;i++)
			if (productRepObj.get(i).getPname() != null && productRepObj.get(i+1).getPname() != null)
				Assert.assertTrue(productRepObj.get(i).getPname().compareTo(productRepObj.get(i+1).getPname()) >= 0);


		// sorting by price ASC
		param.sort = ProductSortOptions.PRICE;
		response = template.getForEntity("/navbox/products?"+param.toString(), ProductsResponse.class);
		productsRes = response.getBody();

		productRepObj = productsRes.getProducts();
		for(int i=0;i<productRepObj.size()-1;i++)
			Assert.assertTrue(productRepObj.get(i).getPrice().compareTo(productRepObj.get(i+1).getPrice()) >= 0);
    }
    
    
    
    
    
    @Test
    public void updateTagsTest() {
    	//assign two tags to two products
    	BaseUserEntity user = empUserRepo.getById(69L);

    	List<Long> productIds = asList(1013L, 1014L);
    	List<Long> tagsIds = asList(5002L, 5003L);
    	Set<TagsEntity> tags = new HashSet<>(tagsRepo.findByIdIn(tagsIds));
    			
		assertTrue("assert products had no tags assigned for them.", allProductsHaveNoTags(productIds));
		//---------------------------------------------------------------------
		ResponseEntity<String> response = runUpdateTagsRequest(user, productIds, tagsIds);
		
		//---------------------------------------------------------------------
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertFalse("assert products have tags now", allProductsHaveNoTags(productIds));		
		assertTrue("each product should have the 2 tags", allProductsHaveTags(productIds, tags) );
    }



	@Test
	public void testGetProductsFilters() {
		ProductSearchParam param = new ProductSearchParam();
		Set<Long> tagsList = new HashSet<>();
		tagsList.add(5001L);
		param.org_id = 99001L;
		param.tags = tagsList;

		ResponseEntity<ProductsFiltersResponse> response =
				template.getForEntity("/navbox/filters?" + param.toString(), ProductsFiltersResponse.class);

		Assert.assertEquals(200, response.getStatusCodeValue());
		Assert.assertEquals(2, response.getBody().getBrands().size());


		//trying to filter products by nonlinked tags
		tagsList.add(0L); tagsList.add(5002L);
		param.tags = tagsList;
		response = template.getForEntity("/navbox/filters?" + param.toString(), ProductsFiltersResponse.class);
		Assert.assertEquals(200, response.getStatusCodeValue());
		Assert.assertEquals(0, response.getBody().getBrands().size());
	}


	private ResponseEntity<String> runUpdateTagsRequest(BaseUserEntity user, List<Long> productIds,
			List<Long> tagsIds) {
		JSONObject requestJson = 
				json()
				.put("products_ids", productIds)
				.put("tags_ids", tagsIds);
		
		HttpEntity<?> request =  getHttpEntity(requestJson.toString() , user.getAuthenticationToken());
		
		ResponseEntity<String> response = 
				template.exchange("/product/tag"
						, POST
						, request
						, String.class);
		return response;
	}


    
    


	private boolean allProductsHaveTags(List<Long> productIds, Set<TagsEntity> tags) {
		return productRepository
				.findFullDataByIdIn(productIds)
				.stream()
				.map(ProductEntity::getTags)
				.allMatch(t -> Objects.equals(tags, t));
	}




	private boolean allProductsHaveNoTags(List<Long> productIds) {
		return productRepository
				.findFullDataByIdIn(productIds)
				.stream()
				.map(ProductEntity::getTags)
				.allMatch(Set::isEmpty);
	}
	
	
	
	
	@Test
	public void deleteAllProductsNoAuthZTest() {
		HttpEntity<?> request =  getHttpEntity("" , "NO_EXISTING_TOKEN");
		
		ResponseEntity<String> response = 
				template.exchange("/product/all" ,DELETE, request, String.class);
		
		assertEquals(UNAUTHORIZED, response.getStatusCode());
	}
	
	
	
	
	
	
	@Test
	public void deleteAllProductsNoAuthNTest() {
		HttpEntity<?> request =  getHttpEntity("" , USER_TOKEN);
		
		ResponseEntity<String> response = 
				template.exchange("/product/all" ,DELETE, request, String.class);
		
		assertEquals(FORBIDDEN, response.getStatusCode());
	}
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Products_API_Test_Data_Insert_3.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void deleteAllProductsTest() {
		Long org = 99002L;
		Long otherOrg = 99001L;
		
		long productCountOtherOrgBefore = productRepository.countByOrganizationId(otherOrg);
		long variantCountOtherOrgBefore = variantRepo.countByProductEntity_organizationId(otherOrg);
		long bundlesCountOtherOrgBefore = bundleRepo.countByOrganizationId(otherOrg);
		long ordersCountOtherOrgBefore = orderRepo.countByStatusAndOrganizationEntity_id(NEW.getValue(), otherOrg);

		assertTestDataExists(org, productCountOtherOrgBefore, variantCountOtherOrgBefore, bundlesCountOtherOrgBefore,
				ordersCountOtherOrgBefore);
		
		//----------------------------------------------------------
		HttpEntity<?> request =  getHttpEntity("" , "131415");
		
		ResponseEntity<String> response = 
				template.exchange("/product/all?confirmed=true" ,DELETE, request, String.class);
		
		assertEquals(OK, response.getStatusCode());

		//----------------------------------------------------------
		assertDataDeleted(org, otherOrg, productCountOtherOrgBefore, variantCountOtherOrgBefore,
				bundlesCountOtherOrgBefore, ordersCountOtherOrgBefore);
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Products_API_Test_Data_Insert_4.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void productHideApiTest() {
		Long orgId = 99002L;
		long unhiddenProductsBefore = productRepository.countByHideAndOrganizationId(false, orgId);
		assertNotEquals("all products have no images in the test", 0L, unhiddenProductsBefore);
		
		HttpEntity<?> request =  getHttpEntity("" , "131415");
		
		ResponseEntity<String> response = 
				template.exchange("/product/hide?hide=true&product_id=" ,POST, request, String.class);
		
		assertEquals(OK, response.getStatusCode());
		
		long unhiddenProductsAfter = productRepository.countByHideAndOrganizationId(false, orgId);
		long hiddenProductsAfter = productRepository.countByHideAndOrganizationId(true, orgId);
		
		assertEquals("all products have no images in the test", 0L, unhiddenProductsAfter);
		assertEquals("all products with no images will be hidden", unhiddenProductsBefore, hiddenProductsAfter);
	}


	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Products_API_Test_Data_Insert_4.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
	public void productListHideApiTest() {
		Long orgId = 99002L;
		long unhiddenProductsBefore = productRepository.countByHideAndOrganizationId(false, orgId);
		assertEquals(10L, unhiddenProductsBefore);

		HttpEntity<?> request =  getHttpEntity( "131415");

		ResponseEntity<String> response =
				template.exchange("/product/hide?hide=true&product_id=1013&product_id=1014" ,
						POST, request, String.class);

		assertEquals(OK, response.getStatusCode());

		long unhiddenProductsAfter = productRepository.countByHideAndOrganizationId(false, orgId);
		long hiddenProductsAfter = productRepository.countByHideAndOrganizationId(true, orgId);

		assertEquals(8L, unhiddenProductsAfter);
		assertEquals(2L, hiddenProductsAfter);
	}

	


	private void assertDataDeleted(Long org, Long otherOrg, long productCountOtherOrgBefore,
			long variantCountOtherOrgBefore, long bundlesCountOtherOrgBefore, long ordersCountOtherOrgBefore) {
		long productCountAfter = productRepository.countByOrganizationId(org);
		long variantCountAfter = variantRepo.countByProductEntity_organizationId(org);
		long bundlesCountAfter = bundleRepo.countByOrganizationId(org);

		long productCountOtherOrgAfter = productRepository.countByOrganizationId(otherOrg);
		long variantCountOtherOrgAfter = variantRepo.countByProductEntity_organizationId(otherOrg);
		long bundlesCountOtherOrgAfter = bundleRepo.countByOrganizationId(otherOrg);
		long ordersCountOtherOrgAfter = orderRepo.countByStatusAndOrganizationEntity_id(NEW.getValue(), otherOrg);
		
		assertEquals(0L, productCountAfter);
		assertEquals(0L, variantCountAfter);
		assertEquals(0L, bundlesCountAfter);

		assertEquals(productCountOtherOrgBefore, productCountOtherOrgAfter);
		assertEquals(variantCountOtherOrgBefore, variantCountOtherOrgAfter);
		assertEquals(bundlesCountOtherOrgBefore, bundlesCountOtherOrgAfter);
		assertEquals(ordersCountOtherOrgBefore, ordersCountOtherOrgAfter);
	}




	private void assertTestDataExists(Long org, long productCountOtherOrgBefore, long variantCountOtherOrgBefore,
			long bundlesCountOtherOrgBefore, long ordersCountOtherOrgBefore) {
		long productCountBefore = productRepository.countByOrganizationId(org);
		long variantCountBefore = variantRepo.countByProductEntity_organizationId(org);
		long bundlesCountBefore = bundleRepo.countByOrganizationId(org);
		long ordersCountBefore = orderRepo.countByStatusAndOrganizationEntity_id(NEW.getValue(), org);
		
		assertNotEquals(0L, productCountBefore);
		assertNotEquals(0L, variantCountBefore);
		assertNotEquals(0L, bundlesCountBefore);
		assertNotEquals(0L, ordersCountBefore);
		
		assertNotEquals(0L, productCountOtherOrgBefore);
		assertNotEquals(0L, variantCountOtherOrgBefore);
		assertNotEquals(0L, bundlesCountOtherOrgBefore);
		assertNotEquals(0L, ordersCountOtherOrgBefore);
	}
	
	
}
