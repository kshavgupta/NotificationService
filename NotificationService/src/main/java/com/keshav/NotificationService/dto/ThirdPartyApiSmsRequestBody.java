package com.keshav.NotificationService.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ThirdPartyApiSmsRequestBody {
    private String deliveryChannel;
    private ThirdPartyApiChannels channels;
    private List<ThirdPartyApiDestination> destination;

}
