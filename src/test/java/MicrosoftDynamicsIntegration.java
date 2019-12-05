
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;


import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.ClientResponse;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
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
import com.nasnav.test.utils.JsonBuilder;

import net.jodah.concurrentunit.Waiter;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@PropertySource("classpath:database.properties")

public class MicrosoftDynamicsIntegration {

    private FortuneWebClient client = new FortuneWebClient("http://41.39.128.74");

    
    @Value("classpath:/json/ms_dynamics_integratoin_test/order_reuqest.json")
	private Resource orderRequest;
    
    
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

        client.getCustomer("0123456720")
        	.subscribe(response);

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

        Address d = createCustomerAddress();
        List<Address> addresses = new ArrayList<>();
        addresses.add(d);
        customer.setAddresses(addresses);

        Consumer<ClientResponse> res = response -> Assert.assertTrue(response.statusCode() == HttpStatus.OK);

        client.updateCustomer(customer)
        	  .subscribe(res);
        Thread.sleep(1000);
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
    	
        String jsonOrder = JsonBuilder.root()
        							.put("SalesId", "UNT18-008133")
        							.putArray("Items")
        								.addJson()
	        								.put("SalesId", "UNT18-008133")
	        								.put("Item", "011APF-74202")
	        							.parentArray()
	        						.close()
	        						.getJson();
        JSONObject obj = new JSONObject()
        						.put("SalesId", "UNT18-008133")
        						.put("Items", 
        								new JSONArray().put(
        											new JSONObject()
		        											.put("SalesId", "UNT18-008133")
		            										.put("Item", "011APF-74202")
        										)
        						 );
        						
        		
//        		"{\"SalesId\":\"UNT18-008133\", \"Items\": [{\"SalesId\":\"UNT18-008133\",\"Item\":\"011APF-74202\", "+
//                             "\"Qty\":1}]}";
        System.out.println("JSON order : " + jsonOrder);
        ReturnSalesOrder order = new ObjectMapper().readValue(jsonOrder, ReturnSalesOrder.class);
        
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
    public void createPayment() throws InterruptedException, IOException, Exception {
    	Waiter waiter = new Waiter();
    	
        String jsonPayment = "{\"SalesId\":\"UNT18-008133\", \"PaymDet\":[{\"SalesId\":\"UNT18-008133\",\"Amount\":2," +
                            "\"PaymentMethod\":\"Credit_CHE\"}]}";

        Payment payment = new ObjectMapper().readValue(jsonPayment, Payment.class);
        
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
