import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.nasnav.NavBox;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.CategoriesRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.persistence.CategoriesEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.security.AuthenticationFilter;
import com.nasnav.test.helpers.TestHelper;

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
	
	private static final String URL_UPLOAD_PRODUCTLIST = "/upload/productlist";


	private static final Long TEST_IMPORT_SHOP = 100003L;


	@Value("classpath:/files/product__list_upload.csv")
    private Resource csvFile;
	
	
	@Value("classpath:/files/product__list_upload_missing_col.csv")
    private Resource csvFileMissingCol;
	
	
	@Value("classpath:/files/product__list_upload_invalid_data.csv")
    private Resource csvFileInvalidData;
	
	
	@Autowired
	private  MockMvc mockMvc;
	
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private ProductVariantsRepository variantRepo;
	
	@Autowired
	private StockRepository stocksRepo;
	
	@Autowired
	private CategoriesRepository categoriesRepo;
	
	@Autowired
	private BrandsRepository brandRepo;
	
	@Autowired
	private TestHelper helper;
	
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



	
	
	@Test
    public void uploadProductsCSVMissingCategoryHeadersTest() throws IOException, Exception {
       
		JSONObject importProperties = createDataImportProperties();
		importProperties.getJSONObject("headers").remove("category_header");		
        
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "131415", csvFile, importProperties);
        
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
        
        String responsebody = result.andExpect(status().is(406))
						        	  .andReturn()
						        	  .getResponse()
						        	  .getContentAsString();
        
        JSONObject bodyJson = new JSONObject(responsebody);
        assertTrue(bodyJson.has("error"));    
        JSONArray errors = new JSONArray(bodyJson.getString("error"));
        assertEquals(3, errors.length());
    }
	
	
	
	
	
	//TODO test valid run
	@Test
	public void uploadProductCSVNewData() throws IOException, Exception {
		JSONObject importProperties = createDataImportProperties();
		importProperties.put("shop_id", TEST_IMPORT_SHOP);
        
		Long productCountBefore = productRepo.count();
		Long variantCountBefore = variantRepo.count();
		Long stocksCountBefore = stocksRepo.count();
		
		
		ResultActions result = uploadProductCsv(URL_UPLOAD_PRODUCTLIST , "ggr45r5", csvFile, importProperties);
		
		Long productCountAfter = productRepo.count();
		Long variantCountAfter = variantRepo.count();
		Long stocksCountAfter = stocksRepo.count();
		
		List<StocksEntity> stocks = helper.getShopStocksFullData(TEST_IMPORT_SHOP);
        List<ProductVariantsEntity> variants = stocks.stream()
													.map(StocksEntity::getProductVariantsEntity)
													.collect(Collectors.toList());

        List<ProductEntity> products = variants.stream()
												.map(v -> v.getProductEntity())
												.collect(Collectors.toList());
        
        result.andExpect(status().is(200));
        
        
        Set<Integer> quantities = new HashSet<>( Arrays.asList(101,102) );
        Set<BigDecimal> prices = new HashSet<>( Arrays.asList(new BigDecimal("10.25"), new BigDecimal("88.6")));
        Set<String> barcodes = new HashSet<>( Arrays.asList("1354ABN", "87847777EW") );
        Set<String> ProductNames = new HashSet<>( Arrays.asList("Squishy shoes", "hard shoes") );
        Set<String> PNames = new HashSet<>( Arrays.asList("s_shoe", "h_shoe") );
        Set<String>  descriptions = new HashSet<>( Arrays.asList("squishy", "too hard") );
        Set<Long> categories = new HashSet<>( Arrays.asList(201L,202L) );
        Set<Long> brands = new HashSet<>( Arrays.asList(101L, 102L) );
        
        
        
        assertEquals(2, productCountAfter - productCountBefore);
        assertEquals(2, variantCountAfter - variantCountBefore);
        assertEquals(2, stocksCountAfter - stocksCountBefore);
        
        
        
        assertEquals(2, stocks.size());
        assertTrue( stocks.stream()
        				.map(StocksEntity::getCurrency)
        				.allMatch( c -> Objects.equals(c, TransactionCurrency.EGP) ) );
        
        assertTrue( compareEntityFieldValues(stocks, StocksEntity::getQuantity, quantities)	);
        assertTrue( compareEntityBigDecimalFieldValues(stocks, StocksEntity::getPrice, prices)	);

        
                
        
        assertTrue( compareEntityFieldValues(variants, ProductVariantsEntity::getBarcode, barcodes)	);
        assertTrue( compareEntityFieldValues(variants, ProductVariantsEntity::getName, ProductNames) );
        assertTrue( compareEntityFieldValues(variants, ProductVariantsEntity::getPname, PNames) );
        assertTrue( compareEntityFieldValues(variants, ProductVariantsEntity::getDescription, descriptions) );
        
        
        assertTrue( compareEntityFieldValues(products, ProductEntity::getName, ProductNames) );
        assertTrue( compareEntityFieldValues(products, ProductEntity::getPname, PNames) );
        assertTrue( compareEntityFieldValues(products, ProductEntity::getDescription, descriptions) );
        assertTrue( compareEntityFieldValues(products, ProductEntity::getCategoryId, categories) );
        assertTrue( compareEntityFieldValues(products, ProductEntity::getBrandId, brands) );
       
	}
	
	
	
	
	private <T,V>  boolean  compareEntityFieldValues(List<T> entityList, Function<T,V> getter, Set<V> expectedValues) {
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
		return colHeadersJson;
	}

}
