package com.music.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {
    private String secretKey;
    // BE endpoint Stripe redirects the browser back to (success & cancel) — reached via the gateway.
    private String returnUrl;
    // Amount in the currency's smallest unit (e.g. cents). 200 + usd = $2.00.
    private long priceCents;
    private String currency;
    private String productName;
}
