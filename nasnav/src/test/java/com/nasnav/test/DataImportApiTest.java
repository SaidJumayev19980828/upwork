package com.nasnav.test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dao.*;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.persistence.*;
import com.nasnav.service.model.importproduct.context.ImportProductContext;
import com.nasnav.test.commons.TestCommons;
import com.nasnav.test.helpers.TestHelper;
import net.jcip.annotations.NotThreadSafe;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static com.nasnav.enumerations.ExtraAttributeType.INVISIBLE;
import static com.nasnav.enumerations.ExtraAttributeType.STRING;
import static com.nasnav.enumerations.OrderStatus.NEW;
import static com.nasnav.enumerations.TransactionCurrency.EGP;
import static com.nasnav.enumerations.TransactionCurrency.USD;
import static com.nasnav.integration.enums.MappingType.PRODUCT_VARIANT;
import static com.nasnav.test.commons.TestCommons.*;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc
@PropertySource("classpath:test.database.properties")
@NotThreadSafe 
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Data_Import_API_Test_Data_Insert.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
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

	@Value("classpath:/files/product__list_upload_units.csv")
	private Resource csvFileWithUnit;
	
	@Value("classpath:/files/product__list_upate_group_variants.csv")
    private Resource csvFileGroupExistingVariants;
	
	@Value("classpath:/files/product__list_upload_group_by_key.csv")
    private Resource csvFileGroupByKey;
	
	@Value("classpath:/files/product__list_upload_with_new_tags.csv")
    private Resource csvFileWithNewTags;

	@Value("classpath:/files/product__list_upload_existing_barcodes_new_tags_only.csv")
	private Resource csvFileWithBarcodesAndNewTagsOnly;

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
	
	@Value("classpath:/files/product__list_upate_null_features.csv")
	private Resource csvFileUpdateNullFeatures;
	
	
	@Value("classpath:/files/product__list_multitag_upate.csv")
	private Resource csvMulitagProductUpdate;

	@Value("classpath:/files/product__list_multitag_different_case_update.csv")
	private Resource csvMulitagDifferentCaseProductUpdate;

	@Value("classpath:/files/product__list_removed_variant.csv")
	private Resource csvRemovedVariant;

	@Value("classpath:/files/product__list_update_invisible_attributes.csv")
	private Resource csvInvisibleAttributes;

	
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
	
	@Autowired
	private OrdersRepository orderRepo;

	@Autowired
	private StockUnitRepository stockUnitRepo;
	
	@Test
    public void uploadProductsCSVNoAuthNTest() throws IOException, Exception {

		var importProperties = createDataImportProperties();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "Not Existing", csvFile, importProperties);
		
        result.andExpect(status().is(401));
    }


	
	@Test
    public void uploadProductsCSVNoAuthZTest() throws IOException, Exception {

		var importProperties = createDataImportProperties();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "101112", csvFile, importProperties);
        
        result.andExpect(status().is(403));
    }

	
	
	@Test
    public void uploadProductsCSVMissingShopIdTest() throws Exception, Throwable {

		var importProperties = createDataImportProperties();
		importProperties.remove("shop_id");

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFile, importProperties);
        
        result.andExpect(status().is(406));
    }
	
	
	
	
	
	
	@Test
    public void uploadProductsCSVMissingEncodingTest() throws IOException, Exception {

		var importProperties = createDataImportProperties();
		importProperties.remove("encoding");

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFile, importProperties);
        
        result.andExpect(status().is(406));
    }
	
	
	
	
	
	
	@Test
    public void uploadProductsCSVMissingCurrencyTest() throws IOException, Exception {

		var importProperties = createDataImportProperties();
		importProperties.remove("currency");


		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFile, importProperties);
        
        result.andExpect(status().is(406));
    }
	
	
	
	
	
	
	@Test
    public void uploadProductsCSVNonExistingShopIdTest() throws IOException, Exception {

		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", 88865);

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFile, importProperties);
        
        result.andExpect(status().is(406));
    }
	
	
	
	
	
	
	@Test
    public void uploadProductsCSVUserFromAnotherOrgTest() throws IOException, Exception {

		var importProperties = createDataImportProperties();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "898dssd", csvFile, importProperties);
        
        result.andExpect(status().is(406));
    }
	
	
	
	
	
	@Test
    public void uploadProductsCSVInvalidEncodingTest() throws IOException, Exception {

		var importProperties = createDataImportProperties();
		importProperties.put("encoding", "KOKI-8");


		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFile, importProperties);
        
        result.andExpect(status().is(406));
    }
	
	
	
	
	
	@Test
    public void uploadProductsCSVInvalidCurrencyTest() throws IOException, Exception {

		var importProperties = createDataImportProperties();
		importProperties.put("currency", 9999);


		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFile, importProperties);
        
        result.andExpect(status().is(406));
    }
	
	
	
	
	
	
	@Test
    public void uploadProductsCSVNoCsvUploadedTest() throws IOException, Exception {

		var importProperties = createDataImportProperties();

		var emptyFilePart = new MockMultipartFile("csv", "nothing.empty", "text/csv", new byte[0]);

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", emptyFilePart, importProperties);
        
        result.andExpect(status().is(406));
    }
	
	
	
	
	
	
	
	
	
	@Test
    public void uploadProductsCSVMissingCol() throws IOException, Exception {

		var importProperties = createDataImportProperties();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFileMissingCol, importProperties);
        
        result.andExpect(status().is(406));
    }
	
	
	
	
	
	@Test
    public void uploadProductsCSVInvalidData() throws IOException, Exception {

		var importProperties = createDataImportProperties();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFileInvalidData, importProperties);
        
        result.andExpect(status().is(406));

		var report = readImportReport(result);
        assertEquals(4, report.getErrors().size());
        assertFalse(report.isSuccess());
    }
	
	
	
	
	
	@Test
	public void uploadProductCSVNewDataTest() throws IOException, Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);

		var before = countExtendedProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFile, importProperties);
		
		result.andExpect(status().is(200));

		var after = countExtendedProductData();
		assertExpectedRowNumInserted(before, after, 2);
		assertEquals(0, after.tags - before.tags);
		assertEquals(0, after.brands - before.brands);

		var expected = getExpectedAllNewData();
        assertProductDataImported(TEST_IMPORT_SHOP, expected);
       
        validateImportReportForNewData(result);
	}



	@Test
	public void uploadProductCSVNewUnit() throws IOException, Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);

		var before = countExtendedProductData();

		var unitsCountBefore = stockUnitRepo.count();
		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileWithUnit, importProperties);

		result.andExpect(status().is(200));

		var unitsCountAfter = stockUnitRepo.count();
		assertEquals("imported two units, one already exists and the other is new"
				, unitsCountAfter, unitsCountBefore + 1);
	}


	@Test
	public void uploadProductCSVRemovedVariant() throws Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);

		var variantsCountBefore = variantRepo.count();
		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvRemovedVariant, importProperties);

		result.andExpect(status().is(200));

		var variantsCountAfter = variantRepo.count();
		assertEquals("imported two variants, one is to removed product and the other is removed itself"
				, variantsCountAfter, variantsCountBefore + 2);
	}



	
	
	@Test
	public void uploadProductCSVNewDataTestGroupByKey() throws IOException, Exception {
		variantRepo.deleteAll();
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);

		var before = countProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileGroupByKey, importProperties);
		
		result.andExpect(status().is(200));

		var after = countProductData();
		
		assertEquals(2L, after.product - before.product);
		assertEquals(3L, after.variant - before.variant);
		assertEquals(3L, after.stocks - before.stocks);

		var expected = getExpectedNewDataGroupedByKey();
        assertProductDataImported(TEST_IMPORT_SHOP, expected);
        
        validateVariantsGrouping();
       
        validateImportReportForNewData(result);
	}






	private void validateVariantsGrouping() {
		var variants = variantRepo.findByOrganizationId(99001L);
        assertEquals(3, variants.size());
		var groupedByProduct =
        		variants
        		.stream()
        		.collect(groupingBy(var -> var.getProductEntity().getId()));
        assertEquals(2, groupedByProduct.keySet().size());

		var variantsBarcodes =
        		groupedByProduct
        		.values()
        		.stream()
        		.map(varList -> varList.stream().map(ProductVariantsEntity::getBarcode).collect(toSet()))
        		.collect(toSet());
        assertEquals( variantsBarcodes
        		 , setOf( setOf("111222A"), setOf("1354ABN", "87847777EW")));
	}
	
	
	
	
	
	@Test
	public void uploadProductInvalidCSV() throws IOException, Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);

		var before = countExtendedProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFile, importProperties);
		
		result.andExpect(status().is(200));

		var after = countExtendedProductData();
		assertExpectedRowNumInserted(before, after, 2);
		assertEquals(0, after.tags - before.tags);
		assertEquals(0, after.brands - before.brands);

		var expected = getExpectedAllNewData();
        assertProductDataImported(TEST_IMPORT_SHOP, expected);
       
        validateImportReportForNewData(result);
	}






	private void validateImportReportForNewData(ResultActions result)
			throws UnsupportedEncodingException, IOException, JsonParseException, JsonMappingException {
		var report = readImportReport(result);
        assertEquals(2, report.getCreatedProducts().size());
        assertTrue(report.getUpdatedProducts().isEmpty());
        assertTrue(report.getCreatedBrands().isEmpty());
        assertTrue(report.getCreatedTags().isEmpty());
        assertTrue(report.isSuccess());
        assertTrue(report.getErrors().isEmpty());
	}
	
	
	
	
	
	@Test
	public void uploadProductCSVNewDataWithMissingFeaturesTest() throws IOException, Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);
		importProperties.put("update_product", true);

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileMissingFeatures, importProperties);

		result.andExpect(status().is(200));

		var product = variantRepo.getVariantFullData(310001L).get().getProductEntity();
		assertEquals("Squishy shoes", product.getName());

		var mapping =
        		integrationMappingRepo.findByOrganizationIdAndMappingType_typeNameAndRemoteValue(
        				99001L
        				, PRODUCT_VARIANT.getValue()
        				, "4");
        assertTrue(mapping.isPresent());
       
	}
	
	
	
	
	
	@Test
	public void uploadProductCSVNewDataVariantsTest() throws IOException, Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);

		var before = countProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileVariants, importProperties);
		
		result.andExpect(status().is(200));

		var after = countProductData();
		assertEquals(2, after.product - before.product);
        assertEquals(4, after.variant - before.variant);
        assertEquals(4, after.stocks  - before.stocks);


		var expected = getExpectedAllNewVariantsData();
        assertProductDataImported(TEST_IMPORT_SHOP, expected);
       
	}






	private void assertExpectedRowNumInserted(ProductDataCount before, ProductDataCount after, int expectedDiff) {
		assertEquals(expectedDiff, after.product - before.product);
        assertEquals(expectedDiff, after.variant - before.variant);
        assertEquals(expectedDiff, after.stocks  - before.stocks);
	}






	private ProductDataCount countProductData() {
		var count = new ProductDataCount();
		count.product = productRepo.countByProductType(0);
		count.variant = variantRepo.count();
		count.stocks = stocksRepo.count();
		return count;
	}
	
	
	
	private ExtendedProductDataCount countExtendedProductData() {
		var count = countProductData();
		var extendedCount = new ExtendedProductDataCount(count);
		extendedCount.tags = tagsRepo.count();
		extendedCount.brands = brandsRepo.count();
		return extendedCount;
	}
	
	
	
	
	@Test
	public void uploadProductCSVNewDataDryRunTest() throws Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);
		importProperties.put("dryrun", true);

		var before = countProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFile, importProperties);
		
		result.andExpect(status().is(200));

		var after = countProductData();
		assertExpectedRowNumInserted(before, after, 0);
		
		validateImportReportForDryRun(result);
	}






	private void validateImportReportForDryRun(ResultActions result)
			throws UnsupportedEncodingException, IOException, JsonParseException, JsonMappingException {
		var report = readImportReport(result);
		assertTrue(report.getCreatedProducts().isEmpty());
		assertTrue(report.getUpdatedProducts().isEmpty());
		assertTrue(report.getCreatedTags().isEmpty());
		assertTrue(report.getCreatedBrands().isEmpty());
		assertEquals(2, report.getProductsNum().intValue());
	}

	
	
	
	@Test
	public void uploadProductCSVUpdateStockDisabledTest() throws Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("update_product", true);
		importProperties.put("update_stock", false);

		var before = countProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "edddre2", csvFileUpdate, importProperties);
		
		result.andExpect(status().is(200));

		var after = countProductData();
		assertExpectedRowNumInserted(before, after, 1);


		var expected = getExpectedNewAndUpdatedDataWithoutStocks();
        assertProductDataImported(TEST_UPDATE_SHOP, expected);
        assertProductUpdatedDataSavedWithoutStock();     
	}

	
	
	@Test
	public void uploadProductCSVUpdateProductsDisabledTest() throws Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("update_product", false);
		importProperties.put("update_stocks", true);

		var before = countProductData();

		var variantBefore = helper.getVariantFullData(TEST_VARIANT_UPDATED);
		var productBefore = variantBefore.getProductEntity();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "edddre2", csvFileUpdate, importProperties);
		
		result.andExpect(status().is(200));

		var after = countProductData();
		assertExpectedRowNumInserted(before, after, 1);

		var variantAfter = helper.getVariantFullData(TEST_VARIANT_UPDATED);
		var productAFter = variantAfter.getProductEntity();
        
        assertEquals(variantBefore, variantAfter);
        assertEquals(productBefore, productAFter);

		var expected = getExpectedNewOnlyAndUpdatedStocks();
        assertProductDataImported(TEST_UPDATE_SHOP, expected);            
	}
	
	
	
	
	@Test
	public void uploadProductCSVUpdateBothProductsAndStockDisabledTest() throws Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("update_product", false);
		importProperties.put("update_stocks", false);

		var before = countProductData();

		var variantBefore = helper.getVariantFullData(TEST_VARIANT_UPDATED);
		var productBefore = variantBefore.getProductEntity();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "edddre2", csvFileUpdate, importProperties);
		
		result.andExpect(status().is(200));

		var after = countProductData();
		assertExpectedRowNumInserted(before, after, 1);

		var variantAfter = helper.getVariantFullData(TEST_VARIANT_UPDATED);
		var productAFter = variantAfter.getProductEntity();
        
        assertEquals(variantBefore, variantAfter);
        assertEquals(productBefore, productAFter);

		var expected = getExpectedNewOnlyWithoutStocks();
        assertProductDataImported(TEST_UPDATE_SHOP, expected);    
        
        validateImportReportWithUpdateDisabled(result);
	}






	private void validateImportReportWithUpdateDisabled(ResultActions result)
			throws UnsupportedEncodingException, IOException, JsonParseException, JsonMappingException {
		var report = readImportReport(result);
        assertEquals(1, report.getCreatedProducts().size());
        assertTrue(report.getUpdatedProducts().isEmpty());
        assertTrue(report.getCreatedTags().isEmpty());
        assertTrue(report.getCreatedBrands().isEmpty());
	}



	@Test
	public void uploadProductCSVLargeColumnTest() throws IOException, Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);

		var before = countProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileLargeColumn, importProperties);

		result.andExpect(status().is(200));

		var after = countProductData();
		assertExpectedRowNumInserted(before, after, 2);


		var expected = getExpectedAllNewData();
		expected.setDescriptions(setOf(readResource(largeDescription), "too hard"));
		expected.setVariantDescriptions(setOf(readResource(largeDescription), "too hard"));
		assertProductDataImported(TEST_IMPORT_SHOP, expected);

	}



	private void assertProductUpdatedDataSavedWithoutStock() {
		var updatedVariant = helper.getVariantFullData(TEST_VARIANT_UPDATED);
        assertTestVariantUpdated(updatedVariant);

		var updatedProduct = updatedVariant.getProductEntity();
        assertTestProductUpdated(updatedProduct);

		var updatedStocks = stocksRepo.findByProductVariantsEntity_IdAndShopsEntity_Id(TEST_VARIANT_UPDATED, TEST_UPDATE_SHOP).get();
        assertTestStockRemainedSame(updatedStocks);
	}






	private void assertTestStockRemainedSame(StocksEntity updatedStocks) {
		assertEquals(TEST_STOCK_UPDATED, updatedStocks.getId().longValue());
        assertEquals(30, updatedStocks.getQuantity().intValue());
        assertEquals( 0, updatedStocks.getPrice().compareTo(new BigDecimal("15")) );
	}






	@Test
	public void uploadProductCSVUpdateDataEnabledTest() throws IOException, Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("update_product", true);
		importProperties.put("update_stocks", true);

		var before = countProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "edddre2", csvFileUpdate, importProperties);
		
		result.andExpect(status().is(200));

		var after = countProductData();
		assertExpectedRowNumInserted(before, after, 1);         

        assertDataSaved();

		var report = readImportReport(result);
        assertEquals(1, report.getCreatedProducts().size());
        assertEquals(1, report.getUpdatedProducts().size());
        assertTrue(report.getErrors().isEmpty());
	}




	@Test
	public void uploadProductCSVUpdateWithInvisibleExtraAttributesTest() throws IOException, Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("update_product", true);
		importProperties.put("update_stocks", true);

		var before = countProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "edddre2", csvInvisibleAttributes, importProperties);

		result.andExpect(status().is(200));

		var after = countProductData();
		assertExpectedRowNumInserted(before, after, 1);

		assertDataSavedWithInvisibleExtraAttributes();

		var report = readImportReport(result);
		assertEquals(1, report.getCreatedProducts().size());
		assertEquals(1, report.getUpdatedProducts().size());
		assertTrue(report.getErrors().isEmpty());
	}
	
	
	
	
	
	@Test
	public void uploadProductCSVInsertNewProductsDisabledTest() throws IOException, Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("update_product", true);
		importProperties.put("update_stocks", true);
		importProperties.put("insert_new_products", false);

		var before = countProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "edddre2", csvFileUpdate, importProperties);
		
		result.andExpect(status().is(200));

		var after = countProductData();
		assertExpectedRowNumInserted(before, after, 0);         

		assertOnlyUpdatedDataSaved();

		var report = readImportReport(result);
        assertEquals(0, report.getCreatedProducts().size());
        assertEquals(1, report.getUpdatedProducts().size());
        assertTrue(report.getErrors().isEmpty());
	}
	
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Data_Import_API_Test_Data_Insert_5.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void uploadProductCSVUpdateNullFeaturesTest() throws IOException, Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("update_product", true);
		importProperties.put("update_stocks", true);

		var before = countProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "edddre2", csvFileUpdateNullFeatures, importProperties);
		
		result.andExpect(status().is(200));

		var after = countProductData();
		assertExpectedRowNumInserted(before, after, 0);         

		assertDataSavedWithoutUpdatingProductFeatures();

		var report = readImportReport(result);
        assertEquals(1, report.getUpdatedProducts().size());
        assertTrue(report.getErrors().isEmpty());
	}
	
	
	
	
	
	
	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Data_Import_API_Test_Data_Insert_3.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void uploadProductCSVUpdateDataDeleteOldProductsTest() throws Throwable {
		var importProperties = createDataImportPropertiesWithDeleteOldProducts();
        
		Long orgId = 99001L;
		Long otherOrg = 99002L;
		Long variantToBeDeleted = 310001L;
		Long productToBeDeleted = 200001L;

		var before = countData(orgId, otherOrg);
		assertDataExistedBeforeImport(variantToBeDeleted, productToBeDeleted, before);

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "edddre2", csvFileUpdate, importProperties);
		
		result.andExpect(status().is(200));

        assertDataSaved();     
        validateDeletedOldProducts(before ,result, productToBeDeleted, variantToBeDeleted);
	}






	private void assertOnlyUpdatedDataSaved() {
		var expected = getExpectedUpdatedDataWithStocks();
        assertProductDataImported(TEST_UPDATE_SHOP, expected);
        assertProductUpdatedDataSavedWithStock();
    }



	private void assertOnlyStockDataSaved() {
		var expected = getExpectedUpdatedStocks();
		assertProductDataImported(TEST_UPDATE_SHOP, expected);
		assertStockUpdatedDataSaved();
	}





	private void assertDataSaved() {
		var expected = getExpectedNewAndUpdatedDataWithStocks();
        assertProductDataImported(TEST_UPDATE_SHOP, expected);
        assertProductUpdatedDataSavedWithStock();
	}



	private void assertDataSavedWithInvisibleExtraAttributes() {
		var expected = getExpectedNewAndUpdatedDataWithInvisibleAttributes();
		assertProductDataImported(TEST_UPDATE_SHOP, expected);
		assertProductUpdatedDataSavedWithStock();
	}
	
	
	
	private void assertDataSavedWithoutUpdatingProductFeatures() {
		var expected = getExpectedUpdatedDataWithNullFeatures();
        assertProductDataImported(TEST_UPDATE_SHOP, expected);
        assertProductUpdatedDataSavedWithStock();
	}
	






	private JSONObject createDataImportPropertiesWithDeleteOldProducts() {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("update_product", true);
		importProperties.put("update_stocks", true);
		importProperties.put("delete_old_products", true);
		return importProperties;
	}






	private void assertOtherOrgDataNotDeleted(Long orgId, Long otherOrg, ProductDataDeleteCounts before) {
		var otherOrgProductAfter = productRepo.countByOrganizationId(otherOrg);
		var otherOrgOrdersCountAfter = orderRepo.countByStatusAndOrganizationEntity_id(NEW.getValue(), otherOrg);
		var otherOrgVariantsAfter = variantRepo.countByProductEntity_organizationId(orgId);
		
		assertEquals(before.otherOrgProducts, otherOrgProductAfter);
		assertEquals(before.otherOrgOrders, otherOrgOrdersCountAfter);
		assertEquals(before.otherOrgVariants, otherOrgVariantsAfter);
	}






	private void assertDataExistedBeforeImport(Long variantToBeDeleted, Long productToBeDeleted,
			ProductDataDeleteCounts before) {
		assertEquals(2, before.orgProducts);
		assertNotEquals(0L, before.orders);
		assertNotEquals(0L, before.orgVariants);
		assertNotEquals(0L, before.otherOrgProducts);
		assertNotEquals(0L, before.otherOrgOrders);
		assertNotEquals(0L, before.otherOrgVariants);
		assertTrue(variantRepo.existsById(variantToBeDeleted));
		assertTrue(productRepo.existsById(productToBeDeleted));
	}






	private ProductDataDeleteCounts countData(Long orgId, Long otherOrg) {
		var before = new ProductDataDeleteCounts();
		before.allProductsDataCount = countProductData();
		before.orgProducts = productRepo.countByOrganizationIdAndProductType(orgId, 0);
		before.orders = orderRepo.countByStatusAndOrganizationEntity_id(NEW.getValue(), orgId);
		before.orgVariants = variantRepo.countByProductEntity_organizationId(orgId);
		before.otherOrgProducts = productRepo.countByOrganizationId(otherOrg);
		before.otherOrgOrders = orderRepo.countByStatusAndOrganizationEntity_id(NEW.getValue(), otherOrg);
		before.otherOrgVariants = variantRepo.countByProductEntity_organizationId(orgId);
		before.orgId = orgId;
		before.otherOrgId = otherOrg;
		return before;
	}






	private void validateDeletedOldProducts(ProductDataDeleteCounts before, ResultActions result
			, Long productToBeDeleted, Long variantToBeDeleted) throws  Throwable {
		var report = readImportReport(result);
		var orgId = before.orgId;
		var otherOrg = before.otherOrgId;
		var after = countData(orgId, otherOrg);
		 
        validateExpectedReport(report);
		validateOrgDataCount(after, before, report, productToBeDeleted, variantToBeDeleted);
		assertOtherOrgDataNotDeleted(orgId, otherOrg, before);
	}






	private void validateOrgDataCount(ProductDataDeleteCounts after, ProductDataDeleteCounts before, ImportProductContext report
			, Long productToBeDeleted, Long variantToBeDeleted) {
		assertEquals("for organization 99001 a single product will be added, and one old product will be deleted"
				,0 , after.allProductsDataCount.product - before.allProductsDataCount.product);
        assertEquals("for organization 99001 a single variant will be added, and one old product will be deleted"
        		,0 , after.allProductsDataCount.variant - before.allProductsDataCount.variant);
        assertEquals("for organization 99001 a single stock will be added, and no stocks will be deleted"
        		,1 , after.allProductsDataCount.stocks  - before.allProductsDataCount.stocks);

		var createdProducts = report.getCreatedProducts().size();
		var updatedProducts = report.getUpdatedProducts().size();
		assertEquals(createdProducts + updatedProducts , after.orgProducts);
		assertEquals("validate variants deleted , each product has single variant in the test..."
				, createdProducts + updatedProducts, after.orgVariants);
		
		assertFalse("assert non updated product deleted.", productRepo.existsById(productToBeDeleted));
		assertFalse("assert non updated product variant deleted.", variantRepo.existsById(variantToBeDeleted));
	}






	private void validateExpectedReport(ImportProductContext report) {
		var createdProducts = report.getCreatedProducts().size();
		var updatedProducts = report.getUpdatedProducts().size();
		assertEquals(1, createdProducts);
        assertEquals(1, updatedProducts);
        assertTrue(report.getErrors().isEmpty());
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Data_Import_API_Test_Data_Insert_2.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void uploadProductCSVUpdateGroupsWithExistingVariantTest() throws IOException, Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("update_product", true);
		importProperties.put("update_stocks", true);

		var before = countProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "edddre2", csvFileGroupExistingVariants, importProperties);
		
		result.andExpect(status().is(200));
		
		//- Grouping new variant to existing variant, will add the new one to the existing product
		//- if two groups have existing variants from a single product, they will be merged.  
		var after = countProductData();
		assertEquals(0, after.product - before.product);
        assertEquals(1, after.variant - before.variant);
        assertEquals(1, after.stocks  - before.stocks);


		var expected = getExpectedUpdateDataGroupExistingVariants();
        assertProductDataImported(TEST_UPDATE_SHOP, expected);

		var report = readImportReport(result);
        assertEquals(0, report.getCreatedProducts().size());
        assertEquals(1, report.getUpdatedProducts().size());
        assertTrue(report.getErrors().isEmpty());
	}


	
	
	@Test
	public void getProductsCsvTemplateInvalidAuthentication() {
		var request = TestCommons.getHttpEntity("","456");
		var res = template.exchange("/upload/productlist/template", HttpMethod.GET, request ,String.class);
		Assert.assertTrue(res.getStatusCodeValue() == 401);

		res = template.exchange("/product/image/bulk/template", HttpMethod.GET, request ,String.class);
		Assert.assertTrue(res.getStatusCodeValue() == 401);
	}

	
	
	
	
	@Test
	public void getProductsCsvTemplateInvalidAuthorization() {
		var request = TestCommons.getHttpEntity("","101112");
		var res = template.exchange("/upload/productlist/template", HttpMethod.GET, request ,String.class);
		Assert.assertTrue(res.getStatusCodeValue() == 403);

		res = template.exchange("/product/image/bulk/template", HttpMethod.GET, request ,String.class);
		Assert.assertTrue(res.getStatusCodeValue() == 403);
	}
	
	
	

	@Test
	public void getProductsCsvTemplate() {
		var expectedTemplateHeaders =
				setOf("product_name", "barcode", "tags", "brand", "price", "quantity", "description"
						, "variant_id", "external_id", "color", "size", "product_group_key", "discount"
						, "sku", "product_code", "unit", "weight");
		var request = getHttpEntity("","131415");
		var res = template.exchange("/upload/productlist/template", GET, request ,String.class);

		assertEquals(200, res.getStatusCodeValue());

		var headersArr = res.getBody()
								.replace(System.lineSeparator(), "")
								.split(",");
		var headers = setOf(headersArr);
		
		assertEquals(expectedTemplateHeaders, headers);
	}
	
	
	
	
	@Test
	public void getImageUploadCsvTemplate() {
		var expectedImageHeaders = new String[]{"variant_id", "external_id", "barcode", "image_file"};

		var request = getHttpEntity("","131415");
		var res =
				template
					.exchange(
						"/product/image/bulk/template", GET, request ,String.class);

		var headers = res.getBody()
								.replace(System.lineSeparator(), "")
								.split(",");
		
		Assert.assertTrue(res.getStatusCodeValue() == 200);
		for(var i = 0; i<headers.length; i++){
			Assert.assertTrue(headers[i].equals(expectedImageHeaders[i]));
		}
	}


	@Test
	public void uploadProductCSVExistingVariantIdNoVariantEntity() throws Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileVariantsWithVariantId, importProperties);

		result.andExpect(status().is(406));
	}

	
	
	
	@Test
	public void uploadProductCSVExistingVariantIdExistVariantEntity() throws Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("update_product", true);
		importProperties.put("shop_id", TEST_IMPORT_SHOP);

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileVariantsWithVariantIdExistingVariant, importProperties);

		@SuppressWarnings("unused")
		var body =
				result
				.andExpect(status().is(200))
				.andReturn()
				.getResponse()
				.getContentAsString();

		var product = helper.getVariantFullData(310001L).getProductEntity();
		assertEquals("Squishy shoes", product.getName());
	}


	@Test
	public void uploadProductCSVExistingExternalIdNoVariantEntity() throws Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileVariantsWithExternalId, importProperties);

		result.andExpect(status().is(200));

		var mapping =
				integrationMappingRepo.findByOrganizationIdAndMappingType_typeNameAndRemoteValue(
						99001L
						, PRODUCT_VARIANT.getValue()
						, "5");
		assertTrue(mapping.isPresent());

		var product = variantRepo.findById(Long.parseLong(mapping.get().getLocalValue()));
		assertTrue(product.isPresent());
	}

	
	
	
	
	@Test
	public void uploadProductCSVExistingExternalIdExistVariantEntity() throws Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);
		importProperties.put("update_product", true);

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileVariantsWithExternalIdExistingMapping, importProperties);

		result.andExpect(status().is(200));

		var product = helper.getVariantFullData(310001L).getProductEntity();
		assertEquals("Squishy shoes", product.getName());
	}



	@Test
	public void uploadProductCSVExistingExternalIdAndBarcodeNoVariantEntity() throws Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);
		importProperties.put("update_product", true);

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileVariantsWithExternalIdAndBarcode, importProperties);

		result.andExpect(status().is(200));

		var product = helper.getVariantFullData(310001L).getProductEntity();
		assertEquals("Squishy shoes", product.getName());

		var mapping =
        		integrationMappingRepo.findByOrganizationIdAndMappingType_typeNameAndRemoteValue(
        				99001L
        				, PRODUCT_VARIANT.getValue()
        				, "4");
        assertTrue(mapping.isPresent());
	}
	
	
	
	
	
	@Test
	public void uploadProductCSVUpdateProductWithMultipleTagsTest() throws IOException, Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("update_product", true);
		importProperties.put("update_stocks", true);

		var before = countProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "edddre2", csvMulitagProductUpdate, importProperties);
		
		result.andExpect(status().is(200));

		var after = countProductData();
		assertExpectedRowNumInserted(before, after, 0);

		var expected = getExpectedUpdatedDataForMultipleTags();
        assertProductDataImported(TEST_UPDATE_SHOP, expected);
        assertProductUpdatedDataSavedWithStock();        
	}



	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Data_Import_API_Test_Data_Insert_6.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void uploadProductCSVUpdateProductWithTagsWithDifferentCaseTest() throws IOException, Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("update_product", true);
		importProperties.put("update_stocks", true);
		importProperties.put("insert_new_products", true);

		var before = countProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "edddre2", csvMulitagDifferentCaseProductUpdate, importProperties);

		result.andExpect(status().is(200));

		var after = countProductData();
		assertExpectedRowNumInserted(before, after, 2);

		var expected = getExpectedUpdatedDataForMultipleWithDifferentCaseTags();
		assertProductDataImported(TEST_UPDATE_SHOP, expected);
		assertProductUpdatedDataSavedWithStock();
	}



	@Test
	public void uploadProductCSVWithBarcodesAndNewTagsOnly() throws IOException, Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("update_product", true);
		importProperties.put("shop_id", TEST_IMPORT_SHOP);
		var before = countExtendedProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileWithBarcodesAndNewTagsOnly, importProperties);

		result.andExpect(status().is(200));

		var after = countExtendedProductData();

		assertExpectedRowNumInserted(before, after, 0);

		assertEquals(2, after.tags - before.tags);

		var report = readImportReport(result);

		assertEquals(2, report.getUpdatedProducts().size());
	}
	
	
	
	@Test
	public void uploadProductCSVWithNewTagsAndBrand() throws IOException, Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);

		var before = countExtendedProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFileWithNewTags, importProperties);
		
		result.andExpect(status().is(200));

		var after = countExtendedProductData();
		assertExpectedRowNumInserted(before, after, 2);
		assertEquals(2, after.tags - before.tags);
		assertEquals(2, after.brands - before.brands);
        
		assertNewTagsAndBrandsImported();

		validateImportReportForNewCreatedTagsAndBrands(result);
	}
	
	
	
	
	
	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Data_Import_API_Test_Data_Insert_4.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void uploadProductCSVWithResetTagsFlag() throws IOException, Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("update_product", true);
		importProperties.put("update_stocks", true);
		importProperties.put("reset_tags", true);
        
		Long productId = 200003L;
		var productBefore = helper.getProductFullData(productId);
		assertEquals(1, productBefore.getTags().size());		
		long tagId = productBefore.getTags().stream().findFirst().map(TagsEntity::getId).orElse(-1L);
		assertEquals("this is the id of the tag that will be removed from the product", 22007L, tagId);

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "edddre2", csvFileUpdate, importProperties);
		
		result.andExpect(status().is(200));

		var productAfter = helper.getProductFullData(productId);
		assertEquals(1, productAfter.getTags().size());		
		long newTagId = productAfter.getTags().stream().findFirst().map(TagsEntity::getId).orElse(-1L);
		assertEquals("this is the id of the tag that te product should have", 22001L, newTagId);
	}


	@Test
	@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Data_Import_API_Test_Data_Insert_4.sql"})
	@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
	public void uploadProductCSVSetWeights() throws IOException, Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("update_product", true);

		Long variantId = 310003L;
		var variantBefore = variantRepo.findById(variantId).get();
		assertEquals(ZERO, variantBefore.getWeight());
		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "edddre2", csvFileUpdate, importProperties);

		result.andExpect(status().is(200));

		var variantAfter = variantRepo.findById(variantId).get();
		assertNotNull(variantAfter.getWeight());
		assertEquals(new BigDecimal(5.5), variantAfter.getWeight());
		var newAddedVariant = variantRepo
				.findByBarcodeAndProductEntity_OrganizationId("87847777EW", 99001L).get(0);
		assertEquals(new BigDecimal(5.5), newAddedVariant.getWeight());
	}




	@Test
	public void updateStockByStoreManagerInvalidFlag1Test() throws Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("update_product", true);
		importProperties.put("update_stock", true);

		var before = countProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST, "TTTRRR", csvFileUpdate, importProperties);

		result.andExpect(status().is(406));

		var after = countProductData();
		assertExpectedRowNumInserted(before, after, 0);
	}




	@Test
	public void updateStockByStoreManagerInvalidFlag2Test() throws Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("update_product", false);
		importProperties.put("insert_new_products", true);
		importProperties.put("update_stock", true);

		var before = countProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST, "TTTRRR", csvFileUpdate, importProperties);

		result.andExpect(status().is(406));

		var after = countProductData();
		assertExpectedRowNumInserted(before, after, 0);
	}



	@Test
	public void updateStockByStoreManagerInvalidFlag3Test() throws Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("insert_new_products", false);
		importProperties.put("delete_old_products", true);
		importProperties.put("update_stock", true);

		var before = countProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST, "TTTRRR", csvFileUpdate, importProperties);

		result.andExpect(status().is(406));

		var after = countProductData();
		assertExpectedRowNumInserted(before, after, 0);
	}



	@Test
	public void updateStockByStoreManagerInvalidFlag4Test() throws Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("reset_tags", true);
		importProperties.put("delete_old_products", false);
		importProperties.put("update_stock", true);

		var before = countProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST, "TTTRRR", csvFileUpdate, importProperties);

		result.andExpect(status().is(406));

		var after = countProductData();
		assertExpectedRowNumInserted(before, after, 0);
	}



	@Test
	public void updateStockByStoreManagerInvalidShopTest() throws Exception {
		var otherShop = TEST_IMPORT_SHOP;
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", otherShop);
		importProperties.put("insert_new_products", false);
		importProperties.put("update_stock", true);

		var before = countProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST, "TTTRRR", csvFileUpdate, importProperties);

		result.andExpect(status().is(406));

		var after = countProductData();
		assertExpectedRowNumInserted(before, after, 0);
	}


	@Test
	public void updateStockByStoreManagerTest() throws IOException, Exception {
		var importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_UPDATE_SHOP);
		importProperties.put("insert_new_products", false);
		importProperties.put("update_stocks", true);

		var before = countProductData();

		var result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "TTTRRR", csvFileUpdate, importProperties);

		result.andExpect(status().is(200));

		var after = countProductData();
		assertExpectedRowNumInserted(before, after, 0);

		assertOnlyStockDataSaved();

		var report = readImportReport(result);
		assertEquals(0, report.getCreatedProducts().size());
		assertEquals(1, report.getUpdatedProducts().size());
		assertTrue(report.getErrors().isEmpty());
	}



	private void validateImportReportForNewCreatedTagsAndBrands(ResultActions result)
			throws UnsupportedEncodingException, IOException, JsonParseException, JsonMappingException {
		var report = readImportReport(result);
		assertEquals(2, report.getCreatedTags().size());
		assertEquals(2, report.getCreatedBrands().size());
		assertEquals(2, report.getCreatedProducts().size());
		assertEquals(0, report.getUpdatedProducts().size());
		assertEquals(0, report.getErrors().size());
		assertTrue(report.isSuccess());
	}






	private ImportProductContext readImportReport(ResultActions result)
			throws UnsupportedEncodingException, IOException, JsonParseException, JsonMappingException {
		var reportStr =
				result
				.andReturn()
				.getResponse()
				.getContentAsString();

		var mapper = new ObjectMapper();
		var report = mapper.readValue(reportStr, ImportProductContext.class);
		return report;
	}






	private void assertNewTagsAndBrandsImported() {
		var newTags= setOf("new squish", "new hill equipment");
		var newBrands = setOf("new brand","shiny new brand");
		assertEquals(2, tagsRepo.findByNameLowerCaseInAndOrganizationEntity_Id(newTags, 99001L).size());
        assertEquals(2, brandsRepo.findByNameInAndRemoved(newBrands, 0).size());
	}
	
	
	
	

	private void assertProductUpdatedDataSavedWithStock() {
		var updatedVariant = helper.getVariantFullData(TEST_VARIANT_UPDATED);
        assertTestVariantUpdated(updatedVariant);

		var updatedProduct = updatedVariant.getProductEntity();
        assertTestProductUpdated(updatedProduct);

		var updatedStocks = stocksRepo.findByProductVariantsEntity_IdAndShopsEntity_Id(TEST_VARIANT_UPDATED, TEST_UPDATE_SHOP).get();
        assertTestStockUpdated(updatedStocks);
	}



	private void assertStockUpdatedDataSaved() {
		var updatedStocks = stocksRepo.findByProductVariantsEntity_IdAndShopsEntity_Id(TEST_VARIANT_UPDATED, TEST_UPDATE_SHOP).get();
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
        assertEquals(101L, updatedProduct.getBrand().getId().longValue());
	}






	private void assertTestVariantUpdated(ProductVariantsEntity updatedVariant) {
		assertEquals("TT232222", updatedVariant.getBarcode());
        assertEquals("Squishy shoes", updatedVariant.getName());
        assertEquals("squishy", updatedVariant.getDescription());
	}
	
	
	
	
	private void assertProductDataImported(Long shopId, ExpectedSavedData expected) {


		var stocks = helper.getShopStocksFullData(shopId);
		var variants =
        		stocks
        		.stream()
				.map(StocksEntity::getProductVariantsEntity)
				.map(variant -> variantRepo.getVariantFullData(variant.getId()).get())
				.collect(toList());

		var productIds =
        		variants
        		.stream()
				.map(ProductVariantsEntity::getProductEntity)
				.map(ProductEntity::getId)
				.collect(toList());
		var products = productRepo.findFullDataByIdIn(productIds);

        
        assertEquals(expected.getStocksNum().intValue() , stocks.size());
        assertTrue( propertyValuesIn(stocks, StocksEntity::getCurrency, expected.getCurrencies())	);
        assertTrue( propertyValuesIn(stocks, this::getUnit , expected.getUnits()));

        assertTrue( propertyValuesIn(stocks, StocksEntity::getQuantity, expected.getQuantities())	);
        assertTrue( compareEntityBigDecimalFieldValues(stocks, StocksEntity::getPrice, expected.getPrices())	);
        assertTrue( compareEntityBigDecimalFieldValues(stocks, StocksEntity::getDiscount, expected.getDiscounts()));

        
        assertTrue( propertyValuesIn(variants, ProductVariantsEntity::getBarcode, expected.getBarcodes())	);
        assertTrue( propertyValuesIn(variants, ProductVariantsEntity::getName, expected.getVariantNames()) );
        assertTrue( propertyValuesIn(variants, ProductVariantsEntity::getPname, expected.getVariantsPNames()) );
        assertTrue( propertyValuesIn(variants, ProductVariantsEntity::getDescription, expected.getVariantDescriptions()) );
        assertTrue( propertyValuesIn(variants, ProductVariantsEntity::getSku, expected.getSku()) );
        assertTrue( propertyValuesIn(variants, ProductVariantsEntity::getProductCode, expected.getProductCodes()) );
        assertTrue( featuresValuesIn(variants, expected.getFeatureSpecs()) );
        assertTrue( jsonValuesIn(variants, this::getExtraAtrributesStr, expected.getExtraAttributes()) );
        assertEquals( expected.getExtraAttributesTypes(), getExtraAttributesTypes(variants));
        
        assertTrue( propertyValuesIn(products, ProductEntity::getName, expected.getProductNames()) );
        assertTrue( propertyValuesIn(products, ProductEntity::getPname, expected.getProductPNames()) );
        assertTrue( propertyValuesIn(products, ProductEntity::getDescription, expected.getDescriptions()) );
        assertTrue( propertyMultiValuesIn(products, this::getTags, expected.getTags()) );
        assertTrue( propertyValuesIn(products, p -> p.getBrand().getId(), expected.getBrands()) );
	}



	private Set<String> getExtraAttributesTypes(List<ProductVariantsEntity> variants) {
		return variants
				.stream()
				.map(ProductVariantsEntity::getExtraAttributes)
				.flatMap(Set::stream)
				.map(ProductExtraAttributesEntity::getExtraAttribute)
				.map(ExtraAttributesEntity::getType)
				.collect(toSet());
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



	private String getUnit(StocksEntity stock){
		return ofNullable(stock.getUnit()).map(StockUnitEntity::getName).orElse(null);
	}
	
	
	
	
	private boolean jsonValuesIn(List<ProductVariantsEntity> variants, Function<ProductVariantsEntity,String> jsonStringGetter, Set<JSONObject> expectedSpecs) {
		return variants
				.stream()
				.map(jsonStringGetter)
				.filter(jsonStr -> jsonStr != null)
				.map(JSONObject::new)
				.allMatch(json -> expectedSpecs.stream().anyMatch(expected -> expected.similar(json)));
	}


	private boolean featuresValuesIn(List<ProductVariantsEntity> variants, Set<JSONObject> expectedSpecs) {
		for (JSONObject json : expectedSpecs){
			if (json.similar(new JSONObject("{}")))
				return true;
		}
		for(ProductVariantsEntity variant : variants) {
			JSONObject json = new JSONObject(variant.getFeatureValues()
					.stream()
					.collect(toMap(f -> f.getFeature().getId(), VariantFeatureValueEntity::getValue)));
			if (expectedSpecs.stream().noneMatch(expected -> expected.similar(json)))
				return false;
		}
		return true;
	}



	private ExpectedSavedData getExpectedAllNewData() {
		var data = new ExpectedSavedData();
		
		data.setQuantities( setOf(101,102) );
		data.setPrices( setOf(new BigDecimal("10.25"), new BigDecimal("88.6")));
		data.setCurrencies(setOf(EGP));
		
		data.setBarcodes( setOf("1354ABN", "87847777EW") );
		data.setProductNames( setOf("Squishy shoes", "hard shoes") );
		data.setVariantsPNames(setOf("squishy-shoes", "hard-shoes") );
		data.setProductPNames(setOf("squishy-shoes", "hard-shoes") );
		data.setDescriptions( setOf("squishy", "too hard") );
		data.setTags( setOf("squishy things", "mountain equipment") );
		data.setBrands(setOf(101L, 102L) );
		data.setStocksNum(2);
		data.setDiscounts(setOf(new BigDecimal("2"), new BigDecimal("8")));
		data.setSku(setOf("ABC123", "XYZ456"));
		data.setProductCodes(setOf("123-111","123-222"));
        
		return data;
	}
	
	
	
	
	private ExpectedSavedData getExpectedNewDataGroupedByKey() {
		var data = new ExpectedSavedData();
		
		data.setQuantities( setOf(101,102, 22) );
		data.setPrices( setOf(new BigDecimal("10.25"), new BigDecimal("88.6"), new BigDecimal("15.23")));
		data.setCurrencies(setOf(EGP));
		
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
		data.setDiscounts(setOf(ZERO));
		
		return data;
	}
	
	
	
	
	private ExpectedSavedData getExpectedUpdateDataGroupExistingVariants() {
		var data = new ExpectedSavedData();
		
		data.setQuantities( setOf(101,102) );
		data.setPrices( setOf(new BigDecimal("10.25"), new BigDecimal("88.6"), new BigDecimal("8.25")));
		data.setCurrencies(setOf(EGP));
		
		data.setBarcodes( setOf("TT232222", "BB232222", "87847777EW") );
		data.setProductNames( setOf("Squishy shoes") );
		data.setVariantsPNames(setOf("u_shoe", "hard-shoes", "n_shoe") );
		data.setVariantNames(setOf("Squishy shoes", "hard shoes") );
		data.setProductPNames(setOf("u_shoe") );
		data.setDescriptions( setOf("squishy") );
		data.setVariantDescriptions( setOf("squishy", "too hard") );
		data.setTags( setOf("squishy things") );
		data.setBrands(setOf(101L) );
		data.setStocksNum(3);
		data.setDiscounts(setOf(ZERO));
        
		return data;
	}
	
	
	
	
	private ExpectedSavedData getExpectedAllNewVariantsData() {
		var data = new ExpectedSavedData();
		
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
		data.setDiscounts(setOf(ZERO));
		data.setExtraAttributesTypes(setOf(STRING.getValue()));
		
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
		var spec1 = createFeatureSpec("XXL", "Lettuce Heart");
		var spec2 = createFeatureSpec("M", "Fo7loqy");
		var spec3 = createFeatureSpec("L", "Browny");
		var spec4 = createFeatureSpec("XXXL", "PINKY");
		specs.addAll( Arrays.asList(spec1, spec2, spec3, spec4));
		return specs;
	}



	private ExpectedSavedData getExpectedNewAndUpdatedDataWithStocks() {
		var data = new ExpectedSavedData();

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
		data.setDiscounts(setOf(ZERO));
		data.setExtraAttributesTypes(setOf(STRING.getValue()));
		
		return data;
	}



	private ExpectedSavedData getExpectedNewAndUpdatedDataWithInvisibleAttributes() {
		var data = new ExpectedSavedData();

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
		data.setExtraAttributes(  createExpectedInvisibleExtraAttr());
		data.setStocksNum(2);
		data.setDiscounts(setOf(ZERO));
		data.setExtraAttributesTypes(setOf(STRING.getValue(), INVISIBLE.getValue()));

		return data;
	}
	
	
	
	
	private ExpectedSavedData getExpectedUpdatedDataWithStocks() {
		var data = new ExpectedSavedData();

		data.setQuantities( setOf(101) );
		data.setPrices( setOf(new BigDecimal("10.25")));
		data.setCurrencies( setOf(EGP));
		
		data.setBarcodes( setOf("TT232222") );
		data.setProductNames( setOf("Squishy shoes") );
		data.setVariantsPNames(setOf("u_shoe") );
		data.setProductPNames(setOf("u_shoe") );
		data.setDescriptions( setOf("squishy") );
		data.setTags( setOf("squishy things") );
		data.setBrands( setOf(101L) );
		data.setFeatureSpecs(  createExpectedFeautreSpecOnlyUpdatedProducts());
		data.setExtraAttributes(  createExpectedExtraAttrUpdatedProduct());
		data.setStocksNum(1);
		data.setDiscounts(setOf(ZERO));
		data.setExtraAttributesTypes(setOf(STRING.getValue()));
		
		return data;
	}



	private ExpectedSavedData getExpectedUpdatedStocks() {
		var data = new ExpectedSavedData();

		data.setQuantities( setOf(101) );
		data.setPrices( setOf(new BigDecimal("10.25")));
		data.setCurrencies( setOf(EGP));

		data.setBarcodes( setOf("TT232222") );
		data.setProductNames( setOf("Product to update") );
		data.setVariantsPNames(setOf("u_shoe") );
		data.setProductPNames(setOf("u_shoe") );
		data.setDescriptions( setOf("old desc") );
		data.setTags( setOf() );
		data.setBrands( setOf(101L) );
		data.setFeatureSpecs(  setOf( new JSONObject()));
		data.setExtraAttributes(  setOf());
		data.setStocksNum(1);
		data.setDiscounts(setOf(ZERO));
		data.setExtraAttributesTypes(setOf());

		return data;
	}
	
	
	
	
	
	private ExpectedSavedData getExpectedUpdatedDataWithNullFeatures() {
		var data = new ExpectedSavedData();

		data.setQuantities( setOf(101) );
		data.setPrices( setOf(new BigDecimal("10.25")));
		data.setCurrencies( setOf(EGP));
		
		data.setBarcodes( setOf("TT232222") );
		data.setProductNames( setOf("Squishy shoes") );
		data.setVariantsPNames(setOf("u_shoe") );
		data.setProductPNames(setOf("u_shoe") );
		data.setDescriptions( setOf("squishy") );
		data.setTags( setOf("squishy things") );
		data.setBrands( setOf(101L) );
		data.setFeatureSpecs(  createExpectedNonChangedFeautreSpec());
		data.setExtraAttributes( emptySet());
		data.setStocksNum(1);
		data.setDiscounts(setOf(ZERO));
		
		return data;
	}
	
	
	
	
	
	private ExpectedSavedData getExpectedUpdatedDataForMultipleTags() {
		var data = new ExpectedSavedData();

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
		data.setDiscounts(setOf(ZERO));
		data.setExtraAttributesTypes(setOf(STRING.getValue()));
		
		return data;
	}




	private ExpectedSavedData getExpectedUpdatedDataForMultipleWithDifferentCaseTags() {
		var data = new ExpectedSavedData();

		data.setQuantities( setOf(101) );
		data.setPrices( setOf(new BigDecimal("10.25")));
		data.setCurrencies( setOf(EGP));

		data.setBarcodes( setOf("TT232222", "TT233333", "TT233344") );
		data.setProductNames( setOf("Squishy shoes", "Mounty Squishy shoes", "Hardy Squishy shoes") );
		data.setVariantsPNames(setOf("u_shoe", "color-lettuce-heart-size-xxl") );
		data.setProductPNames(setOf("u_shoe", "mounty-squishy-shoes", "hardy-squishy-shoes") );
		data.setDescriptions( setOf("squishy", "squishy but hard", "not hard") );
		data.setTags( setOf("squishy things", "mOuntain Equipment") );
		data.setBrands( setOf(101L) );
		data.setFeatureSpecs(  createExpectedFeautreSpecForOnlyUpdatedProduct());
		data.setExtraAttributes(  createExpectedExtraAttrForOnlyUpdatedProduct());
		data.setStocksNum(3);
		data.setDiscounts(setOf(ZERO));
		data.setExtraAttributesTypes(setOf(STRING.getValue()));

		return data;
	}




	private ExpectedSavedData getExpectedUpdatedDataWithUnits() {
		var data = new ExpectedSavedData();

		data.setQuantities( setOf(101, 102) );
		data.setPrices( setOf(new BigDecimal("10.25"), new BigDecimal("88.6")));
		data.setCurrencies( setOf(EGP));

		data.setBarcodes( setOf("1354ABN", "87847777EW") );
		data.setProductNames( setOf("Squishy shoes", "hard shoes") );
		data.setProductPNames(setOf("squishy-shoes", "hard-shoes") );
		data.setVariantsPNames(setOf("squishy-shoes", "hard-shoes") );
		data.setDescriptions( setOf("squishy", "too hard") );
		data.setTags( setOf("squishy things", "mountain equipment") );
		data.setBrands( setOf(101L, 102L) );
		data.setStocksNum(2);
		data.setDiscounts(setOf(ZERO));
		data.setUnits(setOf("KG", "m"));
		data.setSku(setOf("ABC123", "XYZ456"));
		data.setProductCodes(setOf("123-111", "123-222"));

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
		var data = new ExpectedSavedData();

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
		data.setDiscounts(setOf(ZERO));
		data.setExtraAttributesTypes(setOf(STRING.getValue()));
		
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


	private Set<JSONObject> createExpectedInvisibleExtraAttr() {
		return setOf(
				json()
					.put("extra", "ext2")
					.put("$not-feature-col", "ok")
				,
				json()
					.put("extra", "ext1")
					.put("$not-feature-col", "no")
		);
	}
	
	
	
	private Set<JSONObject> createExpectedExtraAttrUpdatedProduct() {
		return setOf(					
					json()
					 .put("extra", "ext1")
					 .put("not-feature-col", "no")
				);
	}
	
	
	
	
	private Set<JSONObject> createExpectedFeautreSpec() {
		Set<JSONObject> specs = new HashSet<>();
		var spec1 = createFeatureSpec("XXL", "Lettuce Heart");
		var spec2 = createFeatureSpec("M", "Fo7loqy");
		specs.addAll( asList(spec1,spec2));
		return specs;
	}
	
	
	
	private Set<JSONObject> createExpectedFeautreSpecOnlyUpdatedProducts() {
		Set<JSONObject> specs = new HashSet<>();
		var spec1 = createFeatureSpec("XXL", "Lettuce Heart");
		specs.addAll( asList(spec1));
		return specs;
	}
	
	
	
	
	
	
	private Set<JSONObject> createExpectedNonChangedFeautreSpec() {
		Set<JSONObject> specs = new HashSet<>();
		var spec1 = createFeatureSpec("6XL", "purple");
		specs.addAll( asList(spec1));
		return specs;
	}
	
	
	
	
	private Set<JSONObject> createExpectedFeautreSpecForOnlyUpdatedProduct() {
		Set<JSONObject> specs = new HashSet<>();
		var spec1 = createFeatureSpec("XXL", "Lettuce Heart");
		specs.addAll( asList(spec1));
		return specs;
	}
	
	






	private JSONObject createFeatureSpec(String size, String color) {
		var spec1 = new JSONObject();
		spec1.put("7001", size);
		spec1.put("7002", color);
		return spec1;
	}






	private ExpectedSavedData getExpectedNewOnlyAndUpdatedStocks() {
		var data = new ExpectedSavedData();

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
		data.setDiscounts(setOf(ZERO));
		data.setExtraAttributesTypes(setOf(STRING.getValue()));
		
		return data;
	}

	
	
	
	
	private ExpectedSavedData getExpectedNewOnlyWithoutStocks() {
		var data = new ExpectedSavedData();

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
		data.setDiscounts(setOf(ZERO));
		data.setExtraAttributesTypes(setOf(STRING.getValue()));
		
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
		var spec1 = new JSONObject("{}") ;
		var spec2 = createFeatureSpec("M", "Fo7loqy");
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
				.filter(Objects::nonNull)
				.allMatch(n -> expectedValues.stream().anyMatch( e-> e.compareTo(n) == 0));
	}
	


	private JSONObject createDataImportProperties() {

		var json = new JSONObject();
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
		var filePart = new MockMultipartFile("csv", csv.getFilename(), "text/csv", csv.getInputStream());
        
     	return uploadProductCsv(url, token, filePart, importProperties);
	}






	private ResultActions uploadProductCsv(String url, String token, MockMultipartFile filePart,
			JSONObject importProperties) throws Exception {
		var jsonPart = new MockPart("properties", "properties",  importProperties.toString().getBytes());
		jsonPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

		var result =
		    mockMvc.perform(MockMvcRequestBuilders
		    				.multipart(url)
			                 .file(filePart)
			                 .part(jsonPart)
			                 .header(TOKEN_HEADER, token)
			                 .cookie(new Cookie(TOKEN_HEADER, token)));
		return result;
	}
	

}


