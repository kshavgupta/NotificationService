package com.keshav.NotificationService.controller;

import com.keshav.NotificationService.model.SmsLog;
import com.keshav.NotificationService.service.SmsLogService;
import com.keshav.NotificationService.utils.ErrorResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SmsLogController {
    private static final Logger log = LoggerFactory.getLogger(SmsLogController.class);
    private final SmsLogService smsLogService;

    public SmsLogController(SmsLogService smsLogService) {
        this.smsLogService = smsLogService;
    }

    @GetMapping("/v1/sms-logs")
    public ResponseEntity<?> getSmsLogs(
            @RequestParam String phoneNumber,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Received request to fetch SMS logs for PhoneNumber: {}, StartTime: {}, EndTime: {}, Page: {}, Size: {}",
                phoneNumber, startTime, endTime, page, size);


        try {
            // Convert request parameters to LocalDateTime
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime start = LocalDateTime.parse(startTime, formatter);
            LocalDateTime end = LocalDateTime.parse(endTime, formatter);

            // Fetch paginated results
            Page<SmsLog> smsLogs = smsLogService.getSmsLogsByPhoneNumberAndDateRange(phoneNumber, start, end, page, size);

            log.info("Fetched {} SMS logs for PhoneNumber: {}", smsLogs.getTotalElements(), phoneNumber);
            return getResponseEntity(smsLogs);
        } catch (Exception e) {
            log.error("Error occurred while fetching SMS logs: {}", e.getMessage(), e);
            return ErrorResponseUtil.getErrorResponseEntity("Unable to fetch SMS logs between " + startTime + " and " + endTime + ". Please try again later.");
        }
    }

    @GetMapping("/v1/sms-logs/search")
    public ResponseEntity<?> getSmsLogsByPhoneNumber(@RequestParam String text, @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to search SMS logs containing text: '{}', Page: {}, Size: {}", text, page, size);

        try {
            Page<SmsLog> smsLogs = smsLogService.getSmsLogsByText(text, page, size);

            log.info("Fetched {} SMS logs containing text: '{}'", smsLogs.getTotalElements(), text);
            return getResponseEntity(smsLogs);
        } catch (Exception e) {
            log.error("Error occurred while searching SMS logs: {}", e.getMessage(), e);
            return ErrorResponseUtil.getErrorResponseEntity("Unable to fetch SMS logs with text '" + text + "'. Please try again later.");
        }
    }

    private ResponseEntity<?> getResponseEntity(Page<SmsLog> smsLogs) {
        Map<String, Object> response = new HashMap<>();
        response.put("data", smsLogs.getContent());

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("currentPage", smsLogs.getNumber());
        pagination.put("pageSize", smsLogs.getSize());
        pagination.put("totalPages", smsLogs.getTotalPages());
        pagination.put("totalItems", smsLogs.getTotalElements());

        response.put("pagination", pagination);

        return ResponseEntity.ok().body(response);
    }
}
