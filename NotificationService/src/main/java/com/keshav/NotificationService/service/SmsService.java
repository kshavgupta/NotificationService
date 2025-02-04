package com.keshav.NotificationService.service;

import com.keshav.NotificationService.dto.SmsRequestDto;
import com.keshav.NotificationService.model.SmsRequest;
import com.keshav.NotificationService.repository.SmsRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Service class responsible for handling SMS requests.
 * This service creates, updates, and processes SMS requests by interacting with the
 * `SmsRequestRepository` for persistence and sending messages to Kafka for notification processing.
 * Methods include creating SMS requests, handling failures in the Kafka message sending process, and updating the status of the SMS requests.
 */
@Service
public class SmsService {
    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    private final SmsRequestRepository smsRequestRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Constructor to initialize the SmsService with necessary dependencies.
     * @param smsRequestRepository The repository to save and retrieve SMS requests.
     * @param kafkaTemplate The Kafka template to send messages to the notification service.
     */
    public SmsService(SmsRequestRepository smsRequestRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.smsRequestRepository = smsRequestRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Sends an SMS request by creating a new SMS request record, saving it to the repository,
     * and sending the request ID to Kafka for processing.
     * @param smsRequestDto The DTO containing information for the SMS request (phone number and message).
     * @return The unique request ID for the SMS request.
     */
    public String sendSms(SmsRequestDto smsRequestDto) {
        String requestId = UUID.randomUUID().toString();

        log.info("Creating a new SMS request: [Request ID: {}, PhoneNumber: {}]", requestId, smsRequestDto.getPhoneNumber());
        SmsRequest smsRequest = new SmsRequest();
        smsRequest.setId(requestId);
        smsRequest.setPhoneNumber(smsRequestDto.getPhoneNumber());
        smsRequest.setMessage(smsRequestDto.getMessage());
        smsRequest.setStatus("PENDING");
        smsRequestRepository.save(smsRequest);
        log.info("SMS request saved with status 'PENDING': [Request ID: {}]", requestId);

        try {
            kafkaTemplate.send("notification.send_sms", requestId);
            log.info("Message sent to Kafka for Request ID: {}", requestId);
        } catch (Exception e) {
            String failureCode = "KAFKA_FAILURE";
            String failureComments = "Failed to publish message to Kafka.";
            log.error("Kafka Publish Error for Request ID: {} - {}", requestId, e.getMessage(), e);

            updateSmsRequestStatusWithFailure(requestId, "FAILED", failureCode, failureComments);
        }

        return requestId;
    }

    /**
     * Retrieves an SMS request by its unique request ID.
     * @param requestId The unique identifier for the SMS request.
     * @return The SMS request associated with the given request ID.
     * @throws IllegalArgumentException if no SMS request is found for the given request ID.
     */
    public SmsRequest getSmsRequestById(String requestId) {
        Optional<SmsRequest> smsRequest = smsRequestRepository.findById(requestId);
        return smsRequest.orElseThrow(() -> {
            log.error("Failed to find SMS request with: [Request ID: {}]", requestId);
            return new IllegalArgumentException(requestId + " not found");
        });
    }

    /**
     * Updates the status of an SMS request.
     * @param requestId The unique identifier of the SMS request.
     * @param status    The new status to set for the SMS request.
     */
    public void updateSmsRequestStatus(String requestId, String status) {
        SmsRequest smsRequest = getSmsRequestById(requestId);

        smsRequest.setStatus(status);
        smsRequestRepository.save(smsRequest);
        log.info("Successfully updated status to '{}': [Request ID: {}]", status, requestId);
    }

    /**
     * Updates the status and includes failure details for an SMS request.
     * @param requestId The unique identifier of the SMS request.
     * @param status The new status to set.
     * @param failureCode The failure code that describes the failure type.
     * @param failureComments Additional comments regarding the failure.
     */
    public void updateSmsRequestStatusWithFailure(String requestId, String status, String failureCode, String failureComments) {
        SmsRequest smsRequest = getSmsRequestById(requestId);

        smsRequest.setStatus(status);
        smsRequest.setFailureCode(failureCode);
        smsRequest.setFailureComments(failureComments);
        smsRequestRepository.save(smsRequest);
        log.info("Successfully updated SMS request with failure details: [Request ID: {}, Status: {}, FailureCode: {}, Comments: {}]",
                requestId, status, failureCode, failureComments);
    }

}
