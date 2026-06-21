package com.codingshuttle.razorpay.merchant.mapper;

import com.codingshuttle.razorpay.merchant.dto.response.ApiKeyResponse;
import com.codingshuttle.razorpay.merchant.dto.response.CreateApiKeyResponse;
import com.codingshuttle.razorpay.merchant.entity.ApiKey;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ApiKeyMapper {

    @Mapping(source = "keySecretHash" , target = "keySecret" )
    CreateApiKeyResponse toCreateResponse(ApiKey apiKey);
    ApiKeyResponse toApiKeyResponse(ApiKey apiKey);
    List<ApiKeyResponse> toApiKeyResponseList(List<ApiKey> apiKeyList);
}
