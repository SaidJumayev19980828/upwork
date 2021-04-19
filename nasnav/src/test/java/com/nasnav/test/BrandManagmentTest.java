package com.nasnav.test;

import com.nasnav.NavBox;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dto.Organization_BrandRepresentationObject;
import com.nasnav.service.BrandService;
import org.json.JSONObject;
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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.DELETE;

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
        String body = createBrandRequestBody().toString();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("logo", file);
        map.add("banner", file);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void setBrandMissingOperationTest() {
        String body = createBrandRequestBody().put("operation", JSONObject.NULL).toString();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void setBrandInvalidOperationTest() {
        String body = createBrandRequestBody().put("operation", "invalid_operation").toString();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void createBrandMissingNameTest() {
        String body = createBrandRequestBody().put("name", JSONObject.NULL).toString();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        assertEquals(406, response.getStatusCode().value());
    }

    
    
    
    @Test
    public void createBrandInvalidPnameTest() {
        String body = createBrandRequestBody().put("p_name", "12Alfa Romero#").toString();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        assertEquals(406, response.getStatusCode().value());
    }
    
    
    

    @Test
    public void createBrandInvalidFilesTest() {
        String body = createBrandRequestBody().toString();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("logo", databaseCleanup);
        map.add("banner", brandsDataInsert);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        assertEquals(406, response.getStatusCode().value());
    }


    @Test
    public void updateBrandInvalidPnameTest() {
        String body = updateBrandRequestBody().put("p_name", "12Alfa Romero#").toString();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateBrandInvalidFilesTest() {
        String body = updateBrandRequestBody().toString();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("logo", databaseCleanup);
        map.add("banner", brandsDataInsert);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateBrandMissingIdTest() {
        String body = updateBrandRequestBody().put("brand_id", JSONObject.NULL).toString();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateBrandInvalidIdTest() {
        String body = updateBrandRequestBody().put("brand_id", 99999).toString();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map, "hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void updateBrandUnauthorizedUserTest() {
        String body = updateBrandRequestBody().put("brand_id", 101).toString();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map,"abcdefg", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    public void createBrandUnauthorizedUserTest() {
        String body = createBrandRequestBody().toString();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map,"abcdefg", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    public void updateBrandSuccessTest() {
        String body = updateBrandRequestBody().toString();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        assertEquals(200, response.getStatusCode().value());
    }


    @Test
    public void updateBrandDifferentOrgTest() {
        String body = updateBrandRequestBody().put("brand_id", 101).toString();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map, "hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void updateBrandPname() {
        String body = updateBrandRequestBody()
                .put("p_name", "p-name")
                .toString();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/brand", json, Object.class);
        LinkedHashMap<?, ?> mapRes = (LinkedHashMap<?, ?>) response.getBody();
        Organization_BrandRepresentationObject brand = brandSvc.getBrandById(((Integer) mapRes.get("brand_id")).longValue());

        assertEquals("p-name", brand.getPname());
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void getBrandsTest() {
        ResponseEntity<List> response = template.getForEntity("/organization/brands?org_id=99001", List.class);
        Assert.assertTrue(200 == response.getStatusCode().value());
        assertEquals(3 ,response.getBody().size());

        response = template.getForEntity("/organization/brands?org_id=99002", List.class);
        Assert.assertTrue(200 == response.getStatusCode().value());
        assertEquals(1 ,response.getBody().size());

        response = template.getForEntity("/organization/brands?org_id=999999", List.class);
        Assert.assertTrue(200 == response.getStatusCode().value());
        assertEquals(0 ,response.getBody().size());
    }


    @Test
    public void deleteBrand() {
    	Long brandId = 103L;
    	Assert.assertTrue(brandRepo.existsById(brandId));
        HttpEntity<?> request = getHttpEntity("hijkllm");
        ResponseEntity<String> res = 
        		template.exchange("/organization/brand?brand_id="+ brandId, DELETE, request, String.class);
        assertEquals(200, res.getStatusCodeValue());
        Assert.assertFalse(brandRepo.existsByIdAndRemoved(brandId, 0));
    }


    @Test
    public void deleteBrandLinkedProducts() {
        Long brandId = 104L;
        Assert.assertTrue(brandRepo.existsById(brandId));
        HttpEntity<?> request = getHttpEntity("hijkllm");
        ResponseEntity<String> res =
                template.exchange("/organization/brand?brand_id="+ brandId, DELETE, request, String.class);
        assertEquals(406, res.getStatusCodeValue());
    }


    @Test
    public void deleteBrandInvalidId() {
        HttpEntity<?> request = getHttpEntity("hijkllm");
        ResponseEntity<String> res = template.exchange("/organization/brand?brand_id=-1", DELETE,
                request, String.class);
        assertEquals(406, res.getStatusCodeValue());
    }



    @Test
    public void deleteBrandInvalidToken() {
        HttpEntity<?> request = getHttpEntity("hijkdllm");
        ResponseEntity<String> res = template.exchange("/organization/brand?brand_id=104", DELETE,
                request, String.class);
        assertEquals(401, res.getStatusCodeValue());
    }


    @Test
    public void deleteBrandUnauthenticated() {
        HttpEntity<?> request = getHttpEntity("abcdefg");
        ResponseEntity<String> res = template.exchange("/organization/brand?brand_id=103", DELETE,
                request, String.class);
        assertEquals(403, res.getStatusCodeValue());
    }


    @Test
    public void deleteBrandDifferentOrg() {
        HttpEntity<?> request = getHttpEntity("123456");
        ResponseEntity<String> res = template.exchange("/organization/brand?brand_id=103", DELETE,
                request, String.class);
        assertEquals(406, res.getStatusCodeValue());
    }

    private JSONObject createBrandRequestBody() {
        return json()
                .put("operation", "create")
                .put("name", "Alfa Romero");
    }

    private JSONObject updateBrandRequestBody() {
        return json()
                .put("operation", "update")
                .put("brand_id", 102)
                .put("name", "Alfa Romero");
    }
}
