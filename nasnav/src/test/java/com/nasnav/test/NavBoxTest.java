package com.nasnav.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.nasnav.controller.NavboxController;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dto.*;
import com.nasnav.persistence.BrandsEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.service.AdminService;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.NotThreadSafe;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static junit.framework.TestCase.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/ExtraAttributes_Test_Data_Insert.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
@NotThreadSafe
@Slf4j
public class NavBoxTest extends AbstractTestWithTempBaseDir {

    private HttpHeaders headers;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private AdminService adminService;

    @Autowired  private BrandsRepository brandsRepository;
    @Autowired  private ShopsRepository shopsRepository;
    @Autowired  private OrganizationRepository organizationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private NavboxController navboxController;

    @Before
    public void clearCache(){
        adminService.invalidateCaches();
    }



    @Test
    public void testBrands() {
        BrandsEntity brand = new BrandsEntity();
        brand.setName("MazooTestBrand");
        brand.setPname("mtb");
        brand.setBannerImage("/some/banner/image.png");
        brand.setLogo("/bright/logo/image.png");
        brandsRepository.save(brand);
        long objectId = brand.getId();

        ResponseEntity<String> response =  template.getForEntity("/navbox/brand/?brand_id=" + objectId, String.class);
        brandsRepository.delete(brand);
        JSONObject json = new JSONObject(response.getBody());

        Assert.assertEquals("MazooTestBrand", json.get("name"));
        Assert.assertEquals("mtb", json.get("p_name"));
        Assert.assertEquals("/bright/logo/image.png", json.get("logo_url"));
        Assert.assertEquals("/some/banner/image.png", json.get("banner"));

        // test non-existent brand_id
        response =  template.getForEntity("/navbox/brand/?brand_id=" + 1243124312341243L, String.class);
        Assert.assertEquals(404, response.getStatusCode().value());
    }


    
    
    
    
    @Test
    public void testShops() throws IOException {
        // TODO: no support for opening times yet
        ShopsEntity shop = shopsRepository.findById(100001L).get();
        long orgId = 99001L;
        OrganizationEntity org  = organizationRepository.findOneById(orgId);
        shop.setOrganizationEntity(org);
        shop.setName("SomeTestShop");
        shop.setPname("sts");
        shop.setLogo("/bright/logo/image.png");
        shop.setBanner("/some/shop/banner.png");
        shopsRepository.save(shop);
        
        long objectId = shop.getId();

        ResponseEntity<String> orgResponse = template.getForEntity("/navbox/shops?org_id=" + orgId, String.class);
        
        ResponseEntity<String> shopResponse = template.getForEntity("/navbox/shop?shop_id=" + objectId, String.class);

        JSONArray jsona = new JSONArray(orgResponse.getBody());
        Assert.assertEquals(1, jsona.length());
        JSONObject json = jsona.getJSONObject(0);
        Assert.assertEquals(objectId, json.getInt("id"));
        Assert.assertEquals("SomeTestShop", json.get("name"));
        Assert.assertEquals("sts", json.get("p_name"));
        Assert.assertEquals("/bright/logo/image.png", json.get("logo"));
        Assert.assertEquals("/some/shop/banner.png", json.get("banner"));

        json = new JSONObject(shopResponse.getBody());

        // test non-existent org_id
        orgResponse =  template.getForEntity("/navbox/shops?org_id=" + 1243124312341243L, String.class);
        assertEquals(200, orgResponse.getStatusCodeValue());
        List<ShopRepresentationObject> shopsList = objectMapper.readValue(orgResponse.getBody(), new TypeReference<List<ShopRepresentationObject>>(){});
        assertEquals(0, shopsList.size());

        // test non-existent shop_id
        shopResponse =  template.getForEntity("/navbox/shop?shop_id=" + 1243124312341243L, String.class);
        assertEquals(404, shopResponse.getStatusCodeValue());
    }


    @Test
    public void performExtraAttributesResponseTest(){
        //// testing extra attributes with no organization filter ////
        ResponseEntity<String> response = template.getForEntity("/navbox/attributes", String.class);
        log.debug(response.getBody());
        JSONArray  json = (JSONArray) JSONParser.parseJSON(response.getBody());

        assertEquals("there are total 2 attributes",2 , json.length());


        //// testing extra attributes with organization filter = 99001 ////
        response = template.getForEntity("/navbox/attributes?org_id=99001", String.class);
        log.debug(response.getBody());
        json = (JSONArray) JSONParser.parseJSON(response.getBody());
        assertEquals("there are total 1 attributes with organization = 99001",1 , json.length());


        //// testing extra attributes with false organization filter ////
        response = template.getForEntity("/navbox/attributes?org_id=403", String.class);
        assertTrue("there are no attributes with organization = 403",response.getBody().equals("[]"));
    }


