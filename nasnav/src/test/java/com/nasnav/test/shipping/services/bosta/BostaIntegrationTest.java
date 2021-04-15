package com.nasnav.test.shipping.services.bosta;

import com.nasnav.shipping.services.bosta.webclient.BostaWebClient;
import com.nasnav.shipping.services.bosta.webclient.dto.*;
import net.jodah.concurrentunit.Waiter;
import org.junit.Assert;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;


//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureWebTestClient
//@PropertySource("classpath:database.properties")
//@DirtiesContext
public class BostaIntegrationTest {

    private static final String BOSTA_SERVER_URL = "https://stg-app.bosta.co/api/v0";
    private BostaWebClient client;
    private final String token = "ae5d5b5601fb68f1b26bf1f059ecaa1a5f9963675707879f2d8a9b0ccfb00357";
    private String id = "yM1ngytZ0";

//    @Before
    public void init() throws Exception {
        client = new BostaWebClient(BOSTA_SERVER_URL);
    }


    //@Test
    public void getDeliveriesTest() throws InterruptedException {
        Consumer<ClientResponse> c = res -> {
            Assert.assertEquals(200, res.statusCode().value());
        };

        client.getAllUserDeliveries(token, null, null).subscribe(c);
        Thread.sleep(1000);
    }


    //@Test
    public void getDeliveryByIdTest() throws InterruptedException {
        Consumer<ClientResponse> c = res -> {
            Assert.assertEquals(200, res.statusCode().value());
        };

        client.getDeliveryInfoById(token, id).subscribe(c);
        Thread.sleep(1000);
    }


    //@Test
    public void getDeliveryHolderLogsTest() throws InterruptedException {
        Consumer<ClientResponse> c = res -> {
            Assert.assertEquals(200, res.statusCode().value());
        };

        client.getDeliveryHolderLogs(token, id).subscribe(c);
        Thread.sleep(1000);
    }


    //@Test
    public void checkCanUpdateDeliveryTest() throws InterruptedException {
        Consumer<ClientResponse> c = res -> {
            Assert.assertEquals(200, res.statusCode().value());
        };

        List<String> queryParams = new ArrayList<>();
        queryParams.add("pickupAddress");
        queryParams.add("receiver");
        queryParams.add("dropOffAddress");

        client.canUpdateDelivery(token, id, queryParams).subscribe(c);
        Thread.sleep(2000);
    }


    //@Test
    public void getDeliveryStateHistoryTest() throws InterruptedException {
        Consumer<ClientResponse> c = res -> {
            Assert.assertEquals(200, res.statusCode().value());
        };

        client.getDeliveryStateHistory(token, id).subscribe(c);
        Thread.sleep(1000);
    }


    //@Test
    public void createDeliveryAirwayBillTest() throws InterruptedException {
        Consumer<ClientResponse> c = res -> {
            Assert.assertEquals(200, res.statusCode().value());
        };

        client.createAirwayBill(token, id).subscribe(c);
        Thread.sleep(3000);
    }


    //@Test
    public void getAllSubAccountsTest() throws InterruptedException {
        Consumer<ClientResponse> c = res -> {
            Assert.assertEquals(200, res.statusCode().value());
        };

        client.getAllSubAccounts(token, null, null).subscribe(c);
        Thread.sleep(2000);
    }


    //@Test
    public void createTrackerTest() throws InterruptedException {
        Consumer<ClientResponse> c = res -> {
            Assert.assertEquals(200, res.statusCode().value());
        };

        client.createTrackers(token, 1).subscribe(c);
        Thread.sleep(1000);
    }


    //@Test
    public void getTrackersTest() throws InterruptedException {
        Consumer<ClientResponse> c = res -> {
            Assert.assertEquals(200, res.statusCode().value());
        };

        client.getTrackers(token, true).subscribe(c);
        Thread.sleep(10000);
    }


    //@Test
    public void getTrackerByIdTest() throws InterruptedException {
        Consumer<ClientResponse> c = res -> {
            Assert.assertEquals(200, res.statusCode().value());
        };

        client.getTrackerInfo(token, "22kDK9YveCv2aH4ix").subscribe(c);
        Thread.sleep(2000);
    }


    //@Test
    public void createSubAccountTest() throws InterruptedException {
        Consumer<ClientResponse> c = res -> {
            Assert.assertEquals(201, res.statusCode().value());
        };

        Address address = new Address();

        SubAccount subAccount = new SubAccount();
        subAccount.setName("john");
        subAccount.setPhone("01000000000");
        subAccount.setAddress(address);

        client.createSubAccount(token, subAccount).subscribe(c);
        Thread.sleep(4000);
    }





    //@Test
    public void createDeliveryTest() throws InterruptedException, Exception {
    	 Waiter waiter = new Waiter();
    	 Consumer<CreateDeliveryResponse> onResponse = body -> {
         	System.out.println(">>>>>>> " + body.toString());
         	waiter.resume();
         };
         
        Mono<ClientResponse> cachedReponse = 
        		client
        		.createDelivery(token, createDelivery("Ahmad"))
        		.cache();
        
        cachedReponse
        .doOnEach(res -> System.out.println(">>>>>>> " + res.get().rawStatusCode()))
        .doOnEach(res -> waiter.assertEquals(201, res.get().rawStatusCode()))
        .subscribe(res -> waiter.resume());
        
        cachedReponse
        .flatMap(res -> res.bodyToMono(CreateDeliveryResponse.class))
        .subscribe(onResponse);

        waiter.await(30000, MILLISECONDS);
    }


    //@Test
    public void updateDelivery() throws InterruptedException {
        Consumer<ClientResponse> c = res -> {
            Assert.assertEquals(200, res.statusCode().value());
        };
        Receiver receiver = new Receiver();
        receiver.setFirstName("Mohsen");
        receiver.setLastName("Kudo");
        receiver.setPhone("01000000000");
        Delivery delivery = new Delivery();
        delivery.setReceiver(receiver);


        client.updateDelivery(token, delivery, id).subscribe(c);
        Thread.sleep(1000);
    }


    ////@Test
    public void deleteDelivery() throws InterruptedException {
        Consumer<ClientResponse> c = res -> {
            Assert.assertEquals(200, res.statusCode().value());
        };

        client.deleteDelivery(token, id).subscribe(c);
        Thread.sleep(1000);
    }


    private Delivery createDelivery(String firstName) {
        Receiver receiver = new Receiver();
        receiver.setFirstName(firstName);
        receiver.setLastName("Kudo");
        receiver.setPhone("01000000000");

        Address address = new Address();
        address.setFirstLine("50 K st.");
        address.setCity("EG-01");

        Delivery delivery = new Delivery();
        delivery.setReceiver(receiver);
        delivery.setPickupAddress(address);
        delivery.setDropOffAddress(address);
        delivery.setType(10L);

        return delivery;
    }
}
