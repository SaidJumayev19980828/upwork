package com.nasnav.controller;

import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.response.VideoChatResponse;
import com.nasnav.service.EmployeeUserService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.VideoChatService;
import io.openvidu.java.client.*;
import net.bytebuddy.utility.RandomString;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.json.simple.parser.JSONParser;

@RestController
@RequestMapping("/videochat")
@CrossOrigin("*")
public class VideoChatController {

    @Autowired
    private VideoChatService videoChatService;

    @RequestMapping(value = "/getAllSessions", method = RequestMethod.GET)
    public List<String> getAllSessions(@RequestHeader(name = "User-Token", required = false) String userToken) {
      return videoChatService.getAllSessions();
    }

    @GetMapping(value = "/getSession")
    public VideoChatResponse getSession(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestParam(required = false) String sessionName) throws RuntimeBusinessException, OpenViduJavaClientException, OpenViduHttpException {
        return  videoChatService.getSession(userToken, sessionName);
    }

    @RequestMapping(value = "/remove-user", method = RequestMethod.GET)
    public ResponseEntity<JSONObject> removeUser(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestParam String sessionName)
            throws Exception {
        return videoChatService.removeUser(userToken, sessionName);
    }
}