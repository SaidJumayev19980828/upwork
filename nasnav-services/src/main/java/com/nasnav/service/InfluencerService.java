package com.nasnav.service;

import com.nasnav.dto.EventRequestsDTO;
import com.nasnav.dto.InfluencerDTO;
import com.nasnav.dto.request.EventOrganiseRequestDTO;
import com.nasnav.dto.response.EventResponseDto;
import com.nasnav.enumerations.EventRequestStatus;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public interface InfluencerService {

    InfluencerDTO getInfluencerById(Long id);
    PageImpl<InfluencerDTO> getAllInfluencers(Integer start, Integer count, Boolean status);
    List<InfluencerDTO> getAllInfluencersByOrg(Long orgId);
    void becomeInfluencerRequest(List<Long> categoryIds);

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
    PageImpl<EventResponseDto> getEventsByInfluencerId(Long influencerId, Integer start, Integer count, Long orgId);
    PageImpl<EventRequestsDTO> getMyEventRequests(Integer start, Integer count, EventRequestStatus status);
    PageImpl<EventRequestsDTO> getEventsRequestByOrgForEmployee(Integer start, Integer count, EventRequestStatus status);
    void joinEvent();
    void userIsGuided();
}
