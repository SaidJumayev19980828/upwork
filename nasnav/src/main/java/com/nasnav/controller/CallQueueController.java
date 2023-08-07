package com.nasnav.controller;

import com.nasnav.dto.response.CallQueueDTO;
import com.nasnav.dto.response.CallQueueStatusDTO;
import com.nasnav.enumerations.CallQueueStatus;
import com.nasnav.response.VideoChatResponse;
import com.nasnav.service.CallQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.nasnav.constatnts.DefaultValueStrings.DEFAULT_PAGING_COUNT;
import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/queue", produces = APPLICATION_JSON_VALUE)
@CrossOrigin("*")
public class CallQueueController {
    @Autowired
    private CallQueueService callQueueService;

    @PostMapping
    public CallQueueStatusDTO enterQueue(@RequestHeader(TOKEN_HEADER) String userToken,
                                         @RequestParam Long orgId) {
        return callQueueService.enterQueue(orgId);
    }

    @GetMapping("/status")
    public CallQueueStatusDTO getStatus(@RequestHeader(TOKEN_HEADER) String userToken) {
        return callQueueService.getQueueStatusForUser();
    }

    @GetMapping
    public List<CallQueueDTO> getQueue(@RequestHeader(TOKEN_HEADER) String userToken) {
        return callQueueService.getQueueForEmployee();
    }

    @GetMapping("/logs")
    public PageImpl<CallQueueDTO> getLogs(@RequestHeader(TOKEN_HEADER) String userToken,
                                          @RequestParam(required = false, defaultValue = "0") Integer start,
                                          @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count,
                                          @RequestParam(required = false) CallQueueStatus status) {
        return callQueueService.getCallLogs(start, count, status);
    }

    @PutMapping("/reject")
    public List<CallQueueDTO> rejectCallByEmployee(@RequestHeader(TOKEN_HEADER) String userToken,
                                                   @RequestParam Long queueId)  {
        return callQueueService.rejectCall(queueId);
    }

    @PutMapping("/cancel")
    public void cancelCallByUser(@RequestHeader(TOKEN_HEADER) String userToken) {
        callQueueService.quitQueue();
    }

    @PutMapping("/accept")
    public VideoChatResponse acceptCallByEmployee(@RequestHeader(TOKEN_HEADER) String userToken,
                                                  @RequestParam Long queueId,
                                                  @RequestParam(required = false, defaultValue = "true") Boolean force) {
        return callQueueService.acceptCall(queueId, force);
    }


}
