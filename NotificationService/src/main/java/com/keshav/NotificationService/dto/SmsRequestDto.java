package com.keshav.NotificationService.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class SmsRequestDto {

    @NotBlank(message = "Phone number is mandatory")
    @Pattern(regexp = "^\\+91[6-9][0-9]{9}$", message = "Phone number must start with +91 and must be of 10 digits")
    private String phoneNumber;

    @NotBlank(message = "Message content cannot be empty.")
    private String message;
}

