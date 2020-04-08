import static com.nasnav.commons.utils.EntityUtils.setOf;
import static com.nasnav.enumerations.TransactionCurrency.EGP;
import static com.nasnav.enumerations.TransactionCurrency.USD;
import static com.nasnav.integration.enums.MappingType.PRODUCT_VARIANT;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.IntegrationMappingRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dao.TagsRepository;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.persistence.IntegrationMappingEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductExtraAttributesEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.persistence.TagsEntity;
import com.nasnav.security.AuthenticationFilter;
import com.nasnav.service.model.ImportProductContext;
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

	@Value("classpath:/files/large_description.txt")
	private Resource largeDescription;

	@Value("classpath:/files/product__list_upload_variants.csv")
    private Resource csvFileVariants;

	@Value("classpath:/files/product__list_upload.csv")
    private Resource csvFile;
	
	@Value("classpath:/files/product__list_upate_move_variant.csv")
    private Resource csvFileMoveVariant;
	
	@Value("classpath:/files/product__list_upload_group_by_key.csv")
    private Resource csvFileGroupByKey;
	
	@Value("classpath:/files/product__list_upload_with_new_tags.csv")
    private Resource csvFileWithNewTags;
	
	@Value("classpath:/files/product__list_upload_missing_features.csv")
    private Resource csvFileMissingFeatures;

	@Value("classpath:/files/product__list_upload_large_column.csv")
	private Resource csvFileLargeColumn;
	
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
	
	
	@Value("classpath:/files/product__list_multitag_upate.csv")
	private Resource csvMulitagProductUpdate;
	
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
	
	@Autowired
	private TagsRepository tagsRepo;
	
	@Autowired
	private BrandsRepository brandsRepo;
	
	
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
    public void uploadProductsCSVMissingCol() throws IOException, Exception {
       
		JSONObject importProperties = createDataImportProperties();
        
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFileMissingCol, importProperties);
        
        result.andExpect(status().is(406));
    }
	
	
	
	
	
	@Test
    public void uploadProductsCSVInvalidData() throws IOException, Exception {
       
		JSONObject importProperties = createDataImportProperties();
        
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFileInvalidData, importProperties);
        
        result.andExpect(status().is(406));
        
        ImportProductContext report = readImportReport(result);
        assertEquals(3, report.getErrors().size());
        assertFalse(report.isSuccess());
    }
	
	
	
	
	
	@Test
	public void uploadProductCSVNewDataTest() throws IOException, Exception {
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);
        
		ExtendedProductDataCount before = countExtendedProductData();	
		
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFile, importProperties);
		
		result.andExpect(status().is(200));
		
		ExtendedProductDataCount after = countExtendedProductData();		
		assertExpectedRowNumInserted(before, after, 2);
		assertEquals(0, after.tags - before.tags);
		assertEquals(0, after.brands - before.brands);

        ExpectedSavedData expected = getExpectedAllNewData();
        assertProductDataImported(TEST_IMPORT_SHOP, expected);
       
        validateImportReportForNewData(result);
	}
	
	
	
	
	
	
	@Test
	public void uploadProductCSVNewDataTestGroupByKey() throws IOException, Exception {
		variantRepo.deleteAll();
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);
        
		ProductDataCount before = countProductData();	
		
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileGroupByKey, importProperties);
		
		result.andExpect(status().is(200));
		
		ProductDataCount after = countProductData();
		
		assertEquals(2L, after.product - before.product);
		assertEquals(3L, after.variant - before.variant);
		assertEquals(3L, after.stocks - before.stocks);

        ExpectedSavedData expected = getExpectedNewDataGroupedByKey();
        assertProductDataImported(TEST_IMPORT_SHOP, expected);
        
        List<ProductVariantsEntity> variants = variantRepo.findByOrganizationId(99001L);
        assertEquals(3, variants.size());
        Map<Long, List<ProductVariantsEntity>> groupedByProduct = 
        		variants
        		.stream()
        		.collect(groupingBy(var -> var.getProductEntity().getId()));
        assertEquals(2, groupedByProduct.keySet().size());
        
        Set<Set<String>> variantsBarcodes = 
        		groupedByProduct
        		.values()
        		.stream()
        		.map(varList -> varList.stream().map(ProductVariantsEntity::getBarcode).collect(toSet()))
        		.collect(toSet());
        assertEquals( variantsBarcodes
        		 , setOf( setOf("111222A"), setOf("1354ABN", "87847777EW")));
       
        validateImportReportForNewData(result);
	}
	
	
	
	
	
	@Test
	public void uploadProductInvalidCSV() throws IOException, Exception {
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);
        
		ExtendedProductDataCount before = countExtendedProductData();	
		
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFile, importProperties);
		
		result.andExpect(status().is(200));
		
		ExtendedProductDataCount after = countExtendedProductData();		
		assertExpectedRowNumInserted(before, after, 2);
		assertEquals(0, after.tags - before.tags);
		assertEquals(0, after.brands - before.brands);

        ExpectedSavedData expected = getExpectedAllNewData();
        assertProductDataImported(TEST_IMPORT_SHOP, expected);
       
        validateImportReportForNewData(result);
	}






	private void validateImportReportForNewData(ResultActions result)
			throws UnsupportedEncodingException, IOException, JsonParseException, JsonMappingException {
		ImportProductContext report = readImportReport(result);
        assertEquals(2, report.getCreatedProducts().size());
        assertTrue(report.getUpdatedProducts().isEmpty());
        assertTrue(report.getCreatedBrands().isEmpty());
        assertTrue(report.getCreatedTags().isEmpty());
        assertTrue(report.isSuccess());
        assertTrue(report.getErrors().isEmpty());
	}
	
	
	
	
	
	@Test
	public void uploadProductCSVNewDataWithMissingFeaturesTest() throws IOException, Exception {
        JSONObject importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);
		importProperties.put("update_product", true);

		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileMissingFeatures, importProperties);

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
	
	
	
	private ExtendedProductDataCount countExtendedProductData() {
		ProductDataCount count = countProductData();
		ExtendedProductDataCount extendedCount = new ExtendedProductDataCount(count);
		extendedCount.tags = tagsRepo.count();
		extendedCount.brands = brandsRepo.count();
		return extendedCount;
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
		
		validateImportReportForDryRun(result);
	}






	private void validateImportReportForDryRun(ResultActions result)
			throws UnsupportedEncodingException, IOException, JsonParseException, JsonMappingException {
		ImportProductContext report = readImportReport(result);
		assertTrue(report.getCreatedProducts().isEmpty());
		assertTrue(report.getUpdatedProducts().isEmpty());
		assertTrue(report.getCreatedTags().isEmpty());
		assertTrue(report.getCreatedBrands().isEmpty());
		assertEquals(2, report.getProductsNum().intValue());
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
	
	
	
	
	@Test
	public void uploadProductCSVUpdateBothProductsAndStockDisabledTest() throws Exception {
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("update_product", false);
		importProperties.put("update_stocks", false);
        
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
        
        ExpectedSavedData expected = getExpectedNewOnlyWithoutStocks();
        assertProductDataImported(TEST_UPDATE_SHOP, expected);    
        
        validateImportReportWithUpdateDisabled(result);
	}






	private void validateImportReportWithUpdateDisabled(ResultActions result)
			throws UnsupportedEncodingException, IOException, JsonParseException, JsonMappingException {
		ImportProductContext report = readImportReport(result);
        assertEquals(1, report.getCreatedProducts().size());
        assertTrue(report.getUpdatedProducts().isEmpty());
        assertTrue(report.getCreatedTags().isEmpty());
        assertTrue(report.getCreatedBrands().isEmpty());
	}



	@Test
	public void uploadProductCSVLargeColumnTest() throws IOException, Exception {
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);

		ProductDataCount before = countProductData();

		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileLargeColumn, importProperties);

		result.andExpect(status().is(200));

		ProductDataCount after = countProductData();
		assertExpectedRowNumInserted(before, after, 2);


		ExpectedSavedData expected = getExpectedAllNewData();
		expected.setDescriptions(setOf(TestCommons.readResource(largeDescription), "too hard"));
		expected.setVariantDescriptions(setOf(TestCommons.readResource(largeDescription), "too hard"));
		assertProductDataImported(TEST_IMPORT_SHOP, expected);

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
        
        ImportProductContext report = readImportReport(result);
        assertEquals(1, report.getCreatedProducts().size());
        assertEquals(1, report.getUpdatedProducts().size());
        assertTrue(report.getErrors().isEmpty());
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=ExecutionPhase.BEFORE_TEST_METHOD,  scripts={"/sql/Data_Import_API_Test_Data_Insert_2.sql"})
	@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void uploadProductCSVUpdateMoveVariantToOtherProductTest() throws IOException, Exception {
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("update_product", true);
		importProperties.put("update_stocks", true);
        
		ProductDataCount before = countProductData();			
		
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "edddre2", csvFileMoveVariant, importProperties);
		
		result.andExpect(status().is(200));
		
		ProductDataCount after = countProductData();		
