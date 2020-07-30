package com.nasnav.test;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.springframework.http.HttpMethod.DELETE;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;

import javax.sql.DataSource;

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
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dto.Organization_BrandRepresentationObject;
import com.nasnav.service.BrandService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
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
    @Autowired
    private BrandsRepository brandRepo;
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
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void setBrandMissingOperationTest() {
        String body = "{\"name\":\"Alfa Romero\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void setBrandInvalidOperationTest() {
        String body = "{\"operation\":\"invalid_operation\", \"name\":\"Alfa Romero\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void createBrandMissingNameTest() {
        String body = "{\"operation\":\"create\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(406, response.getStatusCode().value());
    }

    
    
    
    @Test
    public void createBrandInvalidPnameTest() {
        String body = "{\"operation\":\"create\", \"name\":\"Alfa Romero\", \"p_name\":\"12Alfa Romero#\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
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
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateBrandInvalidNameTest() {
        String body = "{\"operation\":\"update\", \"brand_id\": 101,\"name\":\"12Alfa Romero#\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void updateBrandInvalidPnameTest() {
        String body = "{\"operation\":\"update\", \"brand_id\": 101,\"name\":\"Alfa Romero\", \"p_name\":\"12Alfa Romero#\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
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
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateBrandMissingIdTest() {
        String body = "{\"operation\":\"update\", \"name\":\"Alfa Romero\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateBrandInvalidIdTest() {
        String body = "{\"operation\":\"update\", \"brand_id\": 9999999, \"name\":\"Alfa Romero\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map, "hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void updateBrandUnauthorizedUserTest() {
        String body = "{\"operation\":\"update\", \"brand_id\": 101, \"name\":\"Alfa Romero\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map,"abcdefg", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(403, response.getStatusCode().value());
    }

    @Test
    public void createBrandUnauthorizedUserTest() {
        String body = "{\"operation\":\"create\", \"name\":\"Alfa Romero\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map,"abcdefg", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(403, response.getStatusCode().value());
    }

    @Test
    public void updateBrandSuccessTest() {
        String body = "{\"operation\":\"update\", \"brand_id\": 101, \"name\":\"Alfa Romero\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void updateBrandPname() throws IOException {
        String body = "{\"operation\":\"update\", \"brand_id\": 101, \"name\":\"Alfa Romero\", \"p_name\":\"p-name\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        LinkedHashMap<?, ?> mapRes = (LinkedHashMap<?, ?>) response.getBody();
        Organization_BrandRepresentationObject brand = brandSvc.getBrandById(((Integer) mapRes.get("brand_id")).longValue());

        Assert.assertEquals("p-name", brand.getPname());
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void getBrandsTest() {
        ResponseEntity<List> response = template.getForEntity("/organization/brands?org_id=99001", List.class);
        Assert.assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals(3 ,response.getBody().size());

        response = template.getForEntity("/organization/brands?org_id=99002", List.class);
        Assert.assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals(1 ,response.getBody().size());

        response = template.getForEntity("/organization/brands?org_id=999999", List.class);
        Assert.assertTrue(200 == response.getStatusCode().value());
        Assert.assertEquals(0 ,response.getBody().size());
    }


    @Test
    public void deleteBrand() {
    	Long brandId = 103L;
    	Assert.assertTrue(brandRepo.existsById(brandId));
        HttpEntity<?> request = getHttpEntity("hijkllm");
        ResponseEntity<String> res = 
        		template.exchange("/organization/brand?brand_id="+ brandId, DELETE, request, String.class);
        Assert.assertEquals(200, res.getStatusCodeValue());
        Assert.assertFalse(brandRepo.existsByIdAndRemoved(brandId, 0));
    }


    @Test
    public void deleteBrandInvalidId() {
        HttpEntity<?> request = getHttpEntity("hijkllm");
        ResponseEntity<String> res = template.exchange("/organization/brand?brand_id=-1", DELETE,
                request, String.class);
        Assert.assertEquals(406, res.getStatusCodeValue());
    }



    @Test
    public void deleteBrandInvalidToken() {
        HttpEntity<?> request = getHttpEntity("hijkdllm");
        ResponseEntity<String> res = template.exchange("/organization/brand?brand_id=104", DELETE,
                request, String.class);
        Assert.assertEquals(401, res.getStatusCodeValue());
    }


    @Test
    public void deleteBrandUnauthenticated() {
        HttpEntity<?> request = getHttpEntity("abcdefg");
        ResponseEntity<String> res = template.exchange("/organization/brand?brand_id=103", DELETE,
                request, String.class);
        Assert.assertEquals(403, res.getStatusCodeValue());
    }


    @Test
    public void deleteBrandDifferentOrg() {
        HttpEntity<?> request = getHttpEntity("123456");
        ResponseEntity<String> res = template.exchange("/organization/brand?brand_id=103", DELETE,
                request, String.class);
        Assert.assertEquals(406, res.getStatusCodeValue());
    }
}
