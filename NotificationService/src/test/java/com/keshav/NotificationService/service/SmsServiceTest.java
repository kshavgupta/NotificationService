package com.keshav.NotificationService.service;

import com.keshav.NotificationService.dto.SmsRequestDto;
import com.keshav.NotificationService.model.SmsRequest;
import com.keshav.NotificationService.repository.SmsRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsServiceTest {

    @Mock
    private SmsRequestRepository smsRequestRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private SmsService smsService;

    private SmsRequestDto smsRequestDto;
    private SmsRequest smsRequest;
    private String requestId;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID().toString();
        smsRequestDto = new SmsRequestDto();
        smsRequestDto.setPhoneNumber("+1234567890");
        smsRequestDto.setMessage("Hello, this is a test message.");

        smsRequest = new SmsRequest();
        smsRequest.setId(requestId);
        smsRequest.setPhoneNumber(smsRequestDto.getPhoneNumber());
        smsRequest.setMessage(smsRequestDto.getMessage());
        smsRequest.setStatus("PENDING");
    }

    @Test
    void testSendSms_Success() {
        when(smsRequestRepository.save(any(SmsRequest.class))).thenReturn(smsRequest);
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(null);

        String returnedRequestId = smsService.sendSms(smsRequestDto);

        assertNotNull(returnedRequestId);
        verify(smsRequestRepository, times(1)).save(any(SmsRequest.class));
        verify(kafkaTemplate, times(1)).send(eq("notification.send_sms"), anyString());
    }


    @Test
    void testSendSms_KafkaFailure() {
        when(smsRequestRepository.save(any(SmsRequest.class))).thenReturn(smsRequest);
        when(smsRequestRepository.findById(anyString())).thenReturn(Optional.of(smsRequest));
        when(kafkaTemplate.send(anyString(), anyString())).thenThrow(new RuntimeException("Kafka error"));

        String returnedRequestId = smsService.sendSms(smsRequestDto);

        assertNotNull(returnedRequestId);
        verify(smsRequestRepository, times(2)).save(any(SmsRequest.class));
        verify(kafkaTemplate, times(1)).send(eq("notification.send_sms"), anyString());
        verify(smsRequestRepository, times(1)).findById(anyString());
        assertEquals("FAILED", smsRequest.getStatus());
    }



    @Test
    void testGetSmsRequestById_Success() {
        when(smsRequestRepository.findById(requestId)).thenReturn(Optional.of(smsRequest));

        SmsRequest retrievedRequest = smsService.getSmsRequestById(requestId);

        assertNotNull(retrievedRequest);
        assertEquals(smsRequest.getId(), retrievedRequest.getId());
        verify(smsRequestRepository, times(1)).findById(requestId);
    }

    @Test
    void testGetSmsRequestById_NotFound() {
        when(smsRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> smsService.getSmsRequestById(requestId));

        assertEquals(requestId + " not found", exception.getMessage());
        verify(smsRequestRepository, times(1)).findById(requestId);
    }

    @Test
    void testUpdateSmsRequestStatus_Success() {
        when(smsRequestRepository.findById(requestId)).thenReturn(Optional.of(smsRequest));

        smsService.updateSmsRequestStatus(requestId, "SENT");

        assertEquals("SENT", smsRequest.getStatus());
        verify(smsRequestRepository, times(1)).save(smsRequest);
    }

    @Test
    void testUpdateSmsRequestStatusWithFailure_Success() {
        when(smsRequestRepository.findById(requestId)).thenReturn(Optional.of(smsRequest));

        smsService.updateSmsRequestStatusWithFailure(requestId, "FAILED", "API_ERROR", "Service unavailable");

        assertEquals("FAILED", smsRequest.getStatus());
        assertEquals("API_ERROR", smsRequest.getFailureCode());
        assertEquals("Service unavailable", smsRequest.getFailureComments());

        verify(smsRequestRepository, times(1)).save(smsRequest);
    }

}
