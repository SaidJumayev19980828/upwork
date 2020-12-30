import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.dao.ProductRatingRepository;
import com.nasnav.dto.response.navbox.ProductRateRepresentationObject;
import net.jcip.annotations.NotThreadSafe;
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

import java.io.IOException;
import java.util.List;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Product_Rating_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class ProductRatingTest {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private ProductRatingRepository ratingRepo;
    @Autowired
    private ObjectMapper mapper;

    @Test
    public void postReviewSuccess() {
        String body = createReviewRequest().toString();
        HttpEntity request = getHttpEntity(body, "123");
        ResponseEntity<String> response = template.postForEntity("/product/rate", request, String.class);

        assertEquals(200 , response.getStatusCodeValue());
        assertTrue(ratingRepo.findByVariant_IdAndUser_Id(310001L, 88L).isPresent());
    }

    @Test
    public void postReviewAuthZ() {
        String body = createReviewRequest().toString();;
        HttpEntity request = getHttpEntity(body, null);
        ResponseEntity<String> response = template.postForEntity("/product/rate", request, String.class);
        assertEquals(401 , response.getStatusCodeValue());
    }

    @Test
    public void postReviewAuthN() {
        String body = createReviewRequest().toString();
        HttpEntity request = getHttpEntity(body, "101112");
        ResponseEntity<String> response = template.postForEntity("/product/rate", request, String.class);
        assertEquals(403 , response.getStatusCodeValue());
    }

    @Test
    public void postReviewMissingVariantId() {
        JSONObject body = createReviewRequest();
        body.remove("variant_id");

        HttpEntity request = getHttpEntity(body.toString(), "123");
        ResponseEntity<String> response = template.postForEntity("/product/rate", request, String.class);
        assertEquals(406 , response.getStatusCodeValue());
    }

    @Test
    public void postReviewInvalidVariantId() {
        String body = createReviewRequest().put("variant_id", 0).toString();

        HttpEntity request = getHttpEntity(body, "123");
        ResponseEntity<String> response = template.postForEntity("/product/rate", request, String.class);
        assertEquals(404 , response.getStatusCodeValue());
    }

    @Test
    public void postReviewMissingRate() {
        JSONObject body = createReviewRequest();
        body.remove("rate");

        HttpEntity request = getHttpEntity(body.toString(), "123");
        ResponseEntity<String> response = template.postForEntity("/product/rate", request, String.class);
        assertEquals(406 , response.getStatusCodeValue());
    }

    @Test
    public void postReviewInvalidRate() {
        JSONObject body = createReviewRequest();
        body.put("rate", -4);

        HttpEntity request = getHttpEntity(body.toString(), "123");
        ResponseEntity<String> response = template.postForEntity("/product/rate", request, String.class);
        assertEquals(406 , response.getStatusCodeValue());

        body.put("rate", 8);
        request = getHttpEntity(body.toString(), "123");
        response = template.postForEntity("/product/rate", request, String.class);
        assertEquals(406 , response.getStatusCodeValue());
    }


    @Test
    public void approveReviewSuccess() {
        HttpEntity request = getHttpEntity("131415");
        ResponseEntity<String> response = template.postForEntity("/product/rate/approve?id=10001", request, String.class);
        assertEquals(200 , response.getStatusCodeValue());
        assertTrue(ratingRepo.findByVariant_IdAndUser_Id(310004L, 88L).get().getApproved());
    }

    @Test
    public void approveReviewAuthZ() {
        HttpEntity request = getHttpEntity(null);
        ResponseEntity<String> response = template.postForEntity("/product/rate/approve?id=10001", request, String.class);
        assertEquals(401 , response.getStatusCodeValue());
    }

    @Test
    public void approveReviewAuthN() {
        HttpEntity request = getHttpEntity("101112");
        ResponseEntity<String> response = template.postForEntity("/product/rate/approve?id=10001", request, String.class);
        assertEquals(403 , response.getStatusCodeValue());
    }

    @Test
    public void approveReviewInvalidRateId() {
        HttpEntity request = getHttpEntity("131415");
        ResponseEntity<String> response = template.postForEntity("/product/rate/approve?id=10003", request, String.class);
        assertEquals(404 , response.getStatusCodeValue());
    }

    @Test
    public void listApprovedVariantRates() throws IOException {
        ResponseEntity<String> response = template.getForEntity("/navbox/variant_rates?variant_id=310004", String.class);
        assertEquals(200 , response.getStatusCodeValue());
        List<ProductRateRepresentationObject> body =
                mapper.readValue(response.getBody(), new TypeReference<List<ProductRateRepresentationObject> >(){});
        assertTrue(body.size() == 1);
    }

    @Test
    public void listVariantRates() throws IOException {
        HttpEntity request = getHttpEntity("131415");
        ResponseEntity<String> response = template.exchange("/product/variant_rates?variant_id=310004", GET, request, String.class);
        assertEquals(200 , response.getStatusCodeValue());
        List<ProductRateRepresentationObject> body =
                mapper.readValue(response.getBody(), new TypeReference<List<ProductRateRepresentationObject> >(){});
        assertTrue(body.size() == 2);
    }

    @Test
    public void listVariantRatesAuthZ() {
        HttpEntity request = getHttpEntity(null);
        ResponseEntity<String> response = template.exchange("/product/variant_rates?variant_id=310004", GET, request, String.class);
        assertEquals(401 , response.getStatusCodeValue());
    }


    @Test
    public void listVariantRatesAuthN() {
        HttpEntity request = getHttpEntity("101112");
        ResponseEntity<String> response = template.exchange("/product/variant_rates?variant_id=310004", GET, request, String.class);
        assertEquals(403 , response.getStatusCodeValue());
    }


    private JSONObject createReviewRequest() {
        return json().put("variant_id", 310001)
                .put("rate", 2)
                .put("review", "good");
    }
}
