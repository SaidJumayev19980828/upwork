package com.nasnav.test.integration.sallab;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mockserver.model.HttpRequest.request;
import static org.springframework.http.HttpStatus.OK;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.junit.MockServerRule;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.NavBox;
import com.nasnav.integration.sallab.webclient.SallabWebClient;
import com.nasnav.integration.sallab.webclient.dto.AuthenticationData;
import com.nasnav.integration.sallab.webclient.dto.AuthenticationResponse;
import com.nasnav.integration.sallab.webclient.dto.CartDTO;
import com.nasnav.integration.sallab.webclient.dto.Customer;
import com.nasnav.integration.sallab.webclient.dto.CustomerDTO;
import com.nasnav.integration.sallab.webclient.dto.CustomerType;
import com.nasnav.integration.sallab.webclient.dto.ItemDTO;
import com.nasnav.integration.sallab.webclient.dto.ItemPrice;
import com.nasnav.integration.sallab.webclient.dto.ItemSearchParam;
import com.nasnav.integration.sallab.webclient.dto.ItemStockBalance;
import com.nasnav.integration.sallab.webclient.dto.Product;
import com.nasnav.integration.sallab.webclient.dto.ProductsResponse;
import com.nasnav.integration.sallab.webclient.dto.Record;
import com.nasnav.integration.sallab.webclient.dto.SalArea;
import com.nasnav.integration.sallab.webclient.dto.SuccessResponse;

import net.jodah.concurrentunit.Waiter;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource("classpath:database.properties")
@AutoConfigureWebTestClient
@DirtiesContext
public class SallabIntegrationWebClientsTest {

    private static final String SALLAB_SERVER_URL = "https://azizsallab--DevSanbox.cs80.my.salesforce.com";    
    private static final String SALLAB_SEVER_URL_2 = "http://41.33.113.70";
    private static final String AUTH_SERVER_URL = "https://test.salesforce.com";
    private static final String MOCK_SERVER_URL = "http://127.0.0.1";
    private static final String IMG_AUTH_SERVER_URL = "https://login.salesforce.com";
    
  private static final String SERVER_URL = MOCK_SERVER_URL;
//  private static final String SERVER_URL = SALLAB_SERVER_URL;
  
    private static final boolean usingMockServer = SERVER_URL.equals(MOCK_SERVER_URL);
    

    public SallabWebClient client;

    public AuthenticationData data;

