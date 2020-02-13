import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;

import javax.sql.DataSource;

import com.nasnav.dto.Organization_BrandRepresentationObject;
import com.nasnav.service.BrandService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.nasnav.NavBox;
import com.nasnav.test.commons.TestCommons;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
public class BrandManagmentTest {

    @Value("classpath:sql/Organization_Test_Data_Insert.sql")
    private Resource brandsDataInsert;
    @Value("classpath:sql/database_cleanup.sql")
    private Resource databaseCleanup;
    @Value("classpath:test_imgs_to_upload/nasnav--Test_Photo.png")
    private Resource file;
    @Autowired
    private DataSource datasource;
    @Autowired
    private TestRestTemplate template;
    @Autowired
    private BrandService brandSvc;

    @Before
    public void setup(){
        performSqlScript(databaseCleanup);
        performSqlScript(brandsDataInsert);
    }

    @After
    public void cleanup(){
        performSqlScript(databaseCleanup);
    }

    void performSqlScript(Resource resource) {
        try (Connection con = datasource.getConnection()) {
            ScriptUtils.executeSqlScript(con, resource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createBrandSuccessTest() {
        String body = "{\"operation\":\"create\", \"name\":\"Alfa Romero\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("logo", file);
        map.add("banner", file);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void setBrandMissingOperationTest() {
        String body = "{\"name\":\"Alfa Romero\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void setBrandInvalidOperationTest() {
        String body = "{\"operation\":\"invalid_operation\", \"name\":\"Alfa Romero\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void createBrandMissingNameTest() {
        String body = "{\"operation\":\"create\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(406, response.getStatusCode().value());
    }

    
    
    
    @Test
    public void createBrandInvalidPnameTest() {
        String body = "{\"operation\":\"create\", \"name\":\"Alfa Romero\", \"p_name\":\"12Alfa Romero#\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(406, response.getStatusCode().value());
    }
    
    
    

    @Test
    public void createBrandInvalidFilesTest() {
        String body = "{\"operation\":\"create\", \"name\":\"Alfa Romero\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("logo", databaseCleanup);
        map.add("banner", brandsDataInsert);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateBrandInvalidNameTest() {
        String body = "{\"operation\":\"update\", \"brand_id\": 101,\"name\":\"12Alfa Romero#\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void updateBrandInvalidPnameTest() {
        String body = "{\"operation\":\"update\", \"brand_id\": 101,\"name\":\"Alfa Romero\", \"p_name\":\"12Alfa Romero#\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateBrandInvalidFilesTest() {
        String body = "{\"operation\":\"update\", \"brand_id\": 101,\"name\":\"Alfa Romero\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("logo", databaseCleanup);
        map.add("banner", brandsDataInsert);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateBrandMissingIdTest() {
        String body = "{\"operation\":\"update\", \"name\":\"Alfa Romero\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateBrandInvalidIdTest() {
        String body = "{\"operation\":\"update\", \"brand_id\": 9999999, \"name\":\"Alfa Romero\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map, "hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void updateBrandUnauthorizedUserTest() {
        String body = "{\"operation\":\"update\", \"brand_id\": 101, \"name\":\"Alfa Romero\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"abcdefg", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(403, response.getStatusCode().value());
    }

    @Test
    public void createBrandUnauthorizedUserTest() {
        String body = "{\"operation\":\"create\", \"name\":\"Alfa Romero\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"abcdefg", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(403, response.getStatusCode().value());
    }

    @Test
    public void updateBrandSuccessTest() {
        String body = "{\"operation\":\"update\", \"brand_id\": 101, \"name\":\"Alfa Romero\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void updateBrandPname() throws IOException {
        String body = "{\"operation\":\"update\", \"brand_id\": 101, \"name\":\"Alfa Romero\", \"p_name\":\"p-name\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        LinkedHashMap mapRes = (LinkedHashMap) response.getBody();
        Organization_BrandRepresentationObject brand = brandSvc.getBrandById(((Integer) mapRes.get("brand_id")).longValue());

        Assert.assertEquals("p-name", brand.getPname());
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void getBrandsTest() {
        ResponseEntity<List> response = template.getForEntity("/organization/brands?org_id=99001", List.class);
        Assert.assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals(1 ,response.getBody().size());

        response = template.getForEntity("/organization/brands?org_id=99002", List.class);
        Assert.assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals(1 ,response.getBody().size());

        response = template.getForEntity("/organization/brands?org_id=999999", List.class);
        Assert.assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals(0 ,response.getBody().size());
    }
}
