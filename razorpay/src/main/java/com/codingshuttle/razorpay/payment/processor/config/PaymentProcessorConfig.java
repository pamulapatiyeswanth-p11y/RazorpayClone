package com.codingshuttle.razorpay.payment.processor.config;

import com.codingshuttle.razorpay.common.enums.PaymentMethod;
import com.codingshuttle.razorpay.payment.processor.PaymentProcessor;
import com.codingshuttle.razorpay.payment.processor.strategy.CardPaymentProcessor;
import com.codingshuttle.razorpay.payment.processor.strategy.NetBankingPaymentProcessor;
import com.codingshuttle.razorpay.payment.processor.strategy.UpiPaymentProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.smartcardio.Card;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentProcessorConfig {
    private final CardPaymentProcessor cardPaymentProcessor;
    private final NetBankingPaymentProcessor netBankingPaymentProcessor;
    private final UpiPaymentProcessor upiPaymentProcessor;

    @Bean
    public Map<PaymentMethod, PaymentProcessor> paymentProcessorMap(){
        return Map.of(PaymentMethod.CARD,cardPaymentProcessor,
                PaymentMethod.NET_BANKING,netBankingPaymentProcessor,
                PaymentMethod.UPI,upiPaymentProcessor);
    }
}
