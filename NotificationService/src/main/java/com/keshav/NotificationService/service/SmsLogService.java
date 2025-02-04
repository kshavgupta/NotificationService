package com.keshav.NotificationService.service;

import com.keshav.NotificationService.model.SmsLog;
import com.keshav.NotificationService.repository.SmsLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service class responsible for managing and retrieving SMS logs.
 * This service handles the creation and retrieval of SMS logs from the `SmsLogRepository`.
 * It provides methods to save SMS log entries and search logs based on different parameters
 * like phone number, date range, or message content.
 */
@Service
public class SmsLogService {

    private final SmsLogRepository smsLogRepository;

    /**
     * Constructor to initialize the SmsLogService with the required SmsLogRepository dependency.
     * @param smsLogRepository The repository for persisting and querying SMS logs.
     */
    public SmsLogService(SmsLogRepository smsLogRepository) {
        this.smsLogRepository = smsLogRepository;
    }

    /**
     * Saves a new SMS log entry with the given details (ID, phone number, message, sent timestamp).
     * @param id The unique identifier of the SMS request.
     * @param phoneNumber The phone number that received the SMS.
     * @param message The SMS message content.
     */
    public void saveSmsLog(String id, String phoneNumber, String message) {
        SmsLog smsLog = new SmsLog();
        smsLog.setId(id);
        smsLog.setPhoneNumber(phoneNumber);
        smsLog.setMessage(message);
        smsLog.setSentAt(LocalDateTime.now());

        smsLogRepository.save(smsLog);
    }

    /**
     * Retrieves a paginated list of SMS logs for a specific phone number and within a date range.
     * @param phoneNumber The phone number to filter logs by.
     * @param start       The start of the date range for filtering.
     * @param end         The end of the date range for filtering.
     * @param page        The page number to retrieve (starting from 0).
     * @param size        The number of records per page.
     * @return A page of SMS logs matching the criteria.
     */
    public Page<SmsLog> getSmsLogsByPhoneNumberAndDateRange(
            String phoneNumber, LocalDateTime start, LocalDateTime end, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        return smsLogRepository.findByPhoneNumberAndSentAtBetween(phoneNumber, start, end, pageable);
    }

    /**
     * Retrieves a paginated list of SMS logs that contain a specific text phrase in the message.
     * @param text The text phrase to search for in the SMS message.
     * @param page The page number to retrieve (starting from 0).
     * @param size The number of records per page.
     * @return A page of SMS logs containing the text phrase.
     */
    public Page<SmsLog> getSmsLogsByText(String text, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        return smsLogRepository.findByMessagePhrase(text, pageable);
    }
}