//		assertExpectedRowNumInserted(before, after, 1);         

		
        ExpectedSavedData expected = getExpectedUpdateDataMoveVariants();
        assertProductDataImported(TEST_UPDATE_SHOP, expected);
        assertProductUpdatedDataSavedWithStock();     
        
        ImportProductContext report = readImportReport(result);
        assertEquals(1, report.getCreatedProducts().size());
        assertEquals(1, report.getUpdatedProducts().size());
        assertTrue(report.getErrors().isEmpty());
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
		Set<String> expectedTemplateHeaders = 
				setOf("product_name", "barcode", "tags", "brand", "price", "quantity", "description"
						, "variant_id", "external_id", "color", "size", "product_group_key", "discount");
		HttpEntity<Object> request = getHttpEntity("","131415");
		ResponseEntity<String> res = template.exchange("/upload/productlist/template", GET, request ,String.class);
		
		Assert.assertTrue(res.getStatusCodeValue() == 200);
		
		String[] headersArr = res.getBody()
								.replace(System.lineSeparator(), "")
								.split(",");		
		Set<String> headers = setOf(headersArr);
		
		assertEquals(expectedTemplateHeaders, headers);
	}
	
	
	
	
	@Test
	public void getImageUploadCsvTemplate() {
		String[] expectedImageHeaders = {"variant_id","external_id","barcode","image_file"};
		
		HttpEntity<Object> request = getHttpEntity("","131415");
		ResponseEntity<String> res = 
				template
					.exchange(
						"/product/image/bulk/template", GET, request ,String.class);
		
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

		@SuppressWarnings("unused")
		String body = 
				result
				.andExpect(status().is(200))
				.andReturn()
				.getResponse()
				.getContentAsString();

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
	
	
	
	
	
	@Test
	public void uploadProductCSVUpdateProductWithMultipleTagsTest() throws IOException, Exception {
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("update_product", true);
		importProperties.put("update_stocks", true);
        
		ProductDataCount before = countProductData();			
		
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "edddre2", csvMulitagProductUpdate, importProperties);
		
		result.andExpect(status().is(200));
		
		ProductDataCount after = countProductData();		
		assertExpectedRowNumInserted(before, after, 0);         

        ExpectedSavedData expected = getExpectedUpdatedDataForMultipleTags();
        assertProductDataImported(TEST_UPDATE_SHOP, expected);
        assertProductUpdatedDataSavedWithStock();        
	}


	
	
	
	
	@Test
	public void uploadProductCSVWithNewTagsAndBrand() throws IOException, Exception {
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);
        
		ExtendedProductDataCount before = countExtendedProductData();	
		
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileWithNewTags, importProperties);
		
		result.andExpect(status().is(200));
		
		ExtendedProductDataCount after = countExtendedProductData();		
		assertExpectedRowNumInserted(before, after, 2);
		assertEquals(2, after.tags - before.tags);
		assertEquals(2, after.brands - before.brands);
        
		assertNewTagsAndBrandsImported();
		
		validateImportReportForNewCreatedTagsAndBrands(result);
	}






	private void validateImportReportForNewCreatedTagsAndBrands(ResultActions result)
			throws UnsupportedEncodingException, IOException, JsonParseException, JsonMappingException {
		ImportProductContext report = readImportReport(result);
		assertEquals(2, report.getCreatedTags().size());
		assertEquals(2, report.getCreatedBrands().size());
		assertEquals(2, report.getCreatedProducts().size());
		assertEquals(0, report.getUpdatedProducts().size());
		assertEquals(0, report.getErrors().size());
		assertTrue(report.isSuccess());
	}






	private ImportProductContext readImportReport(ResultActions result)
			throws UnsupportedEncodingException, IOException, JsonParseException, JsonMappingException {
		String reportStr = 
				result
				.andReturn()
				.getResponse()
				.getContentAsString();
		
		ObjectMapper mapper = new ObjectMapper();
		ImportProductContext report = mapper.readValue(reportStr, ImportProductContext.class);
		return report;
	}






	private void assertNewTagsAndBrandsImported() {
		Set<String> newTags= setOf("new squish", "new hill equipment");
		Set<String> newBrands = setOf("new brand","shiny new brand");
		assertEquals(2, tagsRepo.findByNameInAndOrganizationEntity_Id(newTags, 99001L).size());
        assertEquals(2, brandsRepo.findByNameIn(newBrands).size());
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
        assertEquals("squishy", updatedProduct.getDescription());
        assertEquals(101L, updatedProduct.getBrandId().longValue());
	}






	private void assertTestVariantUpdated(ProductVariantsEntity updatedVariant) {
		assertEquals("TT232222", updatedVariant.getBarcode());
        assertEquals("Squishy shoes", updatedVariant.getName());
        assertEquals("squishy", updatedVariant.getDescription());
	}
	
	
	
	
	private void assertProductDataImported(Long shopId, ExpectedSavedData expected) {
		
		
		List<StocksEntity> stocks = helper.getShopStocksFullData(shopId);
        List<ProductVariantsEntity> variants = stocks.stream()
													.map(StocksEntity::getProductVariantsEntity)
													.collect(toList());

        Set<ProductEntity> products = variants.stream()
												.map(v -> v.getProductEntity())
												.collect(toSet()); 
        
        
        assertEquals(expected.getStocksNum().intValue() , stocks.size());
        assertTrue( propertyValuesIn(stocks, StocksEntity::getCurrency, expected.getCurrencies())	);
        
        assertTrue( propertyValuesIn(stocks, StocksEntity::getQuantity, expected.getQuantities())	);
        assertTrue( compareEntityBigDecimalFieldValues(stocks, StocksEntity::getPrice, expected.getPrices())	);

        
                
        
        assertTrue( propertyValuesIn(variants, ProductVariantsEntity::getBarcode, expected.getBarcodes())	);
        assertTrue( propertyValuesIn(variants, ProductVariantsEntity::getName, expected.getVariantNames()) );
        assertTrue( propertyValuesIn(variants, ProductVariantsEntity::getPname, expected.getVariantsPNames()) );
        assertTrue( propertyValuesIn(variants, ProductVariantsEntity::getDescription, expected.getVariantDescriptions()) );
        assertTrue( jsonValuesIn(variants, ProductVariantsEntity::getFeatureSpec, expected.getFeatureSpecs()) );
        assertTrue( jsonValuesIn(variants, this::getExtraAtrributesStr, expected.getExtraAttributes()) );
        
        
        assertTrue( propertyValuesIn(products, ProductEntity::getName, expected.getProductNames()) );
        assertTrue( propertyValuesIn(products, ProductEntity::getPname, expected.getProductPNames()) );
        assertTrue( propertyValuesIn(products, ProductEntity::getDescription, expected.getDescriptions()) );
        assertTrue( propertyMultiValuesIn(products, this::getTags, expected.getTags()) );
        assertTrue( propertyValuesIn(products, ProductEntity::getBrandId, expected.getBrands()) );
	}
	
	
	
	
	private String getExtraAtrributesStr(ProductVariantsEntity variant){		
		return variant
				.getExtraAttributes()
				.stream()
				.collect(Collector.of(JSONObject::new
							, this::putAttributeIntoJson
							, this::notWorkingMergeJson
							, JSONObject::toString));
	}
	
	
	
	
	private void putAttributeIntoJson( JSONObject json, ProductExtraAttributesEntity attr) {
		json.put(attr.getExtraAttribute().getName(), attr.getValue());
	}
	
	
	
	private JSONObject notWorkingMergeJson(JSONObject json1, JSONObject json2) {
		return json1;
	}
	
	
	private Set<String> getTags(ProductEntity product){
		return product
				.getTags()
				.stream()
				.map(TagsEntity::getName)
				.collect(toSet());
	}
	
	
	
	
	private boolean jsonValuesIn(List<ProductVariantsEntity> variants, Function<ProductVariantsEntity,String> jsonStringGetter, Set<JSONObject> expectedSpecs) {
		return variants
				.stream()
				.map(jsonStringGetter)
				.filter(jsonStr -> jsonStr != null && !jsonStr.equals("{}"))
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
		data.setVariantsPNames(setOf("squishy-shoes", "hard-shoes") );
		data.setProductPNames(setOf("squishy-shoes", "hard-shoes") );
		data.setDescriptions( setOf("squishy", "too hard") );
		data.setTags( setOf("squishy things", "mountain equipment") );
		data.setBrands(setOf(101L, 102L) );
		data.setStocksNum(2);
        
		return data;
	}
	
	
	
	
	private ExpectedSavedData getExpectedNewDataGroupedByKey() {
		ExpectedSavedData data = new ExpectedSavedData();
		
		data.setQuantities( setOf(101,102, 22) );
		data.setPrices( setOf(new BigDecimal("10.25"), new BigDecimal("88.6"), new BigDecimal("15.23")));
		data.setCurrencies(setOf(TransactionCurrency.EGP));
		
		data.setBarcodes( setOf("1354ABN", "87847777EW", "111222A") );
		data.setProductNames( setOf("Squishy shoes") );
		data.setVariantsPNames(setOf("squishy-shoes", "hard-shoes") );
		data.setVariantNames(setOf("Squishy shoes", "hard shoes") );
		data.setProductPNames(setOf("squishy-shoes") );
		data.setDescriptions( setOf("squishy", "Another") );
		data.setVariantDescriptions( setOf("squishy", "Another", "too hard") );
		data.setTags( setOf("squishy things") );
		data.setBrands(setOf(101L) );
		data.setStocksNum(3);
        
		return data;
	}
	
	
	
	
	private ExpectedSavedData getExpectedUpdateDataMoveVariants() {
		ExpectedSavedData data = new ExpectedSavedData();
		
		data.setQuantities( setOf(101,102) );
		data.setPrices( setOf(new BigDecimal("10.25"), new BigDecimal("88.6"), new BigDecimal("8.25")));
		data.setCurrencies(setOf(TransactionCurrency.EGP));
		
		data.setBarcodes( setOf("TT232222", "BB232222", "87847777EW") );
		data.setProductNames( setOf("Squishy shoes") );
		data.setVariantsPNames(setOf("squishy-shoes", "hard-shoes") );
		data.setVariantNames(setOf("Squishy shoes", "hard shoes") );
		data.setProductPNames(setOf("squishy-shoes") );
		data.setDescriptions( setOf("squishy") );
		data.setVariantDescriptions( setOf("squishy", "too hard") );
		data.setTags( setOf("squishy things") );
		data.setBrands(setOf(101L) );
		data.setStocksNum(3);
        
		return data;
	}
	
	
	
	
	private ExpectedSavedData getExpectedAllNewVariantsData() {
		ExpectedSavedData data = new ExpectedSavedData();
		
		data.setQuantities( setOf(101,102,45) );
		data.setPrices( setOf(new BigDecimal("10.25"), new BigDecimal("88.6")));
		data.setCurrencies(setOf(TransactionCurrency.EGP));
		
		data.setBarcodes( setOf("1354ABN", "87847777EW", "878466658S", "878849956E") );
		data.setProductNames( setOf("Squishy shoes", "hard shoes") );
		data.setVariantsPNames(setOf("color-lettuce-heart-size-xxl", "color-fo7loqy-size-m", "color-browny-size-l", "color-pinky-size-xxxl") );
		data.setProductPNames(setOf("squishy-shoes", "hard-shoes") );
		data.setDescriptions( setOf("squishy", "too hard") );
		data.setTags( setOf("squishy things", "mountain equipment") );
		data.setBrands(setOf(101L, 102L) );
		data.setStocksNum(4);
		data.setFeatureSpecs(  createNewVariantsExpectedFeautreSpec());
		data.setExtraAttributes(  createNewVaraintsExpectedExtraAttr());
		
		return data;
	}


	
	
	private Set<JSONObject> createNewVaraintsExpectedExtraAttr() {
		return setOf(
					json()
					 .put("extra", "ext1")
					 .put("not-feature-col", "no")
					,
					json()
					 .put("extra", "ext2")
					 .put("not-feature-col", "ok")
					,
					json()
					 .put("extra", "ext3")
					 .put("not-feature-col", "ok")
					,
					json()
					 .put("extra", "ext4")
					 .put("not-feature-col", "ok")
				);
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
		data.setVariantsPNames(setOf("color-fo7loqy-size-m", "u_shoe") );
		data.setProductPNames(setOf("u_shoe", "hard-shoes") );
		data.setDescriptions( setOf("squishy", "too hard") );
		data.setTags( setOf("squishy things", "mountain equipment") );
		data.setBrands( setOf(101L, 102L) );
		data.setFeatureSpecs(  createExpectedFeautreSpec());
		data.setExtraAttributes(  createExpectedExtraAttr());
		data.setStocksNum(2);
		
		return data;
	}
	
	
	
	
	
	private ExpectedSavedData getExpectedUpdatedDataForMultipleTags() {
		ExpectedSavedData data = new ExpectedSavedData();		

		data.setQuantities( setOf(101) );
		data.setPrices( setOf(new BigDecimal("10.25")));
		data.setCurrencies( setOf(EGP));
		
		data.setBarcodes( setOf("TT232222") );
		data.setProductNames( setOf("Squishy shoes") );
		data.setVariantsPNames(setOf("u_shoe") );
		data.setProductPNames(setOf("u_shoe") );
		data.setDescriptions( setOf("squishy") );
		data.setTags( setOf("squishy things", "mountain equipment") );
		data.setBrands( setOf(101L) );
		data.setFeatureSpecs(  createExpectedFeautreSpecForOnlyUpdatedProduct());
		data.setExtraAttributes(  createExpectedExtraAttrForOnlyUpdatedProduct());
		data.setStocksNum(1);
		
		return data;
	}
	
	
	
	
	private Set<JSONObject> createExpectedExtraAttrForOnlyUpdatedProduct() {
		return setOf(
					json()
					.put("extra", "ext1")
					.put("not-feature-col", "no")
				);
	}
	
	
	
	private ExpectedSavedData getExpectedNewAndUpdatedDataWithoutStocks() {
		ExpectedSavedData data = new ExpectedSavedData();		

		data.setQuantities( setOf(30,102) );
		data.setPrices( setOf(new BigDecimal("15"), new BigDecimal("88.6")));
		data.setCurrencies( setOf(EGP, USD));		
		data.setBarcodes( setOf("TT232222", "87847777EW") );
		data.setProductNames( setOf("Squishy shoes", "hard shoes") );
		data.setVariantsPNames(setOf("u_shoe", "color-fo7loqy-size-m") );
		data.setProductPNames(setOf("u_shoe", "hard-shoes") );
		data.setDescriptions( setOf("squishy", "too hard") );
		data.setTags( setOf("squishy things", "mountain equipment") );
		data.setBrands( setOf(101L, 102L) );
		data.setFeatureSpecs(  createExpectedFeautreSpec());
		data.setExtraAttributes(  createExpectedExtraAttr());
		data.setStocksNum(2);
        
		return data;
	}
	
	
	
	
	private Set<JSONObject> createExpectedExtraAttr() {
		return setOf(
					json()
					 .put("extra", "ext2")
					 .put("not-feature-col", "ok")
					, 
					json()
					 .put("extra", "ext1")
					 .put("not-feature-col", "no")
				);
	}
	
	
	
	
	private Set<JSONObject> createExpectedFeautreSpec() {
		Set<JSONObject> specs = new HashSet<>();
		JSONObject spec1 = createFeatureSpec("XXL", "Lettuce Heart");
		JSONObject spec2 = createFeatureSpec("M", "Fo7loqy");
		specs.addAll( asList(spec1,spec2));
		return specs;
	}
	
	
	
	
	private Set<JSONObject> createExpectedFeautreSpecForOnlyUpdatedProduct() {
		Set<JSONObject> specs = new HashSet<>();
		JSONObject spec1 = createFeatureSpec("XXL", "Lettuce Heart");
		specs.addAll( asList(spec1));
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
		data.setVariantsPNames(setOf("color-fo7loqy-size-m", "u_shoe") );
		data.setProductPNames(setOf( "u_shoe", "hard-shoes") );
		data.setDescriptions( setOf("old desc", "too hard") );
		data.setTags( setOf("mountain equipment") );
		data.setBrands( setOf(101L, 102L) );
		data.setFeatureSpecs(  createNewProductOnlyExpectedFeautreSpec());
		data.setExtraAttributes(  createNewProductOnlyExpectedExtraAttr());
		data.setStocksNum(2);
		
		return data;
	}

	
	
	
	
	private ExpectedSavedData getExpectedNewOnlyWithoutStocks() {
		ExpectedSavedData data = new ExpectedSavedData();		

		data.setQuantities( setOf(30,102) );
		data.setPrices( setOf(new BigDecimal("15"), new BigDecimal("88.6")));
		data.setCurrencies( setOf(EGP, USD));
		
		data.setBarcodes( setOf("TT232222", "87847777EW") );
		data.setProductNames( setOf("Product to update", "hard shoes") );
		data.setVariantsPNames(setOf("color-fo7loqy-size-m", "u_shoe") );
		data.setProductPNames(setOf( "u_shoe", "hard-shoes") );
		data.setDescriptions( setOf("old desc", "too hard") );
		data.setTags( setOf("mountain equipment") );
		data.setBrands( setOf(101L, 102L) );
		data.setFeatureSpecs(  createNewProductOnlyExpectedFeautreSpec());
		data.setExtraAttributes(  createNewProductOnlyExpectedExtraAttr());
		data.setStocksNum(2);
		
		return data;
	}

	
	
	
	private Set<JSONObject> createNewProductOnlyExpectedExtraAttr() {
		return setOf(
					json()
					.put("extra", "ext2")
					.put("not-feature-col", "ok")
				);
	}






	private Set<JSONObject> createNewProductOnlyExpectedFeautreSpec() {
		Set<JSONObject> specs = new HashSet<>();
		JSONObject spec1 = new JSONObject("{}") ;
		JSONObject spec2 = createFeatureSpec("M", "Fo7loqy");
		specs.addAll( Arrays.asList(spec1,spec2));
		return specs;
	}






	private <T,V>  boolean  propertyValuesIn(Collection<T> entityList, Function<T,V> getter, Set<V> expectedValues) {
		return entityList
				.stream()
				.map(getter)
				.flatMap(this::getStreamOf)
				.filter(Objects::nonNull)
				.collect(toSet())
				.equals(expectedValues);
	}
	
	
	
	
	
	private <T,V>  boolean  propertyMultiValuesIn(Collection<T> entityList, Function<T,? extends Collection<V>> getter, Set<V> expectedValues) {
		return entityList
				.stream()
				.map(getter)
				.flatMap(Collection::stream)
				.filter(Objects::nonNull)
				.collect(toSet())
				.equals(expectedValues);
	}
	
	
	
	
	@SuppressWarnings("unchecked")
	private <T, R> Stream<R> getStreamOf(T value ){
		if(value instanceof Collection) {
			return Collection.class.cast(value).stream();
		}else {
			return (Stream<R>) Stream.of(value);
		}
	}
	
	

	
	
	
	private <T,V extends BigDecimal>  boolean  compareEntityBigDecimalFieldValues(List<T> entityList, Function<T, V> getter, Set<V> expectedValues) {
		return entityList
				.stream()
				.map(getter)
				.allMatch(n -> expectedValues.stream().anyMatch( e-> e.compareTo(n) == 0));
	}
	


	private JSONObject createDataImportProperties() {
		
		JSONObject json = new JSONObject();
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
	

}



