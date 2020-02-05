package com.nasnav.test.integration.sallab;

import com.nasnav.NavBox;
import com.nasnav.integration.sallab.webclient.SallabWebClient;
import com.nasnav.integration.sallab.webclient.dto.*;
import net.jodah.concurrentunit.Waiter;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@PropertySource("classpath:database.properties")
//@AutoConfigureWebTestClient
//@DirtiesContext
public class SallabIntegrationWebClientsTest {

    private static final String sallabServerUrl = "https://azizsallab--DevSanbox.cs80.my.salesforce.com";
    private static final String mockServerUrl = "http://127.0.0.1";

    public SallabWebClient client;

    public AuthenticationData data;

    @Value("classpath:/json/sallab_integration_test/productsIds.json")
    private Resource productsJson;

    @Before
    public void init() throws Exception {
        client = new SallabWebClient(sallabServerUrl);
        data = new AuthenticationData("password", "3MVG98_Psg5cppyZgL4kzqXARpsy8tyvcM1d8DwhODOxPiDTnqaf71BGU2cmzBpvf8l_myMTql31bhVa.ar8V",
                "4085100268240543918", "mzaklama@elsallab.com.devsanbox", "CloudzLab001tBHMDjhBGvDRsmWMrfog0oHG7");
    }

//    @Test
    public void getProducts() throws Exception {
        Waiter waiter = new Waiter();
        Consumer<AuthenticationResponse> res = response -> {
                    client.getProducts("Bearer "+response.getAccessToken())
                    .flatMap(r -> r.bodyToMono(ProductsResponse.class))
                    .subscribe(r -> printProductsData(r, waiter));
                };

        client.authenticate(data).subscribe(res);
        waiter.await(20000, TimeUnit.MILLISECONDS);
    }

    
    
//    @Test
    public void getProductsNextRecords() throws Exception {
        Waiter waiter = new Waiter();
        Consumer<AuthenticationResponse> res = response -> {
            client.getProductsNextRecords("Bearer "+response.getAccessToken(), "01g25000014VP64AAG-4000")
                    .flatMap(r -> r.bodyToMono(ProductsResponse.class))
                    .subscribe(r -> printProductsData(r, waiter));
        };

        client.authenticate(data).subscribe(res);
        waiter.await(30000, TimeUnit.MILLISECONDS);
    }

    
    
    
    
//    @Test
    public void getProduct() throws Exception {
        Waiter waiter = new Waiter();
        Consumer<AuthenticationResponse> res = response -> {
                    client.getProduct("Bearer "+response.getAccessToken(), "01t58000002DKaLAAW")
                            .flatMap(r -> r.bodyToMono(Product.class))
                            .subscribe(r -> printProductData(r, waiter));
                };

        client.authenticate(data).subscribe(res);
        waiter.await(20000, TimeUnit.MILLISECONDS);
    }

    
    
    
    
//    @Test
    public void getItemPrice() throws TimeoutException, InterruptedException {
        Waiter waiter = new Waiter();
        ItemSearchParam param = new ItemSearchParam("80151507570000", 1, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO);

        client.getItemPrice(param).subscribe(res -> printItemData(res, waiter));
        waiter.await(5000, TimeUnit.MILLISECONDS);
    }

    
    
    
    
    
//    @Test
    public void getItemStock() throws TimeoutException, InterruptedException {
        Waiter waiter = new Waiter();

        client.getItemStockBalance("80151507570000", 2019).subscribe(res -> printItemStock(res, waiter));
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

        client.authenticate(data).subscribe(res);
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

        client.authenticate(data).subscribe(res);
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

        client.authenticate(data).subscribe(res);
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

        client.authenticate(data).subscribe(res);
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

        client.authenticate(data).subscribe(res);
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
        System.out.println(response.totalSize);
        System.out.println(response.nextRecordsUrl);
        System.out.println(response.done);
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
