package com.Tu.Tu.service;

import com.Tu.Tu.configuration.VNPayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayService {

    private final VNPayConfig vnPayConfig;

    private static final DateTimeFormatter VNPAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Tạo URL thanh toán VNPay
     */
    public String createPaymentUrl(String transactionRef, long amount,
                                   String orderInfo, String ipAddress, String returnUrlOverride) {

        // Bước 1: tạo map params, dùng TreeMap để tự sort theo key (A-Z)
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", vnPayConfig.getVersion());
        params.put("vnp_Command", vnPayConfig.getCommand());
        params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay nhân 100
        params.put("vnp_CurrCode", vnPayConfig.getCurrencyCode());
        params.put("vnp_TxnRef", transactionRef);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", vnPayConfig.getOrderType());
        params.put("vnp_Locale", vnPayConfig.getLocale());
        String returnUrl = (returnUrlOverride != null && !returnUrlOverride.isBlank())
            ? returnUrlOverride
            : vnPayConfig.getReturnUrl();
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", ipAddress);
        params.put("vnp_CreateDate", LocalDateTime.now().format(VNPAY_DATE_FORMAT));
        params.put("vnp_ExpireDate",
                LocalDateTime.now()
                        .plusMinutes(VNPayConfig.PAYMENT_TIMEOUT_MINUTES)
                        .format(VNPAY_DATE_FORMAT));

        // Bước 2: Build hashData + queryString theo sample chính thức VNPay 2.1.0:
        // - hashData: key raw + value encode US_ASCII
        // - queryString: key encode US_ASCII + value encode US_ASCII
        StringBuilder hashData = new StringBuilder();
        StringBuilder queryString = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (value == null || value.isBlank()) {
                continue;
            }

            String encodedKey = URLEncoder.encode(key, StandardCharsets.US_ASCII);
            String encodedValue = URLEncoder.encode(value, StandardCharsets.US_ASCII);

            // hashData dùng encoded value để khớp spec
            if (hashData.length() > 0) hashData.append("&");
            hashData.append(key).append("=").append(encodedValue);

            // queryString dùng encoded value
            if (queryString.length() > 0) queryString.append("&");
            queryString.append(encodedKey)
                    .append("=")
                    .append(encodedValue);
        }

        // Bước 3: Tính HMAC SHA512 trên hashData
        String secureHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());

        // Bước 4: Append hash type + hash
        queryString.append("&vnp_SecureHashType=HmacSHA512");
        queryString.append("&vnp_SecureHash=").append(secureHash);

        String url = vnPayConfig.getPayUrl() + "?" + queryString;
        log.info("VNPay URL created for txnRef={}", transactionRef);
        return url;
    }

    /**
     * Xác minh chữ ký webhook từ VNPay
     */
    public boolean verifyWebhook(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isEmpty()) {
            log.warn("VNPay webhook: thiếu vnp_SecureHash");
            return false;
        }

        // Loại bỏ vnp_SecureHash và vnp_SecureHashType trước khi tính lại hash
        Map<String, String> signParams = new TreeMap<>(params);
        signParams.remove("vnp_SecureHash");
        signParams.remove("vnp_SecureHashType");

        // Build hashData theo cùng chuẩn với lúc tạo URL
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : signParams.entrySet()) {
            String value = entry.getValue();
            if (value == null || value.isBlank()) {
                continue;
            }

            String encodedValue = URLEncoder.encode(value, StandardCharsets.US_ASCII);
            if (hashData.length() > 0) hashData.append("&");
            hashData.append(entry.getKey()).append("=").append(encodedValue);
        }

        String expectedHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        boolean valid = expectedHash.equalsIgnoreCase(receivedHash);

        if (!valid) {
            log.warn("VNPay signature mismatch. Expected: {}, Received: {}", expectedHash, receivedHash);
        }

        return valid;
    }

    /**
     * Tạo transactionRef unique
     */
    public String generateTransactionRef(Long bookingId) {
        int randomPart = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return "TU" + bookingId + System.currentTimeMillis() + randomPart;
        // Giữ ngắn gọn, vẫn an toàn dưới giới hạn 100 ký tự của VNPay
    }

    /**
     * HMAC SHA512
     */
    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo chữ ký HMAC SHA512", e);
        }
    }
}