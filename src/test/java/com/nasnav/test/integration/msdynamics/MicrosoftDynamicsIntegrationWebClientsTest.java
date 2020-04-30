package com.nasnav.test.integration.msdynamics;

import static com.nasnav.test.commons.TestCommons.json;
import static com.nasnav.test.commons.TestCommons.jsonArray;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.junit.MockServerRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.ClientResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.NavBox;
import com.nasnav.integration.microsoftdynamics.webclient.FortuneWebClient;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Address;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Customer;
import com.nasnav.integration.microsoftdynamics.webclient.dto.CustomerRepObj;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Payment;
import com.nasnav.integration.microsoftdynamics.webclient.dto.ProductsResponse;
import com.nasnav.integration.microsoftdynamics.webclient.dto.ReturnSalesOrder;
import com.nasnav.integration.microsoftdynamics.webclient.dto.SalesOrder;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Store;

import net.jodah.concurrentunit.Waiter;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")
@DirtiesContext
public class MicrosoftDynamicsIntegrationWebClientsTest {

	private static final String msServerUrl = "http://41.39.128.74";
	private static final String mockServerUrl = "http://127.0.0.1";
	
    private FortuneWebClient client;

    
    @Value("classpath:/json/ms_dynamics_integratoin_test/order_reuqest.json")
	private Resource orderRequest;
    
    
    @Value("classpath:/json/ms_dynamics_integratoin_test/get_products_response.json")
	private Resource productsJson;
    
    
    @Value("classpath:/json/ms_dynamics_integratoin_test/get_product_by_sku_response.json")
	private Resource singleProductJson;
    
    
    @Value("classpath:/json/ms_dynamics_integratoin_test/get_customer_response.json")
	private Resource singleCustomerJson;
    
    
    @Value("classpath:/json/ms_dynamics_integratoin_test/get_stores_response.json")
	private Resource getStoresJson;
    
    
    
    
    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);
    
    
    
    @Before
    public void init() throws Exception {    	
    	int port = 1080;
    	
    	 prepareMockRequests();    	 
    	 
    	 port = mockServerRule.getPort();
     	String serverUrl  = mockServerUrl + ":"+ port;
     	client = new FortuneWebClient(serverUrl);
     	 
    }




	private void prepareMockRequests() throws Exception {
		 mockPaymentRequest();
		 mockCreateCustomerRequest(); 
		 mockUpdateCustomerRequest();
		 mockReversePaymentRequest();
		 mockCancelOrderRequest();
		 mockCreateOrderRequest();
		 mockReturnOrderRequest();
		 mockGetProductsRequest();
		 mockGetProductBySKURequest();
		 mockGetCustomerByPhoneRequest();
		 mockGetStoresRequest();
	}




	private void mockGetStoresRequest() throws Exception {
		String storesResponse = new String( Files.readAllBytes(getStoresJson.getFile().toPath()) );
   	 	mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withPath("/api/stores"))
			.respond(
					response().withBody(storesResponse) 
							  .withStatusCode(200))
				;
	}




	private void mockGetProductBySKURequest() throws IOException {
		String productBySkuResponse = new String( Files.readAllBytes(singleProductJson.getFile().toPath()) );
    	 mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withPath("/api/products/6221105441060"))
			.respond(
					response().withBody(productBySkuResponse) 
							  .withStatusCode(200))
				;
	}
	
	
	
	
	
	private void mockGetCustomerByPhoneRequest() throws IOException {
		String customerResponse = new String( Files.readAllBytes(singleCustomerJson.getFile().toPath()) );
    	 mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withPath("/apiCust/customer/GetCustByPhone/.*"))
			.respond(
					response().withBody(customerResponse) 
							  .withStatusCode(200))
				;
	}
	
	




	private void mockGetProductsRequest() throws IOException {
		String productsResponse = new String( Files.readAllBytes(productsJson.getFile().toPath()) );
    	 mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withPath("/api/products/.*"))
			.respond(
					response().withBody(productsResponse) 
							  .withStatusCode(200))
				;
	}




	private void mockReturnOrderRequest() {
		mockServerRule.getClient()
			.when(
				request().withMethod("PUT")
						.withPath("/api/ReturnOrder"))
			.respond(
					response().withBody("UNR88-066000") 
							  .withStatusCode(200))
				;
	}




	private void mockCreateOrderRequest() {
		mockServerRule.getClient()
			.when(
				request().withMethod("PUT")
						.withPath("/api/salesorder"))
			.respond(
					response().withBody("UNR19-050000") 
							  .withStatusCode(200))
				;
	}




	private void mockCancelOrderRequest() {
		mockServerRule.getClient()
			.when(
				request().withMethod("POST")
						.withPath("/api/SalesOrder/.*"))
			.respond(
					response().withBody("UNR19-051580") 
							  .withStatusCode(200))
				;
	}




	private void mockReversePaymentRequest() {
		mockServerRule.getClient()
			.when(
				request().withMethod("PUT")
						.withPath("/api/ReversePayment"))
			.respond(
					response().withBody("UNR19-051580") 
							  .withStatusCode(200))
				;
	}




	private void mockUpdateCustomerRequest() {
		mockServerRule.getClient()
			.when(
				request().withMethod("POST")
						.withPath("/api/customer"))
			.respond(
					response().withBody("UNR-023517") 
							  .withStatusCode(200))
				;
	}




	private void mockCreateCustomerRequest() {
		mockServerRule.getClient()
			.when(
				request().withMethod("PUT")
						.withPath("/api/customer"))
			.respond(
					response().withBody("UNR-023517") 
							  .withStatusCode(200))
				;
	}




	private void mockPaymentRequest() {
		mockServerRule.getClient()
				.when(
					request().withMethod("PUT")
							.withPath("/api/Payment"))
				.respond(
						response().withBody("UNR19-000000") 
								  .withStatusCode(200))
					;
	}
    
    
    
    
    @Test
    public void getCustomer() throws InterruptedException, Exception {
    	Waiter waiter = new Waiter();
    	
        Consumer<Customer> printCustomers = customer -> {
            for(CustomerRepObj i : customer.getObj())
                System.out.println(i.toString());
        };

        Consumer<ClientResponse> response = res -> {
            System.out.println(res.statusCode());
            waiter.assertEquals(HttpStatus.OK, res.statusCode());
            res.bodyToFlux(Customer.class).subscribe(printCustomers);
            waiter.resume();
        };

        client.getCustomer("0123456720")
        	.subscribe(response);

        waiter.await(4000, TimeUnit.MILLISECONDS);
    }

    
    
    
    
    @Test
    public void updateCustomer() throws InterruptedException, Exception {
    	Waiter waiter = new Waiter();
    	
    	Customer customer = getCustomerData();  
    	customer.setAccountNumber("UNR-021675");
    	
        Consumer<ClientResponse> res = 
        		response -> { 
        			waiter.assertEquals(HttpStatus.OK, response.statusCode()); 
        			response.bodyToMono(String.class).subscribe(id -> System.out.println("Customer-id: " + id));
        			waiter.resume();
        		};        		

        client.updateCustomer(customer)
        	  .subscribe(res);
        
        waiter.await(4000, TimeUnit.MILLISECONDS);
    }

    
    
    
    @Test
    public void createCustomer() throws InterruptedException, Exception {
    	Waiter waiter = new Waiter();
    	
        Customer customer = getCustomerData();        
        Consumer<ClientResponse> res = 
        		response -> { 
        			waiter.assertEquals(HttpStatus.OK, response.statusCode()); 
        			response.bodyToMono(String.class).subscribe(id -> System.out.println("Customer-id: " + id));
        			waiter.resume();
        		};
        		
        client.createCustomer(customer)
        	 .subscribe(res);

        waiter.await(4000, TimeUnit.MILLISECONDS);
    }





	private Customer getCustomerData() {
		Customer customer = new Customer();
        customer.setFirstName("Hussien");
        customer.setMiddleName("Ahmad");
        customer.setLastName("Bishoy");
        customer.setEmail("KKK@KKK.com");
        customer.setBirthDate(LocalDate.of(1995, 02, 02));

        List<Address> addresses = Arrays.asList( createCustomerAddress() );
        customer.setAddresses(addresses);
        
		return customer;
	}





	private Address createCustomerAddress() {
		Address d = new Address();
        d.setStreet("q");
        d.setCity("Cairo");
        d.setCountry("Egy");
        d.setState("Cairo");
        d.setZipCode("12345");
        d.setPhoneNumber("0123456720");
		return d;
	}

    
    
    

    @Test
    public void getStores() throws InterruptedException, Exception {
    	Waiter waiter = new Waiter();

        Consumer<ClientResponse> response = 
        		res -> {
        			waiter.assertEquals(HttpStatus.OK, res.statusCode());
		            res.bodyToMono(String.class).subscribe(this::printStoresData);
		            waiter.resume();	
		        };

        client.getStores().subscribe(response);

        waiter.await(2000, TimeUnit.MILLISECONDS);
    }

    
    
    
    private void printStoresData(String bodyJson) {
    	JSONArray shopsJson = new JSONObject(bodyJson)
		    							.getJSONArray("results")
		    							.getJSONObject(0)
		    							.getJSONArray("shops");
    	
    	ObjectMapper mapper = new ObjectMapper();
    	
        List<Store> stores;
		try {
			stores = mapper.readValue(shopsJson.toString(), new TypeReference<List<Store>>(){});
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		
        stores.forEach(System.out::println);
    }
    
    
    
    
    @Test
    public void getProducts() throws InterruptedException, Exception {
    	Waiter waiter = new Waiter();    	

        Consumer<ClientResponse> response = 
        		res -> {
        			waiter.assertEquals(HttpStatus.OK, res.statusCode());
		            res.bodyToMono(ProductsResponse.class).subscribe(System.out::println);		            
		            waiter.resume();	
		        };

        client.getProducts(2, 1)
        	.subscribe(response);
        
        waiter.await(2000, TimeUnit.MILLISECONDS);
    }

    
    
    

    @Test
    public void createSalesOrder() throws InterruptedException, IOException, Exception {
    	Waiter waiter = new Waiter();
    	
        String jsonOrder = new String( Files.readAllBytes(orderRequest.getFile().toPath()) );

        SalesOrder order = new ObjectMapper().readValue(jsonOrder, SalesOrder.class);
        Consumer<ClientResponse> response = 
        		res -> {
        			waiter.assertEquals(HttpStatus.OK, res.statusCode());
        			res.bodyToMono(String.class).subscribe(id -> System.out.println("order id : " + id));		            
		            waiter.resume();	
		        };

        client.createSalesOrder(order)
        	.subscribe(response);

        waiter.await(2000, TimeUnit.MILLISECONDS);
    }

    
    
    
    
    @Test
    public void createReturnSalesOrder() throws InterruptedException, IOException, Exception {
    	Waiter waiter = new Waiter();
    	
        JSONObject jsonOrder = json().put("SalesId", "UNT18-008133")
		    						.put("Items"
	    									, jsonArray()
	    										.put(json()
		    											.put("SalesId", "UNT18-008133")
		        										.put("Item", "011APF-74202")
		        										.put("Qty", 1) ));
        System.out.println("JSON order : " + jsonOrder.toString());
        ReturnSalesOrder order = new ObjectMapper().readValue(jsonOrder.toString(), ReturnSalesOrder.class);
        
        Consumer<ClientResponse> response = 
        		res -> {
        			waiter.assertEquals(HttpStatus.OK, res.statusCode());
        			res.bodyToMono(String.class).subscribe(id -> System.out.println("return order id : " + id));		            
		            waiter.resume();	
		        };

        client.createReturnSalesOrder(order)
        	.subscribe(response);

        waiter.await(2000, TimeUnit.MILLISECONDS);
    }

    
    
    
    
    @Test
    public void cancelSalesOrder() throws InterruptedException, IOException, Exception {

        String order = "UNR19-051580";
    	Waiter waiter = new Waiter();
        Consumer<ClientResponse> response = 
        		res -> {
        			waiter.assertEquals(HttpStatus.OK, res.statusCode());
        			res.bodyToMono(String.class).subscribe(id -> System.out.println("cancelled order id : " + id));		            
		            waiter.resume();	
		        };

        client.cancelSalesOrder(order)
        	.subscribe(response);

        waiter.await(2000, TimeUnit.MILLISECONDS);
    }
    
    
    
    

    @Test
    public void createPayment() throws InterruptedException, IOException, Exception {
    	Waiter waiter = new Waiter();
    	
        JSONObject jsonPayment = json().put("SalesId", "UNT18-008133")
        								.put("PaymDet", jsonArray()
        													.put(json().put("SalesId","UNT18-008133")
        																.put("Amount", 2)
        																.put("PaymentMethod", "Credit_CHE")));
        System.out.println("Payment JSON: " + jsonPayment.toString());
        Payment payment = new ObjectMapper().readValue(jsonPayment.toString(), Payment.class);
        
        Consumer<ClientResponse> response = 
        		res -> {
        			waiter.assertEquals(HttpStatus.OK, res.statusCode());
        			res.bodyToMono(String.class).subscribe(id -> System.out.println("payment id : " + id));		            
		            waiter.resume();	
		        };

        client.createPayment(payment).subscribe(response);

        waiter.await(2000, TimeUnit.MILLISECONDS);
    }
    
    
    
    

    @Test
    public void createReversePayment() throws InterruptedException, IOException, Exception {
    	Waiter waiter = new Waiter();
    	
        String jsonPayment = "{\"SalesId\":\"UNT18-008133\", \"PaymDet\":[{\"SalesId\":\"UNT18-008133\",\"Amount\":2," +
                "\"PaymentMethod\":\"Credit_CHE\"}]}";

        Payment payment = new ObjectMapper().readValue(jsonPayment, Payment.class);
                
        Consumer<ClientResponse> response = 
        		res -> {
        			waiter.assertEquals(HttpStatus.OK, res.statusCode());
        			res.bodyToMono(String.class).subscribe(id -> System.out.println("reverse payment id : " + id));		            
		            waiter.resume();	
		        };

        client.createReversePayment(payment).subscribe(response);

        waiter.await(2000, TimeUnit.MILLISECONDS);
    }
}
