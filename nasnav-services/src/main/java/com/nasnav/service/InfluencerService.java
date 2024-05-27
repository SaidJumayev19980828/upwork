package com.nasnav.service;

import com.nasnav.dto.EventRequestsDTO;
import com.nasnav.dto.InfluencerDTO;
import com.nasnav.dto.InfluencerStatsDTO;
import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.dto.request.EventOrganiseRequestDTO;
import com.nasnav.dto.response.*;
import com.nasnav.enumerations.EventRequestStatus;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.InfluencerEntity;
import com.nasnav.persistence.UserEntity;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.util.List;

public interface InfluencerService {

    InfluencerDTO getInfluencerById(Long id);
    PageImpl<InfluencerDTO> getAllInfluencers(Integer start, Integer count, Boolean status);
    List<InfluencerDTO> getAllInfluencersByOrg(Long orgId);
    void becomeInfluencerRequest(List<Long> categoryIds);
    Long becomeApprovedInfluencer(List<Long> categoryIds, UserEntity user);

    /**
     * init with false
     * if action true means approved
     * if false means delete it
     * @param influencerId
     * @param action
     */
    void becomeInfluencerResponse(Long influencerId, boolean action);
    void requestEventHosting(EventOrganiseRequestDTO dto);
    void cancelEventHostingRequestByInfluencer(Long requestId);
    EventRequestsDTO getEventRequestById(Long requestId);
    void approveOrCancelEventHostingRequest(Long requestId, boolean action);
    void rejectTheRestIfEventHostingRequestApproved(Long requestId);
    void deleteEventHostingRequest(Long requestId);
    PageImpl<EventResponseDto> getMyEvents(Integer start, Integer count);
    PageImpl<EventResponseDto> getMyPendingEvent(Integer start, Integer count, String sortBy);
    PageImpl<EventResponseDto> getEventsByInfluencerId(Long influencerId, Integer start, Integer count, Long orgId);
    PageImpl<EventRequestsDTO> getMyEventRequests(Integer start, Integer count, EventRequestStatus status);
    EventsAndReqsResponse getMyEventsAndRequests(Integer start, Integer count, EventRequestStatus status, String sortBy);
    PageImpl<EventRequestsDTO> getEventsRequestByOrgForEmployee(Integer start, Integer count, EventRequestStatus status);
    void joinEvent();
    void userIsGuided();
    List<InfluencerStatsDTO> getInfluencerStats(long influecerId, LocalDate start, LocalDate end, Long orgId);
    List<OrganizationRepresentationObject> getInfluencerOrgs(Long influencerId);

    InfluencerDTO toInfluencerDto(InfluencerEntity entity);

}
