package com.nasnav.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.nasnav.AppConfig;
import com.nasnav.commons.criteria.AbstractCriteriaQueryBuilder;
import com.nasnav.commons.criteria.data.CrieteriaQueryResults;
import com.nasnav.dao.*;
import com.nasnav.dto.VideoChatLogRepresentationObject;
import com.nasnav.dto.request.OpenViduCallbackDTO;
import com.nasnav.enumerations.VideoChatOrgState;
import com.nasnav.enumerations.VideoChatStatus;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.request.VideoChatSearchParam;
import com.nasnav.response.VideoChatListResponse;
import com.nasnav.response.VideoChatResponse;
import com.nasnav.service.SecurityService;
import com.nasnav.service.VideoChatService;
import com.nasnav.service.notification.NotificationService;
import com.rometools.utils.Strings;
import io.openvidu.java.client.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.nasnav.commons.utils.EntityUtils.allIsNull;
import static com.nasnav.enumerations.Settings.CONCURRENT_VIDEO_CHAT_CONNECTIONS;
import static com.nasnav.enumerations.VideoChatStatus.*;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.*;

@Service
public class VideoChatServiceImpl implements VideoChatService {

    private final Logger logger = LogManager.getLogger();

    @Autowired
    private AppConfig appConfig;

    private OpenVidu openVidu;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private ShopsRepository shopsRepository;
    @Autowired
    private SettingRepository settingRepo;
    @Autowired
    @Qualifier("videoChatQueryBuilder")
    private AbstractCriteriaQueryBuilder<VideoChatLogEntity, VideoChatSearchParam> criteriaQueryBuilder;
    private Map<String, Session> sessionsMap = new ConcurrentHashMap<>();

    private Map<String, List<UserSessionInfo>> mapSessionNamesTokens = new ConcurrentHashMap<>();

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private VideoChatLogRepository videoChatLogRepository;

    @Autowired
    private GroupVideoChatLogRepository groupVideoChatLogRepository;

    private ConnectionProperties connectionProperties;

    private SessionProperties sessionProperties;

    @Autowired
    private  NotificationService notificationService;
    @Autowired
    private  EmployeeUserRepository employeeUserRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    public void initController() {
        this.openVidu = new OpenVidu(appConfig.openViduUrl, appConfig.openViduSecret);
        sessionProperties = getSessionProperties();
        connectionProperties = getConnectionProperties();
    }

    private SessionProperties getSessionProperties() {
        return new SessionProperties.Builder().build();
    }


