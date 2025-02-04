package com.keshav.NotificationService.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class ThirdPartyApiDestination {
    private List<String> msisdn;
    private String correlationId;
}
