package com.Tu.Tu.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),

    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_PRICE_RANGE(1008, "minPrice must be less than or equal to maxPrice", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE(1009, "fromDate must be before or equal to toDate", HttpStatus.BAD_REQUEST),

    PASSWORD_NOT_MATCH(1101, "Password not match", HttpStatus.BAD_REQUEST),

    ROLE_NOT_FOUND(1201, "Role not existed", HttpStatus.NOT_FOUND),

    EMAIL_NOT_VERIFIED(1301, "Email not verified", HttpStatus.FORBIDDEN),
    INVALID_VERIFICATION_TOKEN(1302, "Invalid or expired verification token", HttpStatus.BAD_REQUEST),
    VERIFICATION_TOKEN_EXPIRED(1303, "Verification token expired", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_VERIFIED(1304, "Email already verified", HttpStatus.BAD_REQUEST),

    TOUR_NOT_FOUND(1401, "Tour not found", HttpStatus.NOT_FOUND),
    TOUR_CODE_EXISTED(1402, "Tour code already existed", HttpStatus.BAD_REQUEST),
    TOUR_HAS_BOOKING(1403, "Tour has existing bookings, cannot delete", HttpStatus.BAD_REQUEST),
    TOUR_HAS_ACTIVE_DEPARTURE(1404, "Tour has active or future departures, cannot hard-delete", HttpStatus.BAD_REQUEST),
    TOUR_HAS_BOOKING_HISTORY(1405, "Tour has booking history, cannot hard-delete", HttpStatus.BAD_REQUEST),
    TOUR_CANNOT_HARD_DELETE(1406, "Tour cannot be hard-deleted due to business constraints", HttpStatus.BAD_REQUEST),
    TOUR_ALREADY_INACTIVE(1407, "Tour is already inactive", HttpStatus.BAD_REQUEST),

    DEPARTURE_NOT_FOUND(1501, "Departure not found", HttpStatus.NOT_FOUND),
    DEPARTURE_ALREADY_BOOKED(1502, "Cannot change departure date when bookings exist", HttpStatus.BAD_REQUEST),
    CAPACITY_LESS_THAN_BOOKED(1503, "Capacity cannot be less than booked seats", HttpStatus.BAD_REQUEST),
    DEPARTURE_HAS_CONFIRMED_BOOKING(1504, "Cannot cancel departure with confirmed bookings — refund required", HttpStatus.BAD_REQUEST),
    DEPARTURE_ALREADY_DEPARTED(1505, "Departure has already departed, cannot modify", HttpStatus.BAD_REQUEST),
    DEPARTURE_FULL(1506, "Departure is full", HttpStatus.BAD_REQUEST),
    DEPARTURE_HAS_BOOKING(1507, "Departure has existing bookings, cannot perform this action", HttpStatus.BAD_REQUEST),
    DEPARTURE_ALREADY_STARTED(1508, "Departure has already started — use cancel instead of delete", HttpStatus.BAD_REQUEST),
    DEPARTURE_ALREADY_CANCELLED(1509, "Departure is already cancelled", HttpStatus.BAD_REQUEST),
    DEPARTURE_CANNOT_DELETE_WITH_BOOKINGS(1510, "Cannot hard-delete departure with existing bookings — use cancel instead", HttpStatus.BAD_REQUEST),
    DEPARTURE_INVALID_STATUS_TRANSITION(1511, "Invalid status transition for this departure", HttpStatus.BAD_REQUEST),

    PASSENGER_TYPE_NOT_FOUND(1601, "Passenger type not found", HttpStatus.NOT_FOUND),
    PASSENGER_TYPE_EXISTED(1602, "Passenger type already existed", HttpStatus.BAD_REQUEST),

    DEPARTURE_PRICE_NOT_FOUND(1701, "Departure price not found", HttpStatus.NOT_FOUND),

    BOOKING_NOT_FOUND(1801, "Booking not found", HttpStatus.NOT_FOUND),
    BOOKING_CANNOT_CANCEL(1802, "Booking cannot be cancelled", HttpStatus.BAD_REQUEST),
    BOOKING_PRICE_NOT_FOUND(1803, "Price not found for passenger type", HttpStatus.BAD_REQUEST),
    BOOKING_ALREADY_CONFIRMED(1804, "The booking has been confirmed and cannot be modified", HttpStatus.BAD_REQUEST),
    BOOKING_NOT_PAID(1805, "Booking has not been paid yet", HttpStatus.BAD_REQUEST),
    BOOKING_INVALID_STATUS(1806, "Booking is not in a valid status for this action", HttpStatus.BAD_REQUEST),

    PAYMENT_NOT_FOUND(1901, "Payment not found", HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_PAID(1902, "Booking has already been paid successfully", HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_PENDING(1903, "Booking already has a pending payment", HttpStatus.BAD_REQUEST),
    PAYMENT_CANNOT_CONFIRM(1904, "Payment is not in pending status, cannot confirm", HttpStatus.BAD_REQUEST),
    PAYMENT_CANNOT_CANCEL(1905, "Payment is not in pending status, cannot cancel", HttpStatus.BAD_REQUEST),
    PAYMENT_CANNOT_REFUND(1906, "Payment is not in success status, cannot refund", HttpStatus.BAD_REQUEST),
    PAYMENT_CANNOT_DELETE(1907, "Payment with success or refunded status cannot be deleted", HttpStatus.BAD_REQUEST),
    VNPAY_INVALID_SIGNATURE(1908, "VNPay signature is invalid", HttpStatus.BAD_REQUEST),

    REVIEW_NOT_FOUND(2001, "Review not found", HttpStatus.NOT_FOUND),
    REVIEW_ALREADY_EXISTED(2002, "You have already reviewed this tour", HttpStatus.BAD_REQUEST),
    REVIEW_NOT_ELIGIBLE(2003, "You must have a confirmed booking to review this tour", HttpStatus.BAD_REQUEST),
    REVIEW_UNAUTHORIZED(2004, "You do not have permission to modify this review", HttpStatus.FORBIDDEN),
    INVALID_RATING(2005, "Rating must be between 1 and 5", HttpStatus.BAD_REQUEST),
    ;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;
}
