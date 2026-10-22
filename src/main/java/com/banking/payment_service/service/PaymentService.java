package com.banking.payment_service.service;


import com.banking.payment_service.dto.CreatePaymentRequest;
import com.banking.payment_service.dto.PaymentOrderResponse;
import com.banking.payment_service.entity.Payment;
import com.banking.payment_service.entity.PaymentStatus;
import com.banking.payment_service.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repository;
    private static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";
    private static final String PAYMENT_FAILED_TOPIC = "payment.failed";


    @Value("${razorpay.key-id}")
    private String keyId;
    @Value("${razorpay.key-secret}")
    private String keySecret;
   
    /*
    * Create Razorpay payment order.
    * Flow:
    * 1. Create order in razorpay
    * 2. Save payment record in db
    * 3. Return order details to frontend 
    * 4. Frontend show razorpay checkout
    * 5. User pays
    * 6. Razorpay calls webhook
    * @param request, @return */
    public PaymentOrderResponse createPaymentOrder(CreatePaymentRequest request) throws RazorpayException{

        log.info("Creating paymnet order for amount: {} amount: {}", request.getAccountNumber(), request.getAmount());

        RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);
        
        int convertedAmount = request.getAmount().multiply(BigDecimal.valueOf(100)).intValue();

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", convertedAmount);
        orderRequest.put("currency", "USD/INR");
        orderRequest.put("receipt", "rcpt_"+System.currentTimeMillis()+ UUID.randomUUID().toString().replace("-", "").substring(0,10));

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);

        log.info("Razorpay order created: {}", razorpayOrder.get("id").toString());
        
        Payment payment = new Payment();
        payment.setRazorpayOrderId(razorpayOrder.get("id").toString());
        payment.setAccountNumber(request.getAccountNumber());
        payment.setAmount(request.getAmount());
        payment.setCurrency("USD/INR");
        payment.setStatus(PaymentStatus.CREATED);
        payment.setDescription(request.getDescription());

        Payment savedPaymemt = repository.save(payment);

        

        return new PaymentOrderResponse(
           savedPaymemt.getId(),
           razorpayOrder.get("id").toString(),
           request.getAmount(),
           "USD/INR",
           "CREATED",
           keyId
        );
    }

    public String handleWebhook(Map<String, Object> payload){
    return null;
    }

}
