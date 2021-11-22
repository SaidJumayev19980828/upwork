package com.nasnav.service;

import com.nasnav.dao.LoyaltyEventRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dto.request.LoyaltyEventDTO;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.LoyaltyEventEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.response.LoyaltyEventUpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LoyaltyEventServiceImpl implements LoyaltyEventService {
    @Autowired
    LoyaltyEventRepository loyaltyEventRepository;
    @Autowired
    OrganizationRepository organizationRepository;

    @Override
    public LoyaltyEventUpdateResponse createUpdateEvent(LoyaltyEventDTO loyaltyEventDTO) {
        LoyaltyEventEntity entity;
        Long id = loyaltyEventDTO.getId();
        if( loyaltyEventDTO.getId()  != null && loyaltyEventDTO.getId() > 0) {
            entity = getLoyaltyEventEntity(id);
        } else {
            entity = new LoyaltyEventEntity();
        }
        Optional<OrganizationEntity> org = organizationRepository.findById(loyaltyEventDTO.getOrganizationId());

        if(org.isEmpty()) {
            throw new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.G$ORG$0001, loyaltyEventDTO.getOrganizationId());
        }
        entity.setName(loyaltyEventDTO.getName());
        entity.setIsActive(loyaltyEventDTO.getIsActive());
        entity.setStartDate(loyaltyEventDTO.getStartDate());
        entity.setEndDate(loyaltyEventDTO.getEndDate());
        entity.setOrganization(org.get());
        loyaltyEventRepository.save(entity);
        return new LoyaltyEventUpdateResponse(entity.getId());
    }

    private LoyaltyEventEntity getLoyaltyEventEntity(Long id) {
        LoyaltyEventEntity entity;
        Optional<LoyaltyEventEntity> entityExists = loyaltyEventRepository.findById(id);
        if(entityExists.isEmpty()) {
            throw new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.ORG$LOY$0016, id);
        }
        entity = entityExists.get();
        return entity;
    }

    @Override
    public void deleteById(Long id) {
        getLoyaltyEventEntity(id);
        loyaltyEventRepository.deleteById(id);
    }

    @Override
    public List<LoyaltyEventDTO> getAllEvents(Long orgId) {
        List<LoyaltyEventEntity> events = loyaltyEventRepository.findByOrganization_Id(orgId);
        return events.stream().map(LoyaltyEventEntity::getRepresentation).collect(Collectors.toList());
    }
}
