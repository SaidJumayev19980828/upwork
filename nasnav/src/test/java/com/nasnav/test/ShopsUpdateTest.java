package com.nasnav.test;

import com.nasnav.dao.AddressRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.service.ShopService;
import com.nasnav.test.commons.TestCommons;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.nasnav.test.commons.TestCommons.*;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Shop_Test_Data_Insert.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
@NotThreadSafe
public class ShopsUpdateTest extends AbstractTestWithTempBaseDir {

    @Autowired
    private TestRestTemplate template;
    
    @Autowired
    private ShopsRepository shopsRepository;

    @Autowired
    private AddressRepository addressRepo;

    @Autowired
    private ShopService shopService;
    
    @Autowired
    private StockRepository stocksRepo;

    @Test
    public void testCreateShopDifferentRoles(){
        // create shop using Org_Mananger role (test success)
        String body = "{\"org_id\":99001,\"shop_name\":\"Test_shop\"}";
        HttpEntity<Object> json = getHttpEntity(body,"161718");
        ResponseEntity<String> response = template.postForEntity("/shop/update", json, String.class);
        JSONObject jsonResponse = (JSONObject) JSONParser.parseJSON(response.getBody());
        shopsRepository.deleteById(jsonResponse.getLong("shop_id"));

        assertEquals(200, response.getStatusCode().value());

        // create shop using Store_Mananger role (test fail)
        json = getHttpEntity(body,"192021");
        response = template.postForEntity("/shop/update", json, String.class);
        jsonResponse = (JSONObject) JSONParser.parseJSON(response.getBody());

        assertEquals(403, response.getStatusCode().value());

        // create shop using NASNAV_ADMIN role (test fail)
        json = getHttpEntity(body,"101112");
        response = template.postForEntity("/shop/update", json, String.class);
        jsonResponse = (JSONObject) JSONParser.parseJSON(response.getBody());

        assertEquals(403, response.getStatusCode().value());

        // create shop using Org_Admin role (test fail)
        json = getHttpEntity(body,"131415");
        response = template.postForEntity("/shop/update", json, String.class);
        jsonResponse = (JSONObject) JSONParser.parseJSON(response.getBody());

        assertEquals(403, response.getStatusCode().value());
    }
    
    
    
    

    @Test
    public void testCreateShopDifferentOrganization(){
        String body = "{\"shop_name\":\"Test_shop\"}";
        HttpEntity<Object> json = getHttpEntity(body,"161718");
        ResponseEntity<String> response = template.postForEntity("/shop/update", json, String.class);
        JSONObject jsonResponse = (JSONObject) JSONParser.parseJSON(response.getBody());

        assertEquals(200, response.getStatusCode().value());
    }
    
    
    
    

    @Test
    public void testCreateShopDifferentData(){
        String body = "{\"address_country\": \"Egypt\",\n" +
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
        HttpEntity<Object> json = getHttpEntity(body,"161718");
        ResponseEntity<String> response = template.postForEntity("/shop/update", json, String.class);
        JSONObject jsonResponse = (JSONObject) JSONParser.parseJSON(response.getBody());

        assertEquals(200, response.getStatusCode().value());
        
        Long id = jsonResponse.getLong("shop_id");
        ShopsEntity saved = shopsRepository.findById(id).get();
        
        assertNotNull(saved.getOrganizationEntity());
        assertEquals(99001L, saved.getOrganizationEntity().getId().longValue());

        
        
        shopsRepository.deleteById(id);
    }

    
    
    
    
    @Test
    public void testUpdateShopDifferentData(){
        JSONObject address = json().put("flat_number", "Second")
                                .put("latitude", 30.0595581)
                                .put("longitude", 31.2234449)
                                .put("address_line_1", "Omar Bin Khatab");
        //create a shop first
        JSONObject body = json().put("address", address)
                                .put("banner", "/banners/banner_256.jpg")
                                .put("brand_id", 101)
                                .put("logo", "/brands/hugo_logo.jpg")
                                .put("photo", "/photos/photo_512.jpg")
                                .put("shop_name", "Eventure For Shipping");

        HttpEntity<Object> request = TestCommons.getHttpEntity(body.toString(),"161718");
        ResponseEntity<String> response = template.postForEntity("/shop/update", request, String.class);
        assertEquals(response.getBody(), HttpStatus.OK, response.getStatusCode());
        JSONObject jsonResponse = (JSONObject) JSONParser.parseJSON(response.getBody());
        

        //get created shop entity
        ShopsEntity oldShop = shopsRepository.findById(jsonResponse.getLong("shop_id")).get();
        //update shop data and check if other data remain the same
        String bodyString = json().put("org_id", 99001)
                .put("id", oldShop.getId())
                .put("brand_id", 102)
                .put("name", "Different Shop")
                .put("dark_logo", "dark logo value")
                .toString();
        request = getHttpEntity(bodyString,"161718");
        response = template.postForEntity("/shop/update", request, String.class);
        jsonResponse = (JSONObject) JSONParser.parseJSON(response.getBody());
        ShopsEntity newShop = shopsRepository.findById(jsonResponse.getLong("shop_id")).get();

        //check if unchanged data remains
        assertEquals(oldShop.getAddressesEntity().getFlatNumber(), newShop.getAddressesEntity().getFlatNumber());
        assertEquals(oldShop.getBanner(), newShop.getBanner());
        assertEquals(oldShop.getLogo(), newShop.getLogo());
        
        //check if changes applied
        assertEquals(102L, newShop.getBrandId().longValue());
        assertEquals("Different Shop", newShop.getName());
        assertEquals("dark logo value", newShop.getDarkLogo());

        shopsRepository.deleteById(jsonResponse.getLong("shop_id"));
    }


