package com.nasnav.integration.microsoftdynamics.webclient;

import com.nasnav.integration.microsoftdynamics.webclient.dto.Customer;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Payment;
import com.nasnav.integration.microsoftdynamics.webclient.dto.ReturnSalesOrder;
import com.nasnav.integration.microsoftdynamics.webclient.dto.SalesOrder;
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

    public void getStores(Consumer<ClientResponse> response) {
        client.get()
                .uri("/api/stores")
                .exchange()
                .subscribe(response);
    }

    public void getProductsByBarcode(String barcode, Consumer<ClientResponse> response) {
        client.get()
                .uri("/api/products/"+barcode)
                .exchange()
                .subscribe(response);
    }

    public void getProducts(int topRows, int pageNumber, Consumer<ClientResponse> response) {
        client.get()
                .uri("/api/products/"+topRows+"/"+pageNumber)
                .exchange()
                .subscribe(response);
    }

    public void createSalesOrder(SalesOrder order, Consumer<ClientResponse> response) {
        client.put()
                .uri("/api/salesorder")
                .syncBody(order)
                .exchange()
                .subscribe(response);
    }

    public void cancelSalesOrder(SalesOrder order, Consumer<ClientResponse> response) {
        client.post()
                .uri("/api/SalesOrder")
                .syncBody(order)
                .exchange()
                .subscribe(response);
    }

    public void createReturnSalesOrder(ReturnSalesOrder order, Consumer<ClientResponse> response) {
        client.put()
                .uri("/api/ReturnOrder")
                .syncBody(order)
                .exchange()
                .subscribe(response);
    }

    public void createPayment(Payment payment, Consumer<ClientResponse> response) {
        client.put()
                .uri("/api/Payment")
                .syncBody(payment)
                .exchange()
                .subscribe(response);
    }

    public void createReversePayment(Payment payment, Consumer<ClientResponse> response) {
        client.put()
                .uri("/api/ReversePayment")
                .syncBody(payment)
                .exchange()
                .subscribe(response);
    }
}
