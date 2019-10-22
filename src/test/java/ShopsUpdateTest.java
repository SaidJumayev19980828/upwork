
import com.nasnav.AppConfig;
import com.nasnav.NavBox;
import com.nasnav.controller.ShopsController;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.response.ShopResponse;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
public class ShopsUpdateTest {

    @Autowired
    private TestRestTemplate template;

    @Mock
    private ShopsController shopsController;

    @Autowired
    private AppConfig config;

    @Autowired
    private ShopsRepository shopsRepository;

    @Value("classpath:/sql/Shop_Test_Data_Insert.sql")
    private Resource userDataInsert;

    @Value("classpath:/sql/database_cleanup.sql")
    private Resource userDataDelete;

    @Autowired
    private DataSource datasource;

    @Before
    public void setup() {
        config.mailDryRun = true;
        MockMvcBuilders.standaloneSetup(shopsController).build();
        PerformSqlScript(userDataInsert);
    }

    @After
    public void cleanUp() {
        PerformSqlScript(userDataDelete);
    }

    public void PerformSqlScript(Resource resource){
        try (Connection con = datasource.getConnection()) {
            ScriptUtils.executeSqlScript(con, resource);
        } catch (
                SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateShopDifferentRoles(){
        // create shop using Org_Mananger role (test success)
        String body = "{\"org_id\":99001,\"shop_name\":\"Test_shop\"}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"161718");
        ResponseEntity<String> response = template.postForEntity("/shop/update", json, String.class);
        JSONObject jsonResponse = (JSONObject) JSONParser.parseJSON(response.getBody());
        shopsRepository.deleteById(jsonResponse.getLong("store_id"));

        Assert.assertEquals("", true, jsonResponse.getBoolean("success"));
        Assert.assertEquals(200, response.getStatusCode().value());

        // create shop using Store_Mananger role (test fail)
        json = TestCommons.getHttpEntity(body,"192021");
        response = template.postForEntity("/shop/update", json, String.class);
        jsonResponse = (JSONObject) JSONParser.parseJSON(response.getBody());

        Assert.assertEquals("", false, jsonResponse.getBoolean("success"));
        Assert.assertEquals(403, response.getStatusCode().value());

        // create shop using Nasnav_Admin role (test fail)
        json = TestCommons.getHttpEntity(body,"101112");
        response = template.postForEntity("/shop/update", json, String.class);
        jsonResponse = (JSONObject) JSONParser.parseJSON(response.getBody());

        Assert.assertEquals("", false, jsonResponse.getBoolean("success"));
        Assert.assertEquals(403, response.getStatusCode().value());

        // create shop using Org_Admin role (test fail)
        json = TestCommons.getHttpEntity(body,"131415");
        response = template.postForEntity("/shop/update", json, String.class);
        jsonResponse = (JSONObject) JSONParser.parseJSON(response.getBody());

        Assert.assertEquals("", false, jsonResponse.getBoolean("success"));
        Assert.assertEquals(403, response.getStatusCode().value());
    }

    @Test
    public void testCreateShopDifferentOrganization(){
        String body = "{\"org_id\":99002,\"shop_name\":\"Test_shop\"}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"161718");
        ResponseEntity<String> response = template.postForEntity("/shop/update", json, String.class);
        JSONObject jsonResponse = (JSONObject) JSONParser.parseJSON(response.getBody());

        Assert.assertEquals( false, jsonResponse.getBoolean("success"));
        Assert.assertEquals(403, response.getStatusCode().value());
    }

    @Test
    public void testCreateShopDifferentData(){
        String body = "{\"org_id\":99001,\n" +
                "  \"address_country\": \"Egypt\",\n" +
                "  \"address_floor\": \"Second\",\n" +
                "  \"address_lat\": 30.0595581,\n" +
                "  \"address_lng\": 31.2234449,\n" +
                "  \"address_street\": \"Omar Bin Khatab\",\n" +
                "  \"address_streetno\": 24,\n" +
                "  \"banner\": \"/banners/banner_256.jpg\",\n" +
                "  \"brand_id\": 101,\n" +
                "  \"logo\": \"/brands/hugo_logo.jpg\",\n" +
                "  \"mall_id\": 901,\n" +
                "  \"photo\": \"/photos/photo_512.jpg\",\n" +
                "  \"shop_name\": \"Eventure For Shipping\"\n" + "}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"161718");
        ResponseEntity<String> response = template.postForEntity("/shop/update", json, String.class);
        JSONObject jsonResponse = (JSONObject) JSONParser.parseJSON(response.getBody());
        shopsRepository.deleteById(jsonResponse.getLong("store_id"));

        Assert.assertEquals("", true, jsonResponse.getBoolean("success"));
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testUpdateShopDifferentData(){
        //create a shop first
        String body = "{\"org_id\":99001,\n" +
                "  \"address_country\": \"Egypt\",\n" +
                "  \"address_floor\": \"Second\",\n" +
                "  \"address_lat\": 30.0595581,\n" +
                "  \"address_lng\": 31.2234449,\n" +
                "  \"address_street\": \"Omar Bin Khatab\",\n" +
                "  \"address_streetno\": 24,\n" +
                "  \"banner\": \"/banners/banner_256.jpg\",\n" +
                "  \"brand_id\": 101,\n" +
                "  \"logo\": \"/brands/hugo_logo.jpg\",\n" +
                "  \"mall_id\": 901,\n" +
                "  \"photo\": \"/photos/photo_512.jpg\",\n" +
                "  \"shop_name\": \"Eventure For Shipping\"\n" + "}";
        HttpEntity<Object> json = TestCommons.getHttpEntity(body,"161718");
        ResponseEntity<String> response = template.postForEntity("/shop/update", json, String.class);
        JSONObject jsonResponse = (JSONObject) JSONParser.parseJSON(response.getBody());
        

        //get created shop entity
        ShopsEntity oldShop = shopsRepository.findById(jsonResponse.getLong("store_id")).get();
        //update shop data and check if other data remain the same
        body = "{\"org_id\":99001,\n" +
                "\"id\":" + oldShop.getId() +",\n" +
                "  \"brand_id\": 102,\n" +
                "  \"shop_name\": \"Different Shop\"\n" + "}";
        json = TestCommons.getHttpEntity(body,"161718");
        response = template.postForEntity("/shop/update", json, String.class);
        jsonResponse = (JSONObject) JSONParser.parseJSON(response.getBody());
        ShopsEntity newShop = shopsRepository.findById(jsonResponse.getLong("store_id")).get();

        //check if unchanged data remains
        Assert.assertEquals(oldShop.getFloor(), newShop.getFloor());
        Assert.assertEquals(oldShop.getBanner(), newShop.getBanner());
        Assert.assertEquals(oldShop.getLogo(), newShop.getLogo());
        
        //check if changes applied
        Assert.assertEquals(new Long(102), newShop.getBrandId());
        Assert.assertEquals("Different Shop", newShop.getName());

        shopsRepository.deleteById(jsonResponse.getLong("store_id"));
    }
}