@Data
class ExpectedSavedData{
	private Set<Integer> quantities;
	private Set<BigDecimal> prices ;
	private Set<String> barcodes ;
	private Set<String> ProductNames;
	private Set<String> VariantNames;
	private Set<String> productPNames;
	private Set<String> variantsPNames;
	private Set<String>  descriptions;
	private Set<String> variantDescriptions;
	private Set<String> tags;
	private Set<Long> brands;
	private Set<TransactionCurrency> currencies;
	private Set<JSONObject> featureSpecs;
	private Set<JSONObject> extraAttributes;
	private Integer stocksNum;
	
	public ExpectedSavedData() {
		extraAttributes = new HashSet<>();
		featureSpecs = new HashSet<>();
		featureSpecs.add(new JSONObject("{}") );
	}
	
	
	public void setProductNames(Set<String> productNames) {
		this.ProductNames = productNames;
		if(this.VariantNames == null) {
			this.VariantNames = productNames;
		}
	}
	
	
	
	public void setDescriptions(Set<String> descriptions) {
		this.descriptions = descriptions;
		if(this.variantDescriptions == null) {
			this.variantDescriptions = descriptions;
		}
	}
}




class ProductDataCount{
	public Long product;
	public Long variant;
	public Long stocks;
}


class ExtendedProductDataCount extends ProductDataCount{
	public Long tags;
	public Long brands;
	
	public ExtendedProductDataCount() {};
	
	public ExtendedProductDataCount(ProductDataCount count) {
		this.product = count.product;
		this.variant = count.variant;
		this.stocks = count.stocks;
	};
}