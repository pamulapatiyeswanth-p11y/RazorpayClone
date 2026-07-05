package com.codingshuttle.razorpay.payment.processor.strategy;

import com.codingshuttle.razorpay.common.util.RandomizerUtil;
import com.codingshuttle.razorpay.payment.processor.PaymentProcessor;
import com.codingshuttle.razorpay.payment.processor.dto.request.PaymentProcessorRequest;
import com.codingshuttle.razorpay.payment.processor.dto.response.PaymentProcessorResponse;

public class UpiPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        final String VPA_CODE_FAIL = "fail@okaxis";
        //calls the third party bank
        String bankCode = request.methodDetails() != null ?
                request.methodDetails().get("vpa").toString() : null;

        //simulation of bank rejection for a specific VPA
        if(VPA_CODE_FAIL.equals(bankCode)){
            return new PaymentProcessorResponse.Failure("BANK_REJECTED",
                    "Bank rejected the transaction registration");
        }
        String processorReference = "UPI_PROCESSOR" + RandomizerUtil.randomBase64(16); // Unique Id for transaction to avoid duplicate transaction registration
        String bankReference = "BANK_REF" + RandomizerUtil.randomBase64(16);
        return new PaymentProcessorResponse.Success(processorReference, bankReference);
    }
}
