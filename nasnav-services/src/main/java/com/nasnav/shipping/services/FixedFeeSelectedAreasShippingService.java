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

import static com.nasnav.commons.utils.EntityUtils.firstExistingValueOf;
import static com.nasnav.enumerations.ShippingStatus.DELIVERED;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0002;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0010;
import static com.nasnav.service.model.common.ParameterType.*;
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

public class FixedFeeSelectedAreasShippingService implements ShippingService {

    private static final Logger logger = LogManager.getLogger();

    static final public String SERVICE_ID = "FIXED_FEE_SELECTED_AREAS";
    static final public String SERVICE_NAME = "Home Delivery";
    static final public String ICON = "/icons/delivery_logo.svg";
    static final public String SUPPORTED_AREAS = "SUPPORTED_AREAS";
    static final public String MIN_SHIPPING_FEE = "MIN_SHIPPING_FEE";
    private static final String RETURN_SHIPMENT_EMAIL_MSG = "Please call customer service to arrange a return shipment, and sorry again for any inconvenience!";
    public static final String ETA_DAYS_MIN = "ETA_DAYS_MIN";
    public static final String ETA_DAYS_MAX = "ETA_DAYS_MAX";
    public static final String ETA_MINUTES_MIN = "ETA_MINUTES_MIN";
    public static final String ETA_MINUTES_MAX = "ETA_MINUTES_MAX";
    public static final String APOLOGY_MSG = "APOLOGY_MSG";
    public static final String DEFAULT_ERR_AREA_NOT_SUPPORTED = "We are very sorry! It seems we don't support shipping to your area!";

    protected static final List<Parameter> SERVICE_PARAM_DEFINITION =
            asList( new Parameter(SUPPORTED_AREAS , LONG_ARRAY)
                    , new Parameter(MIN_SHIPPING_FEE, NUMBER)
                    , new Parameter(ETA_DAYS_MIN, NUMBER, false)
                    , new Parameter(ETA_DAYS_MAX, NUMBER, false)
                    , new Parameter(ETA_MINUTES_MIN, NUMBER, false)
                    , new Parameter(ETA_MINUTES_MAX, NUMBER, false)
                    , new Parameter(APOLOGY_MSG, STRING, false));

    private static final Integer ETA_DAYS_MIN_DEFAULT = 1;
    private static final Integer ETA_DAYS_MAX_DEFAULT = 1;

    private List<Long> supportedAreas;
    private BigDecimal minFee;
    private Integer etaMinutesMin;
    private Integer etaMinutesMax;
    private String apologyMsg;


    @Autowired
    @Setter
    private ObjectMapper objectMapper;



    public FixedFeeSelectedAreasShippingService() {
        etaMinutesMin = ETA_DAYS_MIN_DEFAULT*1440;
        etaMinutesMax = ETA_DAYS_MAX_DEFAULT*1440;
        apologyMsg = DEFAULT_ERR_AREA_NOT_SUPPORTED;
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
                ofNullable(serviceParameters.get(SUPPORTED_AREAS))
                        .orElse("[]");
        String minFeeString =
                ofNullable(serviceParameters.get(MIN_SHIPPING_FEE))
                        .orElse("0");
        try {
            supportedAreas = objectMapper.readValue(supportedCitiesString, new TypeReference<List<Long>>(){});
            minFee = new BigDecimal(minFeeString);
            validateSupportedAreas(supportedAreas);
            setEtaMinutesMin(serviceParameters);
            setEtaMinutesMax(serviceParameters);
            setApologyMsg(serviceParameters);
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
        if(!areAreasSupported(shippingInfo)) {
            return Mono.just(new ShippingOffer(serviceInfo, apologyMsg));
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
        ShippingEta eta = new ShippingEta(now().plusMinutes(etaMinutesMin), now().plusMinutes(etaMinutesMax));
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
        if(!areAreasSupported(items)) {
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



    private void setEtaMinutesMin(Map<String, String> serviceParams) {
        firstExistingValueOf(
            getIntegerParameter(serviceParams, ETA_MINUTES_MIN),
            getEtaFromDays(serviceParams, ETA_DAYS_MIN))
        .ifPresent(val -> etaMinutesMin = val);
    }


    private Optional<Integer> getEtaFromDays(Map<String, String> serviceParams, String etaDaysMin) {
        return getIntegerParameter(serviceParams, etaDaysMin)
                .map(this::toMinutes);
    }



    private Optional<Integer> getIntegerParameter(Map<String, String> serviceParams, String etaDaysMin) {
        return ofNullable(serviceParams.get(etaDaysMin))
                .flatMap(EntityUtils::parseLongSafely)
                .map(Long::intValue);
    }



    private Integer toMinutes(Integer days) {
        return days*1440;
    }



    private void setEtaMinutesMax(Map<String, String> serviceParams) {
        firstExistingValueOf(
                getIntegerParameter(serviceParams, ETA_MINUTES_MAX),
                getEtaFromDays(serviceParams, ETA_DAYS_MAX))
        .ifPresent(val -> etaMinutesMax = val);
    }



    private void setApologyMsg(Map<String, String> serviceParams) {
        ofNullable(serviceParams.get(APOLOGY_MSG))
                .ifPresent(val -> apologyMsg = val);
    }



    private void validateSupportedAreas(List<Long> cities) {
        if(cities == null || cities.isEmpty()) {
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0002, SERVICE_ID);
        }
    }



    private boolean areAreasSupported(List<ShippingDetails> details) {
        return details
                .stream()
                .map(this::getAreas)
                .flatMap(List::stream)
                .allMatch(supportedAreas::contains);
    }



    private List<Long> getAreas(ShippingDetails details){
        Long destinationArea = details.getDestination().getArea();
        Long sourceArea = details.getSource().getArea();
        return asList(destinationArea, sourceArea);
    }



    private ReturnShipmentTracker createReturnShipment(ShippingDetails shippingDetails) {
        return new ReturnShipmentTracker(new ShipmentTracker(shippingDetails), RETURN_SHIPMENT_EMAIL_MSG);
    }
}
