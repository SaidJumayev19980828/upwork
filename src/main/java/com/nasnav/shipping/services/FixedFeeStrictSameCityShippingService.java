package com.nasnav.shipping.services;

import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.shipping.model.ShippingDetails;
import com.nasnav.shipping.model.ShippingOffer;
import com.nasnav.shipping.model.ShippingServiceInfo;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0014;
import static com.nasnav.shipping.model.ShippingServiceType.DELIVERY;
import static com.nasnav.shipping.utils.ShippingUtils.getSourceAndDestinationCities;
import static java.util.Collections.emptyList;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

public class FixedFeeStrictSameCityShippingService extends FixedFeeShippingService{
    static final public String SERVICE_ID = "FIXED_FEE_STRICT_SAME_CITY";


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
        validateBothAddressesInSameCity(shippingDetails);
    }




    @Override
    public Mono<ShippingOffer> createShippingOffer(List<ShippingDetails> shippingDetails) {
        if(isNotAddressesInSameCity(shippingDetails)){
            return Mono.empty();
        }
        return doCreateShippingOffer(shippingDetails, getServiceInfo());
    }





    private void validateBothAddressesInSameCity(List<ShippingDetails> shippingDetails) {
        if(isNotAddressesInSameCity(shippingDetails)){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0014);
        }
    }



    private boolean isNotAddressesInSameCity(List<ShippingDetails> shippingDetails) {
        return getSourceAndDestinationCities(shippingDetails).size() != 1;
    }


}
