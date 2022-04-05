package com.nasnav.shipping.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.service.model.common.Parameter;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.model.*;
import com.nasnav.shipping.utils.ShippingUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.nasnav.enumerations.ShippingStatus.DELIVERED;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.service.model.common.ParameterType.*;
import static com.nasnav.shipping.model.ShippingServiceType.DELIVERY;
import static com.nasnav.shipping.utils.ShippingUtils.*;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.FLOOR;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

public class VarFeeSelectedSubAreaMinOrderShippingService implements ShippingService {
    private static final Logger logger = LogManager.getLogger();


    static final public String SERVICE_ID = "VAR_FEE_SELECTED_SUBAREAS_MIN_ORDER";
    static final public String SERVICE_NAME = "Special Delivery";
    static final public String ICON = "/icons/delivery_logo.svg";
    static final public String MIN_ORDER_VALUE = "MIN_ORDER_VALUE";
    static final public String INVALID_ORDER_MSG = "INVALID_ORDER_MSG";
    static final public String ETA_MINUTES_MIN = "ETA_MINUTES_MIN";
    static final public String ETA_MINUTES_MAX = "ETA_MINUTES_MAX";
    static final public String APOLOGY_MSG = "APOLOGY_MSG";
    static final public String SUBAREAS_SHIPPING_FEES = "SUBAREAS_SHIPPING_FEES";
    private static final String RETURN_SHIPMENT_EMAIL_MSG = "Please call customer service to arrange a return shipment, and sorry again for any inconvenience!";

    static final private Integer DEFAULT_ETA_MINUTES_MIN = 60;
    static final private Integer DEFAULT_ETA_MINUTES_MAX = 90;
    static final private String DEFAULT_INVALID_ORDER_MSG = "Only Available for orders of %s LE and more!";
    static final private String DEFAULT_ERR_AREA_NOT_SUPPORTED = "We are very sorry! It seems we don't support shipping to your area!";

    protected static final List<Parameter> SERVICE_PARAM_DEFINITION =
            asList( new Parameter(SUBAREAS_SHIPPING_FEES , JSON)
                    , new Parameter(ETA_MINUTES_MIN, NUMBER, false)
                    , new Parameter(ETA_MINUTES_MAX, NUMBER, false)
                    , new Parameter(APOLOGY_MSG, STRING, false)
                    , new Parameter(MIN_ORDER_VALUE, NUMBER, false)
                    , new Parameter(INVALID_ORDER_MSG, STRING, false));

    private BigDecimal minOrderValue;
    private String invalidOrderMsg;
    private Integer etaFrom;
    private Integer etaTo;
    private String apologyMsg;
    private Map<String, BigDecimal> subAreasFees;


    @Autowired
    private ObjectMapper objectMapper;


    public VarFeeSelectedSubAreaMinOrderShippingService(){
        minOrderValue = ZERO;
        invalidOrderMsg = DEFAULT_INVALID_ORDER_MSG;
        etaFrom = DEFAULT_ETA_MINUTES_MIN;
        etaTo = DEFAULT_ETA_MINUTES_MAX;
        apologyMsg = DEFAULT_ERR_AREA_NOT_SUPPORTED;
        subAreasFees = emptyMap();
    }



    @Override
    public ShippingServiceInfo getServiceInfo() {
        return new ShippingServiceInfo(
                SERVICE_ID
                , SERVICE_NAME
                , true
                , SERVICE_PARAM_DEFINITION
                , emptyList()
                , DELIVERY
                , ICON);
    }



