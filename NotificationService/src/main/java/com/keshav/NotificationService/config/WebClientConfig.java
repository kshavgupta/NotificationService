package com.keshav.NotificationService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl("https://api.imiconnect.in/resources/v1/messaging")
                .defaultHeader("Key", "93ceffda-5941-11ea-9da9-025282c394f2") // Set the required API key
                .build();
    }
}
