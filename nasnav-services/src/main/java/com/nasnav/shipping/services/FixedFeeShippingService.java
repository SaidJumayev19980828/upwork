package com.nasnav.shipping.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.service.model.common.Parameter;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.model.*;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.nasnav.enumerations.ShippingStatus.DELIVERED;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0002;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0010;
import static com.nasnav.service.model.common.ParameterType.LONG_ARRAY;
import static com.nasnav.service.model.common.ParameterType.NUMBER;
import static com.nasnav.shipping.model.ShippingServiceType.DELIVERY;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.FLOOR;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

public class FixedFeeShippingService implements ShippingService {
    private static final Logger logger = LogManager.getLogger();

    static final public String SERVICE_ID = "FIXED_FEE";
    static final public String SERVICE_NAME = "Special Shipping";
    static final public String ICON = "/icons/delivery_logo.svg";
    static final public String SUPPORTED_CITIES = "SUPPORTED_CITIES";
    static final public String MIN_SHIPPING_FEE = "MIN_SHIPPING_FEE";
    private static final String RETURN_SHIPMENT_EMAIL_MSG = "Please call customer service to arrange a return shipment, and sorry again for any inconvenience!";
    public static final String ETA_DAYS_MIN = "ETA_DAYS_MIN";
    public static final String ETA_DAYS_MAX = "ETA_DAYS_MAX";

    public static final String ERR_CITY_NOT_SUPPORTED = "Sorry! Our delivery service doesn't cover this city!";

    protected static final List<Parameter> SERVICE_PARAM_DEFINITION =
            asList( new Parameter(SUPPORTED_CITIES , LONG_ARRAY)
                    , new Parameter(MIN_SHIPPING_FEE, NUMBER)
                    , new Parameter(ETA_DAYS_MIN, NUMBER, false)
                    , new Parameter(ETA_DAYS_MAX, NUMBER, false));

    private static final Integer ETA_DAYS_MIN_DEFAULT = 1;
    private static final Integer ETA_DAYS_MAX_DEFAULT = 1;

    private List<Long> supportedCities;
    private BigDecimal minFee;
    private Integer etaDaysMin;
    private Integer etaDaysMax;


    @Autowired
    @Setter
    private ObjectMapper objectMapper;



    public FixedFeeShippingService() {
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
                , emptyList()
                , DELIVERY
                , ICON);
    }



    @Override
    public void setServiceParameters(List<ServiceParameter> params) {
        Map<String, String> serviceParameters = params
                .stream()
                .collect(
                        toMap(ServiceParameter::getParameter, ServiceParameter::getValue));
        String supportedCitiesString =
                ofNullable(serviceParameters.get(SUPPORTED_CITIES))
                        .orElse("[]");
        String minFeeString =
                ofNullable(serviceParameters.get(MIN_SHIPPING_FEE))
                        .orElse("0");
        try {
            supportedCities = objectMapper.readValue(supportedCitiesString, new TypeReference<List<Long>>(){});
            minFee = new BigDecimal(minFeeString);
            validateSupportedCities(supportedCities);
            setEtaDaysMin(serviceParameters);
            setEtaDaysMax(serviceParameters);
        } catch (Throwable e) {
            logger.error(e,e);
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0002, SERVICE_ID);
        }
    }



    @Override
    public Mono<ShippingOffer> createShippingOffer(List<ShippingDetails> shippingInfo) {
        return doCreateShippingOffer(shippingInfo, getServiceInfo());
    }




    protected Mono<ShippingOffer> doCreateShippingOffer(List<ShippingDetails> shippingInfo, ShippingServiceInfo serviceInfo) {
        if(!areCitiesSupported(shippingInfo)) {
            return Mono.just(new ShippingOffer(serviceInfo, ERR_CITY_NOT_SUPPORTED));
        }
        Integer shipmentsNum = shippingInfo.size();
        List<Shipment> shipments =
                shippingInfo
                        .stream()
                        .map(subOrderInfo -> createShipmentOfferForSubOrder(subOrderInfo, shipmentsNum))
                        .collect(toList());

        correctCalculationError(minFee , shipments);

        return Mono.just(new ShippingOffer(serviceInfo, shipments));
    }



    private Shipment createShipmentOfferForSubOrder(ShippingDetails shippingInfo, Integer shipmentsNumInt) {
        BigDecimal shipmentsNum = BigDecimal.valueOf(shipmentsNumInt);
        BigDecimal fee = minFee.divide(shipmentsNum, 2, FLOOR);
        ShippingEta eta = new ShippingEta(now().plusDays(etaDaysMin), now().plusDays(etaDaysMax));
        List<Long> stockIds = getItemsStockId(shippingInfo);
        return new Shipment(fee, eta, stockIds, shippingInfo.getSubOrderId());
    }



    private List<Long> getItemsStockId(ShippingDetails shippingInfo) {
        return shippingInfo
                .getItems()
                .stream()
                .map(ShipmentItems::getStockId)
                .collect(toList());
    }




    private void correctCalculationError(BigDecimal fee, List<Shipment> shipments) {
        BigDecimal accumlatedFeeTotal =
                shipments
                        .stream()
                        .map(Shipment::getShippingFee)
                        .reduce(ZERO, BigDecimal::add);
        BigDecimal error = fee.subtract(accumlatedFeeTotal);
        shipments
                .stream()
                .peek( shipment -> shipment.setShippingFee(shipment.getShippingFee().add(error)))
                .findFirst();
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



    @Override
    public void validateShipment(List<ShippingDetails> items) {
        if(!areCitiesSupported(items)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0010);
        }
    }



    @Override
    public ShipmentStatusData createShipmentStatusData(String serviceId, Long orgId, String params) {
        ShipmentStatusData statusData = new ShipmentStatusData();
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


    private void setEtaDaysMin(Map<String, String> serviceParams) {
        ofNullable(serviceParams.get(ETA_DAYS_MIN))
                .flatMap(EntityUtils::parseLongSafely)
                .map(Long::intValue)
                .ifPresent(val -> etaDaysMin = val);
    }



    private void setEtaDaysMax(Map<String, String> serviceParams) {
        ofNullable(serviceParams.get(ETA_DAYS_MAX))
                .flatMap(EntityUtils::parseLongSafely)
                .map(Long::intValue)
                .ifPresent(val -> etaDaysMax = val);
    }




    private void validateSupportedCities(List<Long> cities) {
        if(cities == null || cities.isEmpty()) {
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0002, SERVICE_ID);
        }
    }



    private boolean areCitiesSupported(List<ShippingDetails> details) {
        return details
                .stream()
                .map(this::getCities)
                .flatMap(List::stream)
                .allMatch(supportedCities::contains);
    }



    private List<Long> getCities(ShippingDetails details){
        Long destinationCity = details.getDestination().getCity();
        Long sourceCity = details.getSource().getCity();
        return asList(destinationCity, sourceCity);
    }



    private ReturnShipmentTracker createReturnShipment(ShippingDetails shippingDetails) {
        return new ReturnShipmentTracker(new ShipmentTracker(shippingDetails), RETURN_SHIPMENT_EMAIL_MSG);
    }
}
