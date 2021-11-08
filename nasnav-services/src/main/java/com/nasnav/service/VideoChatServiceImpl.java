package com.nasnav.service;

import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.response.VideoChatResponse;
import net.bytebuddy.utility.RandomString;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nasnav.persistence.BaseUserEntity;
import io.openvidu.java.client.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class VideoChatServiceImpl implements VideoChatService{

    private OpenVidu openVidu;

    private Map<String, Session> mapSessions = new ConcurrentHashMap<>();

    private Map<String, Map<String, OpenViduRole>> mapSessionNamesTokens = new ConcurrentHashMap<>();


    private String OPENVIDU_URL = "http://34.125.116.133:5443/"; // TODO move to properties file
    private String SECRET = "MY_SECRET"; // TODO move to properties file

    @Autowired
    private SecurityService securityService;

    @Autowired
    private EmployeeUserService employeeUserService;

    @Autowired
    public void initController() {
        this.openVidu = new OpenVidu(OPENVIDU_URL, SECRET);
    }

    @Override
    public ResponseEntity<JSONObject> removeUser(String userToken, String sessionName) {


        String token = userToken;

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

    @Override
    public VideoChatResponse getSession(String userToken, String sessionName) throws OpenViduHttpException, OpenViduJavaClientException {

        BaseUserEntity loggedInUser = securityService.getCurrentUser();

        Long orgId = securityService.getCurrentUserOrganizationId();

        ConnectionProperties connectionProperties = new ConnectionProperties.Builder().type(ConnectionType.WEBRTC).role(OpenViduRole.PUBLISHER).build();


        if (sessionName != null && this.mapSessions.get(sessionName) != null) {
            // Session already exists
            System.out.println("Existing session " + sessionName);
            try {

                String token = this.mapSessions.get(sessionName).createConnection(connectionProperties).getToken();

                this.mapSessionNamesTokens.get(sessionName).put(userToken, OpenViduRole.PUBLISHER);

                return new VideoChatResponse(true, null, token, null, sessionName);
            }
            catch(OpenViduHttpException ex){
                if(ex.getStatus()==404) {
                    this.mapSessions.remove(sessionName);
                    this.mapSessionNamesTokens.remove(sessionName);
                }
                throw ex;

            }
            catch (Exception e1) {
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

    @Override
    public List<String> getAllSessions() {
        BaseUserEntity loggedInUser = securityService.getCurrentUser();

        List<String> sessions = new ArrayList<>();
        sessions = this.mapSessions.keySet().stream().collect(Collectors.toList());

        return sessions;
    }
}
