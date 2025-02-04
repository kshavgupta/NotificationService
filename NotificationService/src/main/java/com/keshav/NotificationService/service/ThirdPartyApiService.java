package com.keshav.NotificationService.service;

import com.keshav.NotificationService.dto.ThirdPartyApiChannels;
import com.keshav.NotificationService.dto.ThirdPartyApiDestination;
import com.keshav.NotificationService.dto.ThirdPartyApiSms;
import com.keshav.NotificationService.dto.ThirdPartyApiSmsRequestBody;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.Random;

/**
 * Service for interacting with a third-party API to send SMS messages.
 * Uses a {@link WebClient} to send HTTP requests to the external API.
 */
@Service
public class ThirdPartyApiService {
    private final WebClient webClient;
    private final Random random;

    /**
     * Constructs a ThirdPartyApiService with the necessary WebClient dependency.
     * @param webClient The WebClient instance for making HTTP requests.
     */
    public ThirdPartyApiService(WebClient webClient) {
        this.webClient = webClient;
        this.random = new Random();
    }

    /**
     * Sends an SMS message to a specified phone number via a third-party API.
     * @param message The message content to be sent.
     * @param phoneNumber The recipient's phone number.
     * @param correlationId A unique identifier for tracking the message.
     * @return {@code true} if the SMS was sent successfully, otherwise {@code false}.
     */
    public boolean sendSms(String message, String phoneNumber, String correlationId) {
        try {
            ThirdPartyApiSmsRequestBody requestBody = createRequestBody(message, phoneNumber, correlationId);
            String response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Simulate API success or failure using a random boolean.
            return random.nextBoolean();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Creates a request body for sending an SMS message.
     * @param message The SMS text message.
     * @param phoneNumber The recipient's phone number.
     * @param correlationId A unique identifier for tracking the request.
     * @return A {@link ThirdPartyApiSmsRequestBody} containing the formatted request.
     */
    private ThirdPartyApiSmsRequestBody createRequestBody(String message, String phoneNumber, String correlationId) {
        ThirdPartyApiSmsRequestBody requestBody = new ThirdPartyApiSmsRequestBody();
        requestBody.setDeliveryChannel("sms");

        ThirdPartyApiChannels channels = new ThirdPartyApiChannels();
        ThirdPartyApiSms sms = new ThirdPartyApiSms();
        sms.setText(message);
        channels.setSms(sms);
        requestBody.setChannels(channels);

        ThirdPartyApiDestination destination = new ThirdPartyApiDestination();
        destination.setMsisdn(Collections.singletonList(phoneNumber));
        destination.setCorrelationId(correlationId);

        requestBody.setDestination(Collections.singletonList(destination));
        return requestBody;
    }
}
