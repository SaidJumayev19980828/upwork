package com.nasnav.integration.microsoftdynamics.webclient;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.nasnav.integration.microsoftdynamics.webclient.dto.Customer;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Payment;
import com.nasnav.integration.microsoftdynamics.webclient.dto.ReturnSalesOrder;
import com.nasnav.integration.microsoftdynamics.webclient.dto.SalesOrder;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;


public class FortuneWebClient {	

    public final WebClient client ;    
    
    public FortuneWebClient(String baseUrl) {
    	client = WebClient.builder()
		    			.clientConnector(new ReactorClientHttpConnector(
		    	                HttpClient.create().wiretap(true)
		    	            ))
						.baseUrl(baseUrl)						
						.build();  
    }
    
    
    
    public Mono<ClientResponse> getCustomer(String phoneNumber) {
         return client.get()
	                .uri("/apiCust/customer/GetCustByPhone/"+phoneNumber)
	                .exchange();
    }

    
    

    public Mono<ClientResponse> createCustomer(Customer customer) {
         return client.put()
                .uri("/api/customer")
                .syncBody(customer)
                .exchange();
    }
    
    
    


    public Mono<ClientResponse> updateCustomer(Customer customer) {
         return client.post()
                .uri("/api/customer")
                .syncBody(customer)
                .exchange();
    }

    
    
    
    public Mono<ClientResponse> getStores() {
        return client.get()
                .uri("/api/stores")
                .exchange();
    }
    
    
    

    public Mono<ClientResponse> getProductsBySKU(String sku) {
        return client.get()
                .uri("/api/products/"+sku)
                .exchange();
    }
    
    
    

    public Mono<ClientResponse> getProducts(int topRows, int pageNumber) {
        return client.get()
                .uri("/api/products/"+topRows+"/"+pageNumber)
                .exchange();
    }
    
    
    
    

    public Mono<ClientResponse> createSalesOrder(SalesOrder order) {
        return client
        		.put()
                .uri("/api/salesorder")
                .syncBody(order)
                .exchange();
    }

    
    
    
    
    public Mono<ClientResponse> cancelSalesOrder(String orderId) {
        return client.post()
                .uri("/api/SalesOrder/"+orderId)
                .exchange();
    }
    
    
    
    

    public Mono<ClientResponse> createReturnSalesOrder(ReturnSalesOrder order) {
        return client.put()
                .uri("/api/ReturnOrder")
                .syncBody(order)
                .exchange();
    }
    
    
    
    

    public Mono<ClientResponse> createPayment(Payment payment) {
        return client.put()
                .uri("/api/Payment")
                .syncBody(payment)
                .exchange();
    }
    
    
    
    

    public Mono<ClientResponse> createReversePayment(Payment payment) {
        return client.put()
                .uri("/api/ReversePayment")
                .syncBody(payment)
                .exchange();
    }
    
    
    
    
    public Mono<ClientResponse> getProductById(String id){
    	return client.get()
                .uri("/api/products/"+id)
                .exchange();
    }
    
    
    
    public Mono<ClientResponse> getCategories() {
        return client.get()
                .uri("/api/categories")
                .exchange();
    }
}
