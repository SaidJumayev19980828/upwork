package com.nasnav.shipping.services.clicknship;

import com.google.common.collect.ImmutableMap;
import com.nasnav.dao.ShippingAreaRepository;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.service.model.common.Parameter;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.model.*;
import com.nasnav.shipping.services.clicknship.webclient.ClickNshipWebClient;
import com.nasnav.shipping.services.clicknship.webclient.dto.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.*;

import static com.nasnav.commons.utils.EntityUtils.anyIsEmpty;
import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.service.model.common.ParameterType.STRING;
import static com.nasnav.shipping.model.DeliveryType.NORMAL_DELIVERY;
import static com.nasnav.shipping.model.ShippingServiceType.DELIVERY;
import static com.nasnav.shipping.utils.ShippingUtils.createAwbFileName;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

public class ClickNShipShippingService implements ShippingService {
    public static final String AWB_MIME = "application/pdf";
    private Logger logger = LogManager.getLogger(getClass());
    private static final String ERR_OUT_OF_SERVICE = "Sorry! We are currently unable to ship to your area!";

    @Autowired
    private ShippingAreaRepository shippingAreaRepo;

    private List<ServiceParameter> serviceParams;
    private Map<String,String> paramMap;
    public static final String SERVICE_ID = "CLICKNSHIP" ;
    public static final String SERVICE_NAME = "ClickNShip";
    public static final String ICON = "/icons/clicknship_logo.jpg";

    public static final String RETURN_EMAIL_MSG =
            "Thanks for patience! " +
                    "Our delivery agents will contact you soon to receive the items from you." +
                    "Please make sure to print the attached airway bills and provide them to the " +
                    "delivery agent.";

    public static String AUTH_TOKEN = "AUTH_TOKEN";
    public static final String SERVER_URL = "SERVER_URL";
    public static final String USER_NAME = "USER_NAME";
    public static final String PASSWORD = "PASSWORD";
    public static final String GRANT_TYPE = "GRANT_TYPE";
    public static final String DELIVERY_TYPE = "DELIVERY_TYPE";

    private static List<Parameter> SERVICE_PARAM_DEFINITION =
            asList( new Parameter(SERVER_URL, STRING)
                    , new Parameter(USER_NAME, STRING)
                    , new Parameter(PASSWORD, STRING)
                    , new Parameter(GRANT_TYPE, STRING));

    private static final List<Parameter> ADDITIONAL_PARAM_DEFINITION = emptyList();

    private static final Map<Long, City> cityIdMapping =
            ImmutableMap
                    .<Long,City>builder()
                    .put(1001L, new City("ABA", "ABA"))
                    .put(1002L, new City("AKA", "ABAKALIKI"))
                    .put(1003L, new City("ABK", "ABEOKUTA"))
                    .put(1004L, new City("ABV", "ABUJA"))
                    .put(1005L, new City("ADK", "ADO EKITI"))
                    .put(1006L, new City("AKR", "AKURE"))
                    .put(1007L, new City("ASB", "ASABA"))
                    .put(1008L, new City("AWK", "AWKA"))
                    .put(1009L, new City("BAU", "BAUCHI"))
                    .put(1010L, new City("BNI", "BENIN"))
                    .put(1011L, new City("BRK", "BIRNIN KEBBI"))
                    .put(1012L, new City("BNY", "BONNY"))
                    .put(1013L, new City("CBQ", "CALABAR"))
                    .put(1014L, new City("DAM", "DAMATURU"))
                    .put(1015L, new City("DUT", "DUTSE"))
                    .put(1016L, new City("EKT", "EKET"))
                    .put(1017L, new City("ENU", "ENUGU"))
                    .put(1018L, new City("GOM", "GOMBE"))
                    .put(1019L, new City("GUS", "GUSAU"))
                    .put(1020L, new City("IBA", "IBADAN"))
                    .put(1021L, new City("IJB", "IJEBU ODE"))
                    .put(1022L, new City("IKP", "IKOT EKPENE"))
                    .put(1023L, new City("IFE", "ILE-IFE"))
                    .put(1024L, new City("ILR", "ILORIN"))
                    .put(1025L, new City("JAL", "JALINGO"))
                    .put(1026L, new City("JOS", "JOS"))
                    .put(1027L, new City("KAD", "KADUNA"))
                    .put(1028L, new City("KAN", "KANO"))
                    .put(1029L, new City("KAS", "KASTINA"))
                    .put(1030L, new City("LAF", "LAFIA"))
                    .put(1031L, new City("ISL", "LAGOS ISLAND"))
                    .put(1032L, new City("MLD", "LAGOS MAINLAND"))
                    .put(1033L, new City("LKJ", "LOKOJA"))
                    .put(1034L, new City("MIU", "MAIDUGURI"))
                    .put(1035L, new City("MDI", "MAKURDI"))
                    .put(1036L, new City("MNA", "MINNA"))
                    .put(1037L, new City("NNI", "NNEWI"))
                    .put(1038L, new City("NSK", "NSUKKA"))
                    .put(1039L, new City("OFA", "OFA"))
                    .put(1040L, new City("OGB", "OGBOMOSHO"))
                    .put(1041L, new City("ONA", "ONITSHA"))
                    .put(1042L, new City("OSG", "OSHOGBO"))
                    .put(1043L, new City("ORI", "OWERRI"))
                    .put(1044L, new City("OYO", "OYO"))
                    .put(1045L, new City("PHC", "PORT HARCOURT"))
                    .put(1046L, new City("SAG", "SAGAMU"))
                    .put(1047L, new City("OTA", "SANGO OTA"))
                    .put(1048L, new City("SAE", "SAPELE"))
                    .put(1049L, new City("SKO", "SOKOTO"))
                    .put(1050L, new City("SUL", "SULEJA"))
                    .put(1051L, new City("UHA", "UMUAHIA"))
                    .put(1052L, new City("UYO", "UYO"))
                    .put(1053L, new City("WRI", "WARRI"))
                    .put(1054L, new City("YEN", "YENAGOA"))
                    .put(1055L, new City("YOL", "YOLA"))
                    .put(1056L, new City("ZRI", "ZARIA"))
                    .build();

