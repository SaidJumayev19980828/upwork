package com.nasnav.mappers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.referral_code.ReferralCodeDto;
import com.nasnav.dto.referral_code.ReferralSettingsDto;
import com.nasnav.enumerations.ReferralCodeStatus;
import com.nasnav.enumerations.ReferralCodeType;
import com.nasnav.persistence.ReferralCodeEntity;
import com.nasnav.persistence.ReferralSettings;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface ReferralSettingsMapper {

    ReferralSettings map(ReferralSettingsDto referralSettingsDto);

    ReferralSettingsDto map(ReferralSettings referralSettings);

    default String map(Map<ReferralCodeType, BigDecimal> constraints) throws JsonProcessingException {
            ObjectMapper objectMapper = new ObjectMapper();
            String result = objectMapper.writeValueAsString(constraints);
            return result;
    }

    default Map<ReferralCodeType, BigDecimal> map(String referralCodeConstrains) throws IOException {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<ReferralCodeType, BigDecimal> result = objectMapper.readValue(referralCodeConstrains, new TypeReference<Map<ReferralCodeType, BigDecimal>>() {});
            return result;
    }

}
