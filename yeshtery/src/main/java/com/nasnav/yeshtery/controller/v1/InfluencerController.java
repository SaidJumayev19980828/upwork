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

import static com.nasnav.constatnts.DefaultValueStrings.DEFAULT_PAGING_COUNT;

import java.util.List;

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

}
