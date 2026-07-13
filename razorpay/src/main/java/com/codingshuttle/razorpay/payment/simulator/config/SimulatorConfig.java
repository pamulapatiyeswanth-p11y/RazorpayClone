package com.codingshuttle.razorpay.payment.simulator.config;

import com.codingshuttle.razorpay.common.enums.ChaosMode;
import com.codingshuttle.razorpay.common.enums.PaymentMethod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "payment.simulator")
@Getter
@Setter
public class SimulatorConfig {

    private Integer pollIntervalMs = 2000;
    private ChaosMode chaosMode = ChaosMode.NORMAL;
    private Map<String,MethodSimulatorConfig> methods  = new HashMap<>();

    public SimulatorConfig.MethodSimulatorConfig config(PaymentMethod paymentMethod){
                return methods.getOrDefault(paymentMethod.name(),new MethodSimulatorConfig());
                //Return configured data from yml if method exists or else returns what we see below
    }

    @Getter
    @Setter
    public static class MethodSimulatorConfig{
        private Integer minDelaySeconds = 1;
        private Integer maxDelaySeconds = 5;
        private Integer successRate = 80;
    }


}
