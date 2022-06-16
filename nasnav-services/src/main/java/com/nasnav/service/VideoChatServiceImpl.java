package com.nasnav.service;

import com.nasnav.AppConfig;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.VideoChatLogRepository;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.enumerations.VideoChatOrgState;
import com.nasnav.enumerations.VideoChatStatus;
import com.nasnav.enumerations.YeshteryState;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.VideoChatResponse;
import com.rometools.utils.Strings;
import io.openvidu.java.client.*;
import net.bytebuddy.utility.RandomString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class VideoChatServiceImpl implements VideoChatService {

    private final Logger logger = LogManager.getLogger();

    @Autowired
    private AppConfig appConfig;

    private OpenVidu openVidu;
    @Autowired
    private OrganizationRepository organizationRepository;

    private Map<String, Session> sessionsMap = new ConcurrentHashMap<>();

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
    public VideoChatResponse getSession(String userToken, String sessionName, Long orgId) {

        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        OrganizationRepresentationObject organization = organizationService.getOrganizationById(orgId, YeshteryState.DISABLED.getValue());

        if (VideoChatOrgState.DISABLED == organization.getEnableVideoChat()) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, VIDEO$PARAM$0001, orgId);
        }
        if (loggedInUser instanceof UserEntity) {
            return getOrCreateUserVideoSession((UserEntity) loggedInUser, userToken, sessionName, orgId);
        } else if (loggedInUser instanceof EmployeeUserEntity) {
            return addEmployeeIntoSession((EmployeeUserEntity) loggedInUser, userToken, sessionName, orgId);
        } else {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, VIDEO$PARAM$0002, orgId);
        }

    }

    private ConnectionProperties getConnectionProperties() {
        return new ConnectionProperties.Builder().type(ConnectionType.WEBRTC).role(OpenViduRole.PUBLISHER).build();
    }

    private VideoChatResponse addEmployeeIntoSession(EmployeeUserEntity loggedInUser, String userToken, String sessionName, Long orgId) {
        VideoChatLogEntity videChatLogObj = getVideoChatLogEntity(sessionName, orgId);
        // Session already exists
        videChatLogObj.setAssignedTo(loggedInUser);
        videoChatLogRepository.save(videChatLogObj);

        String token = videChatLogObj.getToken();
        mapSessionNamesTokens.get(sessionName).put(userToken, OpenViduRole.PUBLISHER);
        return new VideoChatResponse(true, null, token, loggedInUser.getName(), sessionName);
    }

    private VideoChatLogEntity getVideoChatLogEntity(String sessionName, Long orgId) {
        return videoChatLogRepository.findByNameAndOrganization_Id(sessionName, orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, VIDEO$PARAM$0004));
    }

    private VideoChatResponse getOrCreateUserVideoSession(UserEntity loggedInUser, String userToken, String sessionName, Long orgId) {
        if (Strings.isNotEmpty(sessionName)) {
            return getExistingChatSession(userToken, sessionName, orgId);
        }
        return createNewVideoSession(loggedInUser, userToken, orgId);
    }

    private VideoChatResponse getExistingChatSession(String userToken, String sessionName, Long orgId) {
        VideoChatLogEntity videoChatObj = getVideoChatLogEntity(sessionName, orgId);
        String token = videoChatObj.getToken();
        mapSessionNamesTokens.get(sessionName).put(userToken, OpenViduRole.PUBLISHER);
        return new VideoChatResponse(true, null, token, null, sessionName);
    }

    private VideoChatResponse createNewVideoSession(UserEntity loggedInUser, String userToken, Long orgId) {
        connectionProperties = getConnectionProperties();


        OrganizationEntity organizationEntity = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, G$ORG$0001, orgId));
        try {

            Session session = this.openVidu.createSession();
            String sessionName = session.getSessionId();
            String token = session.createConnection(connectionProperties).getToken();
            VideoChatLogEntity newVideChatLog = new VideoChatLogEntity();
            newVideChatLog.setCreatedAt(java.time.LocalDateTime.now());
            newVideChatLog.setUser(loggedInUser);
            newVideChatLog.setToken(token);
            newVideChatLog.setName(sessionName);
            newVideChatLog.setIsActive(true);
            newVideChatLog.setOrganization(organizationEntity);
            newVideChatLog.setStatus(VideoChatStatus.NEW.getValue());

            videoChatLogRepository.saveAndFlush(newVideChatLog);

            this.sessionsMap.put(sessionName, session);
            this.mapSessionNamesTokens.put(sessionName, new ConcurrentHashMap<>());
            this.mapSessionNamesTokens.get(sessionName).put(userToken, OpenViduRole.PUBLISHER);
            return new VideoChatResponse(true, null, token, null, sessionName);

        } catch (Exception e) {
            return new VideoChatResponse(false, e.getMessage(), null, null, null);
        }
    }

    @Override
    public List<BaseRepresentationObject> getOrgSessions(Long orgId) {
        return videoChatLogRepository.findByStatusAndOrganization_Id(VideoChatStatus.NEW.getValue(), orgId)
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
        try {
            sessionsMap.get(videoEntity.getName()).close();
        } catch (OpenViduHttpException | OpenViduJavaClientException ex) {
            logger.error("couldn't close session! , "+ ex.getMessage());
        }
        sessionsMap.remove(videoEntity.getName());
        mapSessionNamesTokens.remove(videoEntity.getName());
        videoEntity.setStatus(VideoChatStatus.FINISHED.getValue());
        videoChatLogRepository.saveAndFlush(videoEntity);
    }
}
