package com.nasnav.controller;

import com.nasnav.request.VideoChatSearchParam;
import com.nasnav.response.VideoChatListResponse;
import com.nasnav.response.VideoChatResponse;
import com.nasnav.service.VideoChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/videochat")
@CrossOrigin("*")
public class VideoChatController {

    @Autowired
    private VideoChatService videoChatService;

    @GetMapping(value = "/sessions", produces = APPLICATION_JSON_VALUE)
    public VideoChatListResponse getOrgSessions(@RequestHeader(name = "User-Token") String userToken, VideoChatSearchParam params) {
      return videoChatService.getOrgSessions(params);
    }

    @PostMapping(value = "/session", produces = APPLICATION_JSON_VALUE)
    public VideoChatResponse createOrJoinSession(@RequestHeader(name = "User-Token") String userToken,
                                         @RequestParam(name = "session_name", required = false) String sessionName,
                                         @RequestParam(name = "force", required = false, defaultValue = "false") Boolean force,
                                         @RequestParam(name = "org_id", required = false) Long orgId,
                                         @RequestParam(name = "shop_id", required = false) Long shopId){
        return videoChatService.createOrJoinSession(sessionName, force, orgId, shopId);
    }

    @PostMapping(value = "/leave")
    public void leaveSession(@RequestHeader(name = "User-Token") String userToken,
                             @RequestParam("session_name") String sessionName,
                             @RequestParam(name = "org_id", required = false) Long orgId,
                             @RequestParam(name = "shop_id", required = false) Long shopId,
                             @RequestParam(name = "end_call") Boolean endCall) {
        videoChatService.leaveSession(sessionName, orgId, shopId, endCall);
    }

    @PostMapping(value = "/group/session/create", produces = APPLICATION_JSON_VALUE)
    public VideoChatResponse createGroupSession(@RequestHeader(name = "User-Token") String userToken,
                                                @RequestParam(value = "session_name",defaultValue = "") String sessionName,
                                                @RequestParam(name = "org_id", required = false) Long orgId,
                                                @RequestParam(name = "shop_id", required = false) Long shopId){
        return videoChatService.createGroupVideoChat(sessionName,orgId, shopId);
    }

    @GetMapping(value = "/group/session/get", produces = APPLICATION_JSON_VALUE)
    public VideoChatResponse joinGroupSession(@RequestHeader(name = "User-Token") String userToken,
                                              @RequestParam(name = "session_name", required = false) String sessionName,
                                              @RequestParam(name = "org_id", required = false) Long orgId){
        return videoChatService.getGroupVideoChat(sessionName, orgId);
    }


    @GetMapping(value = "/credentials")
    public Map<String, String> getVideoChatCredentials() {
        return  videoChatService.getVideoChatCredentials();
    }
}
