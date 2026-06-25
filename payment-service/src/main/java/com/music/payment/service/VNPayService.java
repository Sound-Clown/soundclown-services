package com.music.payment.service;

import com.music.common.exception.AppException;
import com.music.common.exception.ErrorCode;
import com.music.payment.config.VnpayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Builds and verifies VNPay (v2.1.0) requests. Signing follows VNPay's official Java sample:
 * sort params by name, build "name=urlencode(value)&..." and HMAC-SHA512 it with the merchant
 * hash secret. Verification recomputes the hash over the returned params (minus the hash itself)
 * and compares — that signature is what authenticates the otherwise-unauthenticated callback.
 */
@Service
@RequiredArgsConstructor
public class VNPayService {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VnpayProperties props;

    /** Fails with a clear error if VNPay credentials aren't configured (e.g. VNP_HASH_SECRET unset). */
    public void ensureConfigured() {
        if (!StringUtils.hasText(props.getTmnCode()) || !StringUtils.hasText(props.getHashSecret())) {
            throw new AppException(ErrorCode.PAYMENT_NOT_CONFIGURED);
        }
    }

    /** Builds the VNPay redirect URL for an order. amountVnd is the plain VND amount (e.g. 50000). */
    public String buildPaymentUrl(String txnRef, long amountVnd, String orderInfo, String clientIp) {
        ensureConfigured();
        ZonedDateTime now = ZonedDateTime.now(VN_ZONE);

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", props.getVersion());
        params.put("vnp_Command", props.getCommand());
        params.put("vnp_TmnCode", props.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amountVnd * 100)); // VNPay expects amount * 100
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", props.getOrderType());
        params.put("vnp_Locale", props.getLocale() == null || props.getLocale().isBlank() ? "vn" : props.getLocale());
        params.put("vnp_ReturnUrl", props.getReturnUrl());
        params.put("vnp_IpAddr", clientIp == null || clientIp.isBlank() ? "127.0.0.1" : clientIp);
        params.put("vnp_CreateDate", now.format(DATE_FORMAT));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(DATE_FORMAT));

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            String value = e.getValue();
            if (value == null || value.isEmpty()) {
                continue;
            }
            String encoded = URLEncoder.encode(value, StandardCharsets.US_ASCII);
            if (hashData.length() > 0) {
                hashData.append('&');
                query.append('&');
            }
            hashData.append(e.getKey()).append('=').append(encoded);
            query.append(URLEncoder.encode(e.getKey(), StandardCharsets.US_ASCII)).append('=').append(encoded);
        }

        String secureHash = hmacSHA512(props.getHashSecret(), hashData.toString());
        return props.getPayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    /** Verifies the vnp_SecureHash of a returned/IPN parameter map. */
    public boolean isValidSignature(Map<String, String> allParams) {
        Map<String, String> fields = new HashMap<>(allParams);
        String received = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        if (received == null) {
            return false;
        }

        List<String> names = new ArrayList<>(fields.keySet());
        names.sort(String::compareTo);
        StringBuilder hashData = new StringBuilder();
        for (String name : names) {
            String value = fields.get(name);
            if (value == null || value.isEmpty()) {
                continue;
            }
            if (hashData.length() > 0) {
                hashData.append('&');
            }
            hashData.append(name).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
        }

        String expected = hmacSHA512(props.getHashSecret(), hashData.toString());
        return expected.equalsIgnoreCase(received);
    }

    private static String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to compute HMAC-SHA512", ex);
        }
    }
}
