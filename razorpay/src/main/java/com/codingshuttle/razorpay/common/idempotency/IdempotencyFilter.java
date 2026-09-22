package com.codingshuttle.razorpay.common.idempotency;

import com.codingshuttle.razorpay.common.exception.IdempotencyConflictException;
import com.codingshuttle.razorpay.merchant.security.MerchantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Set<String> GUARDED_METHODS = Set.of("POST","PUT","PATCH");
    private final MerchantContext merchantContext;
    private final IdempotencyStore idempotencyStore;
    private static final Duration IN_PROGRESS_TTL = Duration.ofSeconds(30);
    private final HandlerExceptionResolver exceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if(!GUARDED_METHODS.contains(request.getMethod())){
            filterChain.doFilter(request,response);
            return;
        }
        String rawKey = request.getHeader("X-Idempotency-Key");
        if(rawKey==null || rawKey.isBlank()){
            filterChain.doFilter(request,response);
            return;

        }
        UUID merchantId = merchantContext.getMerchantId();
        String key = merchantId != null? merchantId+rawKey : rawKey;

        boolean claimed = idempotencyStore.setIfAbsent(key,IN_PROGRESS_TTL);
        if(!claimed){
            // another thread has already claimed this key or used this key
            Optional<String> existingKey = idempotencyStore.get(key);
            log.info("Existing Idempotency Key record in redis is {}",existingKey);
           // Replay the previously stored response only when the idempotency key
            // exists and contains a completed response, not the temporary IN_PROGRESS marker.
            if(existingKey.isPresent() && !IdempotencyStore.IN_PROGRESS.equals(existingKey.get())){
                replay(request,response,existingKey.get()); // Sends the same completed response for the same key

            }
            else {
                //It is still in progress by other thread
                exceptionResolver.resolveException(request,response,null,
                        new IdempotencyConflictException("A request with this idempotency key is already in progress","409"));
            }

        }
        // first time claim
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(request, wrapper);
        } finally {
                int status = wrapper.getStatus();
                byte[] bodyBytes= wrapper.getContentAsByteArray();
                String body = new String(bodyBytes, StandardCharsets.UTF_8);
                if(status <400 && bodyBytes.length>0){
                    // success - store the completed response for future replays
//                    String stored = status + SEPERATOR + body;
                }
        }



    }

    private void replay(HttpServletRequest request, HttpServletResponse response, String s) {
    }
}
