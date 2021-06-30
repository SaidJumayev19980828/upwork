package com.nasnav.yeshtery.test;

import com.nasnav.yeshtery.Yeshtery;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Yeshtery.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@NotThreadSafe
public class YeshteryRecommendationApiTest {

    @Autowired
    private TestRestTemplate template;

    @Test
    public void getProductRatingRecommendTest() {
        var orgId = 66L;
        var response = template.getForEntity("/v1/yeshtery/recommend/rating?orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductRatingRecommendOrgNotExistsTest() {
        var orgId = 23L;
        var response = template.getForEntity("/v1/yeshtery/recommend/rating?orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductRatingRecommendWithoutOrgTest() {
        var response = template.getForEntity("/v1/yeshtery/recommend/rating", String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductRatingRecommendByTagTest() {
        var tagId = 11661L;
        var orgId = 66L;
        var response = template.getForEntity("/v1/yeshtery/recommend/rating/tag?tagid="+tagId+"&orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductRatingRecommendByTagWithOrgNotExistsTest() {
        var tagId = 11661L;
        var orgId = 23L;
        var response = template.getForEntity("/v1/yeshtery/recommend/rating/tag?tagid="+tagId+"&orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductRatingRecommendWithoutOrgByTagTest() {
        var tagId = 11661L;
        var response = template.getForEntity("/v1/yeshtery/recommend/rating/tag?tagid="+tagId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductRatingRecommendByTagNotExistsTest() {
        var tagId = 301L;
        var orgId = 66L;
        var response = template.getForEntity("/v1/yeshtery/recommend/rating/tag?tagid="+tagId+"&orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductRatingRecommendByTagNotExistsWithOrgNotExistsTest() {
        var tagId = 301L;
        var orgId = 23L;
        var response = template.getForEntity("/v1/yeshtery/recommend/rating/tag?tagid="+tagId+"&orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductRatingRecommendByTagNotExistsWithoutOrgTest() {
        var tagId = 301L;
        var response = template.getForEntity("/v1/yeshtery/recommend/rating/tag?tagid="+tagId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendTest() {
        var orgId = 66L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling?orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendWithOrgNotExistsTest() {
        var orgId = 23L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling?orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendWithoutOrgTest() {
        var response = template.getForEntity("/v1/yeshtery/recommend/selling", String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByTagTest() {
        var tagId = 11661L;
        var orgId = 66L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/tag?tagid="+tagId+"&orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByTagWithOrgNotExistsTest() {
        var tagId = 11661L;
        var orgId = 23L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/tag?tagid="+tagId+"&orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByTagWithoutOrgTest() {
        var tagId = 11661L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/tag?tagid="+tagId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByTagNotExistsTest() {
        var tagId = 301L;
        var orgId = 66L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/tag?tagid="+tagId+"&orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByTagNotExistsWithOrgNotExistsTest() {
        var tagId = 301L;
        var orgId = 23L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/tag?tagid="+tagId+"&orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByTagNotExistsWithoutOrgTest() {
        var tagId = 301L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/tag?tagid="+tagId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByShopTest() {
        var shopId = 528L;
        var orgId = 66L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/shop?shopid="+shopId+"&orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByShopWithOrgNotExistsTest() {
        var shopId = 528L;
        var orgId = 23L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/shop?shopid="+shopId+"&orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByShopWithoutOrgTest() {
        var shopId = 528L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/shop?shopid="+shopId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByShopNotExistsTest() {
        var shopId = 305L;
        var orgId = 66L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/shop?shopid="+shopId+"&orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByShopNotExistsWithOrgNotExistsTest() {
        var shopId = 305L;
        var orgId = 23L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/shop?shopid="+shopId+"&orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByShopNotExistsWithoutOrgTest() {
        var shopId = 305L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/shop?shopid="+shopId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByShopTagTest() {
        var shopId = 528L;
        var tagId = 11661L;
        var orgId = 66L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/shop?tagid="+tagId+"&shopid="+shopId+"&orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByShopTagWithOrgNotExistsTest() {
        var shopId = 528L;
        var tagId = 11661L;
        var orgId = 23L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/shop?tagid="+tagId+"&shopid="+shopId+"&orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByShopTagWithoutOrgTest() {
        var shopId = 528L;
        var tagId = 11661L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/shop?tagid="+tagId+"&shopid="+shopId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByShopTagNotExistsTagTest() {
        var shopId = 528L;
        var tagId = 301L;
        var orgId = 66L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/shop?tagid="+tagId+"&shopid="+shopId+"&orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByShopTagNotExistsTagWithOrgNotExistsTest() {
        var shopId = 528L;
        var tagId = 301L;
        var orgId = 23L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/shop?tagid="+tagId+"&shopid="+shopId+"&orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByShopTagNotExistsTagWithoutOrgTest() {
        var shopId = 528L;
        var tagId = 301L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/shop?tagid="+tagId+"&shopid="+shopId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByShopTagNotExistsShopTest() {
        var shopId = 305L;
        var tagId = 11661L;
        var orgId = 66L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/shop?tagid="+tagId+"&shopid="+shopId+"&orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByShopTagNotExistsShopWithOrgNotExistsTest() {
        var shopId = 305L;
        var tagId = 11661L;
        var orgId = 23L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/shop?tagid="+tagId+"&shopid="+shopId+"&orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByShopTagNotExistsShopWithoutOrgTest() {
        var shopId = 305L;
        var tagId = 11661L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/shop?tagid="+tagId+"&shopid="+shopId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByShopTagNotExistsTest() {
        var shopId = 305L;
        var tagId = 301L;
        var orgId = 66L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/shop?tagid="+tagId+"&shopid="+shopId+"&orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByShopTagNotExistsWithOrgNotExistsTest() {
        var shopId = 305L;
        var tagId = 301L;
        var orgId = 23L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/shop?tagid="+tagId+"&shopid="+shopId+"&orgid="+orgId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getProductSellingRecommendByShopTagNotExistsWithoutOrgTest() {
        var shopId = 305L;
        var tagId = 301L;
        var response = template.getForEntity("/v1/yeshtery/recommend/selling/shop?tagid="+tagId+"&shopid="+shopId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getSimilarityProductTest() {
        var itemCounts = 2;
        var userId = 331;
        var response = template.getForEntity("/v1/yeshtery/recommend/similarityproducts?itemcounts="+itemCounts+"&userid="+userId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getSimilarityProductNotExistUserTest() {
        var itemCounts = 2;
        var userId = 112;
        var response = template.getForEntity("/v1/yeshtery/recommend/similarityproducts?itemcounts="+itemCounts+"&userid="+userId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getSimilarityProductWithZeroCountTest() {
        var itemCounts = 0;
        var userId = 331;
        var response = template.getForEntity("/v1/yeshtery/recommend/similarityproducts?itemcounts="+itemCounts+"&userid="+userId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getSimilarityProductWithZeroCountAndNotExistsUserTest() {
        var itemCounts = 0;
        var userId = 112;
        var response = template.getForEntity("/v1/yeshtery/recommend/similarityproducts?itemcounts="+itemCounts+"&userid="+userId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getSimilarityOrdersTest() {
        var itemCounts = 2;
        var userId = 331;
        var response = template.getForEntity("/v1/yeshtery/recommend/similarityitemorders?itemcounts="+itemCounts+"&userid="+userId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getSimilarityOrdersNotExistUserTest() {
        var itemCounts = 2;
        var userId = 112;
        var response = template.getForEntity("/v1/yeshtery/recommend/similarityitemorders?itemcounts="+itemCounts+"&userid="+userId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getSimilarityOrdersWithZeroCountTest() {
        var itemCounts = 0;
        var userId = 331;
        var response = template.getForEntity("/v1/yeshtery/recommend/similarityitemorders?itemcounts="+itemCounts+"&userid="+userId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void getSimilarityOrdersWithZeroCountAndNotExistsUserTest() {
        var itemCounts = 0;
        var userId = 112;
        var response = template.getForEntity("/v1/yeshtery/recommend/similarityitemorders?itemcounts="+itemCounts+"&userid="+userId, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }
}
