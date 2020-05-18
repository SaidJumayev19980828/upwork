import static com.nasnav.test.commons.TestCommons.*;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.nasnav.persistence.OrganizationEntity;
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

import com.nasnav.NavBox;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.test.commons.TestCommons;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
public class OrganizationManagmentTest {
    @Value("classpath:sql/Organization_Test_Data_Insert.sql")
    private Resource organizationsDataInsert;
    @Value("classpath:sql/database_cleanup.sql")
    private Resource databaseCleanup;
    @Value("classpath:test_imgs_to_upload/nasnav--Test_Photo.png")
    private Resource file;
    @Autowired
    private DataSource datasource;
    @Autowired
    private TestRestTemplate template;
    @Autowired
    private OrganizationRepository organizationRepository;

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
    public void updateOrganizationDataSuccessTest() {
        String body = "{\"org_id\":99001, \"description\":\"this company is old and unique\"," +
                       "\"social_twitter\": \"https://www.twitter.com/fortunestores/\"," +
                       "\"social_facebook\": \"https://www.facebook.com/fortune.stores11/\"," +
                       "\"social_instagram\": \"https://www.instagram.com/islamify/\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("logo", file);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MULTIPART_FORM_DATA);
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/organization/info", json, OrganizationResponse.class);
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    //trying to update organization with nasnav_admin user
    @Test
    public void updateOrganizationUnauthorizedUserTest() {
        String body = "{\"org_id\":99001, \"description\":\"this company is old and unique\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("logo", file);
        HttpEntity<Object> json = getHttpEntity(map,"abcdefg", MULTIPART_FORM_DATA);
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/organization/info", json, OrganizationResponse.class);
        Assert.assertEquals(403, response.getStatusCode().value());
    }

    @Test
    public void updateOrganizationInvalidIdTest() {
        String body = "{\"org_id\":99005,\"description\":\"this company is old and unique\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("logo", file);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MULTIPART_FORM_DATA);
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/organization/info", json, OrganizationResponse.class);
        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateOrganizationMissingIdTest() {
        String body = "{\"description\":\"this company is old and unique\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("logo", file);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/organization/info", json, OrganizationResponse.class);
        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateOrganizationInvalidSocialLinksTest() {
        // invalid twitter link
        String body = "{\"org_id\":99005, \"social_twitter\": \"htps://www.twitte.com/fortunestores\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("logo", file);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/organization/info", json, OrganizationResponse.class);
        Assert.assertEquals(406, response.getStatusCode().value());

        // invalid facebook link
        body = "{\"org_id\":99005, \"social_facebook\": \"htts://www.faceboo.com/fortune.stores11/\"}";
        map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("logo", file);
        json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        response = template.postForEntity("/organization/info", json, OrganizationResponse.class);
        Assert.assertEquals(406, response.getStatusCode().value());

        // invalid instagram link
        body = "{\"org_id\":99005, \"social_instagram\": \"htps://instgram.com/fortunestores\"}";
        map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("logo", file);
        json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        response = template.postForEntity("/organization/info", json, OrganizationResponse.class);
        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateOrganizationInvalidLogoTest() {
        String body = "{\"org_id\":99001, \"description\":\"this company is old and unique\"}";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("properties", body);
        map.add("logo", organizationsDataInsert);
        HttpEntity<Object> json = getHttpEntity(map,"hijkllm", MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/organization/info", json, OrganizationResponse.class);
        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void createOrganizationSuccessTest() {
        String body = "{\"name\":\"Solad Pant\", \"p_name\":\"solad-pant-trello\"}";
        HttpEntity<Object> json = getHttpEntity(body,"abcdefg");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization", json,
                OrganizationResponse.class);
        organizationRepository.deleteById(response.getBody().getOrganizationId());
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void createOrganizationMissingValuesTest() {
        String body = "{\"p_name\":\"solad-pant\"}";
        HttpEntity<Object> json = getHttpEntity(body, "abcdefg");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization", json,
                OrganizationResponse.class);
        Assert.assertEquals(406, response.getStatusCode().value());

        body = "{\"name\":\"Solad Pant\"}";
        json = getHttpEntity(body,"abcdefg");
        response = template.postForEntity("/admin/organization", json, OrganizationResponse.class);
        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void createOrganizationInvalidValuesTest() {
        String body = "{\"name\":\"23Solad Pant#\", \"p_name\":\"solad-pant\"}";
        HttpEntity<Object> json = getHttpEntity(body,"abcdefg");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization", json,
                OrganizationResponse.class);
        Assert.assertEquals(406, response.getStatusCode().value());

        body = "{\"name\":\"Solad Pant\", \"p_name\":\"solad_pant#$!^*\"}";
        json = getHttpEntity(body,"abcdefg");
        response = template.postForEntity("/admin/organization", json, OrganizationResponse.class);
        Assert.assertEquals(406, response.getStatusCode().value());
    }

    //trying to create organization with organization_admin user
    @Test
    public void createOrganizationUnauthorizedUserTest() {
        String body = "{\"name\":\"Solad Pant\", \"p_name\":\"solad-pant\"}";
        HttpEntity<Object> json = getHttpEntity(body,"hijkllm");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization", json,
                OrganizationResponse.class);
        Assert.assertEquals(403, response.getStatusCode().value());
    }



    @Test
    public void getOrgByURLTestSuccess() throws URISyntaxException {
        URI url = new URI("http://www.fortune.nasnav.com/");
        ResponseEntity<String> response = template.getForEntity("/navbox/orgid?url="+ url, String.class);

        checkSuccessResponse(response);

        url = new URI("https://www.nasnav.com/fortune/product/74");
        response = template.getForEntity("/navbox/orgid?url="+ url, String.class);

        checkSuccessResponse(response);

        url = new URI("www.fortune-egypt.com/categories/");
        response = template.getForEntity("/navbox/orgid?url="+ url, String.class);

        checkSuccessResponse(response);
    }

    @Test
    public void getOrgByURLNoOrgTest() throws URISyntaxException {
        URI url = new URI("http://www.invaliddomain.nasnav.com/");
        ResponseEntity<String> response = template.getForEntity("/navbox/orgid?url="+ url, String.class);
        checkFailResponse(response);

        url = new URI("https://www.nasnav.com/invaliddomain/product/74");
        response = template.getForEntity("/navbox/orgid?url="+ url, String.class);
        checkFailResponse(response);

        url = new URI("https://www.invaliddomain-egypt.com/categories/");
        response = template.getForEntity("/navbox/orgid?url="+ url, String.class);
        checkFailResponse(response);
    }

    @Test
    public void updateOrgNameTest () {
        JSONObject body = json().put("id", 99001)
                                .put("name", "new org name")
                                .put("p_name", "org-name");
        HttpEntity<Object> json = getHttpEntity(body.toString(),"abcdefg");
        ResponseEntity<OrganizationResponse> response = template.postForEntity("/admin/organization",
                json, OrganizationResponse.class);
        Assert.assertEquals(200, response.getStatusCode().value());
        OrganizationEntity org = organizationRepository.findOneById(99001L);
        Assert.assertEquals("new org name", org.getName());
        Assert.assertEquals("org-name", org.getPname());
    }


    private void checkSuccessResponse(ResponseEntity<String> response) {
        JSONObject json = new JSONObject(response.getBody());

        Assert.assertEquals(200, response.getStatusCode().value());
        Assert.assertEquals(99001, json.getInt("id"));
    }

    private void checkFailResponse(ResponseEntity<String> response) {
        JSONObject json = new JSONObject(response.getBody());

        Assert.assertEquals(200, response.getStatusCode().value());
        Assert.assertEquals(0, json.getInt("id"));
    }

}