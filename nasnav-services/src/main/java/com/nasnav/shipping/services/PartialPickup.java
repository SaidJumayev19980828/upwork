package com.nasnav.shipping.services;

import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.enumerations.ShippingStatus;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.service.CartService;
import com.nasnav.service.model.common.Parameter;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.*;

import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0011;
import static com.nasnav.service.model.common.ParameterType.*;
import static com.nasnav.shipping.model.ShippingServiceType.PICKUP;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

public class PartialPickup implements ShippingService {

    private final Logger logger = LogManager.getLogger();
    public static final String SERVICE_ID = "PARTIAL_PICKUP";
    public static final String SERVICE_NAME = "Partial pickup from Shop";
    public static final String ICON = "/icons/pickup_from_shop_logo.svg";
    public static final String SHOP_ID = "SHOP_ID";
    public static final String RETURN_EMAIL_MSG =
            "Thanks for you patience! To complete the return process, please return " +
                    " the items back to shop and provide the sellers with this email!";
    public static final String ETA_DAYS_MIN = "ETA_DAYS_MIN";
    public static final String ETA_DAYS_MAX = "ETA_DAYS_MAX";

    private static final Integer ETA_DAYS_MIN_DEFAULT = 1;
    private static final Integer ETA_DAYS_MAX_DEFAULT = 7;

    private static final List<Parameter> SERVICE_PARAM_DEFINITION =
            asList(new Parameter(ETA_DAYS_MIN, NUMBER, false),
                    new Parameter(ETA_DAYS_MAX, NUMBER, false));

    private static final List<Parameter> ADDITIONAL_PARAM_DEFINITION =
            asList(new Parameter(SHOP_ID, NUMBER));

    private Integer etaDaysMin;
    private Integer etaDaysMax;

    @Autowired
    private CartService cartService;

    public PartialPickup() {
        etaDaysMin = ETA_DAYS_MIN_DEFAULT;
        etaDaysMax = ETA_DAYS_MAX_DEFAULT;
    }

    @Override
    public ShippingServiceInfo getServiceInfo() {
        return new ShippingServiceInfo(
                SERVICE_ID
                , SERVICE_NAME
                , true
                , SERVICE_PARAM_DEFINITION
                , ADDITIONAL_PARAM_DEFINITION
                , PICKUP
                , ICON);
    }

    @Override
    public void setServiceParameters(List<ServiceParameter> params) {
        Map<String, String> serviceParams =
                params.stream().collect(toMap(ServiceParameter::getParameter, ServiceParameter::getValue, (v1, v2) -> v1));
        setEtaDaysMin(serviceParams);
        setEtaDaysMax(serviceParams);
    }

    private void setEtaDaysMax(Map<String, String> serviceParams) {
        ofNullable(serviceParams.get(ETA_DAYS_MAX))
                .flatMap(EntityUtils::parseLongSafely)
                .map(Long::intValue)
                .ifPresent(val -> etaDaysMax = val);
    }

    private void setEtaDaysMin(Map<String, String> serviceParams) {
        ofNullable(serviceParams.get(ETA_DAYS_MIN))
                .flatMap(EntityUtils::parseLongSafely)
                .map(Long::intValue)
                .ifPresent(val -> etaDaysMin = val);
    }

    @Override
    public Mono<ShippingOffer> createShippingOffer(List<ShippingDetails> items) {
        List<ShopRepresentationObject> shops = cartService.getShopsThatCanProvideEachItem();

        ShippingServiceInfo serviceInfo = createServiceInfoWithShopsOptions(shops);

        List<Shipment> shipments =
                items
                        .stream()
                        .map(this::createShipment)
                        .collect(toList());

        ShippingOffer offer = new ShippingOffer(serviceInfo, shipments);
        return shops.isEmpty()? Mono.empty() : Mono.just(offer) ;
    }


    private Shipment createShipment(ShippingDetails shippingDetails) {
        BigDecimal fee = BigDecimal.ZERO;
        ShippingEta eta = new ShippingEta(now().plusDays(etaDaysMin), now().plusDays(etaDaysMax));
        List<Long> stocks =
                shippingDetails
                        .getItems()
                        .stream()
                        .map(ShipmentItems::getStockId)
                        .collect(toList());
        Long orderId = shippingDetails.getSubOrderId();
        return new Shipment(fee, eta, stocks ,orderId);
    }




    private ShippingServiceInfo createServiceInfoWithShopsOptions(List<ShopRepresentationObject> shops) {
        ShippingServiceInfo serviceInfo = getServiceInfo();

        serviceInfo
                .getAdditionalDataParams()
                .stream()
                .filter(param -> Objects.equals(param.getName(), SHOP_ID))
                .findFirst()
                .ifPresent(param -> param.setOptions(asList(shops.toArray())));

        return serviceInfo;
    }

    @Override
    public Flux<ShipmentTracker> requestShipment(List<ShippingDetails> items) {
        ShipmentTracker tracker =
                items
                        .stream()
                        .map(ShipmentTracker::new)
                        .findFirst()
                        .orElse(new ShipmentTracker());
        return Flux.just(tracker);
    }

    @Override
    public void validateShipment(List<ShippingDetails> items) {
        if(items.size()!= 1) {
            String message = "Cart has items from multiple shops, while pickup service is allowed "
                    + "for a single shop!";
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0011, message);
        }
    }


    @Override
    public ShipmentStatusData createShipmentStatusData(String serviceId, Long orgId, String params) {
        ShipmentStatusData status = new ShipmentStatusData();
        status.setOrgId(orgId);
        status.setServiceId(serviceId);
        status.setState(ShippingStatus.valueOf(params).getValue());
        return status;
    }



    @Override
    public Optional<Long> getPickupShop(String additionalParametersJson) {
        try{
            return ofNullable(additionalParametersJson)
                    .map(JSONObject::new)
                    .map(json -> json.getLong(SHOP_ID));
        }catch(Throwable e){
            logger.error(e,e);
            return Optional.empty();
        }
    }

    @Override
    public Mono<String> getAirwayBill(String airwayBillNumber) {
        return Mono.empty();
    }

    @Override
    public String getTrackingUrl(String trackingNumber) {
        return null;
    }


    @Override
    public Flux<ReturnShipmentTracker> requestReturnShipment(List<ShippingDetails> items) {
        return requestShipment(items)
                .map(shpTracker -> new ReturnShipmentTracker(shpTracker, RETURN_EMAIL_MSG));
    }

}
