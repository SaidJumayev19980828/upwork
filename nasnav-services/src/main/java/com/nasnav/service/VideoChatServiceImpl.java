package com.nasnav.service;

import com.nasnav.AppConfig;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.VideoChatLogRepository;
import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.dto.VideoChatLogRepresentationObject;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.*;

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
        connectionProperties = getConnectionProperties();
    }


    @Override
    public VideoChatResponse getSession(String sessionName, Long orgId) {

        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        OrganizationRepresentationObject organization = organizationService.getOrganizationById(orgId, YeshteryState.DISABLED.getValue());

        if (VideoChatOrgState.DISABLED == organization.getEnableVideoChat()) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, VIDEO$PARAM$0001, orgId);
        }
        if (loggedInUser instanceof UserEntity) {
            return getOrCreateUserVideoSession((UserEntity) loggedInUser, sessionName, orgId);
        } else if (loggedInUser instanceof EmployeeUserEntity) {
            return addEmployeeIntoSession((EmployeeUserEntity) loggedInUser, sessionName, orgId);
        } else {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, VIDEO$PARAM$0002, orgId);
        }

    }

    private ConnectionProperties getConnectionProperties() {
        return new ConnectionProperties.Builder().type(ConnectionType.WEBRTC).role(OpenViduRole.PUBLISHER).build();
    }

    private VideoChatResponse addEmployeeIntoSession(EmployeeUserEntity loggedInUser, String sessionName, Long orgId) {
        VideoChatLogEntity videChatLogObj = getVideoChatLogEntity(sessionName, orgId);
        videChatLogObj.setAssignedTo(loggedInUser);
        videoChatLogRepository.save(videChatLogObj);

        String token = createConnection(getSession(sessionName));
        mapSessionNamesTokens.get(sessionName).put(token, OpenViduRole.PUBLISHER);
        return new VideoChatResponse(true, null, token, loggedInUser.getName(), sessionName);
    }

    private VideoChatLogEntity getVideoChatLogEntity(String sessionName, Long orgId) {
        if (!sessionsMap.containsKey(sessionName)) {
            throw new RuntimeBusinessException(NOT_FOUND, VIDEO$PARAM$0003);
        }
        return videoChatLogRepository.findByNameAndOrganization_Id(sessionName, orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, VIDEO$PARAM$0004));
    }

    private VideoChatResponse getOrCreateUserVideoSession(UserEntity loggedInUser, String sessionName, Long orgId) {
        if (Strings.isNotEmpty(sessionName)) {
            return getExistingChatSession(sessionName);
        }
        return createNewVideoSession(loggedInUser, orgId);
    }

    private VideoChatResponse getExistingChatSession(String sessionName) {
        String token = createConnection(getSession(sessionName));
        mapSessionNamesTokens.get(sessionName).put(token, OpenViduRole.PUBLISHER);
        return new VideoChatResponse(true, null, token, null, sessionName);
    }

    private String createConnection(Session session) {
        try {
            return session.createConnection(connectionProperties).getToken();
        } catch (OpenViduJavaClientException | OpenViduHttpException ex) {
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, VIDEO$PARAM$0005, ex.getMessage());
        }
    }

    private Session getSession(String sessionName) {
        return ofNullable(sessionName)
                .map(name -> sessionsMap.get(name))
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, VIDEO$PARAM$0003));
    }

    private VideoChatResponse createNewVideoSession(UserEntity loggedInUser, Long orgId) {
        OrganizationEntity organizationEntity = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, G$ORG$0001, orgId));
        try {

            Session session = this.openVidu.createSession();
            String sessionName = session.getSessionId();
            String token = createConnection(session);
            VideoChatLogEntity newVideChatLog = new VideoChatLogEntity();
            newVideChatLog.setCreatedAt(LocalDateTime.now());
            newVideChatLog.setUser(loggedInUser);
            newVideChatLog.setToken(token);
            newVideChatLog.setName(sessionName);
            newVideChatLog.setIsActive(true);
            newVideChatLog.setOrganization(organizationEntity);
            newVideChatLog.setStatus(VideoChatStatus.NEW.getValue());

            videoChatLogRepository.saveAndFlush(newVideChatLog);

            this.sessionsMap.put(sessionName, session);
            this.mapSessionNamesTokens.put(sessionName, new ConcurrentHashMap<>());
            this.mapSessionNamesTokens.get(sessionName).put(token, OpenViduRole.PUBLISHER);
            return new VideoChatResponse(true, null, token, null, sessionName);

        } catch (Exception e) {
            return new VideoChatResponse(false, e.getMessage(), null, null, null);
        }
    }

    @Override
    public List<VideoChatLogRepresentationObject> getOrgSessions(Long orgId) {
        return videoChatLogRepository.findByStatusAndOrganization_Id(VideoChatStatus.NEW.getValue(), orgId)
                .stream()
                .map(e ->(VideoChatLogRepresentationObject) e.getRepresentation())
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
            Session session = getSession(videoEntity.getName());
            session.close();
        } catch (OpenViduHttpException | OpenViduJavaClientException ex) {
            logger.error("couldn't close session! , "+ ex.getMessage());
        }
        sessionsMap.remove(videoEntity.getName());
        mapSessionNamesTokens.remove(videoEntity.getName());
        videoEntity.setStatus(VideoChatStatus.FINISHED.getValue());
        videoChatLogRepository.saveAndFlush(videoEntity);
    }
}
