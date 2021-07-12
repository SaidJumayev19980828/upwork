package com.nasnav.test.integration.msdynamics;

import com.google.common.net.MediaType;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.JsonSchemaBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;

import static com.google.common.net.MediaType.JSON_UTF_8;
import static com.nasnav.test.commons.TestCommons.readResource;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockserver.matchers.MatchType.ONLY_MATCHING_FIELDS;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;


@Component
public class IntegrationTestCommon {
	
	public static final long GET_BY_ID_DELAY = 3L;
	public static final String DUMMY_CUSTOMER_ID = "UNR-023517";
	public static final String DUMMY_PAYMENT_ID  = "UNR-168360";
	private static final String PAYMENT_BODY_SCHEMA = 
			"{type: 'object', properties: { 'SalesId': { 'type': 'string' }, 'PaymDet': { 'type': 'array' } }}";;


	public  final String mockServerUrl = "http://127.0.0.1";
	
	
	
	@Value("classpath:/json/ms_dynamics_integratoin_test/order_reuqest.json")
	private Resource orderRequest;
    
    
    @Value("classpath:/json/ms_dynamics_integratoin_test/get_products_response.json")
	private Resource productsJson;
    
    
    @Value("classpath:/json/ms_dynamics_integratoin_test/get_product_by_sku_response.json")
	private Resource singleProductJson;
    
    @Value("classpath:/json/ms_dynamics_integratoin_test/get_product_by_sku_response_2.json")
	private Resource singleProductJson2;
    
    
    @Value("classpath:/json/ms_dynamics_integratoin_test/get_customer_response.json")
	private Resource singleCustomerJson;
    
    
    @Value("classpath:/json/ms_dynamics_integratoin_test/get_stores_response.json")
	private Resource getStoresJson;
    
    @Value("classpath:/json/ms_dynamics_integratoin_test/get_products_response_2.json")
	private Resource productsJson2;
    
    
    @Value("classpath:/json/ms_dynamics_integratoin_test/get_categories_response.json")
	private Resource categoriesJson;
    
    
    

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
		 mockCreateOrder2Request(mockServerRule);
		 mockReturnOrderRequest(mockServerRule);
		 mockGetProductByIdRequest(mockServerRule);
		 mockGetProductByIdRequestWithDelay(mockServerRule);
		 mockGetProductByIdRequestFailure(mockServerRule);
		 mockGetProductBySKURequest(mockServerRule);
		 mockGetProductsRequestWithPagination(mockServerRule);
		 mockGetProductsRequest(mockServerRule);		 
		 mockGetCustomerByPhoneRequest(mockServerRule);
		 mockGetStoresRequest(mockServerRule);
		 mockGetCategoriesRequest(mockServerRule);
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
	
	
	
	private  void mockGetCategoriesRequest(MockServerRule mockServerRule) throws Exception {
		String storesResponse = new String( Files.readAllBytes(categoriesJson.getFile().toPath()) );
   	 	mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withPath("/api/categories"))
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
					response().withBody(productBySkuResponse, JSON_UTF_8) 
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
					response().withBody(productBySkuResponse, JSON_UTF_8) 
							  .withStatusCode(200))
				;
	}
	
	
	

	private  void mockGetProductByIdRequestWithDelay(MockServerRule mockServerRule) throws IOException {
		String productBySkuResponse = new String( Files.readAllBytes(singleProductJson.getFile().toPath()) );
    	 mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withPath("/api/products/11CYM-0015566"))
			.respond(
					response().withBody(productBySkuResponse, JSON_UTF_8) 
							  .withStatusCode(200)
							  .withDelay(SECONDS, GET_BY_ID_DELAY))
				;
	}
	
	
	
	
	
	
	private  void mockGetProductByIdRequestFailure(MockServerRule mockServerRule) throws IOException {
    	 mockServerRule.getClient()
			.when(
				request().withMethod("GET")
						.withPath("/api/products/11CYM-0015551"))
			.respond(
					response().withStatusCode(500))
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
				request()
					.withMethod("PUT")
					.withPath("/api/salesorder")
					.withBody(json("{'Store': 'FOoscar'}"
							, ONLY_MATCHING_FIELDS)))
			.respond(
					response().withBody("UNR19-050000") 
							  .withStatusCode(200))
				;
	}



	private  void mockCreateOrder2Request(MockServerRule mockServerRule) {
		mockServerRule.getClient()
				.when(
						request()
								.withMethod("PUT")
								.withPath("/api/salesorder")
								.withBody(json("{'Store': 'FOarabia'}"
										, ONLY_MATCHING_FIELDS)))
				.respond(
						response().withBody("UNR18-066600")
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
							.withPath("/api/Payment")
							.withBody(new JsonSchemaBody(PAYMENT_BODY_SCHEMA)))
				.respond(
						response().withBody(DUMMY_PAYMENT_ID) 
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
