package com.keshav.NotificationService.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ValidationErrorUtil {
    private static final Logger log = LoggerFactory.getLogger(ValidationErrorUtil.class);

    private ValidationErrorUtil() {}

    public static ResponseEntity<?> handleValidationErrors(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, Object> errorResponse = new HashMap<>();
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("code", "BAD_REQUEST");

            for (FieldError error : bindingResult.getFieldErrors()) {
                errorMap.put("message", error.getDefaultMessage());
                log.error("Validation Errors : {}", error.getDefaultMessage());
                break;
            }

            errorResponse.put("error", errorMap);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        return null;
    }
}
