package com.keshav.NotificationService.service;

import com.keshav.NotificationService.model.SmsRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsProcessingServiceTest {

    @Mock
    private SmsService smsService;

    @Mock
    private BlacklistService blacklistService;

    @Mock
    private ThirdPartyApiService thirdPartyApiService;

    @Mock
    private SmsLogService smsLogService;

    @InjectMocks
    private SmsProcessingService smsProcessingService;

    private SmsRequest smsRequest;
    private final String requestId = "12345";
    private final String phoneNumber = "+1234567890";
    private final String message = "Hello, this is a test SMS!";

    @BeforeEach
    void setUp() {
        smsRequest = new SmsRequest();
        smsRequest.setId(requestId);
        smsRequest.setPhoneNumber(phoneNumber);
        smsRequest.setMessage(message);
    }

    @Test
    void testConsumeMessage_Success() {
        when(smsService.getSmsRequestById(requestId)).thenReturn(smsRequest);
        when(blacklistService.isBlacklisted(phoneNumber)).thenReturn(false);
        when(thirdPartyApiService.sendSms(phoneNumber, requestId, message)).thenReturn(true);

        smsProcessingService.processSmsRequest(requestId);

        verify(smsService, times(1)).getSmsRequestById(requestId);
        verify(blacklistService, times(1)).isBlacklisted(phoneNumber);
        verify(thirdPartyApiService, times(1)).sendSms(phoneNumber, requestId, message);
        verify(smsService, times(1)).updateSmsRequestStatus(requestId, "SENT");
        verify(smsLogService, times(1)).saveSmsLog(requestId, phoneNumber, message);
    }

    @Test
    void testConsumeMessage_BlacklistedPhoneNumber() {
        when(smsService.getSmsRequestById(requestId)).thenReturn(smsRequest);
        when(blacklistService.isBlacklisted(phoneNumber)).thenReturn(true);

        smsProcessingService.processSmsRequest(requestId);

        verify(smsService, times(1)).getSmsRequestById(requestId);
        verify(blacklistService, times(1)).isBlacklisted(phoneNumber);
        verify(smsService, times(1)).updateSmsRequestStatusWithFailure(eq(requestId), eq("FAILED"), eq("BLACKLISTED"), anyString());

        verifyNoInteractions(thirdPartyApiService);
        verifyNoInteractions(smsLogService);
    }

    @Test
    void testConsumeMessage_ApiFailure() {
        when(smsService.getSmsRequestById(requestId)).thenReturn(smsRequest);
        when(blacklistService.isBlacklisted(phoneNumber)).thenReturn(false);
        when(thirdPartyApiService.sendSms(phoneNumber, requestId, message)).thenReturn(false);

        smsProcessingService.processSmsRequest(requestId);

        verify(smsService, times(1)).updateSmsRequestStatusWithFailure(eq(requestId), eq("FAILED"), eq("API_ERROR"), anyString());

        verifyNoInteractions(smsLogService);
    }

    @Test
    void testConsumeMessage_ElasticsearchFailure() {
        when(smsService.getSmsRequestById(requestId)).thenReturn(smsRequest);
        when(blacklistService.isBlacklisted(phoneNumber)).thenReturn(false);
        when(thirdPartyApiService.sendSms(phoneNumber, requestId, message)).thenReturn(true);

        doThrow(new RuntimeException("Elasticsearch Down")).when(smsLogService).saveSmsLog(requestId, phoneNumber, message);

        smsProcessingService.processSmsRequest(requestId);

        verify(smsService, times(1)).updateSmsRequestStatus(requestId, "SENT");
        verify(smsLogService, times(1)).saveSmsLog(requestId, phoneNumber, message);
    }

    @Test
    void testConsumeMessage_InvalidRequestId() {
        when(smsService.getSmsRequestById(requestId)).thenThrow(new IllegalArgumentException("Invalid Request ID"));

        smsProcessingService.processSmsRequest(requestId);

        verify(smsService, times(1)).getSmsRequestById(requestId);
        verifyNoInteractions(blacklistService);
        verifyNoInteractions(thirdPartyApiService);
        verifyNoInteractions(smsLogService);
    }

    @Test
    void testConsumeMessage_UnexpectedException() {
        when(smsService.getSmsRequestById(requestId)).thenThrow(new RuntimeException("Unexpected Error"));

        smsProcessingService.processSmsRequest(requestId);

        verify(smsService, times(1)).getSmsRequestById(requestId);
        verifyNoInteractions(blacklistService);
        verifyNoInteractions(thirdPartyApiService);
        verifyNoInteractions(smsLogService);
    }
}
