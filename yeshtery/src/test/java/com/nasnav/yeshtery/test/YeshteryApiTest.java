package com.nasnav.yeshtery.test;

import com.nasnav.dao.ProductRepository;
import com.nasnav.enumerations.ProductFeatureType;
import com.nasnav.yeshtery.Yeshtery;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Yeshtery.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Products_Test_Data_Insert.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class YeshteryApiTest {
    private final String PRODUCT_FEATURE_1_NAME = "Lispstick Color";
    private final String PRODUCT_FEATURE_1_P_NAME = "lipstick_color";
    private final String PRODUCT_FEATURE_2_NAME = "Lipstick flavour";
    private final String PRODUCT_FEATURE_2_P_NAME = "lipstick_flavour";


    @Autowired
    private TestRestTemplate template;

    @Autowired
    private ProductRepository productRepo;


    @Test
    public void getProductWithMultipleVariantsTest() {
        var response = template.getForEntity("/v1/yeshtery/product?product_id=1001", String.class);

        var expectedVariantFeatures = createExpectedFeaturesJson();
        var product = new JSONObject(response.getBody());
        var variantFeatures = product.getJSONArray("variant_features");
        var variants = product.getJSONArray("variants");

        assertEquals("Product 1001 has 5 variants, only the 4 with stock records will be returned", 4, variants.length());
        assertEquals("The product have only 2 variant features", 2, variantFeatures.length());
        assertTrue(variantFeatures.similar(expectedVariantFeatures));
    }



    @Test
    public void getProductInNonRegisteredOrgTest() {
        var productId = 1002L;
        var orgId = 99002L;
        assertTrue(productRepo.existsByIdAndOrganizationId(productId, orgId));
        var response = template.getForEntity("/v1/yeshtery/product?product_id="+productId, String.class);
        assertEquals(NOT_FOUND, response.getStatusCode());
    }



    private JSONArray createExpectedFeaturesJson() {
        var expectedFeature1 = new JSONObject();
        expectedFeature1.put("name", PRODUCT_FEATURE_1_NAME);
        expectedFeature1.put("label", PRODUCT_FEATURE_1_P_NAME);
        expectedFeature1.put("type", ProductFeatureType.STRING.name());
        expectedFeature1.put("extra_data", new JSONObject());

        var expectedFeature2 = new JSONObject();
        expectedFeature2.put("name", PRODUCT_FEATURE_2_NAME);
        expectedFeature2.put("label", PRODUCT_FEATURE_2_P_NAME);
        expectedFeature2.put("type", ProductFeatureType.STRING.name());
        expectedFeature2.put("extra_data", new JSONObject());

        return new JSONArray(Arrays.asList(expectedFeature1, expectedFeature2));
    }
}
