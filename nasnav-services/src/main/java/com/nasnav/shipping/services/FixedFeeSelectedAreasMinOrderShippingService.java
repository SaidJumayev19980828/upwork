package com.nasnav.shipping.services;

import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.service.model.common.Parameter;
import com.nasnav.shipping.model.ServiceParameter;
import com.nasnav.shipping.model.ShippingDetails;
import com.nasnav.shipping.model.ShippingOffer;
import com.nasnav.shipping.model.ShippingServiceInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0002;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0015;
import static com.nasnav.service.model.common.ParameterType.*;
import static com.nasnav.shipping.model.ShippingServiceType.DELIVERY;
import static com.nasnav.shipping.utils.ShippingUtils.calcItemsTotalValue;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

public class FixedFeeSelectedAreasMinOrderShippingService extends FixedFeeSelectedAreasShippingService{
    private static final Logger logger = LogManager.getLogger();


    static final public String SERVICE_ID = "FIXED_FEE_SELECTED_AREAS_MIN_ORDER";
    static final public String MIN_ORDER_VALUE = "MIN_ORDER_VALUE";
    static final public String INVALID_ORDER_MSG = "INVALID_ORDER_MSG";
    static final public String DEFAULT_INVALID_ORDER_MSG = "Only Available for orders of %s LE and more!";

    protected static final List<Parameter> SERVICE_PARAM_DEFINITION =
            asList( new Parameter(SUPPORTED_AREAS , LONG_ARRAY)
                    , new Parameter(MIN_SHIPPING_FEE, NUMBER)
                    , new Parameter(ETA_DAYS_MIN, NUMBER, false)
                    , new Parameter(ETA_DAYS_MAX, NUMBER, false)
                    , new Parameter(ETA_MINUTES_MIN, NUMBER, false)
                    , new Parameter(ETA_MINUTES_MAX, NUMBER, false)
                    , new Parameter(APOLOGY_MSG, STRING, false)
                    , new Parameter(MIN_ORDER_VALUE, NUMBER, false)
                    , new Parameter(INVALID_ORDER_MSG, STRING, false));

    private BigDecimal minOrderValue;
    private String invalidOrderMsg;


    public FixedFeeSelectedAreasMinOrderShippingService(){
        super();
        minOrderValue = ZERO;
        invalidOrderMsg = DEFAULT_INVALID_ORDER_MSG;
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
    public void validateShipment(List<ShippingDetails> shippingDetails) {
        super.validateShipment(shippingDetails);
        validateOrderValue(shippingDetails);
    }



    @Override
    public void setServiceParameters(List<ServiceParameter> params) {
        super.setServiceParameters(params);
        try {
            var serviceParameters = params
                    .stream()
                    .collect(
                            toMap(ServiceParameter::getParameter, ServiceParameter::getValue));
            setMinOrderValue(serviceParameters);
            setInvalidOrderMsg(serviceParameters);
        } catch (Throwable e) {
            logger.error(e,e);
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0002, SERVICE_ID);
        }
    }



    @Override
    public Mono<ShippingOffer> createShippingOffer(List<ShippingDetails> shippingDetails) {
        return super.doCreateShippingOffer(shippingDetails, getServiceInfo())
                .map(offer -> checkOrderValue(offer, shippingDetails));
    }



    private ShippingOffer checkOrderValue(ShippingOffer offer, List<ShippingDetails> shippingDetails){
        if(offer.isAvailable() && isOrderValueTooLow(shippingDetails)){
            var msg = createInvalidOrderMessage();
            return new ShippingOffer(getServiceInfo(), msg);
        }else {
            return offer;
        }
    }



    private String createInvalidOrderMessage() {
        try{
            return format(invalidOrderMsg, minOrderValue.toString());
        }catch(Throwable t){
            logger.error(t,t);
            return invalidOrderMsg;
        }
    }



    private void validateOrderValue(List<ShippingDetails> shippingDetails) {
        if(isOrderValueTooLow(shippingDetails)){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0015, minOrderValue);
        }
    }



    private boolean isOrderValueTooLow(List<ShippingDetails> shippingDetails) {
        return calcItemsTotalValue(shippingDetails).compareTo(minOrderValue) < 0;
    }


    private void setMinOrderValue(Map<String, String> serviceParams) {
        ofNullable(serviceParams.get(MIN_ORDER_VALUE))
                .map(BigDecimal::new)
                .ifPresent(val -> minOrderValue = val.setScale(2, HALF_EVEN));
    }



    private void setInvalidOrderMsg(Map<String, String> serviceParams) {
        ofNullable(serviceParams.get(INVALID_ORDER_MSG))
                .ifPresent(val -> invalidOrderMsg = val);
    }


}
