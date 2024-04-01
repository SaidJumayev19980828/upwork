package com.nasnav.service.impl;

import com.nasnav.AppConfig;
import com.nasnav.commons.utils.CustomPaginationPageRequest;
import com.nasnav.dao.ContactUsRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.SettingRepository;
import com.nasnav.dto.request.ContactUsFeedBackRequestDto;
import com.nasnav.dto.request.ContactUsRequestDto;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.ContactUsEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.SettingEntity;
import com.nasnav.service.ContactUsService;
import com.nasnav.service.MailService;
import com.nasnav.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.nasnav.constatnts.EmailConstants.CONTACT_US_CUSTOMER_MAIL;
import static com.nasnav.constatnts.EmailConstants.CONTACT_US_FEEDBACK_MAIL;
import static com.nasnav.enumerations.Settings.ORG_EMAIL;
import static com.nasnav.exceptions.ErrorCodes.G$CONTACT$0001;
import static com.nasnav.exceptions.ErrorCodes.G$ORG$0001;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ContactUsServiceImpl  implements ContactUsService {
    private final OrganizationRepository organizationRepository;
    private final ContactUsRepository contactUsRepository;
    private final MailService mailService;
    private final SettingRepository settingRepo;
     private final  AppConfig config;
     private final SecurityService securityService;
    @Override
    public void sendContactUsEmail(ContactUsRequestDto request, Long orgId) throws MessagingException, IOException {
       OrganizationEntity organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, G$ORG$0001, orgId));
        String OrganizationEmail = getOrganizationEmail(orgId);

            sendConatctUsEmails(OrganizationEmail, organization.getName(),  request.getCustomerName(),request.getCustomerEmail(), request.getMessage(),"A new email from" + request.getCustomerName() + "- " + organization.getName() ,CONTACT_US_CUSTOMER_MAIL );

        contactUsRepository.save(ContactUsEntity.buildEntity(request,organization));

    }

    @Override
    public void sendFeedbackEmail(ContactUsFeedBackRequestDto request) throws MessagingException, IOException {
        ContactUsEntity contactUsEntity = contactUsRepository.findById(request.getFormId())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, G$CONTACT$0001, request.getFormId()));
        sendConatctUsEmails(contactUsEntity.getCustomerEmail(), contactUsEntity.getOrganization().getName(),  contactUsEntity.getCustomerName(),contactUsEntity.getCustomerEmail(), request.getMessage(),"Follow-up regarding your message to" + contactUsEntity.getOrganization().getName() ,CONTACT_US_FEEDBACK_MAIL );

    }

    @Override
    public ContactUsEntity getContactUsFormById(Long id) {
        return contactUsRepository.findById(id)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, G$CONTACT$0001, id));
    }

    @Override
    public PageImpl<ContactUsEntity> getContactUsForms(Integer start, Integer count, LocalDateTime fromDate) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        Pageable page = new CustomPaginationPageRequest(start, count);
        return contactUsRepository.findAllByOrganizationIdAndCreatedAt(orgId,page,fromDate);
    }


    public void sendConatctUsEmails(String sendTo,String orgName,String customerName,String customerEmail,String message , String emailSubject , String mailTemplate) throws MessagingException, IOException {
        Map<String, String> parametersMap = prepareMailContent(orgName,customerName,customerEmail,message,sendTo);
        mailService.send(orgName, sendTo, emailSubject, mailTemplate,parametersMap);
    }




    public Map<String, String> prepareMailContent(String orgName,String customerName,String customerEmail,String message,String sendTo) {
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put("#CustomerEmail#", customerEmail);
        parametersMap.put("#CustomerName#", customerName);
        parametersMap.put("#OrgName#", orgName);
        parametersMap.put("#message#", message);
        parametersMap.put("#CustomerServicesMail#",sendTo);
        return parametersMap;
    }

    private String getOrganizationEmail(Long orgId) {
        return settingRepo.findBySettingNameAndOrganization_Id(ORG_EMAIL.name(), orgId)
                .map(SettingEntity::getSettingValue)
                .orElse(config.mailSenderAddress);
    }




}
