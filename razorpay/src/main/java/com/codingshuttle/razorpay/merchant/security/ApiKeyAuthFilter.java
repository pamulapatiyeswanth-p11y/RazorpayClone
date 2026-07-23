package com.codingshuttle.razorpay.merchant.security;

import com.codingshuttle.razorpay.merchant.cache.ApiKeyCache;
import com.codingshuttle.razorpay.merchant.cache.ApiKeyCacheEntry;
import com.codingshuttle.razorpay.merchant.entity.ApiKey;
import com.codingshuttle.razorpay.merchant.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;


@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    private static final String BASIC_PREFIX = "Basic ";
    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private final MerchantContext merchantContext;
    private final HandlerExceptionResolver handlerExceptionResolver; // To propagate any exception from filters to MVC layer which then can be handled by global exception handler
    private final ApiKeyCache apiKeyCache;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("Incoming request {}", request.getRequestURI());
        try {
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith(BASIC_PREFIX)) {
                filterChain.doFilter(request, response);
            }
// Authorization Header: Basic key id:key secret ex: Basic rzp_production_WNJisyghFTWeMHOVlMC3SrhRLS580-0v:oJqWGZl2dJnvwjzcYzAdcjox1sUzOxg81R08VQ-hKr7xLFZJnjL_6w
            String[] credentials = decode(header);
            if (credentials == null) {
                throw new BadRequestException("Malformed API key header");
            }
            String keyId = credentials[0];
            String rawSecret = credentials[1];
            ApiKeyCacheEntry apiKeyEntry = apiKeyCache.get(keyId)
                    .orElseGet(() -> loadAndCache(keyId));
//            ApiKey apiKey = apiKeyRepository.findByKeyId(keyId)
//                    .orElseThrow(() -> new BadRequestException("Invalid or missing API key"));

            if (apiKeyEntry==null || !apiKeyEntry.enabled() || !verifySecret(rawSecret, apiKeyEntry)) {
                throw new BadRequestException("Invalid or missing API key");
            }
            Authentication authentication = new UsernamePasswordAuthenticationToken(keyId, null, List.of(new SimpleGrantedAuthority("API_KEY_ROLE")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            merchantContext.setKeyId(apiKeyEntry.keyId());
            merchantContext.setMerchantId(apiKeyEntry.merchantId());
            filterChain.doFilter(request,response);
        }catch (Exception ex){
            handlerExceptionResolver.resolveException(request,response,null,ex);
        }
    }

    private String[] decode(String header){
        String encoded = header.substring(BASIC_PREFIX.length());
        String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        int colon = decoded.indexOf(":");
        if(colon<1) return null;
        return new String[]{decoded.substring(0,colon),decoded.substring(colon+1)};
    }

    private boolean verifySecret(String rawSecret, ApiKeyCacheEntry apiKeyEntry){
            if(passwordEncoder.matches(rawSecret,apiKeyEntry.keySecretHash())){
                return  true;
            }

            return apiKeyEntry.isInGracePeriod() &&
                    apiKeyEntry.previousKeySecretHash() != null &&
                    passwordEncoder.matches(rawSecret, apiKeyEntry.previousKeySecretHash());

    }

    private ApiKeyCacheEntry loadAndCache(String keyId){
        ApiKey apiKey = apiKeyRepository.findByKeyId(keyId).orElse(null);
        if(apiKey == null)
            return null;
        ApiKeyCacheEntry apiKeyCacheEntry = new ApiKeyCacheEntry(
                apiKey.getMerchantId().getId(),
                apiKey.getKeyId(),
                apiKey.getKeySecretHash(),
                apiKey.getPreviousKeySecretHash(),
                apiKey.getEnvironment(),
                apiKey.isEnabled(),
                apiKey.getGracePeriodExpiryAt()
        );
        apiKeyCache.put(keyId,apiKeyCacheEntry);
        return apiKeyCacheEntry;
    }
}
