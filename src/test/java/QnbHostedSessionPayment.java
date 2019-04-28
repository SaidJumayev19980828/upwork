import com.nasnav.NavBox;
import com.nasnav.payments.qnb.Account;
import com.nasnav.payments.qnb.Session;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.test.properties")
public class QnbHostedSessionPayment {
    Account testAccount = new Account();

    @Autowired
    private WebTestClient webClient;

    @Test
    public void rawSessionCreationTest() {
        Session session = new Session(testAccount);
        Assert.assertTrue(session.initialize(123, Session.TransactionCurrency.EGP));
    }

    @Test
    public void initSessionViaApiTest() {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("order_id", "12");  // TODO: create a valid order entry in the DB first

        WebTestClient.ResponseSpec response = webClient.post().uri("/payment/qnb/initialize")
                .body(BodyInserters.fromFormData(formData)).exchange();

        JSONObject jsonResponse = new JSONObject(new String(response
                        .expectStatus().isOk()
                        .expectBody()
                        .jsonPath("$.success").isNotEmpty()
                        .jsonPath("$.success").isEqualTo(true)
                        .returnResult().getResponseBody()
                )
        );
        System.out.println(jsonResponse);
    }


}
