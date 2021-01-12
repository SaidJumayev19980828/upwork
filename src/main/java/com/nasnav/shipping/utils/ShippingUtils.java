package com.nasnav.shipping.utils;

import com.nasnav.shipping.model.ShippingAddress;
import com.nasnav.shipping.model.ShippingDetails;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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
}
