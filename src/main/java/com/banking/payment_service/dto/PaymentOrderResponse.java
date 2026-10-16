package com.banking.payment_service.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentOrderResponse {

    private String paymentId;
    private String razorPayOrderId;
     private BigDecimal amount;
    private String currency;
    private String status;
    private String razorpayKeyId;
}
