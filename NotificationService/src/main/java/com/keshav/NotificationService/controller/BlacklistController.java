package com.keshav.NotificationService.controller;

import com.keshav.NotificationService.dto.BlacklistRequestDto;
import com.keshav.NotificationService.service.BlacklistService;
import com.keshav.NotificationService.utils.ErrorResponseUtil;
import com.keshav.NotificationService.utils.ValidationErrorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/v1/blacklist")
public class BlacklistController {
    private static final Logger log = LoggerFactory.getLogger(BlacklistController.class);
    private final BlacklistService blacklistService;

    public BlacklistController(BlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @PostMapping
    public ResponseEntity<?> addToBlacklist(@Valid @RequestBody BlacklistRequestDto blacklistRequestDto, BindingResult bindingResult) {
        log.info("Received request to add numbers to blacklist: {}", blacklistRequestDto.getPhoneNumbers());

        ResponseEntity<?> errorResponse = ValidationErrorUtil.handleValidationErrors(bindingResult);
        if(errorResponse != null){
            return errorResponse;
        }

        try {
            blacklistService.addToBlacklist(blacklistRequestDto);

            Map<String, String> response = new HashMap<>();
            response.put("data", "Successfully blacklisted");
            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            log.error("Error adding numbers to blacklist: {}", e.getMessage(), e);
            return ErrorResponseUtil.getErrorResponseEntity("Failed to blacklist numbers. Please try again later.");
        }
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity<?> removeFromBlacklist(@Valid @RequestBody BlacklistRequestDto blacklistRequestDto, BindingResult bindingResult) {
        log.info("Received request to remove numbers from blacklist: {}", blacklistRequestDto.getPhoneNumbers());

        ResponseEntity<?> errorResponse = ValidationErrorUtil.handleValidationErrors(bindingResult);
        if(errorResponse != null){
            return errorResponse;
        }

        try {
            blacklistService.removeFromBlacklist(blacklistRequestDto);

            Map<String, String> response = new HashMap<>();
            response.put("data", "Successfully whitelisted");
            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            log.error("Error removing numbers from blacklist: {}", e.getMessage(), e);
            return ErrorResponseUtil.getErrorResponseEntity("Failed to whitelist numbers. Please try again later.");
        }
    }

    @GetMapping
    public ResponseEntity<?> getBlacklist() {
        log.info("Received request to fetch the list of blacklisted numbers.");

        try {
            List<String> blacklistedNumbers = blacklistService.getBlacklist();
            log.info("Returning {} blacklisted numbers.", blacklistedNumbers.size());

            Map<String, List<String>> blacklistMap = new HashMap<>();
            blacklistMap.put("data", blacklistedNumbers);
            return ResponseEntity.ok().body(blacklistMap);

        } catch (Exception e) {
            log.error("Error fetching blacklist: {}", e.getMessage(), e);
            return ErrorResponseUtil.getErrorResponseEntity("Failed to fetch blacklist. Please try again later.");
        }
    }
}
