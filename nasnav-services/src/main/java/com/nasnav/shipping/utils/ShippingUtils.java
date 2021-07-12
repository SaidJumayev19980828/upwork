package com.nasnav.shipping.utils;

import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.commons.utils.FunctionalUtils;
import com.nasnav.shipping.model.*;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.nasnav.shipping.model.Constants.DEFAULT_AWB_FILE_NAME_PATTERN;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static java.time.LocalDateTime.now;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;

public class ShippingUtils {
	
	public static Optional<Long> getSourceCity(ShippingDetails details){
	    return ofNullable(details)
                .map(ShippingDetails::getSource)
                .map(ShippingAddress::getCity);
    }



    public static Optional<Long> getDestinationCity(ShippingDetails details){
        return ofNullable(details)
                .map(ShippingDetails::getDestination)
                .map(ShippingAddress::getCity);
    }


    public static Set<Long> getSourceAndDestinationCities(ShippingDetails details){
	    return Stream.of(getSourceCity(details), getDestinationCity(details))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toSet());
    }



    public static Set<Long> getSourceAndDestinationCities(List<ShippingDetails> details){
	    return details
                .stream()
                .map(ShippingUtils::getSourceAndDestinationCities)
                .flatMap(Set::stream)
                .collect(toSet());
    }



    public static BigDecimal calcItemsTotalValue(List<ShippingDetails> items) {
        return items
                .stream()
                .map(ShippingDetails::getItems)
                .flatMap(List::stream)
                .map(ShippingUtils::getItemValue)
                .reduce(ZERO, BigDecimal::add)
                .setScale(2, HALF_EVEN);
    }



    public static BigDecimal getItemValue(ShipmentItems shipmentItems) {
        BigDecimal price = shipmentItems.getPrice();
        return price.multiply(BigDecimal.valueOf(shipmentItems.getQuantity()));
    }



    public static Optional<Integer> getIntegerParameter(Map<String, String> serviceParams, String etaDaysMin) {
        return ofNullable(serviceParams.get(etaDaysMin))
                .flatMap(EntityUtils::parseLongSafely)
                .map(Long::intValue);
    }


    public static void correctCalculationError(BigDecimal fee, List<Shipment> shipments) {
        BigDecimal accumulatedFeeTotal =
                shipments
                        .stream()
                        .map(Shipment::getShippingFee)
                        .reduce(ZERO, BigDecimal::add);
        BigDecimal error = fee.subtract(accumulatedFeeTotal);
        shipments
                .stream()
                .peek( shipment -> shipment.setShippingFee(shipment.getShippingFee().add(error)))
                .findFirst();
    }


    public static Map<String, String> toServiceParamMap(List<ServiceParameter> params) {
        return params
                .stream()
                .collect(
                        toMap(ServiceParameter::getParameter, ServiceParameter::getValue, FunctionalUtils::getFirst));
    }


    public static List<Long> getItemsStockId(ShippingDetails shippingInfo) {
        return shippingInfo
                .getItems()
                .stream()
                .map(ShipmentItems::getStockId)
                .collect(toList());
    }


    public static String createAwbFileName(ShippingDetails shipment, String trackNumber, String fileNamePattern){
        var dateFormatted = DateTimeFormatter.ofPattern("yyyyMMdd").format(now());
        var metaOrderId =
                ofNullable(shipment)
                .map(ShippingDetails::getMetaOrderId)
                .map(EntityUtils::parseLongSafely)
                .map(Object::toString)
                .orElse("");
        var subOrderId =
                ofNullable(shipment)
                    .map(ShippingDetails::getSubOrderId)
                    .map(EntityUtils::parseLongSafely)
                    .map(Object::toString)
                    .orElse("");
        var num = ofNullable(trackNumber).orElse("");
        return format(fileNamePattern, dateFormatted, num, metaOrderId, subOrderId);
    }


    public static String createAwbFileName(ShippingDetails shipment, String trackNumber) {
        return createAwbFileName(shipment, trackNumber, DEFAULT_AWB_FILE_NAME_PATTERN);
    }
}
