package com.nasnav.yeshtery.controller.v1;

import com.nasnav.response.VideoChatResponse;
import com.nasnav.service.VideoChatService;
import com.nasnav.yeshtery.YeshteryConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(VideoChatController.API_PATH)
@CrossOrigin("*")
public class VideoChatController {

    static final String API_PATH = YeshteryConstants.API_PATH +"/videochat/";

    @Autowired
    private VideoChatService videoChatService;

    @GetMapping(value = "/get-session")
    public VideoChatResponse getSession(@RequestHeader(name = "User-Token", required = false) String userToken,
                                        @RequestParam(required = false) String sessionName,
                                        @RequestParam(name = "org_id") Long orgId) {
        return videoChatService.getSession(userToken, sessionName, orgId);
    }

    @GetMapping(value = "/leave")
    public void leaveSession(@RequestHeader(name = "User-Token") String userToken,
                             @RequestParam String sessionName,
                             @RequestParam Long orgId) {
        videoChatService.leaveSession(sessionName, orgId);
    }
}