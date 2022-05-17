package com.nasnav.service;

import com.nasnav.AppConfig;
import com.nasnav.dao.VideoChatLogRepository;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.enumerations.VideoChatOrgState;
import com.nasnav.enumerations.VideoChatStatus;
import com.nasnav.enumerations.YeshteryState;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.VideoChatLogEntity;
import com.nasnav.response.VideoChatResponse;
import com.rometools.utils.Strings;
import io.openvidu.java.client.*;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.nasnav.exceptions.ErrorCodes.*;

@Service
public class VideoChatServiceImpl implements VideoChatService {

    @Autowired
    private AppConfig appConfig;

    private OpenVidu openVidu;

    private Map<String, Session> mapSessionsToToken = new ConcurrentHashMap<>();

    private Map<String, Map<String, OpenViduRole>> mapSessionNamesTokens = new ConcurrentHashMap<>();


    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private VideoChatLogRepository videoChatLogRepository;

    private ConnectionProperties connectionProperties;

    @Autowired
    public void initController() {
        this.openVidu = new OpenVidu(appConfig.openViduUrl, appConfig.openViduSecret);
    }


    @Override
    public VideoChatResponse getSession(String userToken, String sessionName, Long orgId) throws RuntimeBusinessException {

        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        OrganizationRepresentationObject organization = organizationService.getOrganizationById(orgId, YeshteryState.DISABLED.getValue());

        if (VideoChatOrgState.DISABLED == organization.getEnableVideoChat()) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, VIDEO$PARAM$0001, orgId);
        }
        if (loggedInUser instanceof UserEntity) {
            return getOrCreateUserVideoSession((UserEntity) loggedInUser, userToken, sessionName, orgId);
        } else if (loggedInUser instanceof EmployeeUserEntity) {
            return getEmployeeIntoSession((EmployeeUserEntity) loggedInUser, userToken, sessionName, orgId);
        } else {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, VIDEO$PARAM$0002, orgId);
        }

    }

    private ConnectionProperties getConnectionProperties() {
        return new ConnectionProperties.Builder().type(ConnectionType.WEBRTC).role(OpenViduRole.PUBLISHER).build();
    }

    private VideoChatResponse getEmployeeIntoSession(EmployeeUserEntity loggedInUser, String userToken, String sessionName, Long orgId) throws RuntimeBusinessException {

        ConnectionProperties connectionProperties = getConnectionProperties();

        VideoChatLogEntity videChatLogObj = getVideoChatLogEntity(sessionName, orgId);
        // Session already exists
        try {
            final String token = this.mapSessionsToToken.get(videChatLogObj.getToken()).createConnection(connectionProperties).getToken();
            this.mapSessionNamesTokens.get(token).put(userToken, OpenViduRole.PUBLISHER);
            return new VideoChatResponse(true, null, token, loggedInUser.getName(), sessionName);

        } catch (OpenViduHttpException ex) {
            if (ex.getStatus() == 404) {
                this.endSession(videChatLogObj);
            }
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, VIDEO$PARAM$0004);
        } catch (Exception e1) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, VIDEO$PARAM$0004);
        }
    }

    private VideoChatLogEntity getVideoChatLogEntity(String sessionName, Long orgId) {
        Optional<VideoChatLogEntity> videoChatObjOpt = videoChatLogRepository.findByNameAndOrganization_Id(sessionName, orgId);
        if (videoChatObjOpt.isEmpty()) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, VIDEO$PARAM$0004);
        }
        return videoChatObjOpt.get();
    }

    private VideoChatResponse getOrCreateUserVideoSession(UserEntity loggedInUser, String userToken, String sessionName, Long orgId) {
        connectionProperties = getConnectionProperties();
        if (Strings.isNotEmpty(sessionName)) {
            return getExistingChatSession(userToken, sessionName, orgId);
        }
        return createNewVideoSession(loggedInUser);
    }

    private VideoChatResponse getExistingChatSession(String userToken, String sessionName, Long orgId) {
        Optional<VideoChatLogEntity> videoChatObjOpt = videoChatLogRepository.findByName(sessionName);
        if (videoChatObjOpt.isEmpty()) {
            throw new RuntimeBusinessException(HttpStatus.NOT_FOUND, VIDEO$PARAM$0003);
        }

        final VideoChatLogEntity videoChatObj = getVideoChatLogEntity(sessionName, orgId);
        try {
            final String token = this.mapSessionsToToken.get(videoChatObj.getToken()).createConnection(connectionProperties).getToken();
            this.mapSessionNamesTokens.get(sessionName).put(userToken, OpenViduRole.PUBLISHER);
            return new VideoChatResponse(true, null, token, null, sessionName);
        } catch (OpenViduHttpException ex) {
            if (ex.getStatus() == 404) {
                this.endSession(videoChatObj);
            }
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, VIDEO$PARAM$0004);
        } catch (Exception e1) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, VIDEO$PARAM$0004);
        }
    }

    private VideoChatResponse createNewVideoSession(UserEntity loggedInUser) {
        String sessionName = RandomString.make(20);

        // New session
        System.out.println("New session " + sessionName);
        try {

            Session session = this.openVidu.createSession();
            String token = session.createConnection(connectionProperties).getToken();
            VideoChatLogEntity newVideChatLog = new VideoChatLogEntity();
            newVideChatLog.setCreatedAt(java.time.LocalDateTime.now());
            newVideChatLog.setUser(loggedInUser);
            newVideChatLog.setToken(token);
            newVideChatLog.setName(sessionName);
            newVideChatLog.setIsActive(true);
            newVideChatLog.setStatus(VideoChatStatus.NEW.getValue());

            videoChatLogRepository.saveAndFlush(newVideChatLog);


            this.mapSessionsToToken.put(sessionName, session);
            this.mapSessionNamesTokens.put(sessionName, new ConcurrentHashMap<>());
            this.mapSessionNamesTokens.get(sessionName).put(token, OpenViduRole.PUBLISHER);
            return new VideoChatResponse(true, null, token, null, sessionName);

        } catch (Exception e) {
            return new VideoChatResponse(false, e.getMessage(), null, null, null);
        }
    }

    @Override
    public List<BaseRepresentationObject> getOrgSessions(Long orgId) {

        return videoChatLogRepository.findByStatusAndOrganization_Id(VideoChatStatus.NEW, orgId)
                .stream()
                .map(VideoChatLogEntity::getRepresentation)
                .collect(Collectors.toList());
    }

    @Override
    public void leaveSession(String sessionName, Long orgId) {
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        VideoChatLogEntity videoEntity = getVideoChatLogEntity(sessionName, orgId);
        if (loggedInUser instanceof UserEntity) {
           endSession(videoEntity);
        } else if (loggedInUser instanceof EmployeeUserEntity) {
            videoEntity.setAssignedTo(null);
            videoEntity.setStatus(VideoChatStatus.STARTED.getValue());
            videoChatLogRepository.saveAndFlush(videoEntity);
        }
    }

    private void endSession(VideoChatLogEntity videoEntity) {
        this.mapSessionsToToken.remove(videoEntity.getToken());
        this.mapSessionNamesTokens.remove(videoEntity.getToken());
        videoEntity.setStatus(VideoChatStatus.FINISHED.getValue());
        videoChatLogRepository.saveAndFlush(videoEntity);
    }
}
