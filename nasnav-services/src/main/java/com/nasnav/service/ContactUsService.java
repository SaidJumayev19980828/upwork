package com.nasnav.service;

import com.nasnav.dto.request.ContactUsFeedBackRequestDto;
import com.nasnav.dto.request.ContactUsRequestDto;
import com.nasnav.persistence.ContactUsEntity;
import org.springframework.data.domain.PageImpl;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDateTime;

public interface ContactUsService {
    void sendContactUsEmail(ContactUsRequestDto request,Long orgId) throws MessagingException, IOException;

    void sendFeedbackEmail(ContactUsFeedBackRequestDto request) throws MessagingException, IOException;

    ContactUsEntity getContactUsFormById(Long id);

    PageImpl<ContactUsEntity> getContactUsForms(Integer page, Integer size, LocalDateTime fromDate);



}
