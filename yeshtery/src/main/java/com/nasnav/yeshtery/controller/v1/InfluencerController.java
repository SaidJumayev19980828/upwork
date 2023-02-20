package com.nasnav.yeshtery.controller.v1;

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.EventRequestsDTO;
import com.nasnav.dto.InfluencerDTO;
import com.nasnav.dto.request.EventOrganiseRequestDTO;
import com.nasnav.dto.response.EventResponseDto;
import com.nasnav.enumerations.EventRequestStatus;
import com.nasnav.service.InfluencerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(InfluencerController.API_PATH)
public class InfluencerController {
    static final String API_PATH = YeshteryConstants.API_PATH +"/influencer";

    @Autowired
    private InfluencerService influencerService;

    @GetMapping("/{influencerId}")
    public InfluencerDTO getInfluencerById(@PathVariable Long influencerId) {
        return influencerService.getInfluencerById(influencerId);
    }

    @GetMapping
    public PageImpl<InfluencerDTO> getAllPageable(@RequestParam(required = false, defaultValue = "0") Integer start,
                                                 @RequestParam(required = false, defaultValue = "10") Integer count,
                                                  @RequestParam(required = false) Boolean status) {
        return influencerService.getAllInfluencers(start, count, status);
    }

    @PostMapping("/request")
    public void becomeInfluencerRequest(@RequestBody List<Long> categoryIds) {
        influencerService.becomeInfluencerRequest(categoryIds);
    }

    @PostMapping("/response")
    public void becomeInfluencerResponse(@RequestParam Long influencerId, @RequestParam boolean action) {
        influencerService.becomeInfluencerResponse(influencerId, action);
    }

    @PostMapping("/host")
    public void requestEventHosting(@RequestBody EventOrganiseRequestDTO dto){
        influencerService.requestEventHosting(dto);
    }

    @GetMapping("/request/{requestId}")
    public EventRequestsDTO getEventRequestById(@PathVariable Long requestId) {
        return influencerService.getEventRequestById(requestId);
    }

    @PutMapping("host/approveAndRejectTheRest/{requestId}")
    public void approveEventHostingAndRejectTheRest(@PathVariable Long requestId) {
        influencerService.rejectTheRestIfEventHostingRequestApproved(requestId);
    }

    @PutMapping("/host/{requestId}")
    public void responseEventHosting(@RequestParam boolean approve, @PathVariable Long requestId){
        influencerService.approveOrCancelEventHostingRequest(requestId,approve);
    }

    @GetMapping("/myEvents")
    public List<EventResponseDto> getMyEvents(){
        return influencerService.getMyEvents();
    }

    @GetMapping("/myEventRequests")
    public PageImpl<EventRequestsDTO> getMyEventRequests(@RequestParam(required = false, defaultValue = "0") Integer start,
                                                         @RequestParam(required = false, defaultValue = "10") Integer count,
                                                         @RequestParam(required = false) EventRequestStatus status){
        return influencerService.getMyEventRequests(start, count, status);
    }

    @GetMapping("/hostingRequests")
    public PageImpl<EventRequestsDTO> getOrgEventsRequests(@RequestParam(required = false, defaultValue = "0") Integer start,
                                                           @RequestParam(required = false, defaultValue = "10") Integer count,
                                                           @RequestParam(required = false) EventRequestStatus status){
        return influencerService.getEventsRequestByOrgForEmployee(start, count, status);
    }

}
