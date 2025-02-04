package com.keshav.NotificationService.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter @Setter
public class BlacklistRequestDto {
    @NotEmpty(message = "Phone Numbers list cannot be empty.")
    private List<String> phoneNumbers;

}
