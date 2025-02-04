package com.keshav.NotificationService.service;

import com.keshav.NotificationService.model.SmsLog;
import com.keshav.NotificationService.repository.SmsLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsLogServiceTest {

    @Mock
    private SmsLogRepository smsLogRepository;

    @InjectMocks
    private SmsLogService smsLogService;

    private SmsLog smsLog;

    @BeforeEach
    void setUp() {
        smsLog = new SmsLog();
        smsLog.setPhoneNumber("+1234567890");
        smsLog.setMessage("Test message");
        smsLog.setSentAt(LocalDateTime.now());
    }

    @Test
    void testSaveSmsLog() {
        smsLogService.saveSmsLog(smsLog.getId(), smsLog.getPhoneNumber(), smsLog.getMessage());

        verify(smsLogRepository, times(1)).save(any(SmsLog.class));
    }

    @Test
    void testGetSmsLogsByPhoneNumberAndDateRange_Success() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);

        List<SmsLog> smsLogsList = new ArrayList<>();
        smsLogsList.add(smsLog);
        Page<SmsLog> mockPage = new PageImpl<>(smsLogsList);

        when(smsLogRepository.findByPhoneNumberAndSentAtBetween(anyString(), any(), any(), any()))
                .thenReturn(mockPage);

        Page<SmsLog> result = smsLogService.getSmsLogsByPhoneNumberAndDateRange(smsLog.getPhoneNumber(), startTime, endTime, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(smsLog.getPhoneNumber(), result.getContent().get(0).getPhoneNumber());

        verify(smsLogRepository, times(1)).findByPhoneNumberAndSentAtBetween(anyString(), any(), any(), any());
    }

    @Test
    void testGetSmsLogsByText_Success() {
        Pageable pageable = PageRequest.of(0, 10);

        List<SmsLog> smsLogsList = new ArrayList<>();
        smsLogsList.add(smsLog);
        Page<SmsLog> mockPage = new PageImpl<>(smsLogsList);

        when(smsLogRepository.findByMessagePhrase(anyString(), any()))
                .thenReturn(mockPage);

        Page<SmsLog> result = smsLogService.getSmsLogsByText("Test", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test message", result.getContent().get(0).getMessage());
        verify(smsLogRepository, times(1)).findByMessagePhrase(anyString(), any());
    }
}
