package com.nasnav.yeshtery.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dto.*;
import com.nasnav.dto.response.ProductsPositionDTO;
import com.nasnav.dto.response.navbox.VariantsResponse;
import com.nasnav.dto.response.CategoryDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

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
    private ObjectMapper mapper;

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

    @Test
    public void getBrandTest()  {
        var response = template.getForEntity("/v1/yeshtery/brand?brand_id=101", Organization_BrandRepresentationObject.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(101, response.getBody().getId().intValue());
    }

    @Test
    public void getCountriesTest() throws JsonProcessingException {
        var response = template.getForEntity("/v1/yeshtery/countries", String.class);
        assertEquals(200, response.getStatusCodeValue());
        Map<String, CountriesRepObj> countries =  mapper.readValue(response.getBody(), new TypeReference<Map<String, CountriesRepObj>>() {});
        assertEquals(1, countries.size());
    }

    @Test
    public void getShopsWithYeshteryProductsTest() throws JsonProcessingException {
        var response = template.getForEntity("/v1/yeshtery/location_shops?name=mountain", String.class);
        assertEquals(200, response.getStatusCodeValue());
        List<ShopRepresentationObject> shops = mapper.readValue(response.getBody(), new TypeReference<List<ShopRepresentationObject>>() {});
        assertTrue(shops.size() == 1);
        assertEquals(502, shops.get(0).getId().intValue());
    }

    @Test
    public void getYeshteryVariantsTest() {
        var response = template.getForEntity("/v1/yeshtery/variants?name=ABCD1234", VariantsResponse.class);
        assertEquals(200, response.getStatusCodeValue());
        List<VariantDTO> variants = response.getBody().getVariants();
        assertEquals(1, variants.size());
        assertEquals(310001, variants.get(0).getId().intValue());
    }

    @Test
    @Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Shop_360_Test_Data.sql"})
    @Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void getCollectionTest() {
        var response = template.getForEntity("/v1/yeshtery/collection?id=1004", ProductDetailsDTO.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1004, response.getBody().getId().intValue());
    }

    @Test
    @Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Shop_360_Test_Data.sql"})
    @Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void get360JsonData() {
        var response = template.getForEntity("/v1/yeshtery/360view/json_data?shop_id=501&type=web", String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("{}", response.getBody());
    }

    @Test
    @Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Shop_360_Test_Data.sql"})
    @Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void get360Setions() throws JsonProcessingException {
        var response = template.getForEntity("/v1/yeshtery/360view/sections?shop_id=501", String.class);
        assertEquals(200, response.getStatusCodeValue());
        Map body = mapper.readValue(response.getBody(), new TypeReference<Map>() {});
        assertEquals(1, body.size());
    }

    @Test
    @Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Shop_360_Test_Data.sql"})
    @Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void get360Shop()  {
        var response = template.getForEntity("/v1/yeshtery/360view/shops?shop_id=501", ShopThreeSixtyDTO.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(10010, response.getBody().getId().intValue());
    }

    @Test
    @Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Shop_360_Test_Data.sql"})
    @Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void get360ProductPositions()  {
        var response = template.getForEntity("/v1/yeshtery/360view/products_positions?shop_id=501&published=1", ProductsPositionDTO.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getProductsData().size());
        assertEquals(1, response.getBody().getCollectionsData().size());
    }

    @Test
    @Sql(executionPhase= Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts= {"/sql/Shop_360_Test_Data.sql"})
    @Sql(executionPhase= Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
    public void get360Products()  {
        var response = template.getForEntity("/v1/yeshtery/360view/products?shop_id=501&published=1", LinkedHashMap.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals(3, ((List)response.getBody().get("products")).size());
    }

    @Test
    public void getYeshteryProductsTest() {
        var response = template.getForEntity("/v1/yeshtery/products", ProductsResponse.class);
        assertEquals(200, response.getStatusCodeValue());
        List<ProductRepresentationObject> products = response.getBody().getProducts();
        assertEquals(3, products.size());
        assertEquals(1003, products.get(0).getId().intValue());
        assertEquals(1001, products.get(1).getId().intValue());
        assertEquals(1004, products.get(2).getId().intValue());
    }

    @Test
    public void getCategoriesTree() throws JsonProcessingException {
        var response = template.getForEntity("/v1/yeshtery/categories", String.class);
        assertEquals(OK, response.getStatusCode());

        var rootLevel = mapper.readValue(response.getBody(), new TypeReference<List<CategoryDto>>(){});
        assertRootLevelCategoriesReturned(rootLevel);
        assertCategoryHadImgInMetadata(rootLevel);
        assertFirstLevelCategoriesReturned(rootLevel);
    }

    @Test
    public void getYeshteryBrands() throws JsonProcessingException {
        var response = template.getForEntity("/v1/yeshtery/brands", String.class);
        assertEquals(200, response.getStatusCodeValue());
        PageImpl<Organization_BrandRepresentationObject> body = mapper.readValue(response.getBody(), new TypeReference<PageImpl<Organization_BrandRepresentationObject>>() {});
        assertEquals(1, body.getTotalPages());
        assertEquals(102, body.get().findFirst().get().getId().intValue());
    }



    private void assertFirstLevelCategoriesReturned(List<CategoryDto> rootLevel) {
        var firstLevel = getCategoriesOfFirstLevel(rootLevel);
        assertTrue(Set.of(203L, 204L, 205L, 206L).containsAll(firstLevel));
    }



    private void assertRootLevelCategoriesReturned(List<CategoryDto> rootLevel) {
        var ids = rootLevel.stream().map(CategoryDto::getId).collect(toSet());
        assertTrue(Set.of(201L, 202L).containsAll(ids));
        assertEquals(2 , rootLevel.size());
    }



    private void assertCategoryHadImgInMetadata(List<CategoryDto> rootLevel) {
        rootLevel.stream().filter(c -> Objects.equals(201L, c.getId())).findFirst().ifPresent(this::hasCoverImage);
    }



    private List<Long> getCategoriesOfFirstLevel(List<CategoryDto> rootLevel) {
        return rootLevel
                .stream()
                .map(CategoryDto::getChildren)
                .flatMap(List::stream)
                .map(CategoryDto::getId)
                .collect(toList());
    }


    private void hasCoverImage(CategoryDto category) {
        assertTrue(category.getMetadata().containsKey("cover"));
        assertTrue(category.getMetadata().containsKey("icon"));
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