    @Test
    public void testOrganizationsGetByName() {
        ResponseEntity<String> response = template.getForEntity("/navbox/organization?p_name=org-number-two", String.class);
        Assert.assertTrue(response.getStatusCodeValue() == 200);

        response = template.getForEntity("/navbox/organization?p_name=organization_2", String.class);
        Assert.assertTrue(response.getStatusCodeValue() == 200);

        response = template.getForEntity("/navbox/organization?p_name=organization 2", String.class);
        Assert.assertTrue(response.getStatusCodeValue() == 404);

        response = template.getForEntity("/navbox/organization?p_name=number-two", String.class);
        Assert.assertTrue(response.getStatusCodeValue() == 404);

        response = template.getForEntity("/navbox/organization?p_name=2", String.class);
        Assert.assertTrue(response.getStatusCodeValue() == 404);
    }


    @Test
    public void getCountries() {
        ResponseEntity<String> response = template.getForEntity("/navbox/countries", String.class);
        assertEquals(200, response.getStatusCodeValue());
        String name = JsonPath.read(response.getBody(), "$['UK']['name']");
        assertEquals(name, "UK");
    }



    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Countries_Invalid_Test_Data.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void getCountriesWithDuplicateAreas() {
        ResponseEntity<String> response = template.getForEntity("/navbox/countries", String.class);
        assertEquals(200, response.getStatusCodeValue());

        int id = JsonPath.read(response.getBody(), "$['Egypt']['cities']['Cairo']['areas']['New Cairo']['id']");
        assertEquals("smaller id should be picked for duplicate areas", 1, id);
    }



    @Test
    public void getCountriesNoOrgProvided() throws IOException {
        ResponseEntity<String> response = template.getForEntity("/navbox/countries", String.class);
        Map<String, CountriesRepObj> body =
                objectMapper.readValue(response.getBody(), new TypeReference<Map<String, CountriesRepObj>>(){});
        assertEquals(200, response.getStatusCodeValue());
        CountriesRepObj egypt = body.get("Egypt");
        assertTrue(egypt != null);
        CitiesRepObj cairo = egypt.getCities().get("Cairo");
        assertEquals("Cairo", cairo.getName());
        AreasRepObj newCairo = cairo.getAreas().get("new cairo");
        assertEquals("new cairo", newCairo.getName());
        SubAreasRepObj werwerLand = newCairo.getSubAreas().get("WerWer Land");
        assertNull("If no organization is provided, no sub-areas should be returned", werwerLand);
    }



    @Test
    public void getCountriesWithSubAreas() throws IOException {
        ResponseEntity<String> response = template.getForEntity("/navbox/countries?org_id=99001", String.class);
        Map<String, CountriesRepObj> body =
                objectMapper.readValue(response.getBody(), new TypeReference<Map<String, CountriesRepObj>>(){});
        assertEquals(200, response.getStatusCodeValue());
        CountriesRepObj egypt = body.get("Egypt");
        assertTrue(egypt != null);
        CitiesRepObj cairo = egypt.getCities().get("Cairo");
        assertEquals("Cairo", cairo.getName());
        AreasRepObj newCairo = cairo.getAreas().get("new cairo");
        assertEquals("new cairo", newCairo.getName());
        SubAreasRepObj werwerLand = newCairo.getSubAreas().get("WerWer Land");
        assertEquals("WerWer Land", werwerLand.getName());
    }



    @Test
    public void getCountriesForOrgWithNoSubAreas() throws IOException {
        ResponseEntity<String> response = template.getForEntity("/navbox/countries?org_id=99002", String.class);
        Map<String, CountriesRepObj> body =
                objectMapper.readValue(response.getBody(), new TypeReference<Map<String, CountriesRepObj>>(){});
        assertEquals(200, response.getStatusCodeValue());
        CountriesRepObj egypt = body.get("Egypt");
        assertTrue(egypt != null);
        CitiesRepObj cairo = egypt.getCities().get("Cairo");
        assertEquals("Cairo", cairo.getName());
        AreasRepObj newCairo = cairo.getAreas().get("new cairo");
        assertEquals("new cairo", newCairo.getName());
        SubAreasRepObj werwerLand = newCairo.getSubAreas().get("WerWer Land");
        assertNull("This organization has no sub-areas", werwerLand);
    }


