package com.nasnav.enumerations;


import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Converter(autoApply = true)
public class DiscountStrategiesConverter implements AttributeConverter<Set<DiscountStrategies>, String> {

    @Override
    public String convertToDatabaseColumn(Set<DiscountStrategies> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        return attribute.stream().map(DiscountStrategies::name).collect(Collectors.joining(","));
    }

    @Override
    public Set<DiscountStrategies> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return Collections.emptySet();
        }
        return Arrays.stream(dbData.split(","))
                .map(String::trim)
                .map(DiscountStrategies::valueOf)
                .collect(Collectors.toSet());
    }
}

