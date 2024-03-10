package com.nasnav.mappers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.nasnav.dto.referral_code.ReferralConstraints;
import com.nasnav.dto.referral_code.ReferralSettingsDto;
import com.nasnav.enumerations.ReferralCodeType;
import com.nasnav.persistence.ReferralSettings;
import org.mapstruct.Mapper;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface ReferralSettingsMapper {

    ReferralSettings map(ReferralSettingsDto referralSettingsDto);

    ReferralSettingsDto map(ReferralSettings referralSettings);

    default String map(Map<ReferralCodeType, ReferralConstraints> constraints) throws JsonProcessingException {
            return getObjectMapper().writeValueAsString(constraints);
    }

    default Map<ReferralCodeType, ReferralConstraints> map(String referralCodeConstrains) throws IOException {
        Map<ReferralCodeType, ReferralConstraints> result = getObjectMapper().readValue(referralCodeConstrains, new TypeReference<Map<ReferralCodeType, ReferralConstraints>>() {});
            return result;
    }

    static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ISO_LOCAL_DATE));
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
