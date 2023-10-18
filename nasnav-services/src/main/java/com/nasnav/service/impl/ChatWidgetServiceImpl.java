package com.nasnav.service.impl;

import com.nasnav.dao.ChatWidgetSettingRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dto.response.ChatWidgetSettingResponse;
import com.nasnav.dto.response.CreateChatWidgetRequest;
import com.nasnav.enumerations.ChatSettingType;
import com.nasnav.mappers.ChatWidgetSettingMapper;
import com.nasnav.persistence.ChatWidgetSettingEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.service.ChatWidgetService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChatWidgetServiceImpl implements ChatWidgetService {
    private final OrganizationRepository organizationRepository;
    private final ChatWidgetSettingRepository chatWidgetSettingRepository;

    private final ChatWidgetSettingMapper chatWidgetSettingMapper;

    public ChatWidgetServiceImpl(OrganizationRepository organizationRepository, ChatWidgetSettingRepository chatWidgetSettingRepository, ChatWidgetSettingMapper chatWidgetSettingMapper) {
        this.organizationRepository = organizationRepository;
        this.chatWidgetSettingRepository = chatWidgetSettingRepository;
        this.chatWidgetSettingMapper = chatWidgetSettingMapper;
    }

    @Override
    public ChatWidgetSettingResponse create(CreateChatWidgetRequest request) {
        Optional<OrganizationEntity> optionalOrganization = organizationRepository.findById(request.getOrganizationId());
        ChatWidgetSettingResponse response = new ChatWidgetSettingResponse();
        if(optionalOrganization.isPresent()){
            OrganizationEntity organization = optionalOrganization.get();
            Optional<ChatWidgetSettingEntity> optionalUnPublishedSetting= chatWidgetSettingRepository.findByOrganizationAndType(organization,ChatSettingType.UNPUBLISHED.getValue());
            if(optionalUnPublishedSetting.isPresent()){
                ChatWidgetSettingEntity chatWidgetSettingEntity = optionalUnPublishedSetting.get();
                chatWidgetSettingEntity.setValue(request.getValue());
                ChatWidgetSettingEntity savedChatWidgetSettingEntity =  chatWidgetSettingRepository.save(chatWidgetSettingEntity);
                response = chatWidgetSettingMapper.toResponse(savedChatWidgetSettingEntity);
            }else {
                ChatWidgetSettingEntity chatWidgetSettingEntity = new ChatWidgetSettingEntity();
                chatWidgetSettingEntity.setType(ChatSettingType.UNPUBLISHED.getValue());
                chatWidgetSettingEntity.setValue(request.getValue());
                optionalOrganization.ifPresent(chatWidgetSettingEntity::setOrganization);
                ChatWidgetSettingEntity savedChatWidgetSettingEntity =  chatWidgetSettingRepository.save(chatWidgetSettingEntity);
                response = chatWidgetSettingMapper.toResponse(savedChatWidgetSettingEntity);
            }

        }

        return response;
    }

    @Override
    public ChatWidgetSettingResponse publish(Long orgId) {
        Optional<OrganizationEntity> optionalOrganization = organizationRepository.findById(orgId);
        ChatWidgetSettingResponse response = new ChatWidgetSettingResponse();
        if(optionalOrganization.isPresent()){
            OrganizationEntity organization = optionalOrganization.get();
            Optional<ChatWidgetSettingEntity> optionalUnPublishedSetting= chatWidgetSettingRepository.findByOrganizationAndType(organization,ChatSettingType.UNPUBLISHED.getValue());
            ChatWidgetSettingEntity unPublishedSetting = new ChatWidgetSettingEntity();
            Optional<ChatWidgetSettingEntity> optionalPublishedSetting= chatWidgetSettingRepository.findByOrganizationAndType(organization,ChatSettingType.PUBLISHED.getValue());
            ChatWidgetSettingEntity publishedSetting = new ChatWidgetSettingEntity();

            if(optionalUnPublishedSetting.isPresent()){
                unPublishedSetting =  optionalUnPublishedSetting.get();
            }

            if (optionalPublishedSetting.isPresent()){
                publishedSetting = optionalPublishedSetting.get();
                publishedSetting.setValue(unPublishedSetting.getValue());
            }else {
                publishedSetting.setType(ChatSettingType.PUBLISHED.getValue());
                publishedSetting.setValue(unPublishedSetting.getValue());
                publishedSetting.setOrganization(unPublishedSetting.getOrganization());
            }

            publishedSetting = chatWidgetSettingRepository.save(publishedSetting);
            response = chatWidgetSettingMapper.toResponse(publishedSetting);

        }
        return response;
    }

    @Override
    public ChatWidgetSettingResponse getPublished(Long orgId) {
        Optional<OrganizationEntity> optionalOrganization = organizationRepository.findById(orgId);
        ChatWidgetSettingResponse response = new ChatWidgetSettingResponse();
        if(optionalOrganization.isPresent()) {
            OrganizationEntity organization = optionalOrganization.get();
            Optional<ChatWidgetSettingEntity> optionalPublishedSetting = chatWidgetSettingRepository.findByOrganizationAndType(organization, ChatSettingType.PUBLISHED.getValue());
            ChatWidgetSettingEntity publishedSetting = new ChatWidgetSettingEntity();
            if (optionalPublishedSetting.isPresent()) {
                publishedSetting = optionalPublishedSetting.get();
            }
            response = chatWidgetSettingMapper.toResponse(publishedSetting);
        }
        return response;
    }

    @Override
    public ChatWidgetSettingResponse getUnPublished(Long orgId) {
        Optional<OrganizationEntity> optionalOrganization = organizationRepository.findById(orgId);
        ChatWidgetSettingResponse response = new ChatWidgetSettingResponse();
        if(optionalOrganization.isPresent()) {
            OrganizationEntity organization = optionalOrganization.get();
            Optional<ChatWidgetSettingEntity> optionalUnPublishedSetting = chatWidgetSettingRepository.findByOrganizationAndType(organization, ChatSettingType.UNPUBLISHED.getValue());
            ChatWidgetSettingEntity unPublishedSetting = new ChatWidgetSettingEntity();
            if (optionalUnPublishedSetting.isPresent()) {
                unPublishedSetting = optionalUnPublishedSetting.get();
            }
            response = chatWidgetSettingMapper.toResponse(unPublishedSetting);
        }
        return response;
    }
}
