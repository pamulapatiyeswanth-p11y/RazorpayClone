package com.codingshuttle.razorpay.payment.service.impl;

import com.codingshuttle.razorpay.common.entity.Money;
import com.codingshuttle.razorpay.common.enums.OrderStatus;
import com.codingshuttle.razorpay.common.exception.DuplicateResourceException;
import com.codingshuttle.razorpay.payment.dto.request.CreateOrderRequest;
import com.codingshuttle.razorpay.payment.dto.response.OrderResponse;
import com.codingshuttle.razorpay.payment.entity.OrderRecord;
import com.codingshuttle.razorpay.payment.repository.OrderRecordRepository;
import com.codingshuttle.razorpay.payment.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImplementation implements OrderService {

    private final OrderRecordRepository orderRecordRepository;

    @Value("${payment.order.default-order-expiry-minutes:30}")
    private int defaultExpiryMinutes;
    @Override
    public OrderResponse createOrder(UUID merchantId, CreateOrderRequest request) {
        if(request.receipt()!=null && orderRecordRepository.existsByMerchantIdAndReceipt(merchantId,request.receipt())){
            throw new DuplicateResourceException("DUPLICATE_ORDER_RECEIPT",
                    "Order with receipt provided already exists: "+request.receipt());
        }

//
        OrderRecord orderRecord = OrderRecord.builder()
                .receipt(request.receipt())
                .amount(request.amount())
                .notes(request.notes())
                .merchantId(merchantId)
                .status(OrderStatus.CREATED)
                .expireAt(request.expireAt()!=null? request.expireAt() :
                        LocalDateTime.now().plusMinutes(defaultExpiryMinutes))
                .build();

        //Todo: publish a Kafka event about order creation
        orderRecord = orderRecordRepository.save(orderRecord);
        return new OrderResponse(orderRecord.getId(),
                orderRecord.getMerchantId(),
                orderRecord.getReceipt(),
                orderRecord.getAmount(),
                orderRecord.getStatus(),
                orderRecord.getAttempts(),
                orderRecord.getNotes(),
                orderRecord.getExpireAt(),
                null);
    }
}
