package com.keshav.NotificationService.controller;

import com.keshav.NotificationService.dto.SmsRequestDto;
import com.keshav.NotificationService.model.SmsRequest;
import com.keshav.NotificationService.service.SmsService;
import com.keshav.NotificationService.utils.ErrorResponseUtil;
import com.keshav.NotificationService.utils.ValidationErrorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/sms")
public class SmsController {
    Logger log = LoggerFactory.getLogger(SmsController.class);

    private final SmsService smsService;

    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendSms(@Valid @RequestBody SmsRequestDto smsRequestDto, BindingResult bindingResult) {
        log.info("Received API request: [Endpoint: /v1/sms/send, PhoneNumber: {}]", smsRequestDto.getPhoneNumber());

        ResponseEntity<?> errorResponse = ValidationErrorUtil.handleValidationErrors(bindingResult);
        if(errorResponse != null){
            return errorResponse;
        }

        try {
            String requestId = smsService.sendSms(smsRequestDto);

            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("requestId", requestId);
            dataMap.put("comments", "Successfully processed request");

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("data", dataMap);

            return ResponseEntity.ok().body(responseMap);
        } catch (Exception e) {
            log.warn("Error while processing request: {}", e.getMessage(), e);
            return ErrorResponseUtil.getErrorResponseEntity("Failed to process request. Please try again later");
        }
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<?> getSmsRequestById(@PathVariable String requestId) {
        log.info("Received API request: [Endpoint: /v1/sms/{requestId}, RequestId: {}]", requestId);

        try {
            SmsRequest smsRequest = smsService.getSmsRequestById(requestId);

            Map<String, Object> response = new HashMap<>();
            response.put("data", smsRequest);

            log.info("Retrieved SMS request details: [Request ID: {}]", requestId);
            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            Map<String, String> errorDetails = new HashMap<>();
            errorDetails.put("code", "RESOURCE_NOT_FOUND");
            errorDetails.put("message", e.getMessage());
            errorResponse.put("error", errorDetails);

            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            log.warn("Error while fetching request: {}", e.getMessage(), e);
            return ErrorResponseUtil.getErrorResponseEntity("Failed to process request. Please try again later");
        }
    }

}