    @Test
    public void updateShopAddressTest() throws BusinessException {
        JSONObject address = json().put("address_line_1", "address line");

        JSONObject body = json().put("id", 502)
                                .put("address", address);


        HttpEntity request = getHttpEntity(body.toString(), "161718");

        //adding address to shop
        ResponseEntity<String> response = template.postForEntity("/shop/update", request, String.class);

        assertEquals(200, response.getStatusCodeValue());

        ShopRepresentationObject shop = shopService.getShopById(502L);

        assertTrue(shop.getAddress() != null);
        assertEquals("address line", shop.getAddress().getAddressLine1());

        // setting address to null (delinking address from shop)
        String json = "{\"id\": 502, \"address\": null}";
        request = getHttpEntity(json, "161718");
        response = template.postForEntity("/shop/update", request, String.class);

        assertEquals(200, response.getStatusCodeValue());
        addressRepo.deleteById(shop.getAddress().getId());
    }


    @Test
    public void deleteShopTest() {
        // create shop first
        JSONObject body = json().put("name", "new shop")
                                .put("org_id", 99001);
        HttpEntity<Object> request = getHttpEntity(body.toString(),"161718");
        ResponseEntity<String> response = template.postForEntity("/shop/update", request, String.class);
        JSONObject jsonResponse = (JSONObject) JSONParser.parseJSON(response.getBody());
        ShopsEntity oldShop = shopsRepository.findByIdAndRemoved(jsonResponse.getLong("shop_id")).get();

        // delete the created shop
        response = template.exchange("/shop/delete?shop_id="+oldShop.getId(),
                                        DELETE, request, String.class);

        assertEquals(200, response.getStatusCodeValue());
        assertFalse(shopsRepository.findByIdAndRemoved(oldShop.getId()).isPresent());
    }


    @Test
    public void deleteShopLinkedStocksTest() {
    	Long countBefore = stocksRepo.countByShopsEntity_Id(503L);
    	assertNotEquals(0L, countBefore.longValue());
    	
        HttpEntity<Object> request = getHttpEntity("161718");
        ResponseEntity<String> response = template.exchange("/shop/delete?shop_id=503",
                DELETE, request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        
        Long countAfter = stocksRepo.countByShopsEntity_Id(503L);
    	assertEquals(1L, countAfter.longValue());
    }


    @Test
    public void deleteShopDifferentLinkedEntitiesTest() {
        // delete shop 503 linked to stock
        HttpEntity<Object> request = getHttpEntity("161718");
        ResponseEntity<String> response = template.exchange("/shop/delete?shop_id=503",
                                                DELETE, request, String.class);
        assertEquals(200, response.getStatusCodeValue());


        // delete shop 504 linked to order
        response = template.exchange("/shop/delete?shop_id=504",
                                    DELETE, request, String.class);
        assertEquals(200, response.getStatusCodeValue());


        // delete shop 505 linked to shop360
        response = template.exchange("/shop/delete?shop_id=505",
                                    DELETE, request, String.class);
        assertEquals(200, response.getStatusCodeValue());

        // delete shop 502 linked to employee
        response = template.exchange("/shop/delete?shop_id=502",
                DELETE, request, String.class);
        assertEquals(406, response.getStatusCodeValue());
    }

    @Test
    public void setShopPriority() {
        ShopsEntity shop = shopsRepository.findById(505L).get();
        assertEquals(0, shop.getPriority().intValue());

        String json = json()
                .put("id", 505)
                .put("priority", 1)
                .toString();
        HttpEntity request = getHttpEntity(json, "161718");
        ResponseEntity<String> response = template.postForEntity("/shop/update", request, String.class);
        assertEquals(200, response.getStatusCodeValue());

        shop = shopsRepository.findById(505L).get();
        assertEquals(1, shop.getPriority().intValue());

        List<ShopsEntity> shops = shopsRepository.findByOrganizationEntity_IdAndRemovedOrderByPriorityDesc(99001L, 0);
        ShopsEntity firstShop = shops.get(0);
        assertEquals(505, firstShop.getId().intValue());
    }

    @Test
    public void setShopsPriority() {
        String json = jsonArray()
                .put(json()
                    .put("shop_id", 505)
                    .put("priority", 3)
                )
                .toString();
        HttpEntity request = getHttpEntity(json, "161718");
        ResponseEntity<String> response = template.postForEntity("/shop/priority", request, String.class);
        assertEquals(200, response.getStatusCodeValue());
        ShopsEntity shop = shopsRepository.findById(505L).get();
        assertEquals(3, shop.getPriority().intValue());
    }
}
