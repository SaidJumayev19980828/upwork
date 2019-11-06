package com.nasnav.integration.microsoftdynamics.webclient;

import com.nasnav.integration.microsoftdynamics.webclient.dto.Customer;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.function.Consumer;

@Service
public class FortuneWebClient {

    public final WebClient client = WebClient.builder().baseUrl("http://41.39.128.74").build();

    public void getCustomer(String phoneNumber, Consumer<ClientResponse> response) {

         client.get()
                .uri("/apiCust/customer/GetCustByPhone/"+phoneNumber)
                .exchange()
                .subscribe(response);
    }


    public void createCustomer(Customer customer, Consumer<ClientResponse> response) {
         client.put()
                .uri("/api/customer")
                .syncBody(customer)
                .exchange()
                .subscribe(response);
    }


    public void updateCustomer(Customer customer, Consumer<ClientResponse> response) {
         client.post()
                .uri("/api/customer")
                .syncBody(customer)
                .exchange()
                .subscribe(response);
    }
}
