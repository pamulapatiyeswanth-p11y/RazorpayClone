package com.codingshuttle.razorpay.payment.statemachine;

import com.codingshuttle.razorpay.common.enums.PaymentEvent;
import com.codingshuttle.razorpay.common.enums.PaymentStatus;
import com.codingshuttle.razorpay.common.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentStateMachine {
    public record Transition(PaymentStatus fromState, PaymentEvent event) {
    }

    private static final Map<Transition, PaymentStatus> TRANSITION =
            Map.ofEntries(
                    Map.entry(new Transition(PaymentStatus.CREATED, PaymentEvent.AUTHORIZE_ATTEMPT), PaymentStatus.AUTHORIZING),
                    Map.entry(new Transition(PaymentStatus.AUTHORIZING, PaymentEvent.AUTHORIZE_SUCCESS), PaymentStatus.AUTHORIZED),
                    Map.entry(new Transition(PaymentStatus.AUTHORIZING, PaymentEvent.AUTHORIZE_FAILED), PaymentStatus.FAILED),
                    Map.entry(new Transition(PaymentStatus.AUTHORIZED, PaymentEvent.CAPTURE_REQUEST), PaymentStatus.CAPTURING),
                    Map.entry(new Transition(PaymentStatus.CAPTURING, PaymentEvent.CAPTURE_SUCCESS), PaymentStatus.CAPTURED),
                    Map.entry(new Transition(PaymentStatus.CAPTURING, PaymentEvent.CAPTURE_FAILED), PaymentStatus.AUTHORIZED),
                    Map.entry(new Transition(PaymentStatus.CAPTURING, PaymentEvent.REFUND_INITIATED), PaymentStatus.PARTIALLY_REFUNDED),
                    Map.entry(new Transition(PaymentStatus.PARTIALLY_REFUNDED, PaymentEvent.REFUND_SUCCESS), PaymentStatus.REFUNDED),
                    Map.entry(new Transition(PaymentStatus.CAPTURED, PaymentEvent.REFUND_SUCCESS), PaymentStatus.REFUNDED),
                    Map.entry(new Transition(PaymentStatus.CREATED, PaymentEvent.CANCEL), PaymentStatus.CANCELLED),
                    Map.entry(new Transition(PaymentStatus.AUTHORIZING, PaymentEvent.CANCEL), PaymentStatus.CANCELLED),
                    Map.entry(new Transition(PaymentStatus.AUTHORIZED, PaymentEvent.CAPTURE_TIMEOUT), PaymentStatus.AUTH_EXPIRED),
                    Map.entry(new Transition(PaymentStatus.CAPTURED, PaymentEvent.SETTLE), PaymentStatus.SETTLED)
            );

    public PaymentStatus transition(PaymentStatus currentState, PaymentEvent event) {
        PaymentStatus nextState = TRANSITION.get(new Transition(currentState, event));
        if (nextState == null) {
            throw new InvalidStateTransitionException(currentState.name(), event.name());
        }
        return nextState;
    }
}
