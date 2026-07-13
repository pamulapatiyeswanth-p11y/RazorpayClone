package com.codingshuttle.razorpay.payment.processor.strategy;

import com.codingshuttle.razorpay.common.util.RandomizerUtil;
import com.codingshuttle.razorpay.payment.processor.PaymentProcessor;
import com.codingshuttle.razorpay.payment.processor.dto.request.PaymentProcessorRequest;
import com.codingshuttle.razorpay.payment.processor.dto.response.PaymentProcessorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CardPaymentProcessor implements PaymentProcessor {
    // Just simulating the card numbers for out testing as we are not connected to a bank service
    public static final String PAN_CARD_DECLINED = "4000000000000002";
    public static final String PAN_CARD_EXPIRED = "4000000000000069";
    public static final String PAN_CARD_INCORRECT_CVV ="4000000000000127";
    public static final String PAN_CARD_INSUFFICIENT_FUNDS = "4000000000009995";
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        //In real processor here we call the card network
        String pan = request.pan();
        if(PAN_CARD_DECLINED.equals(pan)){
            log.warn("Card Declined");
            return new PaymentProcessorResponse.Failure("CARD_DECLINED",
                    "Card decline by the bank");
        }
        else if(PAN_CARD_EXPIRED.equals(pan)){
            return new PaymentProcessorResponse.Failure("CARD_EXPIRED",
                    "Card has expired");
        }
        else if(PAN_CARD_INSUFFICIENT_FUNDS.equals(pan)){
            return new PaymentProcessorResponse.Failure("INSUFFICIENT_FUNDS",
                    "Card doesn't have sufficient funds");
        }
        else if(PAN_CARD_INCORRECT_CVV.equals(pan)){
            return new PaymentProcessorResponse.Failure("INCORRECT_CVV",
                    "Incorrect CVV provided");
        }
        String processorReference = "CARD_PROCESSOR" + RandomizerUtil.randomBase64(16); // Unique Id for transaction to avoid duplicate transaction registration

        return new PaymentProcessorResponse.Pending(processorReference);
    }
}
