package com.codingshuttle.razorpay.payment.simulator;

import com.codingshuttle.razorpay.common.enums.ChaosMode;
import com.codingshuttle.razorpay.common.enums.PaymentStatus;
import com.codingshuttle.razorpay.common.util.RandomizerUtil;
import com.codingshuttle.razorpay.payment.entity.Payments;
import com.codingshuttle.razorpay.payment.repository.PaymentRepository;
import com.codingshuttle.razorpay.payment.service.PaymentService;
import com.codingshuttle.razorpay.payment.simulator.config.SimulatorConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class BankCallBackSimulator {

    private final PaymentRepository paymentRepository;
    private final SimulatorConfig simulatorConfig;
    private final PaymentService paymentService;

    @Scheduled(fixedDelayString = "${payment.simulator.poll-interval-ms:5000}")
    public void processCallBack(){
        LocalDateTime globalWindow = LocalDateTime.now().minusSeconds(1); // Need check all rows until a second ago
        //Get all payment rows with payment status as Authorizing created until a second ago
        List<Payments> candidates = paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.AUTHORIZING,globalWindow);
        log.info("Simulating bank response for payments {}",candidates.size());
        if(candidates.isEmpty()){
            return ;
        }
        for(Payments payment : candidates){
            simulateCallBack(payment);
        }

    }

    private void simulateCallBack(Payments payment){
        SimulatorConfig.MethodSimulatorConfig methodSimulatorConfig =
                simulatorConfig.config(payment.getPaymentMethod());

        LocalDateTime dueAt = dueAt(payment,methodSimulatorConfig);
        if(LocalDateTime.now().isBefore(dueAt)){
            log.info("Payment is early {}",payment.getId());
            return;
        }

        ChaosMode chaosMode = simulatorConfig.getChaosMode();

        switch (chaosMode){
            case SUCCESS ->  resolve(payment,true);
            case FAILURE ->  resolve(payment,false);
            case TIMEOUT ->  {
                log.debug("BankCallBack simulator: payment timed out {}",payment.getId());
            }
            case NORMAL, SLOW ->  resolve(payment,shouldApprove(payment,methodSimulatorConfig));
        }
    }

    private void resolve(Payments payments, boolean approve){
            if(approve){
                String bankRef = "SIM_BANK_REF" + RandomizerUtil.randomBase64(8);
                paymentService.resolveAuthorization(payments.getId(),true,bankRef,null,null);
                //In real world case a Bank responds to payment processor which sends response to gateway and gateway calls the payment service

            }
            else {
                paymentService.resolveAuthorization(payments.getId(),false,null,"SIM_BANK_ERROR" ,"Simulated bank declined");

            }
    }

    private boolean shouldApprove(Payments payments,SimulatorConfig.MethodSimulatorConfig methodConfig ){
        int bucket = Math.abs(payments.getId().hashCode()) % 100; // gives a number from 0 to 99
        return bucket < methodConfig.getSuccessRate();
        // 90% of the time this would give us true and 10% of time this would give me false
    }

    //This gives the date and time after creation at which the payment should be processed.
    // We are adding some delay with this
    private LocalDateTime dueAt(Payments payments,SimulatorConfig.MethodSimulatorConfig methodConfig){
                int range = methodConfig.getMaxDelaySeconds()-methodConfig.getMinDelaySeconds();
                int delaySeconds = methodConfig.getMinDelaySeconds() +
                        Math.abs(payments.getId().hashCode()) % (range+1); // will generate a random num within 0 to the range+1;
                //Alternative
//                Random random = new Random();
//                int delaySeconds = methodConfig.getMinDelaySeconds() + random.nextInt(range+1);

                if (simulatorConfig.getChaosMode() == ChaosMode.SLOW){
                    delaySeconds *= 2;

                }
                return payments.getCreatedAt().plusSeconds(delaySeconds); // Return a time after createdAt (by delayseconds)
    }

}

