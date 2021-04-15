package com.nasnav.payments.cod;


import com.nasnav.shipping.services.PickupFromShop;
import com.nasnav.shipping.services.PickupPointsWithInternalLogistics;

public class CodCommons {
    public static final String COD_OPERATOR = "COD";

    public static boolean isCodAvailableForService(String ServiceId) {
        if (PickupFromShop.SERVICE_ID.equalsIgnoreCase(ServiceId)) {
            return false;
        }
        if (PickupPointsWithInternalLogistics.SERVICE_ID.equalsIgnoreCase(ServiceId)) {
            return false;
        }
        return true;
    }
}
