package com.example.shared.messaging;

/** Nombres de los topics Kafka del sistema, para no repetir strings en cada servicio. */
public final class Topics {

    public static final String BOOKING_CREATED = "tickets.booking.created";
    public static final String ORDER_EXPIRED = "tickets.order.expired";
    public static final String PAYMENT_SUCCESS = "tickets.payment.success";
    public static final String QR_READY = "tickets.qr.ready";
    public static final String QR_TRANSFER = "tickets.qr.transfer";

    private Topics() {
    }
}
