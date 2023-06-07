package com.nasnav.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dao.Product360ShopsRepository;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.dto.response.PostProductPositionsResponse;
import com.nasnav.dto.response.ProductsPositionDTO;
import com.nasnav.dto.response.navbox.ThreeSixtyProductsDTO;
import net.jcip.annotations.NotThreadSafe;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

import static com.nasnav.test.commons.TestCommons.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/database_cleanup.sql", "/sql/Shop_360_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
public class ShopThreeSixtyTest {

    @Autowired
    private TestRestTemplate template;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private Product360ShopsRepository product360ShopsRepo;

    @Test
    public void getProductsPositions() {
        //get preview products positions
        ResponseEntity<ProductsPositionDTO> res = template.getForEntity("/360view/products_positions?shop_id=501&published=1", ProductsPositionDTO.class);
        assertEquals(200, res.getStatusCodeValue());
        ProductsPositionDTO body = res.getBody();
        assertEquals(501, body.getShopId().intValue());
        assertFalse(body.getProductsData().isEmpty());
        assertFalse(body.getCollectionsData().isEmpty());


        //get published products positions
        res = template.getForEntity("/360view/products_positions?shop_id=501&published=2", ProductsPositionDTO.class);
        assertEquals(200, res.getStatusCodeValue());
        body = res.getBody();
        assertEquals(501, body.getShopId().intValue());
        assertFalse(body.getProductsData().isEmpty());
        assertFalse(body.getCollectionsData().isEmpty());
    }


    @Test
    public void postProductsPositions() {
        JSONObject productPosition = json()
                .put("id", 1005)
                .put("scene_id", 100012)
                .put("pitch", 1001)
                .put("yaw", 1001);

        JSONArray body = jsonArray().put(productPosition);

        Long countBefore = product360ShopsRepo.findProductsPositionsByShopId(502L).stream().count();

        HttpEntity request = getHttpEntity(body.toString(), "131415");
        ResponseEntity<PostProductPositionsResponse> response = template.postForEntity("/360view/products_positions?shop_id=502",
                                                                            request, PostProductPositionsResponse.class);

        Long countAfter = product360ShopsRepo.findProductsPositionsByShopId(502L).stream().count();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(countAfter.intValue(), countBefore + 1);
    }


    @Test
    public void postProductsPositionsMissingProduct() {
        JSONObject productPosition = json()
                .put("id", 1001)
                .put("scene_id", 100012)
                .put("pitch", 1001)
                .put("yaw", 1001);

        JSONArray body = jsonArray().put(productPosition);

        Long countBefore = product360ShopsRepo.findProductsPositionsByShopId(502L).stream().count();

        HttpEntity request = getHttpEntity(body.toString(), "131415");
        ResponseEntity<PostProductPositionsResponse> response = template.postForEntity("/360view/products_positions?shop_id=502",
                request, PostProductPositionsResponse.class);

        Long countAfter = product360ShopsRepo.findProductsPositionsByShopId(502L).stream().count();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(countAfter.intValue(), countBefore.intValue());
        assertEquals(1, response.getBody().getMissingProducts().size());
    }

    @Test
    public void postProductsPositionsMissingScene() {
        JSONObject productPosition = json()
                .put("id", 1005)
                .put("scene_id", 10001)
                .put("pitch", 1001)
                .put("yaw", 1001);

        JSONArray body = jsonArray().put(productPosition);

        Long countBefore = product360ShopsRepo.findProductsPositionsByShopId(502L).stream().count();

        HttpEntity request = getHttpEntity(body.toString(), "131415");
        ResponseEntity<PostProductPositionsResponse> response = template.postForEntity("/360view/products_positions?shop_id=502",
                request, PostProductPositionsResponse.class);

        Long countAfter = product360ShopsRepo.findProductsPositionsByShopId(502L).stream().count();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(countAfter.intValue(), countBefore.intValue());
        assertEquals(1, response.getBody().getMissingScenes().size());
    }

    @Test
    public void postProductsPositionsAuthN() {
        JSONArray body = jsonArray();
        HttpEntity request = getHttpEntity(body.toString(), "invalid");
        ResponseEntity<PostProductPositionsResponse> response = template.postForEntity("/360view/products_positions?shop_id=502",
                request, PostProductPositionsResponse.class);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    public void postProductsPositionsAuthZ() {
        JSONArray body = jsonArray();
        HttpEntity request = getHttpEntity(body.toString(), "222324");
        ResponseEntity<PostProductPositionsResponse> response = template.postForEntity("/360view/products_positions?shop_id=502",
                request, PostProductPositionsResponse.class);
        assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    public void getAllProducts() throws IOException {
        ResponseEntity<String> response = template.getForEntity("/360view/products?shop_id=502", String.class);
        assertEquals(200, response.getStatusCodeValue());
        JSONObject object = new JSONObject(response.getBody());
        List<ThreeSixtyProductsDTO> products = mapper.readValue(object.get("products").toString(), new TypeReference<List<ThreeSixtyProductsDTO>>(){});
        // shop 502 has variants 310004, 310005, 310006
        // variant 310004 has product (1004) and collection (1007) , variant 310005 has product (1005) , variant 310006 has product (1006) and collection (1004, 1008)
        // the intersection of these products and collections is 1004, 1005, 1006, 1007, 1008 = 5 items
        assertEquals(5, products.size());
    }

    @Test
    public void getCollectionsOnly() throws IOException {
        ResponseEntity<String> response = template.getForEntity("/360view/products?shop_id=502&product_type=2", String.class);
        assertEquals(200, response.getStatusCodeValue());
        JSONObject object = new JSONObject(response.getBody());
        List<ThreeSixtyProductsDTO> products = mapper.readValue(object.get("products").toString(), new TypeReference<List<ThreeSixtyProductsDTO>>(){});
        assertEquals("there are 3 collections 1007, 1008, 1009 in shop 502", 3, products.size());
    }

    @Test
    public void getProductsOnly() throws IOException {
        ResponseEntity<String> response = template.getForEntity("/360view/products?shop_id=502&product_type=0", String.class);
        assertEquals(200, response.getStatusCodeValue());
        JSONObject object = new JSONObject(response.getBody());
        List<ThreeSixtyProductsDTO> products = mapper.readValue(object.get("products").toString(), new TypeReference<List<ThreeSixtyProductsDTO>>(){});

        assertEquals("there are 2 products in shop 502", 2, products.size());
    }

    @Test
    public void get360Products() throws IOException {
        ResponseEntity<String> response = template.getForEntity("/360view/products?shop_id=502&has_360=true", String.class);
        assertEquals(200, response.getStatusCodeValue());
        JSONObject object = new JSONObject(response.getBody());
        List<ThreeSixtyProductsDTO> products = mapper.readValue(object.get("products").toString(), new TypeReference<List<ThreeSixtyProductsDTO>>(){});
        assertEquals(1, products.size());
    }

    @Test
    public void getShop() {
        var response = template.getForEntity("/360view/shop?shop_id=501", ShopRepresentationObject.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Long.valueOf(99001), response.getBody().getOrgId());
    }
}
