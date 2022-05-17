package com.nasnav.yeshtery.controller.v1;

import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.response.VideoChatResponse;
import com.nasnav.service.VideoChatService;
import com.nasnav.yeshtery.YeshteryConstants;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(VideoChatController.API_PATH)
@CrossOrigin("*")
public class VideoChatController {

    static final String API_PATH = YeshteryConstants.API_PATH +"/videochat/";

    @Autowired
    private VideoChatService videoChatService;

    @GetMapping(value = "/get-session")
    public VideoChatResponse getSession(@RequestHeader(name = "User-Token", required = false) String userToken
                                        , @RequestParam(required = false) String sessionName
                                        ,@RequestParam(name = "org_id") Long orgId)
            throws RuntimeBusinessException, OpenViduJavaClientException, OpenViduHttpException {
        return  videoChatService.getSession(userToken, sessionName, orgId);
    }

    @RequestMapping(value = "/leave", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveSession(@RequestHeader(name = "User-Token" , required = true) String userToken, @RequestParam String sessionName, @RequestParam Long orgId) {
        videoChatService.leaveSession(sessionName, orgId);
    }
}