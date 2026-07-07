package com.codingshuttle.razorpay.vault.controller;

import com.codingshuttle.razorpay.vault.dto.request.TokenizeRequest;
import com.codingshuttle.razorpay.vault.dto.response.TokenizeResponse;
import com.codingshuttle.razorpay.vault.service.VaultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/vault")
public class VaultController {

    private final VaultService vaultService;
    private final UUID merchantId = UUID.fromString("5cb45f97-0f80-4904-901d-46a763ad59ea");// Todo: replace it with merchant context


    @PostMapping("/tokenize")
    public ResponseEntity<TokenizeResponse> tokenizeCard(@Valid @RequestBody TokenizeRequest request) {


        // Implement the logic to tokenize the card and return the response
        return ResponseEntity.status(HttpStatus.CREATED).body(vaultService.tokenizeCard(merchantId, request));
    }
}
