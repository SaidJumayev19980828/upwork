package com.nasnav.controller;

import com.nasnav.dto.request.ContactUsFeedBackRequestDto;
import com.nasnav.dto.request.ContactUsRequestDto;
import com.nasnav.persistence.ContactUsEntity;
import com.nasnav.service.ContactUsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDateTime;

import static com.nasnav.constatnts.DefaultValueStrings.DEFAULT_PAGING_COUNT;
import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/contactUs", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ContactUsController {
    private final ContactUsService contactUsService;


    @PostMapping
    public void contactUsMail(@RequestHeader(value = TOKEN_HEADER , required = false) String userToken, @RequestBody ContactUsRequestDto request, @RequestParam Long orgId) throws MessagingException, IOException {
        contactUsService.sendContactUsEmail(request,orgId);
    }

    @PostMapping("/feedback")
    public void contactUsFeedbackMail(@RequestHeader(value = TOKEN_HEADER , required = false) String userToken, @RequestBody ContactUsFeedBackRequestDto request) throws MessagingException, IOException {
        contactUsService.sendFeedbackEmail(request);
    }

    @GetMapping
    public ContactUsEntity getContactUs(@RequestHeader(value=TOKEN_HEADER ,required = false) String userToken, @RequestParam Long formId) {
       return contactUsService.getContactUsFormById(formId);
    }

    @GetMapping("/all")
    public PageImpl<ContactUsEntity> getAllForms(               @RequestParam(required = false, defaultValue = "0") Integer start,
                                                                @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count ,
                                                                @RequestParam(required = false, name = "fromDate")
                                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                                LocalDateTime fromDate

    ) {
        return contactUsService.getContactUsForms(start, count, fromDate);

    }



}
