package com.nasnav.shipping.services.bosta.webclient;

import com.nasnav.shipping.services.bosta.webclient.dto.Delivery;
import com.nasnav.shipping.services.bosta.webclient.dto.SubAccount;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.List;
import java.util.Optional;

public class BostaWebClient {

    private final WebClient client;

    public BostaWebClient(String baseUrl) {
        client = WebClient.builder()
                        .clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap(true)))
                        .baseUrl(baseUrl)
                        .build();
    }


    public Mono<ClientResponse> createDelivery(String token, Delivery dto) {
        return client.post()
                     .uri("/deliveries")
                     .bodyValue(dto)
                     .header("Authorization", token)
                     .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                     .exchange();
    }


    public Mono<ClientResponse> updateDelivery(String token, Delivery dto, String deliveryId) {
        return client.patch()
                .uri("/deliveries/" + deliveryId)
                .bodyValue(dto)
                .header("Authorization", token)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .exchange();
    }


    public Mono<ClientResponse> deleteDelivery(String token, String deliveryId) {
        return client.delete()
                .uri("/deliveries/" + deliveryId)
                .header("Authorization", token)
                .exchange();
    }


    public Mono<ClientResponse> getDeliveryInfoById(String token, String deliveryId) {
        return client.get()
                .uri("/deliveries/" + deliveryId)
                .header("Authorization", token)
                .exchange();
    }


    public Mono<ClientResponse> getAllUserDeliveries(String token, Integer page, Integer perPage) {

        Optional.ofNullable(page).orElse(1);
        Optional.ofNullable(perPage).orElse(10);

        return client.get()
                .uri("/deliveries")
                .attribute("page", page)
                .attribute("perPage", perPage)
                .header("Authorization", token)
                .exchange();
    }


    public Mono<ClientResponse> getDeliveryHolderLogs(String token, String deliveryId) {
        return client.get()
                .uri("/deliveries/" + deliveryId + "/holder-logs")
                .header("Authorization", token)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .exchange();
    }


    public Mono<ClientResponse> canUpdateDelivery(String token, String deliveryId,
                                                  List<String> queryParams) {
        String criteria = "";

        if (queryParams.contains("receiver"))
            criteria += "&receiver=1";
        if (queryParams.contains("notes"))
            criteria += "&notes=1";
        if (queryParams.contains("cod"))
            criteria += "&cod=1";
        if (queryParams.contains("businessReference"))
            criteria += "&businessReference=1";
        if (queryParams.contains("webhookUrl"))
            criteria += "&webhookUrl=1";
        if (queryParams.contains("pickupAddress"))
            criteria += "&pickupAddress=1";
        if (queryParams.contains("dropOffAddress"))
            criteria += "&dropOffAddress=1";

        if (criteria.length() > 0)
            criteria = criteria.replaceFirst("&", "?");

        return client.get()
                .uri("/deliveries/can_update_delivery/" + deliveryId + criteria)
                .header("Authorization", token)
                .exchange();
    }


    public Mono<ClientResponse> getDeliveryStateHistory(String token, String deliveryId) {
        return client.get()
                .uri("/deliveries/" + deliveryId + "/state-history")
                .header("Authorization", token)
                .exchange();
    }


    public Mono<ClientResponse> createAirwayBill(String token, String deliveryId) {
        return client.get()
                .uri("/deliveries/awb?id=" + deliveryId)
                .header("Authorization", token)
                .exchange();
    }


    public Mono<ClientResponse> createSubAccount(String token, SubAccount subAccount) {
        return client.post()
                .uri("/business-subaccounts")
                .bodyValue(subAccount)
                .header("Authorization", token)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .exchange();
    }


    public Mono<ClientResponse> updateSubAccount(String token, SubAccount subAccount) {
        return client.patch()
                .uri("/business-subaccounts")
                .bodyValue(subAccount)
                .header("Authorization", token)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .exchange();
    }


    public Mono<ClientResponse> deleteSubAccount(String token, String subAccId) {
        return client.delete()
                .uri("/business-subaccounts/" + subAccId)
                .header("Authorization", token)
                .exchange();
    }


    public Mono<ClientResponse> getSubAccount(String token, String subAccId) {
        return client.get()
                .uri("/business-subaccounts/" + subAccId)
                .header("Authorization", token)
                .exchange();
    }


    public Mono<ClientResponse> getAllSubAccounts(String token, Integer page, Integer perPage) {

        Optional.ofNullable(page).orElse(1);
        Optional.ofNullable(perPage).orElse(10);

        return client.get()
                .uri("/business-subaccounts")
                .attribute("page", page)
                .attribute("perPage", perPage)
                .header("Authorization", token)
                .exchange();
    }


    public Mono<ClientResponse> createTrackers(String token, Integer amount) {
        JSONObject body = new JSONObject();
        body.put("amount", amount);
        return client.post()
                .uri("/trackers")
                .bodyValue(body)
                .header("Authorization", token)
                .exchange();
    }


    public Mono<ClientResponse> getTrackers(String token, Boolean available) {
        return client.get()
                .uri("/trackers")
                .attribute("available", available)
                .header("Authorization", token)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .exchange();
    }


    public Mono<ClientResponse> getTrackerInfo(String token, String id) {
        return client.get()
                .uri("/trackers/" + id)
                .header("Authorization", token)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .exchange();
    }
}
