package com.nasnav.shipping.services.clicknship.webclient;

import com.nasnav.shipping.services.clicknship.webclient.dto.DeliveryFeeRequest;
import com.nasnav.shipping.services.clicknship.webclient.dto.ShipmentRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static java.lang.String.format;

public class ClickNshipWebClient {

    private final WebClient client;

    public ClickNshipWebClient(String baseUrl) {
        client = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap(true)))
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<ClientResponse> authenticateUser(String userName, String password, String grantType) {
        return client.post()
                .uri("/Token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData("username", userName)
                        .with("password", password)
                        .with("grant_type", grantType))
                .exchange();
    }

    public Mono<ClientResponse> getStates(String token) {
        return client.get()
                .uri("/clicknship/Operations/States")
                .header("Authorization","Bearer "+token)
                .exchange();
    }

    public Mono<ClientResponse> getCities(String token) {
        return client.get()
                .uri("/clicknship/Operations/cities")
                .header("Authorization","Bearer "+token)
                .exchange();
    }

    public Mono<ClientResponse> getTowns(String token, String cityCode) {
        return client.get()
                .uri("/clicknship/Operations/DeliveryTowns?CityCode="+cityCode)
                .header("Authorization","Bearer "+token)
                .exchange();
    }

    public Mono<ClientResponse> calculateDeliveryFee(String token, DeliveryFeeRequest dto) {
        return client.post()
                .uri("/clicknship/Operations/DeliveryFee")
                .header("Authorization","Bearer "+token)
                .syncBody(dto)
                .exchange();
    }

    public Mono<ClientResponse> submitShipmentRequest(String token, ShipmentRequest dto) {
        return client.post()
                .uri("/clicknship/Operations/PickupRequest")
                .header("Authorization","Bearer "+token)
                .syncBody(dto)
                .exchange();
    }

    public Mono<ClientResponse> trackShipment(String token, String waybillNo) {
        return client.get()
                .uri("/clicknship/Operations/TrackShipment?waybillno="+ waybillNo)
                .header("Authorization","Bearer "+token)
                .exchange();
    }

    public Mono<ClientResponse> printWaybill(String token, String waybillNo) {
        return client.get()
                .uri("/clicknship/Operations/PrintWaybill?waybillno="+ waybillNo)
                .header("Authorization","Bearer "+token)
                .exchange();
    }

    public Mono<ClientResponse> getDropOffLocations(String token, String cityCode) {
        return client.get()
                .uri("/clicknship/Operations/DropOffAddresses?citycode="+cityCode)
                .header("Authorization","Bearer "+token)
                .exchange();
    }
}
