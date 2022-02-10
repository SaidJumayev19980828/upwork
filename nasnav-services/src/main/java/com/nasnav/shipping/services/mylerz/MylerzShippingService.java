package com.nasnav.shipping.services.mylerz;

import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public class MylerzShippingService implements ShippingService {

    public static final String AWB_MIME = "application/pdf";
    private Logger logger = LogManager.getLogger(getClass());

    public  static final String SERVICE_ID = "MYLERZ" ;
    public static final String SERVICE_NAME = "Mylerz";
    public static final String ICON = "/icons/mylerz.svg";

    public static final String SERVER_URL = "SERVER_URL";
    public static final String USER_NAME = "USER_NAME";
    public static final String PASSWORD = "PASSWORD";
    public static final String GRANT_TYPE = "GRANT_TYPE";
    public static final String DELIVERY_TYPE = "DELIVERY_TYPE";

    @Override
    public ShippingServiceInfo getServiceInfo() {
        return null;
    }

    @Override
    public void setServiceParameters(List<ServiceParameter> params) {

    }

    @Override
    public Mono<ShippingOffer> createShippingOffer(List<ShippingDetails> items) {
        return null;
    }

    @Override
    public Flux<ShipmentTracker> requestShipment(List<ShippingDetails> items) {
        return null;
    }

    @Override
    public Flux<ReturnShipmentTracker> requestReturnShipment(List<ShippingDetails> items) {
        return null;
    }

    @Override
    public void validateShipment(List<ShippingDetails> items) {

    }

    @Override
    public ShipmentStatusData createShipmentStatusData(String serviceId, Long orgId, String params) {
        return null;
    }

    @Override
    public Optional<Long> getPickupShop(String additionalParametersJson) {
        return Optional.empty();
    }

    @Override
    public Mono<String> getAirwayBill(String airwayBillNumber) {
        return null;
    }

    @Override
    public String getTrackingUrl(String trackingNumber) {
        return null;
    }
}
