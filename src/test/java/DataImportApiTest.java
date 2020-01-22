import static com.nasnav.commons.utils.EntityUtils.setOf;
import static com.nasnav.integration.enums.MappingType.PRODUCT_VARIANT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.nasnav.NavBox;
import com.nasnav.dao.IntegrationMappingRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.persistence.IntegrationMappingEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.security.AuthenticationFilter;
import com.nasnav.test.commons.TestCommons;
import com.nasnav.test.helpers.TestHelper;

import lombok.Data;
import net.jcip.annotations.NotThreadSafe;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc
@PropertySource("classpath:database.properties")
@NotThreadSafe 
@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Data_Import_API_Test_Data_Insert.sql"})
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class DataImportApiTest {
	
	private static final long TEST_STOCK_UPDATED = 400003L;


	private static final long TEST_VARIANT_UPDATED = 310003L;


	private static final String URL_UPLOAD_PRODUCTLIST = "/upload/productlist";


	private static final Long TEST_IMPORT_SHOP = 100003L;
	
	private static final Long TEST_UPDATE_SHOP = 100004L;

	@Value("classpath:/files/product__list_upload_variants.csv")
    private Resource csvFileVariants;

	@Value("classpath:/files/product__list_upload.csv")
    private Resource csvFile;
	
	@Value("classpath:/files/product__list_upload_variants_with_variant_id.csv")
	private Resource csvFileVariantsWithVariantId;
	
    @Value("classpath:/files/product__list_upload_variants_with_variant_id_existing_variant.csv")
    private Resource csvFileVariantsWithVariantIdExistingVariant;

	@Value("classpath:/files/product__list_upload_variants_with_external_id.csv")
	private Resource csvFileVariantsWithExternalId;

	@Value("classpath:/files/product__list_upload_variants_with_external_id_barcode.csv")
	private Resource csvFileVariantsWithExternalIdAndBarcode;

	@Value("classpath:/files/product__list_upload_variants_with_external_id_existing_mapping.csv")
	private Resource csvFileVariantsWithExternalIdExistingMapping;

	@Value("classpath:/files/product__list_upload_missing_col.csv")
    private Resource csvFileMissingCol;
	
	
	@Value("classpath:/files/product__list_upload_invalid_data.csv")
    private Resource csvFileInvalidData;
	
	@Value("classpath:/files/product__list_upate.csv")
	private Resource csvFileUpdate;
	
	@Autowired
	private  MockMvc mockMvc;
	
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private ProductVariantsRepository variantRepo;
	
	@Autowired
	private StockRepository stocksRepo;
	
	@Autowired
	private TestHelper helper;

	@Autowired
	private TestRestTemplate template;
	
	@Autowired
	private IntegrationMappingRepository integrationMappingRepo;
	
	
	@Test
    public void uploadProductsCSVNoAuthNTest() throws IOException, Exception {
       
		JSONObject importProperties = createDataImportProperties();
        
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "Not Existing", csvFile, importProperties);
		
        result.andExpect(status().is(401));
    }


	
	
	
	
	@Test
    public void uploadProductsCSVNoAuthZTest() throws IOException, Exception {
       
		JSONObject importProperties = createDataImportProperties();
        
        ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "101112", csvFile, importProperties);
        
        result.andExpect(status().is(403));
    }
	
	
	
	
	@Test
    public void uploadProductsCSVMissingShopIdTest() throws Exception, Throwable {
       
		JSONObject importProperties = createDataImportProperties();
		importProperties.remove("shop_id");
        
        ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFile, importProperties);
        
        result.andExpect(status().is(406));
    }
	
	
	
	
	
	
	@Test
    public void uploadProductsCSVMissingEncodingTest() throws IOException, Exception {
       
		JSONObject importProperties = createDataImportProperties();
		importProperties.remove("encoding");
        
        ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFile, importProperties);
        
        result.andExpect(status().is(406));
    }
	
	
	
	
	
	
	@Test
    public void uploadProductsCSVMissingCurrencyTest() throws IOException, Exception {
       
		JSONObject importProperties = createDataImportProperties();
		importProperties.remove("currency");
		
        
        ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFile, importProperties);
        
        result.andExpect(status().is(406));
    }
	
	
	
	
	
	
	@Test
    public void uploadProductsCSVNonExistingShopIdTest() throws IOException, Exception {
       
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("shop_id", 88865);
        
        ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFile, importProperties);
        
        result.andExpect(status().is(406));
    }
	
	
	
	
	
	
	@Test
    public void uploadProductsCSVUserFromAnotherOrgTest() throws IOException, Exception {
       
		JSONObject importProperties = createDataImportProperties();
        
        ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "898dssd", csvFile, importProperties);
        
        result.andExpect(status().is(406));
    }
	
	
	
	
	
	@Test
    public void uploadProductsCSVInvalidEncodingTest() throws IOException, Exception {
       
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("encoding", "KOKI-8");
		
        
        ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFile, importProperties);
        
        result.andExpect(status().is(406));
    }
	
	
	
	
	
	@Test
    public void uploadProductsCSVInvalidCurrencyTest() throws IOException, Exception {
       
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("currency", 9999);
		
        
        ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFile, importProperties);
        
        result.andExpect(status().is(406));
    }
	
	
	
	
	
	
	@Test
    public void uploadProductsCSVNoCsvUploadedTest() throws IOException, Exception {
       
		JSONObject importProperties = createDataImportProperties();
		
		MockMultipartFile emptyFilePart = new MockMultipartFile("csv", "nothing.empty", "text/csv", new byte[0]);
        
        ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", emptyFilePart, importProperties);
        
        result.andExpect(status().is(406));
    }
	
	
	
	
	@Test
    public void uploadProductsCSVMissingBarcodeHeadersTest() throws IOException, Exception {
       
		JSONObject importProperties = createDataImportProperties();
		importProperties.getJSONObject("headers").remove("barcode_header");
		
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFile, importProperties);
        
        result.andExpect(status().is(406));
    }
	
	
	
	@Test
    public void uploadProductsCSVMissingPriceHeadersTest() throws IOException, Exception {
       
		JSONObject importProperties = createDataImportProperties();
		importProperties.getJSONObject("headers").remove("price_header");
		
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFile, importProperties);
        
        result.andExpect(status().is(406));
    }

	
	
	
	@Test
    public void uploadProductsCSVMissingQuantityHeadersTest() throws IOException, Exception {
       
		JSONObject importProperties = createDataImportProperties();
		importProperties.getJSONObject("headers").remove("quantity_header");
		
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFile, importProperties);
        
        result.andExpect(status().is(406));
    }
	

	
	
	@Test
    public void uploadProductsCSVMissingNameHeadersTest() throws IOException, Exception {
       
		JSONObject importProperties = createDataImportProperties();
		importProperties.getJSONObject("headers").remove("name_header");
		
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFile, importProperties);
        
        result.andExpect(status().is(406));
    }




	/*
	@Test
    public void uploadProductsCSVMissingCategoryHeadersTest() throws IOException, Exception {

		JSONObject importProperties = createDataImportProperties();
		importProperties.getJSONObject("headers").remove("category_header");

		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFile, importProperties);
        
        result.andExpect(status().is(406));
    }*/
	
	
	
	
	
	@Test
    public void uploadProductsCSVMissingCol() throws IOException, Exception {
       
		JSONObject importProperties = createDataImportProperties();
        
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFileMissingCol, importProperties);
        
        result.andExpect(status().is(406));
    }
	
	
	
	
	
	@Test
    public void uploadProductsCSVInvalidData() throws IOException, Exception {
       
		JSONObject importProperties = createDataImportProperties();
        
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFileInvalidData, importProperties);
        
        String responsebody = result.andExpect(status().is(406))
						        	  .andReturn()
						        	  .getResponse()
						        	  .getContentAsString();
        
        JSONObject bodyJson = new JSONObject(responsebody);
        assertTrue(bodyJson.has("error"));    
        JSONArray errors = new JSONArray(bodyJson.getString("error"));
        assertEquals(3, errors.length());
    }
	
	
	
	
	
	@Test
	public void uploadProductCSVNewDataTest() throws IOException, Exception {
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);
        
		ProductDataCount before = countProductData();	
		
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFile, importProperties);
		
		result.andExpect(status().is(200));
		
		ProductDataCount after = countProductData();		
		assertExpectedRowNumInserted(before, after, 2);
        

        ExpectedSavedData expected = getExpectedAllNewData();
        assertProductDataImported(TEST_IMPORT_SHOP, expected);
       
	}
	
	
	
	
	
	@Test
	public void uploadProductCSVNewDataVariantsTest() throws IOException, Exception {
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);
        
		ProductDataCount before = countProductData();	
		
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileVariants, importProperties);
		
		result.andExpect(status().is(200));
		
		ProductDataCount after = countProductData();
		assertEquals(2, after.product - before.product);
        assertEquals(4, after.variant - before.variant);
        assertEquals(4, after.stocks  - before.stocks);
        

        ExpectedSavedData expected = getExpectedAllNewVariantsData();
        assertProductDataImported(TEST_IMPORT_SHOP, expected);
       
	}






	private void assertExpectedRowNumInserted(ProductDataCount before, ProductDataCount after, int expectedDiff) {
		assertEquals(expectedDiff, after.product - before.product);
        assertEquals(expectedDiff, after.variant - before.variant);
        assertEquals(expectedDiff, after.stocks  - before.stocks);
	}






	private ProductDataCount countProductData() {
		ProductDataCount count = new ProductDataCount();
		count.product = productRepo.count();
		count.variant = variantRepo.count();
		count.stocks = stocksRepo.count();
		return count;
	}
	
	
	
	
	@Test
	public void uploadProductCSVNewDataDryRunTest() throws Exception {
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);
		importProperties.put("dryrun", true);
        
		ProductDataCount before = countProductData();		
		
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFile, importProperties);
		
		result.andExpect(status().is(200));
		
		ProductDataCount after = countProductData();		
		assertExpectedRowNumInserted(before, after, 0);
	}

	
	
	
	@Test
	public void uploadProductCSVUpdateStockDisabledTest() throws Exception {
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("update_product", true);
		importProperties.put("update_stock", false);
        
		ProductDataCount before = countProductData();		
		
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "edddre2", csvFileUpdate, importProperties);
		
		result.andExpect(status().is(200));
		
		ProductDataCount after = countProductData();		
		assertExpectedRowNumInserted(before, after, 1);        

        
        ExpectedSavedData expected = getExpectedNewAndUpdatedDataWithoutStocks();
        assertProductDataImported(TEST_UPDATE_SHOP, expected);
        assertProductUpdatedDataSavedWithoutStock();     
	}



	
	
	
	
	@Test
	public void uploadProductCSVUpdateProductsDisabledTest() throws Exception {
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("update_product", false);
		importProperties.put("update_stocks", true);
        
		ProductDataCount before = countProductData();	
		
		ProductVariantsEntity variantBefore = helper.getVariantFullData(TEST_VARIANT_UPDATED);
		ProductEntity productBefore = variantBefore.getProductEntity();
		
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "edddre2", csvFileUpdate, importProperties);
		
		result.andExpect(status().is(200));
		
		ProductDataCount after = countProductData();		
		assertExpectedRowNumInserted(before, after, 1);          

        ProductVariantsEntity variantAfter = helper.getVariantFullData(TEST_VARIANT_UPDATED);
        ProductEntity productAFter = variantAfter.getProductEntity();
        
        assertEquals(variantBefore, variantAfter);
        assertEquals(productBefore, productAFter);
        
        ExpectedSavedData expected = getExpectedNewOnlyAndUpdatedStocks();
        assertProductDataImported(TEST_UPDATE_SHOP, expected);            
	}
	
	


	private void assertProductUpdatedDataSavedWithoutStock() {
		ProductVariantsEntity updatedVariant = helper.getVariantFullData(TEST_VARIANT_UPDATED);
        assertTestVariantUpdated(updatedVariant);
        
        ProductEntity updatedProduct = updatedVariant.getProductEntity();
        assertTestProductUpdated(updatedProduct);
        
        StocksEntity updatedStocks = stocksRepo.findByProductVariantsEntity_IdAndShopsEntity_Id(TEST_VARIANT_UPDATED, TEST_UPDATE_SHOP).get();
        assertTestStockRemainedSame(updatedStocks);
	}






	private void assertTestStockRemainedSame(StocksEntity updatedStocks) {
		assertEquals(TEST_STOCK_UPDATED, updatedStocks.getId().longValue());
        assertEquals(30, updatedStocks.getQuantity().intValue());
        assertEquals( 0, updatedStocks.getPrice().compareTo(new BigDecimal("15")) );
	}






	@Test
	public void uploadProductCSVUpdateDataEnabledTest() throws IOException, Exception {
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("update_product", true);
		importProperties.put("update_stocks", true);
        
		ProductDataCount before = countProductData();			
		
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "edddre2", csvFileUpdate, importProperties);
		
		result.andExpect(status().is(200));
		
		ProductDataCount after = countProductData();		
		assertExpectedRowNumInserted(before, after, 1);         

        ExpectedSavedData expected = getExpectedNewAndUpdatedDataWithStocks();
        assertProductDataImported(TEST_UPDATE_SHOP, expected);
        assertProductUpdatedDataSavedWithStock();        
	}


	
	
	@Test
	public void getProductsCsvTemplateInvalidAuthentication() {
		HttpEntity<Object> request = TestCommons.getHttpEntity("","456");
		ResponseEntity<String> res = template.exchange("/upload/productlist/template", HttpMethod.GET, request ,String.class);
		Assert.assertTrue(res.getStatusCodeValue() == 401);

		res = template.exchange("/product/image/bulk/template", HttpMethod.GET, request ,String.class);
		Assert.assertTrue(res.getStatusCodeValue() == 401);
	}

	
	
	
	
	@Test
	public void getProductsCsvTemplateInvalidAuthorization() {
		HttpEntity<Object> request = TestCommons.getHttpEntity("","101112");
		ResponseEntity<String> res = template.exchange("/upload/productlist/template", HttpMethod.GET, request ,String.class);
		Assert.assertTrue(res.getStatusCodeValue() == 403);

		res = template.exchange("/product/image/bulk/template", HttpMethod.GET, request ,String.class);
		Assert.assertTrue(res.getStatusCodeValue() == 403);
	}
	
	
	

	@Test
	public void getProductsCsvTemplate() {
		String[] expectedProductHeaders = {"product_name","barcode","brand","price"
											 ,"quantity","description","color","size"};
		HttpEntity<Object> request = TestCommons.getHttpEntity("","131415");
		ResponseEntity<String> res = template.exchange("/upload/productlist/template", HttpMethod.GET, request ,String.class);
		
		Assert.assertTrue(res.getStatusCodeValue() == 200);
		
		String[] headers = res.getBody()
							.replace(System.lineSeparator(), "")
							.split(",");
		for(int i=0;i<headers.length;i++){
			Assert.assertTrue(headers[i].equals(expectedProductHeaders[i]));
		}

		
	}
	
	
	
	
	@Test
	public void getImageUploadCsvTemplate() {
		String[] expectedImageHeaders = {"variant_id","external_id","barcode","image_file"};
		
		HttpEntity<Object> request = TestCommons.getHttpEntity("","131415");
		ResponseEntity<String> res = template.exchange("/product/image/bulk/template", HttpMethod.GET, request ,String.class);
		
		String[] headers = res.getBody()
								.replace(System.lineSeparator(), "")
								.split(",");
		
		Assert.assertTrue(res.getStatusCodeValue() == 200);
		for(int i=0;i<headers.length;i++){
			Assert.assertTrue(headers[i].equals(expectedImageHeaders[i]));
		}
	}


	@Test
	public void uploadProductCSVExistingVariantIdNoVariantEntity() throws Exception {
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);

		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileVariantsWithVariantId, importProperties);

		result.andExpect(status().is(406));
	}

	@Test
	public void uploadProductCSVExistingVariantIdExistVariantEntity() throws Exception {
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("update_product", true);
		importProperties.put("shop_id", TEST_IMPORT_SHOP);

		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileVariantsWithVariantIdExistingVariant, importProperties);

		result.andExpect(status().is(200));

		ProductEntity product = variantRepo.findById(310001L).get().getProductEntity();
		assertEquals("Squishy shoes", product.getName());
	}


	@Test
	public void uploadProductCSVExistingExternalIdNoVariantEntity() throws Exception {
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);

		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileVariantsWithExternalId, importProperties);

		result.andExpect(status().is(200));

		Optional<IntegrationMappingEntity> mapping = 
				integrationMappingRepo.findByOrganizationIdAndMappingType_typeNameAndRemoteValue(
						99001L
						, PRODUCT_VARIANT.getValue()
						, "5");
		assertTrue(mapping.isPresent());

		Optional<ProductVariantsEntity> product = variantRepo.findById(Long.parseLong(mapping.get().getLocalValue()));
		assertTrue(product.isPresent());
	}

	
	
	
	
	@Test
	public void uploadProductCSVExistingExternalIdExistVariantEntity() throws Exception {
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);
		importProperties.put("update_product", true);

		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileVariantsWithExternalIdExistingMapping, importProperties);

		result.andExpect(status().is(200));

		ProductEntity product = variantRepo.findById(310001L).get().getProductEntity();
		assertEquals("Squishy shoes", product.getName());
	}

	@Test
	public void uploadProductCSVExistingExternalIdAndBarcodeNoVariantEntity() throws Exception {
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);
		importProperties.put("update_product", true);

		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileVariantsWithExternalIdAndBarcode, importProperties);

		result.andExpect(status().is(200));

		ProductEntity product = variantRepo.findById(310001L).get().getProductEntity();
		assertEquals("Squishy shoes", product.getName());

        Optional<IntegrationMappingEntity> mapping = 
        		integrationMappingRepo.findByOrganizationIdAndMappingType_typeNameAndRemoteValue(
        				99001L
        				, PRODUCT_VARIANT.getValue()
        				, "4");
        assertTrue(mapping.isPresent());
	}
	
	
	
	

	private void assertProductUpdatedDataSavedWithStock() {
		ProductVariantsEntity updatedVariant = helper.getVariantFullData(TEST_VARIANT_UPDATED);
        assertTestVariantUpdated(updatedVariant);
        
        ProductEntity updatedProduct = updatedVariant.getProductEntity();
        assertTestProductUpdated(updatedProduct);
        
        StocksEntity updatedStocks = stocksRepo.findByProductVariantsEntity_IdAndShopsEntity_Id(TEST_VARIANT_UPDATED, TEST_UPDATE_SHOP).get();
        assertTestStockUpdated(updatedStocks);
	}






	private void assertTestStockUpdated(StocksEntity updatedStocks) {
		assertEquals(TEST_STOCK_UPDATED, updatedStocks.getId().longValue());
        assertEquals(101, updatedStocks.getQuantity().intValue());
        assertEquals( 0, updatedStocks.getPrice().compareTo(new BigDecimal("10.25")) );
	}






	private void assertTestProductUpdated(ProductEntity updatedProduct) {
		assertEquals("TT232222", updatedProduct.getBarcode());
        assertEquals("Squishy shoes", updatedProduct.getName());
        assertEquals("s_shoe", updatedProduct.getPname());
        assertEquals("squishy", updatedProduct.getDescription());
        assertEquals(101L, updatedProduct.getBrandId().longValue());
	}






	private void assertTestVariantUpdated(ProductVariantsEntity updatedVariant) {
		assertEquals("TT232222", updatedVariant.getBarcode());
        assertEquals("Squishy shoes", updatedVariant.getName());
        assertEquals("s_shoe", updatedVariant.getPname());
        assertEquals("squishy", updatedVariant.getDescription());
	}
	
	
	
	
	private void assertProductDataImported(Long shopId, ExpectedSavedData expected) {
		
		
		List<StocksEntity> stocks = helper.getShopStocksFullData(shopId);
        List<ProductVariantsEntity> variants = stocks.stream()
													.map(StocksEntity::getProductVariantsEntity)
													.collect(Collectors.toList());

        List<ProductEntity> products = variants.stream()
												.map(v -> v.getProductEntity())
												.collect(Collectors.toList()); 
        
        
        assertEquals(expected.getStocksNum().intValue() , stocks.size());
        assertTrue( propertyValuesIn(stocks, StocksEntity::getCurrency, expected.getCurrencies())	);
        
        assertTrue( propertyValuesIn(stocks, StocksEntity::getQuantity, expected.getQuantities())	);
        assertTrue( compareEntityBigDecimalFieldValues(stocks, StocksEntity::getPrice, expected.getPrices())	);

        
                
        
        assertTrue( propertyValuesIn(variants, ProductVariantsEntity::getBarcode, expected.getBarcodes())	);
        assertTrue( propertyValuesIn(variants, ProductVariantsEntity::getName, expected.getProductNames()) );
        assertTrue( propertyValuesIn(variants, ProductVariantsEntity::getPname, expected.getPNames()) );
        assertTrue( propertyValuesIn(variants, ProductVariantsEntity::getDescription, expected.getDescriptions()) );
        assertTrue( jsonValuesIn(variants, ProductVariantsEntity::getFeatureSpec, expected.getFeatureSpecs()) );
        
        
        assertTrue( propertyValuesIn(products, ProductEntity::getName, expected.getProductNames()) );
        assertTrue( propertyValuesIn(products, ProductEntity::getPname, expected.getPNames()) );
        assertTrue( propertyValuesIn(products, ProductEntity::getDescription, expected.getDescriptions()) );
        assertTrue( propertyValuesIn(products, ProductEntity::getBrandId, expected.getBrands()) );
	}
	
	
	
	
	private boolean jsonValuesIn(List<ProductVariantsEntity> variants, Function<ProductVariantsEntity,String> jsonStringGetter, Set<JSONObject> expectedSpecs) {
		return variants.stream()
					.map(jsonStringGetter)
					.map(JSONObject::new)
					.allMatch(json -> expectedSpecs.stream().anyMatch(expected -> expected.similar(json)));
		
	}






	private ExpectedSavedData getExpectedAllNewData() {
		ExpectedSavedData data = new ExpectedSavedData();
		
		data.setQuantities( setOf(101,102) );
		data.setPrices( setOf(new BigDecimal("10.25"), new BigDecimal("88.6")));
		data.setCurrencies(setOf(TransactionCurrency.EGP));
		
		data.setBarcodes( setOf("1354ABN", "87847777EW") );
		data.setProductNames( setOf("Squishy shoes", "hard shoes") );
		data.setPNames(setOf("s_shoe", "h_shoe") );
		data.setDescriptions( setOf("squishy", "too hard") );
		data.setCategories( setOf(201L, 202L) );
		data.setBrands(setOf(101L, 102L) );
		data.setStocksNum(2);
        
		return data;
	}
	
	
	
	
	private ExpectedSavedData getExpectedAllNewVariantsData() {
		ExpectedSavedData data = new ExpectedSavedData();
		
		data.setQuantities( setOf(101,102,45) );
		data.setPrices( setOf(new BigDecimal("10.25"), new BigDecimal("88.6")));
		data.setCurrencies(setOf(TransactionCurrency.EGP));
		
		data.setBarcodes( setOf("1354ABN", "87847777EW", "878466658S", "878849956E") );
		data.setProductNames( setOf("Squishy shoes", "hard shoes") );
		data.setPNames(setOf("s_shoe", "h_shoe") );
		data.setDescriptions( setOf("squishy", "too hard") );
		data.setCategories( setOf(201L, 202L) );
		data.setBrands(setOf(101L, 102L) );
		data.setStocksNum(4);
		data.setFeatureSpecs(  createNewVariantsExpectedFeautreSpec());
		
		return data;
	}





	
	
	
	
	private Set<JSONObject> createNewVariantsExpectedFeautreSpec() {
		Set<JSONObject> specs = new HashSet<>();
		JSONObject spec1 = createFeatureSpec("XXL", "Lettuce Heart");
		JSONObject spec2 = createFeatureSpec("M", "Fo7loqy");
		JSONObject spec3 = createFeatureSpec("L", "Browny");
		JSONObject spec4 = createFeatureSpec("XXXL", "PINKY");
		specs.addAll( Arrays.asList(spec1, spec2, spec3, spec4));
		return specs;
	}






	private ExpectedSavedData getExpectedNewAndUpdatedDataWithStocks() {
		ExpectedSavedData data = new ExpectedSavedData();		

		data.setQuantities( setOf(101,102) );
		data.setPrices( setOf(new BigDecimal("10.25"), new BigDecimal("88.6")));
		data.setCurrencies( setOf(TransactionCurrency.EGP));
		
		data.setBarcodes( setOf("TT232222", "87847777EW") );
		data.setProductNames( setOf("Squishy shoes", "hard shoes") );
		data.setPNames(setOf("s_shoe", "h_shoe") );
		data.setDescriptions( setOf("squishy", "too hard") );
		data.setCategories( setOf(201L, 202L) );
		data.setBrands( setOf(101L, 102L) );
		data.setFeatureSpecs(  createExpectedFeautreSpec());
		data.setStocksNum(2);
		
		return data;
	}
	
	
	
	private ExpectedSavedData getExpectedNewAndUpdatedDataWithoutStocks() {
		ExpectedSavedData data = new ExpectedSavedData();		

		data.setQuantities( setOf(30,102) );
		data.setPrices( setOf(new BigDecimal("15"), new BigDecimal("88.6")));
		data.setCurrencies( setOf(TransactionCurrency.EGP, TransactionCurrency.USD));
		
		data.setBarcodes( setOf("TT232222", "87847777EW") );
		data.setProductNames( setOf("Squishy shoes", "hard shoes") );
		data.setPNames(setOf("s_shoe", "h_shoe") );
		data.setDescriptions( setOf("squishy", "too hard") );
		data.setCategories( setOf(201L, 202L) );
		data.setBrands( setOf(101L, 102L) );
		data.setFeatureSpecs(  createExpectedFeautreSpec());
		data.setStocksNum(2);
        
		return data;
	}
	
	
	
	
	private Set<JSONObject> createExpectedFeautreSpec() {
		Set<JSONObject> specs = new HashSet<>();
		JSONObject spec1 = createFeatureSpec("XXL", "Lettuce Heart");
		JSONObject spec2 = createFeatureSpec("M", "Fo7loqy");
		specs.addAll( Arrays.asList(spec1,spec2));
		return specs;
	}






	private JSONObject createFeatureSpec(String size, String color) {
		JSONObject spec1 = new JSONObject();
		spec1.put("7001", size);
		spec1.put("7002", color);
		return spec1;
	}






	private ExpectedSavedData getExpectedNewOnlyAndUpdatedStocks() {
		ExpectedSavedData data = new ExpectedSavedData();		

		data.setQuantities( setOf(101,102) );
		data.setPrices( setOf(new BigDecimal("10.25"), new BigDecimal("88.6")));
		data.setCurrencies( setOf(TransactionCurrency.EGP));
		
		data.setBarcodes( setOf("TT232222", "87847777EW") );
		data.setProductNames( setOf("Product to update", "hard shoes") );
		data.setPNames(setOf("u_shoe", "h_shoe") );
		data.setDescriptions( setOf("old desc", "too hard") );
		data.setCategories( setOf(201L, 202L) );
		data.setBrands( setOf(101L, 102L) );
		data.setFeatureSpecs(  createNewProductOnlyExpectedFeautreSpec());
		data.setStocksNum(2);
		
		return data;
	}
	
	
	
	
	private Set<JSONObject> createNewProductOnlyExpectedFeautreSpec() {
		Set<JSONObject> specs = new HashSet<>();
		JSONObject spec1 = new JSONObject("{}") ;
		JSONObject spec2 = createFeatureSpec("M", "Fo7loqy");
		specs.addAll( Arrays.asList(spec1,spec2));
		return specs;
	}






	private <T,V>  boolean  propertyValuesIn(List<T> entityList, Function<T,V> getter, Set<V> expectedValues) {
		return entityList.stream()
						.map(getter)
						.collect(Collectors.toSet())
						.equals(expectedValues);
	}
	
	
	private <T,V extends BigDecimal>  boolean  compareEntityBigDecimalFieldValues(List<T> entityList, Function<T, V> getter, Set<V> expectedValues) {
		return entityList.stream()
						.map(getter)
						.allMatch(n -> expectedValues.stream().anyMatch( e-> e.compareTo(n) == 0));
	}
	


	private JSONObject createDataImportProperties() {
		JSONObject colHeadersJson = createCsvHeaderJson();
		
		JSONObject json = new JSONObject();
		json.put("headers", colHeadersJson);
		json.put("dryrun", false);
		json.put("update_product", false);
		json.put("update_stocks", false);
		json.put("shop_id", 100001L);
		json.put("encoding", "UTF-8");
		json.put("currency", 1);
		return json;
	}
	
	
	
	
	

	private ResultActions uploadProductCsv(String url, String token, Resource csv, JSONObject importProperties)
			throws IOException, Exception {
		MockMultipartFile filePart = new MockMultipartFile("csv", csv.getFilename(), "text/csv", csv.getInputStream());
        
     	return uploadProductCsv(url, token, filePart, importProperties);
	}






	private ResultActions uploadProductCsv(String url, String token, MockMultipartFile filePart,
			JSONObject importProperties) throws Exception {
		MockPart jsonPart = new MockPart("properties", "properties",  importProperties.toString().getBytes());		
		jsonPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		
		 ResultActions result = 
		    mockMvc.perform(MockMvcRequestBuilders.multipart(url)
								                 .file(filePart)
								                 .part(jsonPart)
								                 .header(AuthenticationFilter.TOKEN_HEADER, token));
		return result;
	}
	
	
	
	
	

	private JSONObject createCsvHeaderJson() {
		JSONObject colHeadersJson = new JSONObject();
		   colHeadersJson.put("name_header", "Product name");
		   colHeadersJson.put("pname_header", "p_name");
		   colHeadersJson.put("description_header", "description");
		   colHeadersJson.put("barcode_header", "barcode");
		   colHeadersJson.put("category_header", "category");
		   colHeadersJson.put("brand_header", "brand");
		   colHeadersJson.put("quantity_header", "quantity");
		   colHeadersJson.put("price_header", "price");
		   colHeadersJson.put("variant_id_header", "variant_id");
		   colHeadersJson.put("external_id_header", "external_id");
		return colHeadersJson;
	}

}



@Data
class ExpectedSavedData{
	private Set<Integer> quantities;
	private Set<BigDecimal> prices ;
	private Set<String> barcodes ;
	private Set<String> ProductNames;
	private Set<String> PNames;
	private Set<String>  descriptions;
	private Set<Long> categories;
	private Set<Long> brands;
	private Set<TransactionCurrency> currencies;
	private Set<JSONObject> featureSpecs;
	private Integer stocksNum;
	
	public ExpectedSavedData() {
		featureSpecs = new HashSet<>();
		featureSpecs.add(new JSONObject("{}") );
	}
}




class ProductDataCount{
	public Long product;
	public Long variant;
	public Long stocks;
}