
import ch.qos.logback.core.net.server.Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.microsoftdynamics.webclient.dto.*;
import com.nasnav.integration.microsoftdynamics.webclient.FortuneWebClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")

public class MicrosoftDynamicsIntegration {

    @Autowired
    private FortuneWebClient client;

    @Test
    public void getCustomer() throws InterruptedException {

        Consumer<Customer> customers = customer -> {
            for(CustomerRepObj i : customer.getObj())
                System.out.println(i.toString());
        };

        Consumer<ClientResponse> response = res -> {
            System.out.println(res.statusCode());
            if (res.statusCode().value() == 200)
                res.bodyToFlux(Customer.class).subscribe(customers);
        };

        client.getCustomer("0123456720", response);

        Thread.sleep(1000);
    }

    @Test
    public void updateCustomer() throws InterruptedException {

        Customer customer = new Customer();
        customer.setAccountNumber("UNR-021675");
        customer.setFirstName("Kira");
        customer.setMiddleName("Lawllet");
        customer.setLastName("Z");
        customer.setEmail("KK@KK.com");
        customer.setBirthDate(LocalDate.of(1995, 02, 02));

        Address d = new Address();
        d.setStreet("q");
        d.setCity("Cairo");
        d.setCountry("Egy");
        d.setState("Cairo");
        d.setZipCode("12345");
        d.setPhoneNumber("0123456720");
        List<Address> addresses = new ArrayList<>();
        addresses.add(d);
        customer.setAddresses(addresses);

        Consumer<ClientResponse> res = response -> Assert.assertTrue(response.statusCode() == HttpStatus.OK);

        client.updateCustomer(customer, res);
        Thread.sleep(1000);
    }

    @Test
    public void createCustomer() throws InterruptedException {

        Customer customer = new Customer();
        customer.setFirstName("Hussien");
        customer.setMiddleName("Ahmad");
        customer.setLastName("Bishoy");
        customer.setEmail("KKK@KKK.com");
        customer.setBirthDate(LocalDate.of(1995, 02, 02));

        Address d = new Address();
        d.setStreet("q");
        d.setCity("Cairo");
        d.setCountry("Egy");
        d.setState("Cairo");
        d.setZipCode("12345");
        d.setPhoneNumber("0123456720");
        List<Address> addresses = new ArrayList<>();
        addresses.add(d);
        customer.setAddresses(addresses);

        Consumer<ClientResponse> res = response -> Assert.assertTrue(response.statusCode() == HttpStatus.OK);
        client.createCustomer(customer, res);

        Thread.sleep(1000);
    }


