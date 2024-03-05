package com.nasnav.test;

import com.nasnav.integration.smsmisr.SmsMisrMobileOTPService;
import com.nasnav.integration.smsmisr.dto.OTPDto;
import com.nasnav.integration.smsmisr.dto.OTPResponse;

import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class SmsMisrMobileOTPServiceTest extends AbstractTestWithTempBaseDir {

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private SmsMisrMobileOTPService smsMisrMobileOTPService;

    @Test
    public void testSendSuccess() {
        OTPDto otpDto = new OTPDto("1234567890", "123456");
        OTPResponse successResponse = new OTPResponse("4901", "123456","1");
        when(restTemplate.postForObject(any(), any(), eq(OTPResponse.class)))
                .thenReturn(successResponse);

        String result = smsMisrMobileOTPService.send(otpDto);

        assertEquals("Success", result);
    }

    @Test
    public void testSendFailure() {
        OTPDto otpDto = new OTPDto("1234567890", "123456");
        OTPResponse failureResponse = new OTPResponse("4001", "123456", "0");
        when(restTemplate.postForObject(any(), any(), eq(OTPResponse.class)))
                .thenReturn(failureResponse);

        String result = smsMisrMobileOTPService.send(otpDto);

        assertEquals("Fail", result);
    }
}
