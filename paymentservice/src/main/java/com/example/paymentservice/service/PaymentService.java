package com.example.paymentservice.service;

import com.example.paymentservice.request.ProductRequest;
import com.example.paymentservice.response.StripeResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Value("${stripe.secretKey}")
    private String secretKey;

    public StripeResponse checkoutProducts(ProductRequest productRequest) {
        Stripe.apiKey = secretKey;

        SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(productRequest.name())
                .build();

        SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency(productRequest.currency() == null ? "usd" : productRequest.currency())
                .setUnitAmount(productRequest.amount())
                .setProductData(productData)
                .build();

        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(productRequest.quantity())
                .setPriceData(priceData)
                .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/success")
                .setCancelUrl("http://localhost:8080/cancel")
                .addLineItem(lineItem)
                .build();

        Session session = null;

        try {
            session = Session.create(params);
        } catch (StripeException e) {
            System.out.println(e.getMessage());
        }

        return new StripeResponse(
                "success",
                "Payment session created successfully",
                session
        );
    }

    public void createPaymentSuccess() {

        System.out.println("Payment successful");
    }
}