    @Value("classpath:/json/sallab_integration_test/productsIds.json")
    private Resource productsJson;

    
    
    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);
    
    
    @Autowired
	private ElSallabIntegrationTestCommon testCommons;
    
    
    @Before
    public void init() throws Exception {
    	String serverFullUrl  = SALLAB_SERVER_URL;
    	String server2FullUrl = SALLAB_SEVER_URL_2;
    	String authServerUrl = AUTH_SERVER_URL;
    	String imgAuthServerUrl = IMG_AUTH_SERVER_URL;
    	
    	if(usingMockServer) {
			serverFullUrl = testCommons.initElSallabMockServer(mockServerRule);
			server2FullUrl = serverFullUrl;
			authServerUrl = serverFullUrl;
			imgAuthServerUrl = serverFullUrl;
		}
    	
        client = new SallabWebClient(serverFullUrl, server2FullUrl,  authServerUrl, imgAuthServerUrl);
        data = new AuthenticationData("password", "3MVG98_Psg5cppyZgL4kzqXARpsy8tyvcM1d8DwhODOxPiDTnqaf71BGU2cmzBpvf8l_myMTql31bhVa.ar8V",
                "4085100268240543918", "mzaklama@elsallab.com.devsanbox", "CloudzLab001tBHMDjhBGvDRsmWMrfog0oHG7");
    }

    
    
    
    
    @Test
    public void getProducts() throws Exception {
        Waiter waiter = new Waiter();
        
        Consumer<ProductsResponse> onResponse = response -> {
        	System.out.println(response.toString());
            waiter.resume();
        };

        client
        	.authenticate(data)
        	.doOnNext(res -> waiter.assertEquals(res.statusCode(), OK))
        	.flatMap(res -> res.bodyToMono(AuthenticationResponse.class))
        	.flatMap(res -> client.getProducts(res.getAccessToken()))
        	.flatMap(prodRes -> prodRes.bodyToMono(ProductsResponse.class))
        	.subscribe(onResponse);
        
        waiter.await(30000, MILLISECONDS);
        //-------------------------------------------------------
        
        if(usingMockServer) {
			mockServerRule.getClient().verify(
				      request()
				        .withMethod("GET")
				        .withPath("/services/data/v44.0/query"),
				      VerificationTimes.exactly(1)
				    );
		}
    }

    
    
    
    
    @Test
    public void getProductsNextRecords() throws Exception {
        Waiter waiter = new Waiter();
        
        Consumer<ProductsResponse> onResponse = response -> {
        	System.out.println(response.toString());
            waiter.resume();
        };

        client
        	.authenticate(data)
        	.doOnNext(res -> waiter.assertEquals(res.statusCode(), OK))
        	.flatMap(res -> res.bodyToMono(AuthenticationResponse.class))
        	.flatMap(res -> client.getProductsNextRecords(res.getAccessToken(), "/services/data/v44.0/query/01g25000014iC4QAAU-2000"))
        	.flatMap(prodRes -> prodRes.bodyToMono(ProductsResponse.class))
        	.subscribe(onResponse);
        
        waiter.await(50000, MILLISECONDS);
        //-------------------------------------------------------
        if(usingMockServer) {
			mockServerRule.getClient().verify(
				      request()
				        .withMethod("GET")
				        .withPath("/services/data/v44.0/query/.+"),
				      VerificationTimes.exactly(1)
				    );
		}
        
    }
    
    
    
    
    
    
    @Test
    public void getPrice() throws Exception {
        Waiter waiter = new Waiter();
        
        Consumer<ItemPrice> onResponse = response -> {
        	System.out.println(response.toString());
            waiter.resume();
        };

        client
        	.authenticate(data)
        	.flatMap(res -> res.bodyToMono(AuthenticationResponse.class))
        	.flatMap(res -> client.getItemPrice(new ItemSearchParam("0550500023100011")) )
        	.flatMap(priceRes -> priceRes.bodyToMono(ItemPrice.class))
        	.subscribe(onResponse);
        
        waiter.await(40000, MILLISECONDS);
        //-------------------------------------------------------
        
        if(usingMockServer) {
			mockServerRule.getClient().verify(
				      request()
				        .withMethod("GET")
				        .withPath("/ElSallab.Webservice/SallabService.svc/getItemPriceBreakdown"),
				      VerificationTimes.exactly(1)
				    );
		}
    }
    
    
    
    
    
    

    @Test
    public void getStock() throws Exception {
        Waiter waiter = new Waiter();
        
        Consumer<ItemStockBalance> onResponse = response -> {
        	System.out.println(response.toString());
            waiter.resume();
        };

        client
        	.authenticate(data)
        	.flatMap(res -> res.bodyToMono(AuthenticationResponse.class))
        	.flatMap(res -> client.getItemStockBalance("0550500023100011" , 2019) )
        	.flatMapMany(stockRes -> stockRes.bodyToFlux(ItemStockBalance.class))
        	.subscribe(onResponse);
        
        waiter.await(5000, MILLISECONDS);
        //-------------------------------------------------------
        
        if(usingMockServer) {
			mockServerRule.getClient().verify(
				      request()
				        .withMethod("GET")
				        .withPath("/ElSallab.Webservice/SallabService.svc/getItemStockBalance"),
				      VerificationTimes.exactly(1)
				    );
		}
    }
    
    
    
    
    
//    @Test
    public void getProduct() throws Exception {
        Waiter waiter = new Waiter();
        Consumer<AuthenticationResponse> res = response -> {
                    client.getProduct("Bearer "+response.getAccessToken(), "01t58000002DKaLAAW")
                            .flatMap(r -> r.bodyToMono(Product.class))
                            .subscribe(r -> printProductData(r, waiter));
                };

//        client.authenticate(data).subscribe(res);
        waiter.await(20000, TimeUnit.MILLISECONDS);
    }

    
    
    
    
//    @Test
    public void getItemPrice() throws TimeoutException, InterruptedException {
        Waiter waiter = new Waiter();
        ItemSearchParam param = new ItemSearchParam("80151507570000", 1, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO);

//        client.getItemPrice(param).subscribe(res -> printItemData(res, waiter));
        waiter.await(5000, TimeUnit.MILLISECONDS);
    }

    
    
    
    
    
//    @Test
    public void getItemStock() throws TimeoutException, InterruptedException {
        Waiter waiter = new Waiter();

//        client.getItemStockBalance("80151507570000", 2019).subscribe(res -> printItemStock(res, waiter));
        waiter.await(2000, TimeUnit.MILLISECONDS);
    }

    
    
    
    
//    @Test
    public void addCartItem() throws TimeoutException, InterruptedException {
        Waiter waiter = new Waiter();

        ItemDTO item = new ItemDTO("01u58000005dJdI", BigDecimal.valueOf(30), BigDecimal.valueOf(17), "a02250000054M5k",
                                    "0062500000G50ji",  1.5);

        Consumer<AuthenticationResponse> res = response -> {
                client.addCartItem("Bearer "+response.getAccessToken(), item)
                      .flatMap(r -> r.bodyToMono(SuccessResponse.class))
                      .subscribe(r -> printSuccessResponse(r, waiter));
        };

//        client.authenticate(data).subscribe(res);
        waiter.await(8000, TimeUnit.MILLISECONDS);

    }

    
    
    
