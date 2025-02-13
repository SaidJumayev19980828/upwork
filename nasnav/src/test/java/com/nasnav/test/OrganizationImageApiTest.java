package com.nasnav.test;

import com.nasnav.dto.OrganizationImagesRepresentationObject;
import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.test.commons.TestCommons;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.sql.DataSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Organizations_image_API_Test_Data_Insert.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class OrganizationImageApiTest extends AbstractTestWithTempBaseDir {

    @Value("classpath:test_imgs_to_upload/nasnav--Test_Photo_UPDATED.png")
    private Resource file;

    @Autowired
    private DataSource datasource;

    @Autowired
    private TestRestTemplate template;

    void performSqlScript(Resource resource) {
        try (Connection con = datasource.getConnection()) {
            ScriptUtils.executeSqlScript(con, resource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    @Test
    public void organizationImageMissingImageIdTest() {
        String body = "{\"org_id\":99002, \"operation\":\"update\"" + "}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("image", file);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,  "456", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/image", json, Object.class);
        Assert.assertEquals(406, response.getStatusCode().value());
        Assert.assertTrue(response.getBody().toString().contains("MISSING PARAM"));
    }

    @Test
    public void organizationNewImageMissingImageTest() {
        String body = "{\"org_id\":99002, \"operation\":\"create\"" + "}";
        Resource emptyFile = null;
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("image", emptyFile);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"456", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/image", json, Object.class);
        Assert.assertEquals(406, response.getStatusCode().value());
        Assert.assertTrue(response.getBody().toString().contains("MISSING PARAM"));
    }

    @Test
    public void organizationImageUpdateNoImgTest() {
        String body = "{\"org_id\":99002, \"operation\":\"update\", \"image_id\":10101 " + "}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("image", file);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"456", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/image", json, Object.class);
        Assert.assertEquals(406, response.getStatusCode().value());
    }
    
    
    

    @Test
    public void organizationImageMissingOrganizationIdTest() {
        String body = "{ \"operation\":\"create\"" + "}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("image", file);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"456", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/image", json, Object.class);
        Assert.assertEquals(406, response.getStatusCode().value());
        Assert.assertTrue(response.getBody().toString().contains("MISSING PARAM"));
    }
    
    

    @Test
    public void organizationImageNullOrganizationTest() {
        String body = "{\"org_id\":null, \"operation\":\"create\"" + "}";
        Resource emptyFile = null;
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("image", emptyFile);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"456", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/image", json, Object.class);
        Assert.assertEquals(406, response.getStatusCode().value());
        Assert.assertTrue(response.getBody().toString().contains("MISSING PARAM"));
    }
    
    
    

    @Test
    public void organizationImageNonExistingOrganizationIdTest() {
        String body = "{\"org_id\":99004, \"operation\":\"create\"" + "}";
        Resource emptyFile = null;
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("image", emptyFile);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map, "456", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/image", json, Object.class);
        Assert.assertEquals(406, response.getStatusCode().value());
        Assert.assertTrue(response.getBody().toString().contains("MISSING PARAM"));
    }
    
    
    

    @Test
    public void organizationImageOrganizationIdFromAnotherOrgTest() {
        String body = "{\"org_id\":99001, \"operation\":\"create\", \"type\":1 " + "}";
        Resource emptyFile = null;
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("image", emptyFile);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"456", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/image", json, Object.class);
        Assert.assertEquals(403, response.getStatusCode().value());
    }
    
    

    @Test
    public void organizationImageMissingOperationTest() {
        String body = "{\"org_id\":99002 }";
        Resource emptyFile = null;
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("image", emptyFile);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"456", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/image", json, Object.class);
        Assert.assertEquals(406, response.getStatusCode().value());
        Assert.assertTrue(response.getBody().toString().contains("MISSING PARAM"));
    }
    
    

    @Test
    public void organizationNewImageUploadTest() {
        String body = "{\"org_id\":99002, \"operation\":\"create\", \"type\":1 " + "}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("image", file);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"456", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/image", json, Object.class);
        Assert.assertEquals(200, response.getStatusCode().value());
    }
    
    

    @Test
    public void organizationUpdateImageUploadTest() {
        String body = "{\"org_id\":99002, \"operation\":\"update\", \"image_id\": 901}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("image", file);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"456", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/image", json, Object.class);
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void getOrganizationImagesTest() {
        String body = "{\"org_id\":99002, \"operation\":\"create\", \"type\":1 }";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("image", file);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"456", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/image", json, Object.class);
        Assert.assertEquals(200, response.getStatusCode().value());

        response = template.postForEntity("/organization/image", json, Object.class);
        Assert.assertEquals(200, response.getStatusCode().value());

        response = template.postForEntity("/organization/image", json, Object.class);
        Assert.assertEquals(200, response.getStatusCode().value());

        ResponseEntity<OrganizationRepresentationObject> getResponse = template.getForEntity("/navbox/organization?org_id=99002",
                         OrganizationRepresentationObject.class);

        OrganizationRepresentationObject obj =  getResponse.getBody();
        List<OrganizationImagesRepresentationObject> imgList = obj.getImages();
        ResponseEntity<byte[]> res;
        for(OrganizationImagesRepresentationObject image : imgList) {
            if (image.getId() != 901) { // image 901 is dummy and created by sql query
                res = template.getForEntity("/files/" + image.getUri(), byte[].class);
                Assert.assertEquals(200, res.getStatusCode().value());
                Assert.assertEquals("image", res.getHeaders().getContentType().getType());
            }
        }
    }

    @Test
    public void getImagesInfo() {
        HttpEntity<Object> request = TestCommons.getHttpEntity("456");
        ResponseEntity<String> response = template.exchange("/organization/images_info?org_id=99002", GET, request, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        InputStream targetStream = new ByteArrayInputStream(response.getBody().getBytes());
        CsvParser parser = new CsvParser(new CsvParserSettings());
        List<String[]> parsedRows = parser.parseAll(targetStream);
        assertEquals(1, parsedRows.size());
    }

    @Test
    @Ignore
    //serving static resources files, depends on taking configurations from AppConfig/
    //this is done at the configuration phase, and I can't still mock the configuration
    //at the configuration phase, without using ContextInitializer and slowing down the tests.
    public void getShopImagesTest() {
        String body = "{\"org_id\":99002, \"shop_id\":501, \"operation\":\"create\", \"type\":1 }";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("image", file);
        HttpEntity<Object> json = TestCommons.getHttpEntity(map,"456", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Object> response = template.postForEntity("/organization/image", json, Object.class);
        Assert.assertEquals(200, response.getStatusCode().value());

        response = template.postForEntity("/organization/image", json, Object.class);
        Assert.assertEquals(200, response.getStatusCode().value());

        response = template.postForEntity("/organization/image", json, Object.class);
        Assert.assertEquals(200, response.getStatusCode().value());

        ResponseEntity<OrganizationRepresentationObject> getResponse = template.getForEntity("/navbox/shop?shop_id=501",
                OrganizationRepresentationObject.class);

        OrganizationRepresentationObject obj =  getResponse.getBody();
        List<OrganizationImagesRepresentationObject> imgList = obj.getImages();
        ResponseEntity<byte[]> res;
        for(OrganizationImagesRepresentationObject image : imgList) {
                res = template.getForEntity("/files/" + image.getUri(), byte[].class);
                Assert.assertEquals(200, res.getStatusCode().value());
                Assert.assertEquals("image", res.getHeaders().getContentType().getType());
        }
    }

}
