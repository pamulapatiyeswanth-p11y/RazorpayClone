package com.codingshuttle.razorpay.payment.mapper;

import com.codingshuttle.razorpay.payment.dto.response.PaymentResponse;
import com.codingshuttle.razorpay.payment.entity.Payments;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "paymentMethodDetails", target = "methodDetails")
    @Mapping(source = "status", target = "paymentStatus")
    PaymentResponse toResponse(Payments payments);

//    @Mapping(source = "orderId", target = "order.id")
//    @Mapping(source = "methodDetails", target = "paymentMethodDetails")
//    @Mapping(source = "paymentStatus", target = "status")
    Payments toEntity(PaymentResponse paymentResponse);

    //Lists
    @Mapping(source = "order.id",target = "orderId")
    @Mapping(source = "paymentMethodDetails", target = "methodDetails")
    @Mapping(source = "status", target = "paymentStatus")
    List<PaymentResponse> toResponseList(List<Payments> paymentsList);
}
