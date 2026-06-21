package com.codingshuttle.razorpay.payment.service.impl;

import com.codingshuttle.razorpay.common.entity.Money;
import com.codingshuttle.razorpay.common.enums.OrderStatus;
import com.codingshuttle.razorpay.common.exception.BusinessRuleViolationException;
import com.codingshuttle.razorpay.common.exception.DuplicateResourceException;
import com.codingshuttle.razorpay.common.exception.ResourceNotFoundException;
import com.codingshuttle.razorpay.payment.dto.request.CreateOrderRequest;
import com.codingshuttle.razorpay.payment.dto.response.OrderResponse;
import com.codingshuttle.razorpay.payment.dto.response.PaymentResponse;
import com.codingshuttle.razorpay.payment.entity.OrderRecord;
import com.codingshuttle.razorpay.payment.entity.Payments;
import com.codingshuttle.razorpay.payment.mapper.OrderMapper;
import com.codingshuttle.razorpay.payment.mapper.PaymentMapper;
import com.codingshuttle.razorpay.payment.repository.OrderRecordRepository;
import com.codingshuttle.razorpay.payment.repository.PaymentRepository;
import com.codingshuttle.razorpay.payment.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.OrderingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImplementation implements OrderService {

    private final OrderRecordRepository orderRecordRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;

    @Value("${payment.order.default-order-expiry-minutes:30}")
    private int defaultExpiryMinutes;
    @Override
    @Transactional
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

        orderRecord = orderRecordRepository.save(orderRecord);
        //Todo: publish a Kafka event about order creation
        return orderMapper.toResponse(orderRecord);
//        return new OrderResponse(orderRecord.getId(),
//                orderRecord.getMerchantId(),
//                orderRecord.getReceipt(),
//                orderRecord.getAmount(),
//                orderRecord.getStatus(),
//                orderRecord.getAttempts(),
//                orderRecord.getNotes(),
//                orderRecord.getExpireAt(),
//                null);
    }
// we pass merchantId because to return orderId if it only belongs to that merchant. To avoid merchants to see orders of other merchants
    @Override
    public OrderResponse getById(UUID merchantId, UUID orderId) {
        OrderRecord order = findOrderByIdAndMerchantId(merchantId,orderId);
        return orderMapper.toResponse(order);
//
    }

    @Override
    @Transactional
    public OrderResponse cancel(UUID merchantId, UUID orderId) {
        OrderRecord order = findOrderByIdAndMerchantId(merchantId,orderId);
        if(order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.PAID){
            throw new BusinessRuleViolationException("ORDER_CANNOT_BE_CANCELLED","Cannot cancel an order with status: "+order.getStatus().name());
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRecordRepository.save(order);
        return orderMapper.toResponse(order);
    }

    @Override
    public List<PaymentResponse> listPayments(UUID merchantId, UUID orderId) {
        OrderRecord order = findOrderByIdAndMerchantId(merchantId,orderId);
        List<Payments> paymentsList = paymentRepository.findByOrder_Id(order);
        return paymentMapper.toResponseList(paymentsList);


    }

    //-------------Helper Methods-------------------------------------------------------------------
    private OrderRecord findOrderByIdAndMerchantId(UUID merchantId, UUID orderId){
       return orderRecordRepository.findByIdAndMerchantId(merchantId,orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order",orderId));

    }
}
