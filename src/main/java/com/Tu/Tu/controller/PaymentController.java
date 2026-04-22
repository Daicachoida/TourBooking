package com.Tu.Tu.controller;

import com.Tu.Tu.dto.request.PaymentCreateRequest;
import com.Tu.Tu.dto.response.ApiResponse;
import com.Tu.Tu.dto.response.PageResponse;
import com.Tu.Tu.dto.response.PaymentResponse;
import com.Tu.Tu.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final PaymentService paymentService;

    // ===== USER =====

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<PaymentResponse> createPayment(@RequestBody @Valid PaymentCreateRequest request) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.createPayment(request))
                .message("success")
                .build();
    }

    /**
     * Tạo URL thanh toán VNPay
     * POST /payments/vnpay/create?bookingId=1
     * Response: { paymentUrl: "https://sandbox.vnpayment.vn/..." }
     */
    @PostMapping("/vnpay/create")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<PaymentResponse> createVNPayPayment(
            @RequestParam Long bookingId,
            HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        if (ipAddress == null || ipAddress.isBlank()) {
            ipAddress = "127.0.0.1";
        }
        return ApiResponse.<PaymentResponse>builder()
            .result(paymentService.createVNPayPayment(bookingId, ipAddress, null))
                .message("success")
                .build();
    }

    /**
     * VNPay gọi về sau khi thanh toán (return URL)
     * GET /payments/vnpay/webhook?vnp_ResponseCode=00&vnp_TxnRef=...&vnp_SecureHash=...
     * Endpoint này KHÔNG cần auth vì VNPay server gọi về
     */
    @GetMapping("/vnpay/webhook")
    ApiResponse<PaymentResponse> handleVNPayWebhook(@RequestParam Map<String, String> params) {
        log.info("VNPay webhook received: {}", params);
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.handleVNPayWebhook(params))
                .message("success")
                .build();
    }

    @GetMapping("/my-payments")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<PageResponse<PaymentResponse>> getMyPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<PaymentResponse>>builder()
                .result(paymentService.getMyPayments(page, size))
                .message("success")
                .build();
    }

    @GetMapping("/my-payments/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<PaymentResponse> getMyPaymentById(@PathVariable Long id) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.getMyPaymentById(id))
                .message("success")
                .build();
    }

    @GetMapping("/my-payments/booking/{bookingId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<PageResponse<PaymentResponse>> getMyPaymentsByBooking(
            @PathVariable Long bookingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<PaymentResponse>>builder()
                .result(paymentService.getMyPaymentsByBooking(bookingId, page, size))
                .message("success")
                .build();
    }

    @PutMapping("/my-payments/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<PaymentResponse> cancelPayment(@PathVariable Long id) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.cancelPayment(id))
                .message("success")
                .build();
    }

    // ===== BUSINESS =====

    @GetMapping("/business")
    @PreAuthorize("hasAuthority('ROLE_BUSINESS')")
    ApiResponse<PageResponse<PaymentResponse>> getPaymentsByMyTours(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<PaymentResponse>>builder()
                .result(paymentService.getPaymentsByMyTours(keyword, page, size))
                .message("success")
                .build();
    }

    @GetMapping("/business/{id}")
    @PreAuthorize("hasAuthority('ROLE_BUSINESS')")
    ApiResponse<PaymentResponse> getPaymentByIdForBusiness(@PathVariable Long id) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.getPaymentByIdForBusiness(id))
                .message("success")
                .build();
    }

    @PutMapping("/business/{id}/confirm")
    @PreAuthorize("hasAnyAuthority('ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<PaymentResponse> confirmPayment(@PathVariable Long id) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.confirmPayment(id))
                .message("success")
                .build();
    }

    @PutMapping("/business/{id}/refund")
    @PreAuthorize("hasAnyAuthority('ROLE_BUSINESS', 'ROLE_ADMIN')")
    ApiResponse<PaymentResponse> refundPayment(@PathVariable Long id) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.refundPayment(id))
                .message("success")
                .build();
    }

    // ===== ADMIN =====

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<PageResponse<PaymentResponse>> getAllPayments(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<PaymentResponse>>builder()
                .result(paymentService.getAllPayments(keyword, page, size))
                .message("success")
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<PaymentResponse> getPaymentById(@PathVariable Long id) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.getPaymentById(id))
                .message("success")
                .build();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<PageResponse<PaymentResponse>> getPaymentsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<PaymentResponse>>builder()
                .result(paymentService.getPaymentsByUser(userId, page, size))
                .message("success")
                .build();
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<PageResponse<PaymentResponse>> getPaymentsByBooking(
            @PathVariable Long bookingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<PaymentResponse>>builder()
                .result(paymentService.getPaymentsByBooking(bookingId, page, size))
                .message("success")
                .build();
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<PaymentResponse> confirmPaymentByAdmin(@PathVariable Long id) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.confirmPaymentByAdmin(id))
                .message("success")
                .build();
    }

    @PutMapping("/{id}/refund")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<PaymentResponse> refundPaymentByAdmin(@PathVariable Long id) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.refundPaymentByAdmin(id))
                .message("success")
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ApiResponse.<Void>builder().message("success").build();
    }

}