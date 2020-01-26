package com.nasnav.integration.sallab.webclient;

import com.nasnav.integration.sallab.webclient.dto.ProductsResponse;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

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


}
