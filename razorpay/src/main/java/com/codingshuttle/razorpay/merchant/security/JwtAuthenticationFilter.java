package com.codingshuttle.razorpay.merchant.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final HandlerExceptionResolver handlerExceptionResolver; // To propagate any exception from filters to MVC layer which then can be handled by global exception handler
    private final MerchantContext merchantContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("Incoming request: {}",request.getRequestURI());
        try {
            final String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer")) {
                filterChain.doFilter(request, response); // Don't modify anything and let it move forward
            }
//            String jwtToken = authorizationHeader.split("Bearer ")[1];
            String jwtToken = authorizationHeader.substring("Bearer ".length());
            log.info("Token Obtained: {}", jwtToken);
            Claims claims = jwtUtils.verifyAccessToken(jwtToken);
            //Check if context is not set yet
            if (claims != null && SecurityContextHolder.getContext().getAuthentication() == null && !jwtUtils.isTokenExpired(claims)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + jwtUtils.extractRole(claims))));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                merchantContext.setMerchantId(UUID.fromString(jwtUtils.extractMerchantId(claims)));
                filterChain.doFilter(request, response);

                //optional - attaches additional information about the current HTTP request (IP,HTTP session,Device) to the Authentication object.
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            }
        }catch (Exception ex){
            handlerExceptionResolver.resolveException(request,response,null,ex);
        }


    }
}
