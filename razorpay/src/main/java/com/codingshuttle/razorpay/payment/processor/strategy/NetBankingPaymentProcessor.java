package com.codingshuttle.razorpay.payment.processor.strategy;

import com.codingshuttle.razorpay.common.util.RandomizerUtil;
import com.codingshuttle.razorpay.payment.processor.PaymentProcessor;
import com.codingshuttle.razorpay.payment.processor.dto.request.PaymentProcessorRequest;
import com.codingshuttle.razorpay.payment.processor.dto.response.PaymentProcessorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NetBankingPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        final String BANK_CODE_FAIL = "BANK_CODE_FAIL";
        //calls the third party bank
        String bankCode = request.methodDetails() != null ?
                request.methodDetails().get("bank").toString() : null;

        //simulation of bank rejection for a specific bank code
        if(BANK_CODE_FAIL.equals(bankCode)){
            return new PaymentProcessorResponse.Failure("BANK_REJECTED",
                    "Bank rejected the transaction registration");
        }
        String processorReference = "NET_BANKING_PROCESSOR" + RandomizerUtil.randomBase64(16); // Unique Id for transaction to avoid duplicate transaction registration
//        String redirectReference = "https://BankRedirect.com/" + processorReference; // To redirect the user to the bank for authentication and authorization
        return new PaymentProcessorResponse.Pending(processorReference);
    }
}
