
import ch.qos.logback.core.net.server.Client;
import com.nasnav.NavBox;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Address;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Customer;
import com.nasnav.integration.microsoftdynamics.webclient.FortuneWebClient;
import com.nasnav.integration.microsoftdynamics.webclient.dto.CustomerRepObj;
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
}
