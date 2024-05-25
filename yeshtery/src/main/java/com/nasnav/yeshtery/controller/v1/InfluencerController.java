package com.nasnav.yeshtery.controller.v1;

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.*;
import com.nasnav.dto.request.EventOrganiseRequestDTO;
import com.nasnav.dto.response.*;
import com.nasnav.enumerations.EventRequestStatus;
import com.nasnav.service.InfluencerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.nasnav.constatnts.DefaultValueStrings.DEFAULT_PAGING_COUNT;

@RestController
@RequestMapping(InfluencerController.API_PATH)
public class InfluencerController {
        static final String API_PATH = YeshteryConstants.API_PATH +"/influencer";

    @Autowired
    private InfluencerService influencerService;

    @GetMapping("/{influencerId}")
    public InfluencerDTO getInfluencerById(@RequestHeader(name = "User-Token", required = false) String token,
                                           @PathVariable Long influencerId) {
        return influencerService.getInfluencerById(influencerId);
    }

    @GetMapping
    public PageImpl<InfluencerDTO> getAllPageable(@RequestHeader(name = "User-Token", required = false) String token,
                                                  @RequestParam(required = false, defaultValue = "0") Integer start,
                                                 @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count,
                                                  @RequestParam(required = false) Boolean status) {
        return influencerService.getAllInfluencers(start, count, status);
    }

    @GetMapping("/org")
    public List<InfluencerDTO> getAllInfluencersByOrgId(@RequestHeader(name = "User-Token", required = false) String token,
                                                        @RequestParam long orgId) {
        return influencerService.getAllInfluencersByOrg(orgId);
    }

    @PostMapping("/request")
    public void becomeInfluencerRequest(@RequestHeader(name = "User-Token", required = false) String token,
                                        @RequestBody List<Long> categoryIds) {
        influencerService.becomeInfluencerRequest(categoryIds);
    }

    @PostMapping("/response")
    public void becomeInfluencerResponse(@RequestHeader(name = "User-Token", required = false) String token,
                                         @RequestParam Long influencerId, @RequestParam boolean action) {
        influencerService.becomeInfluencerResponse(influencerId, action);
    }

    @PostMapping("/host")
    public void requestEventHosting(@RequestHeader(name = "User-Token", required = false) String token,
                                    @RequestBody EventOrganiseRequestDTO dto){
        influencerService.requestEventHosting(dto);
    }

    @DeleteMapping("/host")
    public void deleteEventHostingByInfluencer(@RequestHeader(name = "User-Token", required = false) String token,
                                               @RequestParam Long requestId) {
        influencerService.cancelEventHostingRequestByInfluencer(requestId);
    }

    @GetMapping("/request/{requestId}")
    public EventRequestsDTO getEventRequestById(@RequestHeader(name = "User-Token", required = false) String token,
                                                @PathVariable Long requestId) {
        return influencerService.getEventRequestById(requestId);
    }

    @PutMapping("host/approveAndRejectTheRest/{requestId}")
    public void approveEventHostingAndRejectTheRest(@RequestHeader(name = "User-Token", required = false) String token,
                                                    @PathVariable Long requestId) {
        influencerService.rejectTheRestIfEventHostingRequestApproved(requestId);
    }

    @PutMapping("/host/{requestId}")
    public void responseEventHosting(@RequestHeader(name = "User-Token", required = false) String token,
                                     @RequestParam boolean approve, @PathVariable Long requestId){
        influencerService.approveOrCancelEventHostingRequest(requestId,approve);
    }

    @GetMapping("/myEvents")
    public PageImpl<EventResponseDto> getMyEvents(@RequestHeader(name = "User-Token", required = false) String token,
                                              @RequestParam(required = false, defaultValue = "0") Integer start,
                                              @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count){
        return influencerService.getMyEvents(start, count);
    }

    @GetMapping("/events")
    public PageImpl<EventResponseDto> getEventsByInfluencerId(@RequestHeader(name = "User-Token", required = false) String token,
                                                              @RequestParam Long influencerId,
                                                              @RequestParam(required = false) Long orgId,
                                                              @RequestParam(required = false, defaultValue = "0") Integer start,
                                                              @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count){
        return influencerService.getEventsByInfluencerId(influencerId,start, count, orgId);
    }

    @GetMapping("/myEventRequests")
    public PageImpl<EventRequestsDTO> getMyEventRequests(@RequestHeader(name = "User-Token", required = false) String token,
                                                         @RequestParam(required = false, defaultValue = "0") Integer start,
                                                         @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count,
                                                         @RequestParam(required = false) EventRequestStatus status){
        return influencerService.getMyEventRequests(start, count, status);
    }

    @GetMapping("/myEventsAndRequests")
    public EventsAndReqsResponse getMyEventsAndRequests(@RequestHeader(name = "User-Token", required = false) String token,
            @RequestParam(required = false, defaultValue = "0") Integer start,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count,
            @RequestParam(required = false) EventRequestStatus status, @RequestParam(required = false, defaultValue = "upcoming") String sortBy) {
        return influencerService.getMyEventsAndRequests(start, count, status, sortBy);
    }

    @GetMapping("/hosting-requests")
    public PageImpl<EventRequestsDTO> getOrgEventsRequests(@RequestHeader(name = "User-Token", required = false) String token,
                                                           @RequestParam(required = false, defaultValue = "0") Integer start,
                                                           @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count,
                                                           @RequestParam(required = false) EventRequestStatus status){
        return influencerService.getEventsRequestByOrgForEmployee(start, count, status);
    }

    @PutMapping("/guided")
    public void influencerIsGuided(@RequestHeader(name = "User-Token", required = false) String token){
        influencerService.userIsGuided();
    }

    @GetMapping("/stats")
    public List<InfluencerStatsDTO> getInfluencerStats(@RequestHeader(name = "User-Token", required = false) String token,
                                                       @RequestParam Long influencerId,
                                                       @RequestParam(required = false) Long orgId,
                                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return influencerService.getInfluencerStats(influencerId, start, end, orgId);
    }

    @GetMapping("host-orgs")
    public List<OrganizationRepresentationObject> getInfluencerOrgs(@RequestHeader(name = "User-Token", required = false) String token,
                                                                    @RequestParam Long influencerId){
        return influencerService.getInfluencerOrgs(influencerId);
    }

}