    @Test
    public void getCountriesForOrgWithNoAllowedCountries() throws IOException {
        ResponseEntity<String> response = template.getForEntity("/navbox/countries?org_id=99001", String.class);
        Map<String, CountriesRepObj> body =
                objectMapper.readValue(response.getBody(), new TypeReference<Map<String, CountriesRepObj>>(){});
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, body.size());
    }

    @Test
    public void getCountriesForOrgWithOneAllowedCountries() throws IOException {
        ResponseEntity<String> response = template.getForEntity("/navbox/countries?org_id=99003", String.class);
        Map<String, CountriesRepObj> body =
                objectMapper.readValue(response.getBody(), new TypeReference<Map<String, CountriesRepObj>>() {
                });
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, body.size());
        CountriesRepObj uk = body.get("UK");
        assertTrue(uk != null);
    }

    @Test
    public void getCountriesForOrgWithInvalidAllowedCountries() throws IOException {
        ResponseEntity<String> response = template.getForEntity("/navbox/countries?org_id=99002", String.class);
        Map<String, CountriesRepObj> body =
                objectMapper.readValue(response.getBody(), new TypeReference<Map<String, CountriesRepObj>>() {
                });
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, body.size());
    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Shop_Test_Data_Insert_2.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void getLocationShopsDifferentFilters() throws JsonProcessingException {
        var response = template.getForEntity("/navbox/location_shops?org_id=99001", String.class);
        assertEquals(200, response.getStatusCodeValue());
        List<ShopRepresentationObject> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<List<ShopRepresentationObject>>() {});
        assertEquals(4, responseBody.size());

        // filter by area_id
        response = template.getForEntity("/navbox/location_shops?org_id=99001&area_id=1", String.class);
        assertEquals(200, response.getStatusCodeValue());
        responseBody = objectMapper.readValue(response.getBody(), new TypeReference<List<ShopRepresentationObject>>() {});
        assertEquals(2, responseBody.size());


        // filter by product names
        response = template.getForEntity("/navbox/location_shops?org_id=99001&name=product_2", String.class);
        assertEquals(200, response.getStatusCodeValue());
        responseBody = objectMapper.readValue(response.getBody(), new TypeReference<List<ShopRepresentationObject>>() {});
        assertEquals(1, responseBody.size());
        assertEquals(502, responseBody.get(0).getId().intValue());

        // filter by tag names
        response = template.getForEntity("/navbox/location_shops?org_id=99001&name=tag_1", String.class);
        assertEquals(200, response.getStatusCodeValue());
        responseBody = objectMapper.readValue(response.getBody(), new TypeReference<List<ShopRepresentationObject>>() {});
        assertEquals(3, responseBody.size());

        // filter by long and lat
        response = template.getForEntity("/navbox/location_shops?org_id=99001&latitude=30.056995&longitude=31.474868&radius=0.01", String.class);
        assertEquals(200, response.getStatusCodeValue());
        responseBody = objectMapper.readValue(response.getBody(), new TypeReference<List<ShopRepresentationObject>>() {});
        assertEquals(2, responseBody.size());

        // filter by product type
        response = template.getForEntity("/navbox/location_shops?org_id=99001&product_type=2", String.class);
        assertEquals(200, response.getStatusCodeValue());
        responseBody = objectMapper.readValue(response.getBody(), new TypeReference<List<ShopRepresentationObject>>() {});
        assertEquals(1, responseBody.size());
    }

    @Test
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Shop_Test_Data_Insert_3.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void getLocationShopsByBrandNameFilter() throws JsonProcessingException {

        var response = template.getForEntity("/navbox/location_shops?org_id=99002&name=brand_2", String.class);
        assertEquals(200, response.getStatusCodeValue());
        List<ShopRepresentationObject> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<List<ShopRepresentationObject>>() {});
        assertEquals(3, responseBody.size());

        response = template.getForEntity("/navbox/location_shops?org_id=99001&name=brand_1", String.class);
        assertEquals(200, response.getStatusCodeValue());
        responseBody = objectMapper.readValue(response.getBody(), new TypeReference<List<ShopRepresentationObject>>() {});
        assertEquals(2, responseBody.size());
    }




}