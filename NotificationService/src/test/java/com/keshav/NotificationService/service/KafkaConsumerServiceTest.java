package com.keshav.NotificationService.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaConsumerServiceTest {

    @Mock
    private SmsProcessingService smsProcessingService;

    @InjectMocks
    private KafkaConsumerService kafkaConsumerService;

    @Test
    public void testConsumeMessage() {
        String requestId = "12345678";
        kafkaConsumerService.consumeMessage(requestId);
        verify(smsProcessingService, times(1)).processSmsRequest(requestId);
    }
}