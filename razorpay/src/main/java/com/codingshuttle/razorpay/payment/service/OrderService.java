package com.codingshuttle.razorpay.payment.service;

import com.codingshuttle.razorpay.payment.dto.request.CreateOrderRequest;
import com.codingshuttle.razorpay.payment.dto.response.OrderResponse;

import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder(UUID merchantId, CreateOrderRequest request);

}
