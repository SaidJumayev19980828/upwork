import com.nasnav.NavBox;
import com.nasnav.controller.NavboxController;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.persistence.BrandsEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ShopsEntity;
import org.json.JSONArray;
import org.json.JSONObject;
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
import org.springframework.http.*;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
public class NavBoxTest {

    private MockMvc mockMvc;
    HttpHeaders headers;

    @Autowired
    private TestRestTemplate template;

    @Value("classpath:sql/ExtraArrtibutes_Test_Data_Insert.sql")
    private Resource extraAttributesDataInsert;

    @Value("classpath:sql/ExtraArrtibutes_Test_Data_Delete.sql")
    private Resource extraAttributesDataDelete;

    @Autowired
    private DataSource datasource;

    @Autowired  private BrandsRepository brandsRepository;
    @Autowired  private ShopsRepository shopsRepository;
    @Autowired  private OrganizationRepository organizationRepository;

    @Mock
    private NavboxController navboxController;

    @Before
    public void setup() {
        headers = new HttpHeaders();
//        headers.se.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    public void testBrands() {
        BrandsEntity brand = new BrandsEntity();
        brand.setName("MazooTestBrand");
        brand.setPname("mtb");
        brand.setBannerImage("/some/banner/image.png");
        brand.setLogo("/bright/logo/image.png");
        brand.setCreatedAt(new Date());
        brand.setUpdatedAt(new Date());
        brandsRepository.save(brand);
        long objectId = brand.getId();

        ResponseEntity<String> response =  template.exchange(
                "/navbox/brand/?brand_id=" + objectId,
                HttpMethod.GET, new HttpEntity<>(headers), String.class);
        brandsRepository.delete(brand);
        JSONObject json = new JSONObject(response.getBody());

        Assert.assertEquals("MazooTestBrand", json.get("name"));
        Assert.assertEquals("mtb", json.get("p_name"));
        Assert.assertEquals("/bright/logo/image.png", json.get("logo"));
        Assert.assertEquals("/some/banner/image.png", json.get("banner"));

        // test non-existent brand_id
        response =  template.exchange(
                "/navbox/brand/?brand_id=" + 1243124312341243L,
                HttpMethod.GET, new HttpEntity<>(headers), String.class);
        Assert.assertEquals(404, response.getStatusCode().value());
    }


    @Test
    public void testShops() {
        // TODO: no support for opening times yet
        ShopsEntity shop = new ShopsEntity();
        OrganizationEntity org  = new OrganizationEntity();
        org.setCreatedAt(new Date());
        organizationRepository.save(org);
        long orgId = org.getId();
        shop.setOrganizationEntity(org);
        shop.setName("SomeTestShop");
        shop.setPname("sts");
        shop.setLogo("/bright/logo/image.png");
        shop.setBanner("/some/shop/banner.png");
        shop.setArea("Some Area");
        shop.setArea("some_area");
        shop.setCountry("EG");
        shop.setCity("Cairo");
        shop.setStreet("Bambino Street");
        shop.setFloor("3");
        shop.setLat(BigDecimal.valueOf(30.072994));
        shop.setLng(BigDecimal.valueOf(31.346011));
        shop.setCreatedAt(new Date());
        System.out.println("XXX " + shop.getOrganizationEntity().getId());
        shopsRepository.save(shop);
        long objectId = shop.getId();

        ResponseEntity<String> orgResponse =  template.exchange(
                "/navbox/shops?org_id=" + orgId,
                HttpMethod.GET, new HttpEntity<>(headers), String.class);
//System.out.println(orgResponse.getBody());
        ResponseEntity<String> shopResponse =  template.exchange(
                "/navbox/shop?shop_id=" + objectId,
                HttpMethod.GET, new HttpEntity<>(headers), String.class);
//System.out.println(orgResponse.getBody());

        shopsRepository.delete(shop);
        organizationRepository.delete(org);

        JSONArray jsona = new JSONArray(orgResponse.getBody());
        Assert.assertEquals(1, jsona.length());
//        Assert.assertTrue(json.getBoolean("success"));
        JSONObject json = jsona.getJSONObject(0);
        Assert.assertEquals(objectId, json.getInt("id"));
        Assert.assertEquals("SomeTestShop", json.get("name"));
        Assert.assertEquals("sts", json.get("p_name"));
        Assert.assertEquals("/bright/logo/image.png", json.get("logo"));
        Assert.assertEquals("/some/shop/banner.png", json.get("banner"));

        json = new JSONObject(shopResponse.getBody());

        // test non-existent org_id
        orgResponse =  template.exchange(
                "/navbox/shops?org_id=" + 1243124312341243L,
                HttpMethod.GET, new HttpEntity<>(headers), String.class);

        // test non-existent shop_id
        shopResponse =  template.exchange(
                "/navbox/shop?shop_id=" + 1243124312341243L,
                HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    @Test
    public void testExtraAttributes() {
        prepareExtraAttributesTestData();
        performExtraAttributesResponseTest();
        RemoveExtraAttributesTestData();
    }

    private void prepareExtraAttributesTestData(){
        try (Connection con = datasource.getConnection()) {
            ScriptUtils.executeSqlScript(con, this.extraAttributesDataInsert);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void RemoveExtraAttributesTestData(){
        try (Connection con = datasource.getConnection()) {
            ScriptUtils.executeSqlScript(con, this.extraAttributesDataDelete);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void performExtraAttributesResponseTest(){
        //// testing extra attributes with no organization filter ////
        ResponseEntity<String> response = template.getForEntity("/navbox/attributes", String.class);
        System.out.println(response.getBody());
        JSONArray  json = (JSONArray) JSONParser.parseJSON(response.getBody());

        assertEquals("there are total 2 attributes",2 , json.length());


        //// testing extra attributes with organization filter = 401 ////
        response = template.getForEntity("/navbox/attributes?org_id=401", String.class);
        System.out.println(response.getBody());
        json = (JSONArray) JSONParser.parseJSON(response.getBody());
        assertEquals("there are total 1 attributes with organization = 401",1 , json.length());


        //// testing extra attributes with false organization filter ////
        response = template.getForEntity("/navbox/attributes?org_id=403", String.class);
        System.out.println(response.getBody());
        assertNull("there are no attributes with organization = 403",response.getBody());
    }

/*
    @Test
    public void testOrganizations() {
        OrganizationEntity org = new OrganizationEntity();
        org.setName("TestOrg");
        org.setPName("torg");



        Assert.assertTrue(response.getBody().getMessages().contains(ResponseStatus.INVALID_EMAIL.name()));
        Assert.assertFalse(response.getBody().isSuccess());
    }
*/


}
