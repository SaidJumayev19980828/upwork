package com.nasnav.yeshtery.controller.v1;

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.request.VideoChatSearchParam;
import com.nasnav.response.VideoChatListResponse;
import com.nasnav.response.VideoChatResponse;
import com.nasnav.service.VideoChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequestMapping(VideoChatController.API_PATH)
@CrossOrigin("*")
public class VideoChatController {

    static final String API_PATH = YeshteryConstants.API_PATH +"/videochat/";

    @Autowired
    private VideoChatService videoChatService;

    @GetMapping(value = "/sessions", produces = APPLICATION_JSON_VALUE)
    public VideoChatListResponse getOrgSessions(@RequestHeader(name = "User-Token", required = false) String userToken, VideoChatSearchParam param) {
        return videoChatService.getOrgSessions(param);
    }

    @PostMapping(value = "/session")
    public VideoChatResponse createOrJoinSession(@RequestHeader(name = "User-Token", required = false) String userToken,
                                                 @RequestParam(name = "session_name", required = false) String sessionName,
                                                 @RequestParam(name = "force", required = false, defaultValue = "false") Boolean force,
                                                 @RequestParam(name = "org_id", required = false) Long orgId,
                                                 @RequestParam(name = "shop_id", required = false) Long shopId) {
        return videoChatService.createOrJoinSession(sessionName, force, orgId, shopId);
    }

    @PostMapping(value = "/leave")
    public void leaveSession(@RequestHeader(name = "User-Token", required = false) String userToken,
                             @RequestParam("session_name") String sessionName,
                             @RequestParam(name = "org_id", required = false) Long orgId,
                             @RequestParam(name = "shop_id", required = false) Long shopId,
                             @RequestParam(name = "end_call") Boolean endCall) {
        videoChatService.leaveSession(sessionName, orgId, shopId, endCall);
    }

    @PostMapping(value = "/group/session/create", produces = APPLICATION_JSON_VALUE)
    public VideoChatResponse createGroupSession(@RequestHeader(name = "User-Token", required = false) String userToken,
                                                @RequestParam(value = "session_name",defaultValue = "") String sessionName,
                                                @RequestParam(name = "org_id", required = false) Long orgId,
                                                @RequestParam(name = "shop_id", required = false) Long shopId){
        return videoChatService.createGroupVideoChat(sessionName,orgId, shopId);
    }

    @GetMapping(value = "/group/session/get", produces = APPLICATION_JSON_VALUE)
    public VideoChatResponse joinGroupSession(@RequestHeader(name = "User-Token", required = false) String userToken,
                                              @RequestParam(name = "session_name", required = false) String sessionName,
                                              @RequestParam(name = "org_id", required = false) Long orgId){
        return videoChatService.getGroupVideoChat(sessionName, orgId);
    }
}