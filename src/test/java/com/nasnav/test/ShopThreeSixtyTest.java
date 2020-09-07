package com.nasnav.test;

import com.nasnav.NavBox;
import com.nasnav.dao.Product360ShopsRepository;
import com.nasnav.dto.response.PostProductPositionsResponse;
import com.nasnav.dto.response.ProductsPositionDTO;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import static com.nasnav.test.commons.TestCommons.*;
import static org.junit.Assert.*;
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
        assertTrue(!body.getProductsData().isEmpty());
        assertTrue(!body.getCollectionsData().isEmpty());
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
}
