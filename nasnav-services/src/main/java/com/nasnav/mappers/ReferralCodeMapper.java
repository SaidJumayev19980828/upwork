package com.nasnav.mappers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.referral_code.ReferralCodeDto;
import com.nasnav.enumerations.ReferralCodeStatus;
import com.nasnav.enumerations.ReferralCodeType;
import com.nasnav.persistence.ReferralCodeEntity;
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
public interface ReferralCodeMapper {

    ReferralCodeEntity map(ReferralCodeDto referralCodeDto);

    @Mapping(target = "username", expression = "java(referralCodeEntity.getUser().getFirstName() + \" \" + referralCodeEntity.getUser().getLastName())")
    ReferralCodeDto map(ReferralCodeEntity referralCodeEntity);

   List<ReferralCodeDto> map(List<ReferralCodeEntity> referralCodeEntity);

    ReferralCodeEntity map(ReferralCodeDto referralCodeDto, @MappingTarget ReferralCodeEntity referralCodeEntity);


    default PaginatedResponse<ReferralCodeDto> map(Page<ReferralCodeEntity> referralCodeEntitiyPage){
        return PaginatedResponse.<ReferralCodeDto>builder()
                .content(map(referralCodeEntitiyPage.getContent()))
                .totalPages(referralCodeEntitiyPage.getTotalPages())
                .totalRecords(referralCodeEntitiyPage.getTotalElements())
                .build();
    };

    default Integer map(ReferralCodeStatus status) {
        return status.getValue();
    }

    default ReferralCodeStatus map(Integer status) {
        return ReferralCodeStatus.getStatus(status);
    }


}
