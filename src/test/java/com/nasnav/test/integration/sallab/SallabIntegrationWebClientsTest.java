package com.nasnav.test.integration.sallab;

import com.nasnav.NavBox;
import com.nasnav.integration.microsoftdynamics.webclient.FortuneWebClient;
import com.nasnav.integration.sallab.webclient.SallabWebClient;
import com.nasnav.integration.sallab.webclient.dto.ProductsResponse;
import net.jodah.concurrentunit.Waiter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.junit.MockServerRule;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.ClientResponse;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DirtiesContext
public class SallabIntegrationWebClientsTest {

    private static final String sallabServerUrl = "https://azizsallab--DevSanbox.cs80.my.salesforce.com";
    private static final String mockServerUrl = "http://127.0.0.1";

    public SallabWebClient client;

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);

    @Before
    public void init() throws Exception {
        int port = 1080;
        port = mockServerRule.getPort();
        String serverUrl  = mockServerUrl + ":"+ port;
        client = new SallabWebClient(sallabServerUrl);

    }


    @Test
    public void getProducts() throws InterruptedException, Exception {
        Waiter waiter = new Waiter();

        String token = "Bearer 00D250000009BEF!AQcAQCyHBa3dR4IFmEvWX7Rqwfcx8C.JRADxVoHJB1QXLY.uWT9e2hm1FTnxCeW2l0AwjJjAgKqNuQTFboCe8J1TXbO3G_Lx";

        Consumer<ClientResponse> response =
                res -> {
                    waiter.assertEquals(HttpStatus.OK, res.statusCode());
                    res.bodyToMono(ProductsResponse.class).subscribe(System.out::println);
                    waiter.resume();
                };

        client.getProducts(token)
                .subscribe(response);

        waiter.await(2000, TimeUnit.MILLISECONDS);
    }

    private String authenticate() {
        return null;
    }
}
