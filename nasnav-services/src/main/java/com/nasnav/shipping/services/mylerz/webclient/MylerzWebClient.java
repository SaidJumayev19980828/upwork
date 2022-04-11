package com.nasnav.shipping.services.mylerz.webclient;

import com.nasnav.shipping.services.mylerz.webclient.dto.Piece;
import com.nasnav.shipping.services.mylerz.webclient.dto.ShipmentRequest;
import com.nasnav.shipping.services.mylerz.webclient.dto.DeliveryFeeRequest;
import org.json.JSONArray;
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

    public Mono<ClientResponse> submitShipmentRequest(String token, ShipmentRequest dto) {
        // had to build the request body from scratch due to error from shipping service provider
        JSONArray piecesArr = new JSONArray();
        for(Piece p : dto.getPieces()) {
            piecesArr.put(new JSONObject().put("PieceNo", p.getPieceNo()));
        }
        JSONObject json = new JSONObject();
        json.put("WarehouseName", dto.getShopName());
        json.put("PickupDueDate", dto.getPickupDate());
        json.put("Package_Serial", dto.getSerial());
        json.put("Description", dto.getDescription());
        json.put("Total_Weight", dto.getTotalWeight());
        json.put("Service_Type", dto.getServiceType());
        json.put("Service", dto.getService());
        json.put("ServiceDate", dto.getServiceDate());
        json.put("Service_Category", dto.getServiceCategory());
        json.put("Payment_Type", dto.getPaymentType());
        json.put("COD_Value", dto.getCodValue());
        json.put("Customer_Name", dto.getCustomerName());
        json.put("Mobile_No", dto.getMobileNo());
        json.put("country", dto.getCountry());
        json.put("Apartment_No", dto.getApartmentNo());
        json.put("Street", dto.getStreet());
        json.put("Neighborhood", dto.getNeighborhood());
        json.put("Pieces", piecesArr);

        return client.post()
                .uri("/api/Orders/AddOrders")
                .header("Authorization","Bearer "+token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new JSONArray().put(json).toString())
                .exchange();
    }

    public Mono<ClientResponse> getAWB(String token, String barcode) {
        JSONObject body = new JSONObject().put("Barcode", barcode);
        return client.post()
                .uri("/api/Packages/GetAWB")
                .header("Authorization","Bearer "+token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body.toString())
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