//    @Test
    public void createCart() throws TimeoutException, InterruptedException {
        Waiter waiter = new Waiter();

        CartDTO cart = new CartDTO("Opportunity Name", "Qualification", "0012500001G8wDtAAJ",
                "2019-12-31", "a0725000008agS8");

        Consumer<AuthenticationResponse> res = response -> {
            client.createCart("Bearer "+response.getAccessToken(), cart)
                    .flatMap(r -> r.bodyToMono(SuccessResponse.class))
                    .subscribe(r -> printSuccessResponse(r, waiter));
        };

//        client.authenticate(data).subscribe(res);
        waiter.await(10000, TimeUnit.MILLISECONDS);

    }

    
    
    
    
//    @Test
    public void createCustomer() throws TimeoutException, InterruptedException {
        Waiter waiter = new Waiter();

        byte[] array = new byte[7]; // length is bounded by 7
        new Random().nextBytes(array);
        String generatedString = new String(array, Charset.forName("UTF-8"));

        CustomerDTO customer = new CustomerDTO(generatedString, null, null, "a@b.com", generatedString, null, new SalArea(6), new CustomerType(1));

        Consumer<AuthenticationResponse> res = response -> {
            client.createCustomer("Bearer "+response.getAccessToken(), customer)
                    .flatMap(r -> r.bodyToMono(SuccessResponse.class))
                    .subscribe(r ->  printSuccessResponse(r, waiter));
        };

//        client.authenticate(data).subscribe(res);
        waiter.await(10000, TimeUnit.MILLISECONDS);

    }
    
    
    
    

//    @Test
    public void getCustomer() throws Exception {
        Waiter waiter = new Waiter();
        Consumer<AuthenticationResponse> res = response -> {
            client.getCustomer("Bearer "+response.getAccessToken(), "0012500001G8wDtAAJ")
                    .flatMap(r -> r.bodyToMono(Customer.class))
                    .subscribe(r ->  printCustomerData(r, waiter));
        };

//        client.authenticate(data).subscribe(res);
        waiter.await(5000, TimeUnit.MILLISECONDS);
    }

    //@Test
    public void getProductImages() throws Exception{
        Waiter waiter = new Waiter();

        File file = new File("D:\\Repositories\\backend-java\\src\\test\\resources\\json\\sallab_integration_test\\productsIds.txt");

        Consumer<AuthenticationResponse> res = response -> {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String id;
                    while ((id = br.readLine()) != null) {
                        String finalId = id;
                        client.getProductImage("Bearer "+response.getAccessToken(), id)
                                //.filter(r -> r.statusCode().is2xxSuccessful())
                                .flatMap(r -> r.bodyToMono(String.class))
                                .subscribe(r -> print(r, finalId, waiter)).wait(16);
                    }
                } catch (Exception e) {

                }
        };

//        client.authenticate(data).subscribe(res);
        waiter.await(90000, TimeUnit.MILLISECONDS);
    }


    private void printCustomerData(Customer c, Waiter w) {
        System.out.println(c.toString());
        w.resume();
    }

    private void printSuccessResponse(SuccessResponse response, Waiter waiter) {
        System.out.println(response.toString());
        waiter.resume();
    }

    private void printErrorResponse(String response, Waiter waiter) {
        JSONArray arr = new JSONArray(response);
        System.out.println(arr.get(0));
        waiter.resume();
    }

    private void printItemData(ItemPrice item, Waiter waiter) {
        System.out.println(item.toString());
        waiter.resume();
    }

    private void print(String res, String id, Waiter waiter) {
        System.out.println(res);
        if (!res.contains("errorCode"))
            System.out.println("thisss "+id);
        if(id.equals("00P58000006DgNT"))
            waiter.resume();
    }

    private void printItemStock(List<ItemStockBalance> items, Waiter waiter) {
        for(Object item : items)
            System.out.println(item.toString());
        waiter.resume();
    }

    private void printProductsData(ProductsResponse response, Waiter waiter) {
//        System.out.println(response.totalSize);
//        System.out.println(response.nextRecordsUrl);
//        System.out.println(response.done);
        /*for(Record r : response.records) {
            printRecordData(r);
        }*/
        waiter.resume();
    }

    private void printRecordData(Record r) {
        System.out.println(r.id +" "+r.attributes+" "+r.unitPrice+" "+r.product);
        System.out.println(r.attributes.type+" "+r.attributes.url);
        System.out.println(r.unitPrice);
        printProductData(r.product);
    }

    private void printProductData(Product p, Waiter w) {
        printProductData(p);
        w.resume();
    }

    private void printProductData(Product p) {
        System.out.println(p.id);
        System.out.println(p.name);
        if(p.iconAttachmentId != null)
            System.out.println(p.iconAttachmentId);
    }


}
