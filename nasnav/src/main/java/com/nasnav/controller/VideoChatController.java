package com.nasnav.controller;

import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.response.VideoChatResponse;
import com.nasnav.service.EmployeeUserService;
import com.nasnav.service.SecurityService;
import io.openvidu.java.client.*;
import net.bytebuddy.utility.RandomString;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/videochat")
@CrossOrigin("*")
public class VideoChatController {

    private OpenVidu openVidu;

    private final Map<String, Session> mapSessions = new ConcurrentHashMap<>();

    private final Map<String, Map<String, OpenViduRole>> mapSessionNamesTokens = new ConcurrentHashMap<>();

    private final String OPENVIDU_URL = "http://34.125.116.133:5443/";
    private final String SECRET = "MY_SECRET";


    @Autowired
    private SecurityService securityService;

    @Autowired
    private EmployeeUserService employeeUserService;

    @Autowired
    public void initController() {
        this.openVidu = new OpenVidu(OPENVIDU_URL, SECRET);
    }

    @RequestMapping(value = "/getSession", method = RequestMethod.POST)
    public VideoChatResponse getToken(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestParam(required = false) String sessionName) throws RuntimeBusinessException, OpenViduJavaClientException, OpenViduHttpException {

        BaseUserEntity loggedInUser = securityService.getCurrentUser();

        Long orgId = securityService.getCurrentUserOrganizationId();
        String memberName = loggedInUser.getName();

        String serverData = "{\"serverData\": \"" + memberName + "\"}";
        ConnectionProperties connectionProperties = new ConnectionProperties.Builder().type(ConnectionType.WEBRTC).data(serverData).role(OpenViduRole.PUBLISHER).build();

        JSONObject responseJson = new JSONObject();

        if (sessionName != null && this.mapSessions.get(sessionName) != null) {
            // Session already exists
            System.out.println("Existing session " + sessionName);
            try {

                String token = this.mapSessions.get(sessionName).createConnection(connectionProperties).getToken();

                this.mapSessionNamesTokens.get(sessionName).put(token, OpenViduRole.PUBLISHER);

                return new VideoChatResponse(true, null, token, null, sessionName);
            } catch (Exception e1) {
                throw e1;
            }
        }

        sessionName = RandomString.make(20);

        // New session
        System.out.println("New session " + sessionName);
        try {
            List<UserRepresentationObject> employees = employeeUserService.getAvailableEmployeesByOrgId(orgId);

            if (!(loggedInUser instanceof EmployeeUserEntity) && employees.isEmpty()) {
                // return json messag says we'll call you back
                return new VideoChatResponse(false, "All employees are busy right now we'll call you back later", null, null, null);
            }

            UserRepresentationObject employee = employees.get(0);
            Session session = this.openVidu.createSession();
            String token = session.createConnection(connectionProperties).getToken();

            this.mapSessions.put(sessionName, session);
            this.mapSessionNamesTokens.put(sessionName, new ConcurrentHashMap<>());
            this.mapSessionNamesTokens.get(sessionName).put(token, OpenViduRole.PUBLISHER);
            return new VideoChatResponse(true, null, token, employee.getName(), sessionName);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping(value = "/remove-user", method = RequestMethod.POST)
    public ResponseEntity<JSONObject> removeUser(@RequestBody String sessionNameToken, HttpSession httpSession)
            throws Exception {

        JSONObject sessionNameTokenJSON = (JSONObject) new JSONParser().parse(sessionNameToken);
        String sessionName = (String) sessionNameTokenJSON.get("sessionName");
        String token = (String) sessionNameTokenJSON.get("token");

        if (this.mapSessions.get(sessionName) != null && this.mapSessionNamesTokens.get(sessionName) != null) {

            // If the token exists
            if (this.mapSessionNamesTokens.get(sessionName).remove(token) != null) {
                // User left the session
                if (this.mapSessionNamesTokens.get(sessionName).isEmpty()) {
                    // Last user left: session must be removed
                    this.mapSessions.remove(sessionName);
                }
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                // The TOKEN wasn't valid
                System.out.println("Problems in the app server: the TOKEN wasn't valid");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } else {
            // The SESSION does not exist
            System.out.println("Problems in the app server: the SESSION does not exist");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
