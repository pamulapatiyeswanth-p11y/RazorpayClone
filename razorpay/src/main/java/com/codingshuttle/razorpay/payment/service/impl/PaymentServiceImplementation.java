package com.codingshuttle.razorpay.payment.service.impl;
import com.codingshuttle.razorpay.common.enums.OrderStatus;
import com.codingshuttle.razorpay.common.enums.PaymentStatus;
import com.codingshuttle.razorpay.common.exception.BusinessRuleViolationException;
import com.codingshuttle.razorpay.common.exception.ResourceNotFoundException;
import com.codingshuttle.razorpay.payment.dto.request.PaymentInitRequest;
import com.codingshuttle.razorpay.payment.dto.response.PaymentResponse;
import com.codingshuttle.razorpay.payment.entity.OrderRecord;
import com.codingshuttle.razorpay.payment.entity.Payments;
import com.codingshuttle.razorpay.payment.gateway.PaymentGatewayRouter;
import com.codingshuttle.razorpay.payment.gateway.dto.PaymentRequest;
import com.codingshuttle.razorpay.payment.gateway.dto.PaymentResult;
import com.codingshuttle.razorpay.payment.mapper.PaymentMapper;
import com.codingshuttle.razorpay.payment.repository.OrderRecordRepository;
import com.codingshuttle.razorpay.payment.repository.PaymentRepository;
import com.codingshuttle.razorpay.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImplementation implements PaymentService {

    private final OrderRecordRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ) // if someone tries to read same thing multiple times, it will return the same
    public PaymentResponse initiatePayment(UUID merchantId, PaymentInitRequest request) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(request.orderId(),merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order",request.orderId()));
        if(order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.ATTEMPTED){
            throw new BusinessRuleViolationException("ORDER_NOT_PAYABLE","Payment cannot be accepted for an order with status: "+ order.getStatus());
        }
        order.setStatus(OrderStatus.ATTEMPTED);
        order.setAttempts(order.getAttempts()+1);


        Payments payment = Payments.builder()
                .order(order)
                .amount(order.getAmount())
                .status(PaymentStatus.CREATED)
                .paymentMethod(request.paymentMethod())
                .paymentMethodDetails(request.methodDetails())
                .build();

        paymentRepository.save(payment);
        PaymentRequest paymentRequest = new PaymentRequest(  // Required for passing request to initiate method of PaymentGatewayRouter
                payment.getId(),
                request.orderId(),
                merchantId,
                order.getAmount(),
                request.paymentMethod(),
                request.methodDetails());

       PaymentResult result =paymentGatewayRouter.initiate(paymentRequest);
        switch (result) {
            case PaymentResult.Pending pending -> payment.setProcessorReference(pending.paymentRegistrationReference());
            case PaymentResult.Failure failure -> {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            } }
        payment = paymentRepository.save(payment); // save payment
        orderRepository.save(order); // save order
        return paymentMapper.toResponse(payment);
    }
}