    @Override
    public void setServiceParameters(List<ServiceParameter> paramsList) {
        var params = ShippingUtils.toServiceParamMap(paramsList);
        var minOrderString = params.getOrDefault(MIN_ORDER_VALUE, "0");
        etaFrom = getIntegerParameter(params, ETA_MINUTES_MIN).orElse(DEFAULT_ETA_MINUTES_MIN);
        etaTo = getIntegerParameter(params, ETA_MINUTES_MAX).orElse(DEFAULT_ETA_MINUTES_MAX);
        apologyMsg = params.getOrDefault(APOLOGY_MSG, DEFAULT_ERR_AREA_NOT_SUPPORTED);
        try {
            var subAreasFeesStr = params.getOrDefault(SUBAREAS_SHIPPING_FEES, "{}");
            subAreasFees = objectMapper.readValue(subAreasFeesStr, new TypeReference<>(){});
            minOrderValue = new BigDecimal(minOrderString);
            invalidOrderMsg = format(params.getOrDefault(INVALID_ORDER_MSG, DEFAULT_INVALID_ORDER_MSG), minOrderValue.toString());
        } catch (Throwable e) {
            logger.error(e,e);
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0002, SERVICE_ID);
        }
    }



    @Override
    public Mono<ShippingOffer> createShippingOffer(List<ShippingDetails> items) {
        var serviceInfo = getServiceInfo();

        if (items.get(0).getDestination().getId() == -1L)
            return Mono.empty();

        if(!areSubAreasSupported(items)) {
            return Mono.just(new ShippingOffer(serviceInfo, apologyMsg));
        }else if(isOrderValueTooLow(items)){
            return Mono.just(new ShippingOffer(serviceInfo, invalidOrderMsg));
        }
        try{
            return doCreateShippingOffer(items);
        }catch(Throwable t){
            logger.error(t,t);
            return Mono.just(new ShippingOffer(serviceInfo, apologyMsg));
        }
    }



    @Override
    public Flux<ShipmentTracker> requestShipment(List<ShippingDetails> items) {
        validateShipment(items);
        return items
                .stream()
                .map(shippingDetails -> new ShipmentTracker(null, null, null, shippingDetails))
                .collect(
                        collectingAndThen(
                                toList()
                                , Flux::fromIterable));
    }



    @Override
    public Flux<ReturnShipmentTracker> requestReturnShipment(List<ShippingDetails> items) {
        validateShipment(items);
        return items
                .stream()
                .map(this::createReturnShipment)
                .collect(
                        collectingAndThen(
                                toList()
                                , Flux::fromIterable));
    }



    private ReturnShipmentTracker createReturnShipment(ShippingDetails shippingDetails) {
        return new ReturnShipmentTracker(new ShipmentTracker(shippingDetails), RETURN_SHIPMENT_EMAIL_MSG);
    }



    @Override
    public void validateShipment(List<ShippingDetails> items) {
        if(!areSubAreasSupported(items)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0010);
        }
        validateOrderValue(items);
    }



    protected Mono<ShippingOffer> doCreateShippingOffer(List<ShippingDetails> shippingInfo) {
        var serviceInfo = getServiceInfo();
        var shipments =
                shippingInfo
                        .stream()
                        .map(this::createShipmentOfferForSubOrder)
                        .collect(toList());
        return Mono.just(new ShippingOffer(serviceInfo, shipments));
    }



    private Shipment createShipmentOfferForSubOrder(ShippingDetails shippingInfo) {
        var fee = getFeeBasedOnSubArea(shippingInfo);
        var eta = new ShippingEta(now().plusMinutes(etaFrom), now().plusMinutes(etaTo));
        var stockIds = getItemsStockId(shippingInfo);
        return new Shipment(fee, eta, stockIds, shippingInfo.getSubOrderId());
    }



    private BigDecimal getFeeBasedOnSubArea(ShippingDetails shippingInfo) {
        var subAreaId = getCustomerSubArea(shippingInfo);
        return ofNullable(subAreaId)
                    .map(Object::toString)
                    .map(subAreasFees::get)
                    .orElseThrow(()-> new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0010));
    }


    private void validateOrderValue(List<ShippingDetails> shippingDetails) {
        if(isOrderValueTooLow(shippingDetails)){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0015, minOrderValue);
        }
    }



    private boolean isOrderValueTooLow(List<ShippingDetails> shippingDetails) {
        return calcItemsTotalValue(shippingDetails).compareTo(minOrderValue) < 0;
    }



    private boolean areSubAreasSupported(List<ShippingDetails> details) {
        var supportedSubAreas = getSupportedSubAreas();
        return details
                .stream()
                .map(this::getCustomerSubArea)
                .allMatch(supportedSubAreas::contains);
    }



    private Set<Long> getSupportedSubAreas() {
        return subAreasFees
                .keySet()
                .stream()
                .map(EntityUtils::parseLongSafely)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toSet());
    }


    private Long getCustomerSubArea(ShippingDetails shippingDetails) {
        return ofNullable(shippingDetails)
                .map(ShippingDetails::getDestination)
                .map(ShippingAddress::getSubArea)
                .orElse(-1L);
    }


    @Override
    public ShipmentStatusData createShipmentStatusData(String serviceId, Long orgId, String params) {
        var statusData = new ShipmentStatusData();
        statusData.setExternalShipmentId(null);
        statusData.setOrgId(orgId);
        statusData.setServiceId(serviceId);
        statusData.setState(DELIVERED.getValue());
        return statusData;
    }



    @Override
    public Optional<Long> getPickupShop(String additionalParametersJson) {
        return Optional.empty();
    }

    @Override
    public Mono<String> getAirwayBill(String airwayBillNumber) {
        return Mono.empty();
    }

    @Override
    public String getTrackingUrl(String trackingNumber) {
        return null;
    }
}
