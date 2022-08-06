package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.VideoChatLogRepresentationObject;
import com.nasnav.response.VideoChatResponse;
import com.nasnav.service.VideoChatService;
import com.nasnav.commons.YeshteryConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequestMapping(VideoChatController.API_PATH)
@CrossOrigin("*")
public class VideoChatController {

    static final String API_PATH = YeshteryConstants.API_PATH +"/videochat/";

    @Autowired
    private VideoChatService videoChatService;

    @GetMapping(value = "/sessions", produces = APPLICATION_JSON_VALUE)
    public List<VideoChatLogRepresentationObject> getOrgSessions(@RequestHeader(name = "User-Token") String userToken,
                                                                 @RequestParam(name = "org_id") Long orgId) {
        return videoChatService.getOrgSessions(orgId);
    }

    @PostMapping(value = "/session")
    public VideoChatResponse createOrJoinSession(@RequestHeader(name = "User-Token", required = false) String userToken,
                                        @RequestParam(name = "session_name", required = false) String sessionName,
                                        @RequestParam(name = "org_id") Long orgId) {
        return videoChatService.createOrJoinSession(sessionName, orgId);
    }

    @PostMapping(value = "/leave")
    public void leaveSession(@RequestHeader(name = "User-Token") String userToken,
                             @RequestParam("session_name") String sessionName,
                             @RequestParam(name = "org_id") Long orgId,
                             @RequestParam(name = "end_call") Boolean endCall) {
        videoChatService.leaveSession(sessionName, orgId, endCall);
    }
}