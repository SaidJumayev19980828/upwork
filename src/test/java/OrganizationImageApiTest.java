import java.sql.Connection;
import java.sql.SQLException;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
import com.nasnav.dto.OrganizationImagesRepresentationObject;
import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.test.commons.TestCommons;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc
@PropertySource("classpath:test.database.properties")
public class OrganizationImageApiTest {

    @Value("classpath:test_imgs_to_upload/nasnav--Test_Photo_UPDATED.png")
    private Resource file;

    @Value("classpath:sql/Organizations_image_API_Test_Data_Insert.sql")
    private Resource organizationsDataInsert;
    
    @Value("classpath:sql/database_cleanup.sql")
    private Resource databaseCleanup;

    @Autowired
    private DataSource datasource;

    @Value("${files.basepath}")
    private String basePathStr;

    @Autowired
    private TestRestTemplate template;

    
    
    @Before
    public void setup(){
        performSqlScript(databaseCleanup);
        performSqlScript(organizationsDataInsert);
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
