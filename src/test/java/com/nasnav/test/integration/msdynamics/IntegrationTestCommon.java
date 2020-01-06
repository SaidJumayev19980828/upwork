package com.nasnav.test.integration.msdynamics;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.nio.file.Files;

import org.mockserver.junit.MockServerRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.google.common.net.MediaType;
import static com.nasnav.test.commons.TestCommons.readResource;


@Component
public class IntegrationTestCommon {
	
	private static final String DUMMY_CUSTOMER_ID = "UNR-023517";



	public  final String mockServerUrl = "http://127.0.0.1";
	
	
	
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
    
    @Value("classpath:/json/ms_dynamics_integratoin_test/get_products_response_2.json")
	private Resource productsJson2;
    
    
    

	public  String initFortuneMockServer(MockServerRule mockServerRule) throws Exception {		
		prepareMockRequests(mockServerRule);
		return mockServerUrl + ":"+ mockServerRule.getPort();
	}
	
	
	
	
	
	
	
	private  void prepareMockRequests(MockServerRule mockServerRule) throws Exception {
		 mockPaymentRequest(mockServerRule);
		 mockCreateCustomerRequest(mockServerRule); 
		 mockUpdateCustomerRequest(mockServerRule);
		 mockReversePaymentRequest(mockServerRule);
		 mockCancelOrderRequest(mockServerRule);
		 mockCreateOrderRequest(mockServerRule);
		 mockReturnOrderRequest(mockServerRule);
		 mockGetProductByIdRequest(mockServerRule);
		 mockGetProductBySKURequest(mockServerRule);
		 mockGetProductsRequestWithPagination(mockServerRule);
		 mockGetProductsRequest(mockServerRule);		 
		 mockGetCustomerByPhoneRequest(mockServerRule);
		 mockGetStoresRequest(mockServerRule);
	}
	
	
	
	
	private  void mockGetStoresRequest(MockServerRule mockServerRule) throws Exception {
		String storesResponse = new String( Files.readAllBytes(getStoresJson.getFile().toPath()) );
   	 	mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withPath("/api/stores"))
			.respond(
					response().withBody(storesResponse, MediaType.JSON_UTF_8) 
							  .withStatusCode(200))
				;
	}




	private  void mockGetProductBySKURequest(MockServerRule mockServerRule) throws IOException {
		String productBySkuResponse = new String( Files.readAllBytes(singleProductJson.getFile().toPath()) );
    	 mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withPath("/api/products/6221105441060"))
			.respond(
					response().withBody(productBySkuResponse, MediaType.JSON_UTF_8) 
							  .withStatusCode(200))
				;
	}
	
	
	
	
	
	private  void mockGetProductByIdRequest(MockServerRule mockServerRule) throws IOException {
		String productBySkuResponse = new String( Files.readAllBytes(singleProductJson.getFile().toPath()) );
    	 mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withPath("/api/products/11CYM-0010001"))
			.respond(
					response().withBody(productBySkuResponse, MediaType.JSON_UTF_8) 
							  .withStatusCode(200))
				;
	}
	
	
	
	
	
	private  void mockGetCustomerByPhoneRequest(MockServerRule mockServerRule) throws IOException {
		String customerResponse = new String( Files.readAllBytes(singleCustomerJson.getFile().toPath()) );
    	 mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withPath("/apiCust/customer/GetCustByPhone/.*"))
			.respond(
					response().withBody(customerResponse, MediaType.JSON_UTF_8) 
							  .withStatusCode(200))
				;
	}
	
	




	private  void mockGetProductsRequest(MockServerRule mockServerRule) throws IOException {
		String productsResponse = new String( Files.readAllBytes(productsJson.getFile().toPath()) );
    	 mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withPath("/api/products/.*"))
			.respond(
					response().withBody(productsResponse, MediaType.JSON_UTF_8) 
							  .withStatusCode(200))
				;
	}
	
	
	
	
	private  void mockGetProductsRequestWithPagination(MockServerRule mockServerRule) throws IOException {
		String productsResponse = readResource(productsJson2);
    	 mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withPath("/api/products/\\d+/\\d+"))
			.respond(
					response().withBody(productsResponse, MediaType.JSON_UTF_8) 
							  .withStatusCode(200))
				;
	}



	


	private  void mockCreateOrderRequest(MockServerRule mockServerRule) {
		mockServerRule.getClient()
			.when(
				request().withMethod("PUT")
						.withPath("/api/salesorder"))
			.respond(
					response().withBody("UNR19-050000") 
							  .withStatusCode(200))
				;
	}




	private  void mockCancelOrderRequest(MockServerRule mockServerRule) {
		mockServerRule.getClient()
			.when(
				request().withMethod("POST")
						.withPath("/api/SalesOrder/.*"))
			.respond(
					response().withBody("UNR19-051580") 
							  .withStatusCode(200))
				;
	}




	private  void mockReversePaymentRequest(MockServerRule mockServerRule) {
		mockServerRule.getClient()
			.when(
				request().withMethod("PUT")
						.withPath("/api/ReversePayment"))
			.respond(
					response().withBody("UNR19-051580") 
							  .withStatusCode(200))
				;
	}




	private  void mockUpdateCustomerRequest(MockServerRule mockServerRule) {
		mockServerRule.getClient()
			.when(
				request().withMethod("POST")
						.withPath("/api/customer"))
			.respond(
					response().withBody(DUMMY_CUSTOMER_ID) 
							  .withStatusCode(200))
				;
	}




	private  void mockCreateCustomerRequest(MockServerRule mockServerRule) {
		mockServerRule.getClient()
			.when(
				request().withMethod("PUT")
						.withPath("/api/customer"))
			.respond(
					response().withBody(DUMMY_CUSTOMER_ID) 
							  .withStatusCode(200))
				;
	}




	private  void mockPaymentRequest(MockServerRule mockServerRule) {
		mockServerRule.getClient()
				.when(
					request().withMethod("PUT")
							.withPath("/api/Payment"))
				.respond(
						response().withBody("UNR19-000000") 
								  .withStatusCode(200))
					;
	}
	
	
	

	private  void mockReturnOrderRequest(MockServerRule mockServerRule) {
		mockServerRule.getClient()
			.when(
				request().withMethod("PUT")
						.withPath("/api/ReturnOrder"))
			.respond(
					response().withBody("UNR88-066000") 
							  .withStatusCode(200))
				;
	}







	public String getDummyCustomerExtId() {
		return DUMMY_CUSTOMER_ID;
	}
}
