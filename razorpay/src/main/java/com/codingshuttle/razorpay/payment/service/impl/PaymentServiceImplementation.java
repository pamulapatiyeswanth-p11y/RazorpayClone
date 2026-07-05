package com.codingshuttle.razorpay.payment.service.impl;
import com.codingshuttle.razorpay.common.enums.OrderStatus;
import com.codingshuttle.razorpay.common.enums.PaymentEvent;
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
import com.codingshuttle.razorpay.payment.statemachine.PaymentTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImplementation implements PaymentService {

    private final OrderRecordRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final PaymentMapper paymentMapper;
    private final PaymentTransitionService paymentTransitionService;

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

       PaymentResult result = paymentGatewayRouter.initiate(paymentRequest);
        switch (result) {
            case PaymentResult.Pending pending ->
                    payment.setProcessorReference(pending.paymentRegistrationReference());
            case PaymentResult.Failure failure -> {
//                payment.setStatus(PaymentStatus.FAILED);
                paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAILED);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
            case PaymentResult.Success success -> {
//                payment.setStatus(PaymentStatus.SETTLED);
                paymentTransitionService.apply(payment, PaymentEvent.SETTLE);
                payment.setBankReference(success.bankReference());
            }
        }
        payment = paymentRepository.save(payment); // save payment
        orderRepository.save(order); // save order
        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse capture(UUID merchantId, UUID paymentId) {
        Payments payment = paymentRepository.findByIdAndMerchantId(paymentId,merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
        paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);
        PaymentResult result =  paymentGatewayRouter.capture(payment.getPaymentMethod(),paymentId);
        if(result instanceof PaymentResult.Success success){
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
            payment.setBankReference(success.bankReference());
            payment.setCapturedAt(LocalDateTime.now());
            log.info("Payment capture successful for payment Id: {}",paymentId);

        }
        else if(result instanceof PaymentResult.Failure failure){
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAILED);
            payment.setErrorCode(failure.errorCode());
            payment.setErrorDescription(failure.errorDescription());
            log.warn("Payment capture failed for payment Id: {} with error code: {} and description: {}",paymentId,failure.errorCode(),failure.errorDescription());

        }
        else{
            throw new BusinessRuleViolationException("PAYMENT_CAPTURE_FAILED","Payment capture failed for payment Id: "+paymentId);
        }
        payment = paymentRepository.save(payment);
        return paymentMapper.toResponse(payment);

    }
}
