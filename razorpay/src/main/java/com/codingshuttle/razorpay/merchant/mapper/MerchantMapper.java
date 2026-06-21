package com.codingshuttle.razorpay.merchant.mapper;

import com.codingshuttle.razorpay.merchant.dto.request.MerchantSignUpRequest;
import com.codingshuttle.razorpay.merchant.dto.response.MerchantResponse;
import com.codingshuttle.razorpay.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MerchantMapper {

    Merchant toEntity(MerchantSignUpRequest request);

    @Mapping(source = "status", target = "merchantStatus")
    MerchantResponse toMerchantResponse(Merchant merchant);
}
