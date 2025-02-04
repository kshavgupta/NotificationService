package com.keshav.NotificationService.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Service to consume messages from Kafka and process SMS requests.
 * Listens to the topic `notification.send_sms` and triggers SMS processing.
 */
@Service
public class KafkaConsumerService {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final SmsProcessingService smsProcessingService;

    /**
     * Constructor for KafkaConsumerService.
     * @param smsProcessingService The service responsible for processing SMS requests.
     */
    public KafkaConsumerService(SmsProcessingService smsProcessingService) {
        this.smsProcessingService = smsProcessingService;
    }

    /**
     * Kafka listener method to consume messages from the `notification.send_sms` topic.
     * Extracts the request ID and delegates the processing to {@link SmsProcessingService}.
     * @param requestId The unique identifier of the SMS request.
     */
    @KafkaListener(topics = "notification.send_sms", groupId = "notification-group")
    public void consumeMessage(String requestId) {
        log.info("Consuming Kafka message for Request ID: {}", requestId);
        smsProcessingService.processSmsRequest(requestId);
    }
}