    public ClickNShipShippingService() {
        paramMap = new HashMap<>();
    }


    @Override
    public ShippingServiceInfo getServiceInfo() {
        return new ShippingServiceInfo(
                SERVICE_ID
                , SERVICE_NAME
                ,false
                , ADDITIONAL_PARAM_DEFINITION
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



    private void authenticateUser() {
        String serverUrl = getServiceParam(SERVER_URL);
        String userName = getServiceParam(USER_NAME);
        String password = getServiceParam(PASSWORD);
        String grantType = getServiceParam(GRANT_TYPE);

        ClickNshipWebClient client = new ClickNshipWebClient(serverUrl);
        AUTH_TOKEN = client.authenticateUser(userName, password, grantType)
                .flatMap(this::throwExceptionIfNotOk)
                .flatMap(res-> res.bodyToMono(UserAuthenticationResponse.class))
                .map(UserAuthenticationResponse::getAccessToken)
                .block();
    }



    private String getServiceParam(String paramName) {
        return ofNullable(paramMap.get(paramName))
                .orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0003, paramName, SERVICE_ID));
    }



    @Override
    public Mono<ShippingOffer> createShippingOffer(List<ShippingDetails> items) {
        return Flux
                .fromIterable(items)
                .flatMap(this::createShipmentOffer)
                .collectList()
                .flatMap(shipments -> doCreateShippingOffer(shipments, items.size()));
    }



    private Mono<ShippingOffer> doCreateShippingOffer(List<Shipment> shipments, Integer ordersNum){
        ShippingServiceInfo serviceInfo = createServiceInfoWithDeliveryOptions();
        if(isAnyOrderHadNoShipment(shipments, ordersNum)){
            return Mono.just(new ShippingOffer(serviceInfo, ERR_OUT_OF_SERVICE));
        }else{
            return Mono.just(new ShippingOffer(serviceInfo, shipments));
        }
    }



    private boolean isAnyOrderHadNoShipment(List<Shipment> shipments, Integer ordersNum) {
        return isNull(shipments) || !Objects.equals(shipments.size(), ordersNum);
    }


    private Mono<Shipment> createShipmentOffer(ShippingDetails details) {
        Mono<ShippingEta> etaMono = getShippingEta();
        List<Long> stocks = getStocks(details);
        String serverUrl = getServiceParam(SERVER_URL);
        ClickNshipWebClient client = new ClickNshipWebClient(serverUrl);
        Optional<DeliveryFeeRequest> request = createDeliveryFeeRequest(details);
        if (!request.isPresent()) {
            return Mono.empty();
        }
        return client
                .calculateDeliveryFee(AUTH_TOKEN, request.get())
                .flatMap(this::throwExceptionIfNotOk)
                .flatMapMany(res -> res.bodyToFlux(DeliveryFee.class))
                .next()
                .map(DeliveryFee::getTotalAmount)
                .zipWith(etaMono)
                .map(feeAndEta -> new Shipment(feeAndEta.getT1(), feeAndEta.getT2(), stocks, details.getSubOrderId()))
                .onErrorResume(err -> {logger.error(err,err); return Mono.empty();});
    }



