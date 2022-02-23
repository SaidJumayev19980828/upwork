package com.nasnav.shipping.services.mylerz.webclient;

import com.nasnav.shipping.services.mylerz.webclient.dto.ShipmentRequest;
import com.nasnav.shipping.services.mylerz.webclient.dto.DeliveryFeeRequest;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.List;

public class MylerzWebClient {

    private final WebClient client;

    public MylerzWebClient(String baseUrl) {
        client = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap(true)))
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<ClientResponse> authenticate(String userName, String password, String grantType) {
        return client.post()
                .uri("/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData("username", userName)
                        .with("password", password)
                        .with("grant_type", grantType))
                .exchange();
    }

    public Mono<ClientResponse> getCitiesAndAreas() {
        return client.get()
                .uri("/api/Packages/GetCityZoneList")
                .exchange();
    }

    public Mono<ClientResponse> getWareHouses(String token) {
        return client.get()
                .uri("/api/Orders/GetWarehouses")
                .header("Authorization","Bearer "+token)
                .exchange();
    }

    public Mono<ClientResponse> calculateDeliveryFee(String token, DeliveryFeeRequest dto) {
        return client.post()
                .uri("/api/Packages/GetExpectedCharges")
                .header("Authorization","Bearer "+token)
                .bodyValue(dto)
                .exchange();
    }

    public Mono<ClientResponse> submitShipmentRequest(String token, List<ShipmentRequest> dto) {
        return client.post()
                .uri("/api/Orders/AddOrders")
                .header("Authorization","Bearer "+token)
                .bodyValue(dto)
                .exchange();
    }

    public Mono<ClientResponse> getAWB(String token, String barcode) {
        JSONObject body = new JSONObject().put("Barcode", barcode);
        return client.post()
                .uri("/api/Packages/GetAWB")
                .header("Authorization","Bearer "+token)
                .bodyValue(body)
                .exchange();
    }

    public Mono<ClientResponse> cancelShipment(String token, String barcode, String merchantId) {
        JSONObject body = new JSONObject()
                .put("Barcode", barcode)
                .put("MerchantId", merchantId);
        return client.post()
                .uri("/api/Packages/CancelPackage")
                .header("Authorization","Bearer "+token)
                .bodyValue(body)
                .exchange();
    }
}
