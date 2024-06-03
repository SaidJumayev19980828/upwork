package com.nasnav.mappers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.nasnav.dto.referral_code.InfluencerReferralConstraints;
import com.nasnav.dto.referral_code.InfluencerReferralDto;
import com.nasnav.persistence.InfluencerReferral;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface InfluencerReferralMapper {

    @Mapping(target = "password", source = "password", qualifiedByName = "hashPassword")
    @Mapping(target = "referralSettings.constraints", source = "constraints")
    InfluencerReferral map(InfluencerReferralDto influencerReferralDto);

    @Named("hashPassword")
    default String hashPassword(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }

    @InheritInverseConfiguration
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "cashback", source = "referralWallet.balance")
    InfluencerReferralDto map(InfluencerReferral influencerReferral);

    List<InfluencerReferralDto> map(List<InfluencerReferral> influencerReferralList);


    default String map(InfluencerReferralConstraints constraints) throws JsonProcessingException {
        return getObjectMapper().writeValueAsString(constraints);
    }

    default InfluencerReferralConstraints map(String referralCodeConstrains) throws IOException {
        return getObjectMapper().readValue(referralCodeConstrains, new TypeReference<InfluencerReferralConstraints>() {});
    }

    static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ISO_LOCAL_DATE));
        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }

}
