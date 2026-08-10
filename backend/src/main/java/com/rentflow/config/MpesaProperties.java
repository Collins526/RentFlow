package com.rentflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.mpesa")
public class MpesaProperties {

    private String baseUrl;
    private String consumerKey;
    private String consumerSecret;
    private String shortCode;
    private String passkey;
    private String callbackUrl;
    private String environment = "sandbox";
    private String accountReference = "RentFlow";
    private String transactionDesc = "Rent payment";
}
