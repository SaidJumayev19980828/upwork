package com.nasnav.yeshtery.controller.v1;


import com.nasnav.dto.response.CallQueueDTO;
import com.nasnav.dto.response.CallQueueStatusDTO;
import com.nasnav.enumerations.CallQueueStatus;
import com.nasnav.response.VideoChatResponse;
import com.nasnav.service.CallQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

import static com.nasnav.constatnts.DefaultValueStrings.DEFAULT_PAGING_COUNT;
import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/v1/queue", produces = APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@RequiredArgsConstructor
    public class CallQueueController {
    private final CallQueueService callQueueService;

        @PostMapping
        public CallQueueStatusDTO enterQueue(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
                                             @RequestParam Long orgId ,
                                             @RequestParam(required = false) Long shopId
        ) throws MessagingException, IOException {
            return callQueueService.enterQueue(orgId,shopId);
        }

        @GetMapping("/status")
        public CallQueueStatusDTO getStatus(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken) {
            return callQueueService.getQueueStatusForUser();
        }

        @GetMapping
        public List<CallQueueDTO> getQueue(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken) {
            return callQueueService.getQueueForEmployee();
        }

        @GetMapping("/logs")
        public PageImpl<CallQueueDTO> getLogs(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
                                              @RequestParam(required = false, defaultValue = "0") Integer start,
                                              @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count,
                                              @RequestParam(required = false) CallQueueStatus status) {
            return callQueueService.getCallLogs(start, count, status);
        }

        @PutMapping("/reject")
        public List<CallQueueDTO> rejectCallByEmployee(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
                                                       @RequestParam Long queueId,
                                                       @RequestParam String rejectionReason
        )  {
            return callQueueService.rejectCall(queueId,rejectionReason);
        }

        @PutMapping("/cancel")
        public void cancelCallByUser(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken) {
            callQueueService.quitQueue();
        }

        @PutMapping("/accept")
        public VideoChatResponse acceptCallByEmployee(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
                                                      @RequestParam Long queueId,
                                                      @RequestParam(required = false, defaultValue = "true") Boolean force) {
            return callQueueService.acceptCall(queueId, force);
        }
}