    @Test
    public void getStores() throws InterruptedException {

        Consumer<String> stores = s -> {
            JSONArray c = new JSONObject(s).getJSONArray("results").getJSONObject(0).getJSONArray("shops");
            List<Store> x = null;
            try {
                x = Arrays.asList(new ObjectMapper().readValue(c.toString(), Store[].class));
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (Object b : x)
                System.out.println(b.toString());
        };

        Consumer<ClientResponse> response = res -> {
            System.out.println(res.statusCode());
            if (res.statusCode().value() == 200)
                res.bodyToMono(String.class).subscribe(stores);
        };

        client.getStores(response);

        Thread.sleep(2000);
    }

    @Test
    public void getProducts() throws InterruptedException {
        Consumer<ProductsResponse> products = s -> System.out.println(s);

        Consumer<ClientResponse> response = res -> {
            System.out.println(res.statusCode());
            if (res.statusCode().value() == 200)
                res.bodyToMono(ProductsResponse.class).subscribe(products);
        };

        client.getProducts(2, 1,response);
        Thread.sleep(2000);
    }


    @Test
    public void createSalesOrder() throws InterruptedException, IOException {

        String jsonOrder = "{\"CountryID\":\"EGY\", \"CustomerID\":\"un5005009\", \"Address\":\"NasrCity\", \"City_Code\":\"\", \"Total_Order_Discount\":0.0," +
                            "\"Total\":0.0, \"Shipping_fees\":10.0000000000000000, \"InventSite\":\"OCTOBER1\", \"Store\":\"116\"," +
                            "\"PaymentMethod\":\"Credit_CHE\", \"CodCode\":\"Non\", \"CODFeeAmount\":10.0000000000000000, \"ShippingfeesCode\":\"Non\", " +
                            "\"Items\":[  {\"Item\":\"011APF-74202\", \"InventSiteID\":\"OCTOBER1\", \"Store\":\"116\"," +
                                          "\"Qty\":10.0000000000000000, \"SalesPrice\":10.0000000000000000, \"DiscountAmount\":0.0000000000000000, "+
                                          "\"NetPrice\":10.0000000000000000, \"Totals\":0.0 } ]" +
                            "}";

        SalesOrder order = new ObjectMapper().readValue(jsonOrder, SalesOrder.class);

        Consumer<String> str = response -> System.out.println("order id : " + response);

        Consumer<ClientResponse> res = response -> {
            Assert.assertTrue(response.statusCode() == HttpStatus.OK);
            response.bodyToMono(String.class).subscribe(str);
        };

        client.createSalesOrder(order, res);

        Thread.sleep(4000);
    }

    @Test
    public void createReturnSalesOrder() throws InterruptedException, IOException {

        String jsonOrder = "{\"SalesId\":\"UNT18-008133\", \"Items\": [{\"SalesId\":\"UNT18-008133\",\"Item\":\"011APF-74202\", "+
                             "\"Qty\":1}]}";

        ReturnSalesOrder order = new ObjectMapper().readValue(jsonOrder, ReturnSalesOrder.class);

        Consumer<String> str = response -> System.out.println("return order id : " + response);

        Consumer<ClientResponse> res = response -> {
            System.out.println(response.statusCode());
            Assert.assertTrue(response.statusCode().value() == 200);
            response.bodyToMono(String.class).subscribe(str);
        };

        client.createReturnSalesOrder(order, res);

        Thread.sleep(5000);
    }

    @Test
    public void cancelSalesOrder() throws InterruptedException, IOException {

        /*String jsonOrder = "{\"SalesId\":\"UNT18-008133\", \"Items\": [{\"SalesId\":\"UNT18-008133\",\"Item\":\"011APF-74202\", "+
                "\"Qty\":1}]}";

        ReturnSalesOrder order = new ObjectMapper().readValue(jsonOrder, ReturnSalesOrder.class);

        Consumer<String> str = response -> System.out.println("return order id : " + response);

        Consumer<ClientResponse> res = response -> {
            System.out.println(response.statusCode());
            Assert.assertTrue(response.statusCode().value() == 200);
            response.bodyToMono(String.class).subscribe(str);
        };

        client.cancelSalesOrder(order, res);

        Thread.sleep(5000);*/
    }

    @Test
    public void createPayment() throws InterruptedException, IOException {

        String jsonPayment = "{\"SalesId\":\"UNT18-008133\", \"PaymDet\":[{\"SalesId\":\"UNT18-008133\",\"Amount\":2," +
                            "\"PaymentMethod\":\"Credit_CHE\"}]}";

        Payment payment = new ObjectMapper().readValue(jsonPayment, Payment.class);

        Consumer<String> str = response -> System.out.println(" id : " + response);

        Consumer<ClientResponse> res = response -> {
            System.out.println(response.statusCode());
            Assert.assertTrue(response.statusCode().value() == 200);
            response.bodyToMono(String.class).subscribe(str);
        };

        client.createPayment(payment, res);

        Thread.sleep(5000);
    }

    @Test
    public void createReversePayment() throws InterruptedException, IOException {

        String jsonPayment = "{\"SalesId\":\"UNT18-008133\", \"PaymDet\":[{\"SalesId\":\"UNT18-008133\",\"Amount\":2," +
                "\"PaymentMethod\":\"Credit_CHE\"}]}";

        Payment payment = new ObjectMapper().readValue(jsonPayment, Payment.class);

        Consumer<String> str = response -> System.out.println(" id : " + response);

        Consumer<ClientResponse> res = response -> {
            System.out.println(response.statusCode());
            Assert.assertTrue(response.statusCode().value() == 200);
            response.bodyToMono(String.class).subscribe(str);
        };

        client.createReversePayment(payment, res);

        Thread.sleep(5000);
    }
}
