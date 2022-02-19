package com.nasnav.shipping.services.mylerz;

import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.service.model.common.Parameter;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.model.*;
import com.nasnav.shipping.services.mylerz.webclient.MylerzWebClient;
import com.nasnav.shipping.services.mylerz.webclient.dto.AuthenticationResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.service.model.common.ParameterType.NUMBER;
import static com.nasnav.service.model.common.ParameterType.STRING;
import static com.nasnav.shipping.model.ShippingServiceType.DELIVERY;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

public class MylerzShippingService implements ShippingService {

    public static final String AWB_MIME = "application/pdf";
    private Logger logger = LogManager.getLogger(getClass());

    private List<ServiceParameter> serviceParams;
    private Map<String,String> paramMap;
    public  static final String SERVICE_ID = "MYLERZ" ;
    public static final String SERVICE_NAME = "Mylerz";
    public static final String ICON = "/icons/mylerz.svg";

    public static String AUTH_TOKEN = "AUTH_TOKEN";
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
        authenticateUser();
    }

    private String getServiceParam(String paramName) {
        return ofNullable(paramMap.get(paramName))
                .orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0003, paramName, SERVICE_ID));
    }

    private void authenticateUser() {
        String serverUrl = getServiceParam(SERVER_URL);
        String userName = getServiceParam(USER_NAME);
        String password = getServiceParam(PASSWORD);
        String grantType = getServiceParam(GRANT_TYPE);

        MylerzWebClient client = new MylerzWebClient(serverUrl);
        AUTH_TOKEN = client.authenticate(userName, password, grantType)
                .flatMap(this::throwExceptionIfNotOk)
                .flatMap(res-> res.bodyToMono(AuthenticationResponse.class))
                .map(AuthenticationResponse::getAccessToken)
                .block();
    }

    private Mono<ClientResponse> throwExceptionIfNotOk(ClientResponse response) {
        return just(response)
                .flatMap(this::checkResponse);
    }

    private Mono<ClientResponse> checkResponse(ClientResponse response){
        if(response.rawStatusCode() < 400) {
            return Mono.just(response);
        }else {
            return error( getFailedResponseRuntimeException(response));
        }
    }

    private RuntimeException getFailedResponseRuntimeException(ClientResponse response) {
        RuntimeException ex =  new RuntimeBusinessException( INTERNAL_SERVER_ERROR, SHP$SRV$0004, SERVICE_ID, getResponseAsStr(response));
        logger.error(ex,ex);
        return ex;
    }

    private String getResponseAsStr(ClientResponse response) {
        response
                .toEntity(String.class)
                .subscribe(res -> logger.info(format(" >>> shipping service [%s] failed, request returned response body [%s]" , SERVICE_ID, res.getBody())));
        return format("{status : %s}", response.statusCode());
    }

    private void validateParams(ServiceParameter param) {
        if(anyIsNull(param, param.getParameter(), param.getValue())) {
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0001, param);
        }
    }

    @Override
    public Mono<ShippingOffer> createShippingOffer(List<ShippingDetails> items) {
        Map<Long, List<ShippingDetails>> shopsAndItemsMap = items.stream().collect(groupingBy(ShippingDetails::getShopId));

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
