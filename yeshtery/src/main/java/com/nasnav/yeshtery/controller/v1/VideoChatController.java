package com.nasnav.yeshtery.controller.v1;

import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.response.VideoChatResponse;
import com.nasnav.service.VideoChatService;
import com.nasnav.yeshtery.YeshteryConstants;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(VideoChatController.API_PATH)
@CrossOrigin("*")
public class VideoChatController {

    static final String API_PATH = YeshteryConstants.API_PATH +"/videochat/";

    @Autowired
    private VideoChatService videoChatService;

    @GetMapping(value = "/getSession")
    public VideoChatResponse getSession(@RequestHeader(name = "User-Token", required = false) String userToken,
                                        @RequestParam(required = false) String sessionName)
            throws RuntimeBusinessException, OpenViduJavaClientException, OpenViduHttpException {
        return  videoChatService.getSession(userToken, sessionName);
    }

    @RequestMapping(value = "/remove-user", method = RequestMethod.GET)
    public ResponseEntity<JSONObject> removeUser(@RequestHeader(name = "User-Token", required = false) String userToken,
                                                 @RequestParam String sessionName)  {
        return videoChatService.removeUser(userToken, sessionName);
    }
}