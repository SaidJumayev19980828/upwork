package com.nasnav.shipping.services.mylerz;

import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.service.model.common.Parameter;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0001;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0002;
import static com.nasnav.service.model.common.ParameterType.NUMBER;
import static com.nasnav.service.model.common.ParameterType.STRING;
import static com.nasnav.shipping.model.ShippingServiceType.DELIVERY;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class MylerzShippingService implements ShippingService {

    public static final String AWB_MIME = "application/pdf";
    private Logger logger = LogManager.getLogger(getClass());

    private List<ServiceParameter> serviceParams;
    private Map<String,String> paramMap;
    public  static final String SERVICE_ID = "MYLERZ" ;
    public static final String SERVICE_NAME = "Mylerz";
    public static final String ICON = "/icons/mylerz.svg";

    public static final String SERVER_URL = "SERVER_URL";
    public static final String USER_NAME = "USER_NAME";
    public static final String PASSWORD = "PASSWORD";
    public static final String GRANT_TYPE = "GRANT_TYPE";
    public static final String COD_VALUE = "COD_VALUE";
    public static final String DELIVERY_TYPE = "DELIVERY_TYPE";

    private static List<Parameter> SERVICE_PARAM_DEFINITION =
            asList(new Parameter(USER_NAME, STRING)
                    , new Parameter(PASSWORD, STRING)
                    , new Parameter(GRANT_TYPE, STRING)
                    , new Parameter(SERVER_URL, STRING)
                    , new Parameter(COD_VALUE, NUMBER));

    public MylerzShippingService() {
        paramMap = new HashMap<>();
    }

    @Override
    public ShippingServiceInfo getServiceInfo() {
        return new ShippingServiceInfo(
                SERVICE_ID
                , SERVICE_NAME
                , false
                , SERVICE_PARAM_DEFINITION
                , emptyList()
                , DELIVERY
                , ICON);
    }

    @Override
    public void setServiceParameters(List<ServiceParameter> params) {
        this.serviceParams = params;
        this.paramMap =
                this.serviceParams
                        .stream()
                        .filter(Objects::nonNull)
                        .peek(this::validateParams)
                        .collect(toMap(ServiceParameter::getParameter, ServiceParameter::getValue));

        if(paramMap.keySet().size() != SERVICE_PARAM_DEFINITION.size()) {
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0002, SERVICE_ID);
        }
    }

    private void validateParams(ServiceParameter param) {
        if(anyIsNull(param, param.getParameter(), param.getValue())) {
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0001, param);
        }
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
