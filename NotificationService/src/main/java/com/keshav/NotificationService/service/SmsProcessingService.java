package com.keshav.NotificationService.service;

import com.keshav.NotificationService.model.SmsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service responsible for processing SMS requests.
 * It performs validation, checks for blacklisted numbers, sends SMS via a third-party API,
 * and logs successful requests to Elasticsearch.
 */
@Service
public class SmsProcessingService {
    private static final Logger log = LoggerFactory.getLogger(SmsProcessingService.class);

    private final SmsService smsService;
    private final BlacklistService blacklistService;
    private final ThirdPartyApiService thirdPartyApiService;
    private final SmsLogService smsLogService;

    /**
     * Constructor for SmsProcessingService.
     * @param smsService Service to manage SMS requests.
     * @param blacklistService Service to check if a phone number is blacklisted.
     * @param thirdPartyApiService Service to send SMS through a third-party API.
     * @param smsLogService Service to log sent SMS messages to Elasticsearch.
     */
    public SmsProcessingService(SmsService smsService, BlacklistService blacklistService,
                                ThirdPartyApiService thirdPartyApiService, SmsLogService smsLogService) {
        this.smsService = smsService;
        this.blacklistService = blacklistService;
        this.thirdPartyApiService = thirdPartyApiService;
        this.smsLogService = smsLogService;
    }

    /**
     * Processes an SMS request by validating the request ID, checking for blacklisting,
     * sending the SMS, updating the request status, and logging the SMS if successful.
     * @param requestId The unique identifier of the SMS request.
     */
    public void processSmsRequest(String requestId) {

        try {
            // Fetch SMS request details from DB
            SmsRequest smsRequest = smsService.getSmsRequestById(requestId);
            String phoneNumber = smsRequest.getPhoneNumber();
            String id = smsRequest.getId();
            String message = smsRequest.getMessage();
            log.info("Fetched SMS Request: [Request ID: {}, Phone Number: {}]", requestId, phoneNumber);

            // Check if the phone number is blacklisted
            if (blacklistService.isBlacklisted(phoneNumber)) {
                log.warn("Phone number is blacklisted: [Phone Number: {}, Request ID: {}]", phoneNumber, requestId);
                smsService.updateSmsRequestStatusWithFailure(requestId, "FAILED", "BLACKLISTED", "Phone number is blacklisted.");
                return;
            }

            // Send SMS using the third-party API
            boolean success = thirdPartyApiService.sendSms(phoneNumber, id, message);
            if (success) {
                log.info("SMS sent successfully: [Request ID: {}, Phone Number: {}]", requestId, phoneNumber);
                smsService.updateSmsRequestStatus(requestId, "SENT");
            } else {
                log.error("Failed to send SMS: [Request ID: {}, Phone Number: {}]", requestId, phoneNumber);
                smsService.updateSmsRequestStatusWithFailure(requestId, "FAILED", "API_ERROR", "Failed to send SMS.");
                return;
            }

            // Save SMS log to Elasticsearch
            try {
                smsLogService.saveSmsLog(requestId, phoneNumber, message);
                log.info("Saved SMS log to Elasticsearch: [Request ID: {}, Phone Number: {}]", requestId, phoneNumber);
            } catch (Exception e) {
                log.error("Failed to save SMS log to Elasticsearch for Request ID: {}. Error: {}", requestId, e.getMessage(), e);
            }

        } catch (IllegalArgumentException e) {
            log.error("Invalid Request ID: {}", requestId, e);
        } catch (Exception e) {
            log.error("Unexpected error occurred while processing Request ID: {}", requestId, e);
        }

        log.info("Finished processing Kafka message for Request ID: {}", requestId);
    }
}
