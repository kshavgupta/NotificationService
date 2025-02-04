package com.keshav.NotificationService.utils;

import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ErrorResponseUtil {
    private ErrorResponseUtil() {}

    public static ResponseEntity<?> getErrorResponseEntity(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("code", "INTERNAL_SERVER_ERROR");
        errorDetails.put("message", message);
        errorResponse.put("error", errorDetails);
        return ResponseEntity.internalServerError().body(errorResponse);
    }
}