    private Mono<ShippingEta> getShippingEta() {
        return Mono.just(new ShippingEta(now().plusDays(1), now().plusDays(4)));
    }




    private Optional<DeliveryFeeRequest> createDeliveryFeeRequest(ShippingDetails details) {
        ShippingAddress originAddr = getSourceAddress(details);
        ShippingAddress destAddr = getDestinationAddress(details);
        Optional<String> originCity = getCityNameOptional(originAddr);
        Optional<String> destCity = getCityNameOptional(destAddr);
        BigDecimal weight = getShipmentItemsWeight(details.getItems());

        if (anyIsEmpty(originCity, destCity)) {
            return Optional.empty();
        }
        DeliveryFeeRequest request = new DeliveryFeeRequest();
        Long areaId = destAddr.getArea();
        if (areaId != null) {
            String providerAreaId = getProviderTownId(areaId);
            request.setOnforwardingTownID(providerAreaId);
        }

        request.setOrigin(originCity.get());
        request.setDestination(destCity.get());
        request.setWeight(weight.toPlainString());
        return of(request);
    }




    private ShippingAddress getDestinationAddress(ShippingDetails details) {
        return ofNullable(details)
                .map(ShippingDetails::getDestination)
                .orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0013, SERVICE_ID));
    }



    private ShippingAddress getSourceAddress(ShippingDetails details) {
        return ofNullable(details)
                .map(ShippingDetails::getSource)
                .orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0013, SERVICE_ID));
    }



    private String getProviderTownId(Long areaId) {
        return shippingAreaRepo.getProviderId(SERVICE_ID, areaId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$SHP$0006, areaId));
    }



    private BigDecimal getShipmentItemsWeight(List<ShipmentItems> items) {
        return items
                .stream()
                .map(ShipmentItems::getWeight)
                .map(weight -> ofNullable(weight).orElse(ZERO))
                .reduce(ZERO, BigDecimal::add);
    }



    private List<Long> getStocks(ShippingDetails details) {
        return ofNullable(details)
                .map(ShippingDetails::getItems)
                .orElse(emptyList())
                .stream()
                .map(ShipmentItems::getStockId)
                .collect(toList());
    }




    @Override
    public Flux<ShipmentTracker> requestShipment(List<ShippingDetails> shipments) {
        return Flux
                .fromIterable(shipments)
                .flatMap(this::requestSingleShipment);
    }




    private Mono<ShipmentTracker> requestSingleShipment(ShippingDetails shipment) {
        String serverUrl = getServiceParam(SERVER_URL);
        ClickNshipWebClient client = new ClickNshipWebClient(serverUrl);
        ShipmentRequest request = createShipmentRequest(shipment);
        return
                client
                .submitShipmentRequest(AUTH_TOKEN, request)
                .flatMap(this::throwExceptionIfNotOk)
                .flatMap(res -> res.bodyToMono(ShipmentResponse.class))
                .flatMap(this::throwErrorForFailureResponse)
                .flatMap(response -> getAirwayBill(client, response))
                .map(res -> createShipmentTracker(shipment, res));
    }



    private ShipmentTracker createShipmentTracker(ShippingDetails shipment, ShipmentResponseWithAwb res) {
        var trackNumber = res.getResponse().getWaybillNumber();
        var tracker = new ShipmentTracker(res.getResponse().getOrderNo(), trackNumber, res.getAirwayBill(), shipment);
        tracker.setAirwayBillFileMime(AWB_MIME);
        tracker.setAirwayBillFileName(createAwbFileName(shipment, trackNumber));
        return tracker;
    }


    private Mono<ShipmentResponse> throwErrorForFailureResponse(ShipmentResponse shipmentResponse) {
        if(Objects.equals(shipmentResponse.transStatus, "Failed")){
            RuntimeException ex = new RuntimeBusinessException( INTERNAL_SERVER_ERROR, SHP$SRV$0004, SERVICE_ID, shipmentResponse.toString());
            logger.error(ex,ex);
            return Mono.error(ex);
        }
        return Mono.just(shipmentResponse);
    }



    private ShippingServiceInfo createServiceInfoWithDeliveryOptions() {
        ShippingServiceInfo serviceInfo = getServiceInfo();
        serviceInfo
                .getAdditionalDataParams()
                .stream()
                .filter(param -> Objects.equals(param.getName(), DELIVERY_TYPE))
                .findFirst()
                .ifPresent(param -> param.setOptions(DeliveryType.getDeliveryTypes()));
        return serviceInfo;
    }




    private Mono<ShipmentResponseWithAwb> getAirwayBill(ClickNshipWebClient client, ShipmentResponse response) {
        Base64.Encoder encoder = Base64.getEncoder();
        return client
                .printWaybill(AUTH_TOKEN, response.getWaybillNumber())
                .flatMap(this::throwExceptionIfNotOk)
                .flatMap(res -> res.bodyToMono(byte[].class))
                .map(fileDataBytes -> encoder.encodeToString(fileDataBytes))
                .map(airwayBill -> new ShipmentResponseWithAwb(response, airwayBill));
    }




    private ShipmentRequest createShipmentRequest(ShippingDetails shipment) {
        ShipmentRequest request = new ShipmentRequest();
        if (Objects.equals(shipment.getCodValue(), null)) {
            request.setPaymentType("prepaid");
        } else {
            request.setPaymentType("pay on delivery");
        }
        request.setDeliveryType(NORMAL_DELIVERY.getValue());
        request.setOrderNo(shipment.getMetaOrderId()+"-"+shipment.getSubOrderId());

        request = setSenderInfo(shipment, request);
        request = setRecipientInfo(shipment, request);

        List<ShipmentItem> shipmentItems =
                shipment.getItems()
                .stream()
                .map(this::createShipmentItems)
                .collect(toList());
        request.setShipmentItems(shipmentItems);

        return request;
    }




    private ShipmentRequest setSenderInfo(ShippingDetails shipment, ShipmentRequest request) {
        ShippingAddress address = getSourceAddress(shipment);
        String senderCity = getCityName(address);
        request.setSenderName(address.getName());
        request.setSenderCity(senderCity);
        Long areaId = address.getArea();
        if (areaId != null) {
            String providerAreaId = getProviderTownId(areaId);
            request.setSenderTownID(providerAreaId);
        }
        request.setSenderAddress(address.getAddressLine1());
        return request;
    }



    private String getCityName(ShippingAddress address) {
        Long cityId = ofNullable(address.getCity()).orElse(-1L);
        return getCityNameOptional(address)
                .orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0005, SERVICE_ID, cityId));
    }



    private Optional<String> getCityNameOptional(ShippingAddress addr) {
        Long cityId = ofNullable(addr.getCity()).orElse(-1L);
        return ofNullable(cityIdMapping.get(cityId))
                .map(City::getCityName);
    }



    private ShipmentRequest setRecipientInfo(ShippingDetails shipment, ShipmentRequest request) {
        ShippingAddress destination = getDestinationAddress(shipment);
        request.setRecipientName(shipment.getReceiver().getFirstName() + " " + shipment.getReceiver().getLastName());
        request.setRecipientPhone(shipment.getReceiver().getPhone());
        request.setRecipientEmail(shipment.getReceiver().getEmail());
        request.setRecipientCity(getCityName(destination));
        Long areaId = destination.getArea();
        if (areaId != null) {
            String providerAreaId = getProviderTownId(areaId);
            request.setRecipientTownID(providerAreaId);
        }
        request.setRecipientAddress(destination.getAddressLine1());
        return request;
    }



    private ShipmentItem createShipmentItems(ShipmentItems item) {
        ShipmentItem shipmentItem = new ShipmentItem();
        shipmentItem.setItemName(item.getName());
        shipmentItem.setItemQuantity(item.getQuantity());
        shipmentItem.setItemUnitCost(item.getPrice());

        return shipmentItem;
    }



    @Override
    public Flux<ReturnShipmentTracker> requestReturnShipment(List<ShippingDetails> items) {
        return requestShipment(items)
                .map(shpTracker -> new ReturnShipmentTracker(shpTracker, RETURN_EMAIL_MSG));
    }



    @Override
    public void validateShipment(List<ShippingDetails> items) {

    }



    @Override
    public ShipmentStatusData createShipmentStatusData(String serviceId, Long orgId, String params) {
        return new ShipmentStatusData();
    }



    @Override
    public Optional<Long> getPickupShop(String additionalParametersJson) {
        return Optional.empty();
    }



    private void validateParams(ServiceParameter param) {
        if(anyIsNull(param, param.getParameter(), param.getValue())) {
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0001, param);
        }
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
}




@Data
@AllArgsConstructor
class ShipmentResponseWithAwb {
    private ShipmentResponse response;
    private String airwayBill;
}