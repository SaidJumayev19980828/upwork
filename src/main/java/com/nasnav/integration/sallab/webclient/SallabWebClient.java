package com.nasnav.integration.sallab.webclient;

import com.nasnav.integration.sallab.webclient.dto.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.List;

public class SallabWebClient {

    public final WebClient client ;

    public SallabWebClient(String baseUrl) {
        client = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().wiretap(true)
                ))
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<AuthenticationResponse> authenticate(AuthenticationData data) {//testing account

        return WebClient.builder()
                .baseUrl("https://test.salesforce.com").build()
                .post()
                .uri("/services/oauth2/token?"+ data.toString())
                .exchange()
                .flatMap(res -> res.bodyToMono(AuthenticationResponse.class));
    }


    public Mono<ClientResponse> getProducts(String token) {
        return client.get()
                    .uri("/services/data/v44.0/query?q=SELECT Id, UnitPrice, Product2.Id, Product2.Name," +
                    " Product2.ProductCode, Product2.Item_No__c, Product2.Description, Product2.Stk_Unit__c, Product2.Stk_Unit__r.Div__c," +
                    " Product2.Stk_Unit__r.EUName__c, Product2.Stk_Unit__r.Mult__c, Product2.Stk_Unit__r.UCode__c, Product2.EItem_Name__c," +
                    " Product2.Icon_Attachment_Id__c, Product2.Arabic_Category__c,Product2.Arabic_Class__c,Product2.Arabic_Color__c,Product2.Arabic_Cut__c," +
                    "Product2.Arabic_Depth__c,Product2.Arabic_Drain__c,Product2.Arabic_Factory__c,Product2.Arabic_Family__c,Product2.Arabic_Glaze__c," +
                    "Product2.Arabic_Mixer__c,Product2.Arabic_Model__c,Product2.Arabic_Origin__c,Product2.Arabic_Shape__c,Product2.Arabic_Specifications__c," +
                    "Product2.Arabic_Style__c,Product2.Arabic_Tank__c,Product2.Arabic_Texture__c,Product2.Arabic_Type__c,Product2.English_Category__c," +
                    "Product2.English_Class__c,Product2.English_Color__c,Product2.English_Cut__c,Product2.English_Depth__c,Product2.English_Drain__c," +
                    "Product2.English_Factory__c,Product2.English_Family__c,Product2.English_Glaze__c,Product2.English_Mixer__c,Product2.English_Model__c," +
                    "Product2.English_Origin__c,Product2.English_Shape__c,Product2.English_Specifications__c,Product2.English_Style__c,Product2.English_Tank__c," +
                    "Product2.English_Texture__c,Product2.English_Type__c,Product2.Category__c,Product2.Class__c,Product2.Color__c,Product2.Cut__c,Product2.Depth__c," +
                    "Product2.Drain__c,Product2.Factory__c,Product2.Family,Product2.Glaze__c,Product2.Mixer__c,Product2.Model__c,Product2.Origin__c,Product2.Shape__c," +
                    "Product2.Specifications__c,Product2.Style__c,Product2.Tank__c,Product2.Texture__c,Product2.Type__c,Product2.Model_No__c, Product2.Size__c," +
                    " Product2.Pack_Closing__c FROM PriceBookEntry WHERE PriceBook2.IsStandard = true")
                    .header("Authorization", token)
                    .exchange();
    }

    public Mono<ClientResponse> getProduct(String token, String productId) {
        return client.get()
                .uri("/services/data/v44.0/sobjects/Product2/"+productId)
                .header("Authorization", token)
                .exchange();
    }


    public Mono<ClientResponse> getProductsNextRecords(String token, String recordsUri) {
        return client.get()
                .uri("/services/data/v44.0/query/"+recordsUri)
                .header("Authorization", token)
                .exchange();
    }


    public Mono<ClientResponse> getProductImage(String token, String uri) {
        return client.get()
                .uri("/services/data/v36.0/sobjects/Attachment/"+uri+"/Body")
                .header("Authorization", token)
                .exchange();
    }


    public Mono<ClientResponse> addCartItem(String token, ItemDTO item) {
        return client.post()
                .uri("/services/data/v40.0/sobjects/OpportunityLineItem")
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .syncBody(item)
                .exchange();
    }

    public Mono<ClientResponse> createCart(String token, CartDTO cart) {
        return client.post()
                .uri("/services/data/v40.0/sobjects/Opportunity")
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .syncBody(cart)
                .exchange();
    }

    public Mono<ClientResponse> createCustomer(String token, CustomerDTO customer) {
        return client.post()
                .uri("/services/data/v40.0/sobjects/Account")
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .syncBody(customer)
                .exchange();
    }

    public Mono<ClientResponse> getCustomer(String token, String id) {
        return client.get()
                .uri("/services/data/v40.0/sobjects/Account/"+id)
                .header("Authorization", token)
                .exchange();
    }


    public Mono<ItemPrice> getItemPrice(ItemSearchParam param) {
        return WebClient.builder()
                .baseUrl("http://41.33.113.70").build()// Static !!!
                .get()
                .uri("/ElSallab.Webservice/SallabService.svc/getItemPriceBreakdown?"+param.toString())
                .exchange()
                .flatMap(res -> res.bodyToMono(ItemPrice.class));
    }


    public Mono<List> getItemStockBalance(String itemId, Integer year) {
        return WebClient.builder()
                .baseUrl("http://41.33.113.70").build()// Static !!!
                .get()
                .uri("/ElSallab.Webservice/SallabService.svc/getItemStockBalance?itemNumber="+itemId+"&year="+year)
                .exchange()
                .flatMap(res -> res.bodyToMono(List.class));
    }

}
