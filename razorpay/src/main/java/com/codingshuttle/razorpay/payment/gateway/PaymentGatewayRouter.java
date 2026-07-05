package com.codingshuttle.razorpay.payment.gateway;

import com.codingshuttle.razorpay.common.enums.PaymentMethod;
import com.codingshuttle.razorpay.payment.gateway.dto.PaymentRequest;
import com.codingshuttle.razorpay.payment.gateway.dto.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentGatewayRouter {
    private final Map<PaymentMethod,PaymentAdapter> paymentAdapters;
    public PaymentResult initiate(PaymentRequest request){
        PaymentAdapter paymentAdapter = paymentAdapters.get(request.method());
        if(paymentAdapter == null){
            throw new IllegalArgumentException("No payment adapter registered for the payment method: "
                    +request.method());
        }
       return paymentAdapter.initiate(request);

    }

    public PaymentResult capture(PaymentMethod method, UUID paymentId){
        PaymentAdapter paymentAdapter = paymentAdapters.get(method);
        if(paymentAdapter == null){
            throw new IllegalArgumentException("No payment adapter registered for the payment method: "
                    +method);
        }
        return paymentAdapter.capture(paymentId);
    }
}
