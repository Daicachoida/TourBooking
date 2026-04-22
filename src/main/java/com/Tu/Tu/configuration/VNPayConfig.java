package com.Tu.Tu.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class VNPayConfig {

    @Value("${vnpay.tmn-code}")
    String tmnCode;

    @Value("${vnpay.hash-secret}")
    String hashSecret;

    @Value("${vnpay.url}")
    String payUrl;

    @Value("${vnpay.return-url}")
    String returnUrl;

    @Value("${vnpay.version:2.1.0}")
    String version;

    @Value("${vnpay.command:pay}")
    String command;

    @Value("${vnpay.order-type:other}")
    String orderType;

    @Value("${vnpay.locale:vn}")
    String locale;

    @Value("${vnpay.currency-code:VND}")
    String currencyCode;

    // Timeout giữ chỗ (phút)
    public static final int PAYMENT_TIMEOUT_MINUTES = 15;
}