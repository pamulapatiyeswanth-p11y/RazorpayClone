package com.codingshuttle.razorpay.payment.gateway.adapter;

import com.codingshuttle.razorpay.payment.gateway.PaymentAdapter;
import com.codingshuttle.razorpay.payment.gateway.dto.PaymentRequest;
import com.codingshuttle.razorpay.payment.gateway.dto.PaymentResult;
import com.codingshuttle.razorpay.payment.processor.PaymentProcessorRouter;
import com.codingshuttle.razorpay.payment.processor.dto.request.PaymentProcessorRequest;
import com.codingshuttle.razorpay.payment.processor.dto.response.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpiPaymentAdapter implements PaymentAdapter {
    private final PaymentProcessorRouter paymentProcessorRouter;
    @Override
    public PaymentResult initiate(PaymentRequest request) {
        log.info("Initiating UPI payment for request Id: {}", request.paymentId());
        try {
            PaymentProcessorRequest processorRequest = PaymentProcessorRequest.nonCard(
                    request.paymentId(),
                    request.method(),
                    request.amount(),
                    request.methodDetails()
            );
            PaymentProcessorResponse processorResponse = paymentProcessorRouter.charge(processorRequest);

            return switch (processorResponse) {
                case PaymentProcessorResponse.Failure failure ->
                        new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());
                case PaymentProcessorResponse.Pending pending ->
                        new PaymentResult.Pending(pending.processorReference());
                case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());
            };
        } catch (Exception e) {
            log.warn("UPI payment failed for payment Id: {}", request.paymentId());
            return new PaymentResult.Failure("UPI_PAYMENT_FAILED", e.getMessage());
        }
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("UPI_PAYMENT_CAPTURED");
    }
}
