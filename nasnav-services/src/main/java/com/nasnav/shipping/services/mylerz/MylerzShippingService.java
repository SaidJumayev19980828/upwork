package com.nasnav.shipping.services.mylerz;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.CityRepository;
import com.nasnav.dao.CountryRepository;
import com.nasnav.dao.ShippingAreaRepository;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.service.model.common.Parameter;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.model.*;
import com.nasnav.shipping.services.mylerz.webclient.MylerzWebClient;
import com.nasnav.shipping.services.mylerz.webclient.dto.*;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.service.model.common.ParameterType.STRING;
import static com.nasnav.shipping.model.ShippingServiceType.DELIVERY;
import static com.nasnav.shipping.services.mylerz.DeliveryType.NEXT_DAY_DELIVERY;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

public class MylerzShippingService implements ShippingService {

    public static final String AWB_MIME = "application/pdf";
    private Logger logger = LogManager.getLogger(getClass());

    @Autowired
    private ShippingAreaRepository shippingAreaRepo;
    @Autowired
    private CountryRepository countryRepo;

    private static final String ERR_OUT_OF_SERVICE = "Sorry! We are currently unable to ship to your area!";

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
                    , new Parameter(SERVER_URL, STRING));

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
        return Flux
                .fromIterable(shopsAndItemsMap.values())
                .flatMap(this::createShipmentOffer)
                .collectList()
                .flatMap(shipments -> doCreateShippingOffer(shipments, items.size()));
    }

    private Mono<Shipment> createShipmentOffer(List<ShippingDetails> detailsList) {
        Mono<ShippingEta> etaMono = getShippingEta();
        List<Long> stocks = getStocks(detailsList);
        String serverUrl = getServiceParam(SERVER_URL);
        MylerzWebClient client = new MylerzWebClient(serverUrl);
        Optional<DeliveryFeeRequest> request = createDeliveryFeeRequest(detailsList);
        if (!request.isPresent()) {
            return Mono.empty();
        }
        return client
                .calculateDeliveryFee(AUTH_TOKEN, request.get())
                .flatMap(this::throwExceptionIfNotOk)
                .flatMap(res -> res.bodyToMono(DeliveryFeeResponse.class))
                .map(this::getTotalShippingPrice)
                .zipWith(etaMono)
                .map(feeAndEta -> new Shipment(feeAndEta.getT1(), feeAndEta.getT2(), stocks, null))
                .onErrorResume(err -> {logger.error(err,err); return Mono.empty();});
    }

    private BigDecimal getTotalShippingPrice(DeliveryFeeResponse res) {
        return new BigDecimal(String.valueOf(res.getCharges())).add(res.getShippingFees()).add(res.getVat());
    }

    private Optional<DeliveryFeeRequest> createDeliveryFeeRequest(List<ShippingDetails> detailsList) {
        ShippingDetails details = detailsList.stream().findFirst().orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0010));
        String shopId = details.getShopId() + "";
        String deliveryAreaId = getDeliveryAreaId(details.getDestination().getArea());
        BigDecimal codValue = getTotalCartPrice(detailsList);
        BigDecimal weight = getShipmentItemsWeight(detailsList);

        DeliveryFeeRequest request = new DeliveryFeeRequest();
        request.setShopName(shopId);
        request.setDeliveryAreaId(deliveryAreaId);
        request.setCodValue(codValue);
        request.setServiceCode(NEXT_DAY_DELIVERY.getValue());
        request.setServiceCategoryCode("DELIVERY");
        request.setServiceTypeCode("CTD");
        request.setWeight(weight.doubleValue());
        if (Objects.equals(details.getPaymentMethodId(), "cod")) {
            request.setPaymentTypeCode("COD");
            request.setCodValue(codValue);
        } else {
            request.setPaymentTypeCode("PP");
            request.setCodValue(ZERO);
        }
        return of(request);
    }

    private BigDecimal getShipmentItemsWeight(List<ShippingDetails> detailsList) {
        return detailsList
                .stream()
                .map(ShippingDetails::getItems)
                .flatMap(List::stream)
                .map(ShipmentItems::getWeight)
                .map(weight -> ofNullable(weight).orElse(ZERO))
                .reduce(ZERO, BigDecimal::add);
    }

    private String getDeliveryAreaId(Long areaId) {
        return shippingAreaRepo.getProviderId(SERVICE_ID, areaId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$SHP$0006, areaId));
    }

    private BigDecimal getTotalCartPrice(List<ShippingDetails> detailsList) {
        return detailsList
                .stream()
                .map(ShippingDetails::getItems)
                .flatMap(List::stream)
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(ZERO, BigDecimal::add);
    }

    private List<Long> getStocks(List<ShippingDetails> details) {
        return details
                .stream()
                .map(ShippingDetails::getItems)
                .flatMap(List::stream)
                .map(ShipmentItems::getStockId)
                .collect(toList());
    }

    private Mono<ShippingEta> getShippingEta() {
        return Mono.just(new ShippingEta(now().plusDays(1), now().plusDays(2)));
    }

    private Mono<ShippingOffer> doCreateShippingOffer(List<Shipment> shipments, Integer ordersNum){
        ShippingServiceInfo serviceInfo = getServiceInfo();
        if(isAnyOrderHadNoShipment(shipments, ordersNum)){
            return Mono.just(new ShippingOffer(serviceInfo, ERR_OUT_OF_SERVICE));
        }else{
            return Mono.just(new ShippingOffer(serviceInfo, shipments));
        }
    }

    private boolean isAnyOrderHadNoShipment(List<Shipment> shipments, Integer ordersNum) {
        return isNull(shipments) || !Objects.equals(shipments.size(), ordersNum);
    }

    @Override
    public Flux<ShipmentTracker> requestShipment(List<ShippingDetails> shipments) {

        return Flux
                .fromIterable(shipments)
                .flatMap(this::requestSingleShipment);
    }

    @Override
    public Flux<ReturnShipmentTracker> requestReturnShipment(List<ShippingDetails> items) {
        return null;
    }

    @Override
    public void validateShipment(List<ShippingDetails> items) {

    }

    private Mono<ShipmentTracker> requestSingleShipment(ShippingDetails shipment) {
        String serverUrl = getServiceParam(SERVER_URL);
        MylerzWebClient client = new MylerzWebClient(serverUrl);
        ShipmentRequest request = createShipmentRequest(shipment);
        return client
                .submitShipmentRequest(AUTH_TOKEN, asList(request))
                .flatMap(this::throwExceptionIfNotOk)
                .flatMap(res -> res.bodyToMono(ShipmentResponse.class))
                .flatMap(this::throwErrorForFailureResponse)
                .flatMap(response -> getAirwayBill(client, response))
                .map(res -> createShipmentTracker(shipment, res));
    }

    private Mono<ShipmentResponse> throwErrorForFailureResponse(ShipmentResponse shipmentResponse) {
        if(Objects.equals(shipmentResponse.transStatus, "Failed")){
            RuntimeException ex = new RuntimeBusinessException( INTERNAL_SERVER_ERROR, SHP$SRV$0004, SERVICE_ID, shipmentResponse.toString());
            logger.error(ex,ex);
            return Mono.error(ex);
        }
        return Mono.just(shipmentResponse);
    }


    private ShipmentRequest createShipmentRequest(ShippingDetails shipment) {

        List<Piece> items = shipment.getItems().stream().map(item -> new Piece(item.getStockId())).collect(toList());

        ShipmentRequest request = new ShipmentRequest();
        setSenderInfo(shipment, request);
        setRecipientInfo(shipment, request);
        request.setPieces(items);

        return request;
    }

    private void setSenderInfo(ShippingDetails shipment, ShipmentRequest request) {
        BigDecimal totalWeight = shipment
                .getItems()
                .stream()
                .map(ShipmentItems::getWeight)
                .map(weight -> ofNullable(weight).orElse(ZERO))
                .reduce(ZERO, BigDecimal::add);
        LocalDateTime now = LocalDateTime.now();
        request.setShopName(shipment.getShopId()+"");
        request.setPickupDate(now);
        request.setSerial(shipment.getSubOrderId());
        request.setDescription("A package with " + shipment.getItems().size()+" items");
        request.setTotalWeight(totalWeight.doubleValue());
        request.setServiceType("CTD");
        request.setService("ND");
        request.setServiceDate(now.plusDays(1));
        request.setServiceCategory("DELIVERY");
        if (Objects.equals(shipment.getCodValue(), null)) {
            request.setPaymentType("PP");
            request.setCodValue(ZERO);
        } else {
            request.setPaymentType("COD");
            request.setCodValue(shipment.getCodValue());
        }
    }

    private void setRecipientInfo(ShippingDetails shipment, ShipmentRequest request) {
        ShipmentReceiver receiver = shipment.getReceiver();
        request.setCustomerName(receiver.getFirstName()+" "+receiver.getLastName());
        request.setMobileNo(receiver.getPhone());

        ShippingAddress destination = shipment.getDestination();
        try {
            request.setBuildingNo(Integer.parseInt(destination.getBuildingNumber()));
        } catch (NumberFormatException e) {}
        request.setStreet(destination.getAddressLine1());
        request.setApartmentNo(destination.getFlatNumber());
        request.setCountry(countryRepo.findById(destination.getCountry()).get().getName());
        request.setNeighborhood(getDeliveryAreaId(destination.getArea()));
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

enum DeliveryType {
    NEXT_DAY_DELIVERY("ND"), SAME_DAY_DELIVERY("SD");

    @Getter
    private String value;

    DeliveryType(String value) {
        this.value = value;
    }

    public static List<String> getDeliveryTypes() {
        return stream(values())
                .map(DeliveryType::getValue)
                .collect(toList());
    }
}
