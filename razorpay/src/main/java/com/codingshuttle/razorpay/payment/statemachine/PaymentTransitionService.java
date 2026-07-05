package com.codingshuttle.razorpay.payment.statemachine;

import com.codingshuttle.razorpay.common.enums.Actor;
import com.codingshuttle.razorpay.common.enums.PaymentEvent;
import com.codingshuttle.razorpay.common.enums.PaymentStatus;
import com.codingshuttle.razorpay.payment.entity.PaymentTransitionLog;
import com.codingshuttle.razorpay.payment.entity.Payments;
import com.codingshuttle.razorpay.payment.repository.PaymentTransitionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentTransitionService {
    private final PaymentTransitionLogRepository paymentTransitionLogRepository;
    private final PaymentStateMachine paymentStateMachine;

    public PaymentStatus apply(Payments payment, PaymentEvent event){
        PaymentStatus nextStatus = paymentStateMachine.transition(payment.getStatus(), event);
        payment.setStatus(nextStatus);
        PaymentTransitionLog transitionLog = PaymentTransitionLog.builder()
                .payment(payment)
                .fromStatus(payment.getStatus())
                .toStatus(nextStatus)
                .actor(Actor.SYSTEM)// ToDo: get the actor from the merchant security context
                .eventType(event)
                .occurredAt(LocalDateTime.now())
                .build();

        paymentTransitionLogRepository.save(transitionLog);
        return nextStatus;

    }
}
