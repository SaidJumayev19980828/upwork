import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.nasnav.NavBox;
import com.nasnav.security.AuthenticationFilter;

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


	@Value("classpath:/files/product__list_upload.csv")
    private Resource csvFile;
	
	
	@Autowired
	private  MockMvc mockMvc;
	
	
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
		MockMultipartFile filePart = new MockMultipartFile("csv", csv.getFilename(), "text/csv", csvFile.getInputStream());
        
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
		   colHeadersJson.put("quantity_header", "quantity");
		   colHeadersJson.put("price_header", "price");
		return colHeadersJson;
	}

}