    @Override
    public VideoChatResponse createOrJoinSession(String sessionName, Boolean force, Long orgId, Long shopId) {
        OrganizationEntity organization = validateAndGetOrganization(orgId, shopId);
        orgId = organization.getId();
//        BaseUserEntity loggedInUser = securityService.getCurrentUserForOrg(orgId);
        BaseUserEntity loggedInUser = securityService.getCurrentUser();

        if (Objects.equals(VideoChatOrgState.DISABLED.getValue(), organization.getEnableVideoChat())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, VIDEO$PARAM$0001, orgId);
        }
        if (loggedInUser instanceof UserEntity) {
            return getOrCreateUserVideoSession((UserEntity) loggedInUser, force, sessionName, orgId, shopId);
        } else if (loggedInUser instanceof EmployeeUserEntity) {
            return addEmployeeIntoSession((EmployeeUserEntity) loggedInUser, sessionName, orgId);
        } else {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, VIDEO$PARAM$0002, orgId);
        }

    }

    @Override
    public VideoChatResponse createOrJoinSessionForUser(String sessionName, Boolean force, Long orgId, Long shopId, UserEntity user) {
        OrganizationEntity organization = validateAndGetOrganization(orgId, shopId);
        orgId = organization.getId();

        if (Objects.equals(VideoChatOrgState.DISABLED.getValue(), organization.getEnableVideoChat())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, VIDEO$PARAM$0001, orgId);
        }
        return getOrCreateUserVideoSession(user, force, sessionName, orgId, shopId);
    }

    private ConnectionProperties getConnectionProperties() {
        return new ConnectionProperties.Builder().type(ConnectionType.WEBRTC).role(OpenViduRole.PUBLISHER).build();
    }

    private VideoChatResponse addEmployeeIntoSession(EmployeeUserEntity loggedInUser, String sessionName, Long orgId) {
        VideoChatLogEntity videChatLogObj = getVideoChatLogEntity(sessionName, orgId);

        if (isMaxLimitReached(orgId)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, VIDEO$PARAM$0008);
        }

        String token = addTokenToSession(loggedInUser, sessionName);
        if (!Objects.equals(videChatLogObj.getAssignedTo(), loggedInUser)) {
            videChatLogObj.setAssignedTo(loggedInUser);
            videChatLogObj.setStatus(STARTED.getValue());
            videChatLogObj.addDescription("employee (" +loggedInUser.getName() + ") has joined the session");
            videoChatLogRepository.saveAndFlush(videChatLogObj);
        }

        return new VideoChatResponse(token, loggedInUser.getName(), sessionName, getVideoChatShopId(videChatLogObj));
    }

    private Boolean isMaxLimitReached(Long orgId) {
        Long limit = settingRepo.findBySettingNameAndOrganization_Id(CONCURRENT_VIDEO_CHAT_CONNECTIONS.name(), orgId)
                .map(SettingEntity::getSettingValue)
                .map(Long::parseLong)
                .orElse(Long.MAX_VALUE);

        long count = videoChatLogRepository.countByOrganization_IdAndStatusIn(orgId, asList(NEW.getValue(), STARTED.getValue()));
        return count >= limit;
    }

    private String addTokenToSession(EmployeeUserEntity loggedInUser, String sessionName) {
        Connection connection = createConnection(getSession(sessionName));
        String token = connection.getToken();
        mapSessionNamesTokens.get(sessionName).add(new UserSessionInfo(loggedInUser.getId(), true, connection.getConnectionId()));
        return token;
    }

    private VideoChatLogEntity getVideoChatLogEntity(String sessionName, Long orgId) {
        if ( sessionName == null || sessionName.isEmpty() || sessionName.isBlank() ) {
            throw new RuntimeBusinessException(NOT_FOUND, VIDEO$PARAM$0003);
        }
        OrganizationEntity organization = organizationRepository.findById(orgId)
                .orElseThrow(()-> new RuntimeBusinessException(HttpStatus.NOT_FOUND,G$ORG$0001,orgId));
        return videoChatLogRepository.findByNameAndOrganization(sessionName, organization)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, VIDEO$PARAM$0004));
    }

    private VideoChatLogEntity getVideoChatLogEntity(String sessionName) {
        return videoChatLogRepository.findByName(sessionName)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, VIDEO$PARAM$0004));
    }

    private VideoChatResponse getOrCreateUserVideoSession(UserEntity loggedInUser, Boolean force, String sessionName, Long orgId, Long shopId) {
        if (Strings.isNotEmpty(sessionName)) {
            return getExistingChatSession(sessionName, orgId);
        }
        //TODO here is the problem replace it by search into DB By status live and userId
//        String userActiveSession = getUserActiveSession(loggedInUser);
        List<VideoChatLogEntity> userActiveSession = videoChatLogRepository.findByStatusAndUser(STARTED.getValue(),loggedInUser);

        if (!userActiveSession.isEmpty()) {
            if (force) {
                userActiveSession.stream().forEach(s->{
                    leaveSession(s.getName(), orgId, shopId, true);
                });
//                leaveSession(userActiveSession, orgId, shopId, true);
            } else {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, VIDEO$PARAM$0007);
            }
        }

        return createNewVideoSession(loggedInUser, orgId, shopId);
    }

    private String getUserActiveSession(UserEntity user) {
        for(Map.Entry<String, List<UserSessionInfo>> e : mapSessionNamesTokens.entrySet()) {
            boolean userHasSession = e.getValue()
                    .stream()
                    .anyMatch(i -> i.getUserId().equals(user.getId()) && !i.getIsEmployee());
            if (userHasSession)
                return e.getKey();
        }
        return null;
    }

    private VideoChatResponse getExistingChatSession(String sessionName, Long orgId) {
        VideoChatLogEntity entity = getVideoChatLogEntity(sessionName, orgId);
        Connection connection = createConnection(sessionsMap.get(sessionName));
        String token = connection.getToken();
        Long shopId = getVideoChatShopId(entity);
        return new VideoChatResponse(token, null, sessionName, shopId);
    }

    private Long getVideoChatShopId(VideoChatLogEntity entity) {
        return ofNullable(entity.getShop()).map(ShopsEntity::getId).orElse(null);
    }

    private Connection createConnection(Session session) {
        try {
            return session.createConnection(connectionProperties);
        } catch (OpenViduJavaClientException | OpenViduHttpException ex) {
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, VIDEO$PARAM$0005, ex.getMessage());
        }
    }

    private Session getSession(String sessionName) {
        return ofNullable(sessionName)
                .map(name -> sessionsMap.get(name))
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, VIDEO$PARAM$0003));
    }


    private VideoChatResponse createNewVideoSession(UserEntity loggedInUser, Long orgId, Long shopId) {
        OrganizationEntity organizationEntity = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, G$ORG$0001, orgId));
        ShopsEntity shop = ofNullable(shopId)
                .map(shopsRepository::findByIdAndRemoved)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElse(null);
        try {
            Session session = this.openVidu.createSession(sessionProperties);
            String sessionName = session.getSessionId();
            Connection connection = createConnection(session);
            String token = connection.getToken();
            VideoChatLogEntity newVideChatLog = new VideoChatLogEntity();
            newVideChatLog.setCreatedAt(LocalDateTime.now());
            newVideChatLog.setUser(loggedInUser);
            newVideChatLog.setToken(token);
            newVideChatLog.setName(sessionName);
            newVideChatLog.setIsActive(true);
            newVideChatLog.setShop(shop);
            newVideChatLog.setOrganization(organizationEntity);
            newVideChatLog.setStatus(NEW.getValue());
            videoChatLogRepository.saveAndFlush(newVideChatLog);

            this.sessionsMap.put(sessionName, session);
            List<UserSessionInfo> sessionInfos = new ArrayList<>();
            sessionInfos.add(new UserSessionInfo(loggedInUser.getId(), false, connection.getConnectionId()));
            this.mapSessionNamesTokens.put(sessionName, sessionInfos);

            return new VideoChatResponse(token, null, sessionName, getVideoChatShopId(newVideChatLog));
        } catch (OpenViduJavaClientException | OpenViduHttpException ex) {
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, VIDEO$PARAM$0005, ex.getMessage());
        }
    }

    @Override
    public VideoChatListResponse getOrgSessions(VideoChatSearchParam params) {
        setVideoChatListDefaultParams(params);
        CrieteriaQueryResults<VideoChatLogEntity> results = criteriaQueryBuilder.getResultList(params, true);
        List<VideoChatLogRepresentationObject> resultList = results.getResultList().stream()
                .map(v -> (VideoChatLogRepresentationObject) v.getRepresentation())
                .collect(toList());
        Long count = results.getResultCount();
        return new VideoChatListResponse(count, resultList);
    }

    private void setVideoChatListDefaultParams(VideoChatSearchParam searchParams) {
        if(searchParams.getStart() == null || searchParams.getStart() < 0){
            searchParams.setStart(0);
        }
        if(searchParams.getCount() == null || (searchParams.getCount() < 1)){
            searchParams.setCount(10);
        } else if (searchParams.getCount() > 1000) {
            searchParams.setCount(1000);
        }

        EmployeeUserEntity currentEmployee = (EmployeeUserEntity) securityService.getCurrentUser();
        if (!securityService.currentEmployeeHasNasnavRoles()) {
            searchParams.setOrgId(currentEmployee.getOrganizationId());
        }

        if (!securityService.currentEmployeeHasOrgRolesOrHigher()) {
            if (securityService.currentEmployeeUserHasShopRolesOrHigher()) {
                searchParams.setShopId(currentEmployee.getShopId());
            } else {
                throw new IllegalStateException("current user has no roles");
            }
        }
    }

    @Override
    public void leaveSession(String sessionName, Long orgId, Long shopId, Boolean endCall) {
        BaseUserEntity loggedInUser = securityService.getCurrentUser();

        OrganizationEntity org = validateAndGetOrganization(orgId, shopId);
        VideoChatLogEntity videoEntity = getVideoChatLogEntity(sessionName, org.getId());

        if (loggedInUser instanceof UserEntity) {
           endSession(videoEntity, ENDED_BY_CUSTOMER);
        } else if (loggedInUser instanceof EmployeeUserEntity) {
            if(endCall){
                endSession(videoEntity, ENDED_BY_EMPLOYEE);
            } else {
                removeUserSessionInfoFromMap(sessionName, loggedInUser.getId());

                videoEntity.setAssignedTo(null);
                videoEntity.setStatus(STARTED.getValue());
                videoEntity.addDescription("employee (" +loggedInUser.getName() + ") has left the session");
                videoChatLogRepository.saveAndFlush(videoEntity);
            }
        }
    }

    @Override
    public void handelCallbackEvent(String eventDTO) {
        try {
            OpenViduCallbackDTO dto = mapper.readValue(eventDTO, OpenViduCallbackDTO.class);
            switch (dto.getEvent()) {
                case sessionDestroyed: handleSessionDestroyed(dto);
                    break;
                case participantLeft: handelParticipantLeft(dto);
                    break;
                default:
            }
        } catch (JsonProcessingException e) {
            // empty
        }
    }
    @Override
    public Map<String, String> getVideoChatCredentials() {
        List<Map<String, String>> videoChatCredentials = new ArrayList<>();
        JsonObject sessionPropertiesMap = sessionProperties.toJson();
        String openViduUrl = appConfig.openViduUrl;
        String openViduSecret = appConfig.openViduSecret;
        Map<String, String> credentials = new HashMap<>();
            credentials.put("openViduUrl", openViduUrl);
            credentials.put("openViduSecret", openViduSecret);
            credentials.put("sessionId", "sessionId");
            credentials.put("token", "token");
            credentials.put("sessionProperties", sessionPropertiesMap.toString());
        return credentials;
    }

    private void handleSessionDestroyed(OpenViduCallbackDTO dto) {
        String sessionName = dto.getSessionId();
        VideoChatLogEntity entity = getVideoChatLogEntity(sessionName);
        entity.addDescription("ended by callback, reason :" + dto.getReason().name());
        endSession(entity, ENDED_BY_CALLBACK);
    }

    private void handelParticipantLeft(OpenViduCallbackDTO dto) {
        String sessionName = dto.getSessionId();
        VideoChatLogEntity entity = getVideoChatLogEntity(sessionName);
        entity.addDescription("participant "+ dto.getClientData()+" left the session, reason : "+ dto.getReason().name());
        mapSessionNamesTokens.get(sessionName)
                .stream()
                .filter(i -> i.getConnection().equals(dto.getConnectionId()))
                .findFirst()
                .ifPresent(userSessionInfo -> mapSessionNamesTokens.get(sessionName).remove(userSessionInfo));
        videoChatLogRepository.save(entity);
    }

    private OrganizationEntity validateAndGetOrganization(Long orgId, Long shopId) {
        if (allIsNull(shopId, orgId))
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, VIDEO$PARAM$0006);

        if (shopId != null) {
            return shopsRepository.findByIdAndRemoved(shopId)
                    .map(ShopsEntity::getOrganizationEntity)
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, S$0002, shopId));
        } else {
            return organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, G$ORG$0001, orgId));
        }
    }

    private Optional<UserSessionInfo> getUserInfoFromSessionsMap(String sessionName, Long userId) {
        return mapSessionNamesTokens
                .get(sessionName)
                .stream()
                .filter(u -> Objects.equals(u.getUserId(), userId))
                .findAny();
    }

    private void removeUserSessionInfoFromMap(String sessionName, Long userId) {
        getUserInfoFromSessionsMap(sessionName, userId)
                .ifPresent(u -> mapSessionNamesTokens.get(sessionName).remove(u));
    }

    private void endSession(VideoChatLogEntity videoEntity, VideoChatStatus status) {
        try {
            Session session = getSession(videoEntity.getName());
            session.close();
        } catch (OpenViduHttpException | OpenViduJavaClientException | RuntimeException ex ) {
            logger.error("couldn't close session! , {}", ex.getMessage());
            videoEntity.setStatus(FAILED.getValue());
        }
        sessionsMap.remove(videoEntity.getName());
        mapSessionNamesTokens.remove(videoEntity.getName());
        videoEntity.setIsActive(false);
        videoEntity.setEndedAt(LocalDateTime.now());
        videoEntity.setStatus(status.getValue());
        videoChatLogRepository.saveAndFlush(videoEntity);
    }


    @Override
    public VideoChatResponse createGroupVideoChat(String sessionName ,Long orgId, Long shopId) {
        GroupVideoChatLogEntity groupVideoChatLogEntity = new GroupVideoChatLogEntity();
//        BaseUserEntity loggedInUser = securityService.getCurrentUserForOrg(orgId);
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if(!sessionName.isEmpty()){
            Optional<GroupVideoChatLogEntity> optionalGroupVideoChatLogEntity = groupVideoChatLogRepository.findByName(sessionName);
            if (optionalGroupVideoChatLogEntity.isPresent()){
                groupVideoChatLogEntity = optionalGroupVideoChatLogEntity.get();
                if (loggedInUser instanceof UserEntity) {
                    List<UserEntity> currentUsers = groupVideoChatLogEntity.getUsers();
                    Optional<UserEntity> optionalUserEntity = userRepository.findById(loggedInUser.getId());
                    optionalUserEntity.ifPresent(currentUsers::add);
                    groupVideoChatLogEntity.setUsers(currentUsers);

                } else if (loggedInUser instanceof EmployeeUserEntity) {
                    List<EmployeeUserEntity> currentEmployeeUsers = groupVideoChatLogEntity.getEmployeeUsers();
                    Optional<EmployeeUserEntity> optionalEmployeeUserEntity = employeeUserRepository.findById(loggedInUser.getId());
                    optionalEmployeeUserEntity.ifPresent(currentEmployeeUsers::add);
                    groupVideoChatLogEntity.setEmployeeUsers(currentEmployeeUsers);

                }
            }
        }else {
            OrganizationEntity organization = validateAndGetOrganization(orgId, shopId);
            orgId = organization.getId();
            ShopsEntity shop = ofNullable(shopId)
                    .map(shopsRepository::findByIdAndRemoved)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .orElse(null);

            if (Objects.equals(VideoChatOrgState.DISABLED.getValue(), organization.getEnableVideoChat())) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, VIDEO$PARAM$0001, orgId);
            }
            groupVideoChatLogEntity.setShop(shop);
            groupVideoChatLogEntity.setOrganization(organization);

            if (loggedInUser instanceof UserEntity) {
                List<UserEntity> currentUsers = new ArrayList<>();
                Optional<UserEntity> optionalUserEntity = userRepository.findById(loggedInUser.getId());
                optionalUserEntity.ifPresent(currentUsers::add);
                groupVideoChatLogEntity.setUsers(currentUsers);

            } else if (loggedInUser instanceof EmployeeUserEntity) {
                List<EmployeeUserEntity> currentEmployeeUsers = new ArrayList<>();
                Optional<EmployeeUserEntity> optionalEmployeeUserEntity = employeeUserRepository.findById(loggedInUser.getId());
                optionalEmployeeUserEntity.ifPresent(currentEmployeeUsers::add);
                groupVideoChatLogEntity.setEmployeeUsers(currentEmployeeUsers);

            }

        }
        VideoChatResponse response = createNewGroupVideoSession(groupVideoChatLogEntity,loggedInUser);
        return response;
    }

    @Override
    public VideoChatResponse getGroupVideoChat(String sessionName, Long orgId){
        if(Strings.isEmpty(sessionName)){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, VIDEO$PARAM$0004, orgId);
        }
        return getExistingChatSession(sessionName, orgId);
    }



    private VideoChatResponse createNewGroupVideoSession(GroupVideoChatLogEntity groupVideoChatLogEntity,BaseUserEntity loggedUser) {
        try {

            Session session = this.openVidu.createSession(sessionProperties);
            String sessionName = session.getSessionId();
            Connection connection = createConnection(session);
            String token = connection.getToken();

            groupVideoChatLogEntity.setCreatedAt(LocalDateTime.now());
            groupVideoChatLogEntity.setToken(token);
            groupVideoChatLogEntity.setName(sessionName);
            groupVideoChatLogEntity.setIsActive(true);
            groupVideoChatLogEntity.setStatus(NEW.getValue());
            groupVideoChatLogEntity = groupVideoChatLogRepository.save(groupVideoChatLogEntity);

            this.sessionsMap.put(sessionName, session);
            List<UserSessionInfo> sessionInfos = new ArrayList<>();
            sessionInfos.add(new UserSessionInfo(loggedUser.getId(), false, connection.getConnectionId()));
            this.mapSessionNamesTokens.put(sessionName, sessionInfos);

            return new VideoChatResponse(token, null, sessionName,groupVideoChatLogEntity.getId() );

        }catch (OpenViduJavaClientException | OpenViduHttpException ex) {
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, VIDEO$PARAM$0005, ex.getMessage());
        }
    }

}


@Data
@AllArgsConstructor
class UserSessionInfo {
    private Long userId;
    private Boolean isEmployee;
    private String connection;
}
