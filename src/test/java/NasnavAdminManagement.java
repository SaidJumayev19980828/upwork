
import com.nasnav.NavBox;
import com.nasnav.dao.TagsRepository;
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
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
public class NasnavAdminManagement {

    @Value("classpath:sql/Category_Test_Data_Insert.sql")
    private Resource dataInsert;
    @Value("classpath:sql/database_cleanup.sql")
    private Resource databaseCleanup;

    @Autowired
    private DataSource datasource;
    @Autowired
    private TestRestTemplate template;
    @Autowired
    private TagsRepository tagsRepo;

    @Before
    public void setup(){
        performSqlScript(databaseCleanup);
        performSqlScript(dataInsert);
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
    public void createNasnavTagSuccess() {
        String body = "{\"operation\":\"create\", \"name\":\"new tag\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"abcdefg");
        ResponseEntity<String> response = template.postForEntity("/admin/tag", json, String.class);

        JSONObject result = new JSONObject(response.getBody());
        tagsRepo.delete(tagsRepo.findById(result.getLong("tag_id")).get());

        Assert.assertTrue(result.getBoolean("success") == true);
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void createNasnavTagMissingOperation() {
        String body = "{\"name\":\"tag-name\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"abcdefg");
        ResponseEntity<String> response = template.postForEntity("/admin/tag", json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void createNasnavTagInvalidOperation() {
        String body = "{\"operation\":\"invalid operation\", \"name\":\"tag-name\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"abcdefg");
        ResponseEntity<String> response = template.postForEntity("/admin/tag", json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void createNasnavTagMissingName() {
        String body = "{\"operation\":\"create\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"abcdefg");
        ResponseEntity<String> response = template.postForEntity("/admin/tag", json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void createNasnavTagRedundantName() {
        String body = "{\"operation\":\"create\", \"name\":\"tag_1\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"abcdefg");
        ResponseEntity<String> response = template.postForEntity("/admin/tag", json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateNasnavTagSuccess() {
        String body = "{\"id\":5001,\"operation\":\"update\", \"name\":\"tag_8\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"abcdefg");
        ResponseEntity<String> response = template.postForEntity("/admin/tag", json, String.class);

        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void updateNasnavTagMissingId() {
        String body = "{\"operation\":\"update\", \"name\":\"tag_8\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"abcdefg");
        ResponseEntity<String> response = template.postForEntity("/admin/tag", json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }

    @Test
    public void updateNasnavTagInvalidId() {
        String body = "{\"id\":5008,\"operation\":\"update\", \"name\":\"tag_8\"}";

        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"abcdefg");
        ResponseEntity<String> response = template.postForEntity("/admin/tag", json, String.class);

        Assert.assertEquals(406, response.getStatusCode().value());
    }
}
