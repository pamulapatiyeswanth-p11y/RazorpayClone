package com.codingshuttle.razorpay.merchant.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {
    private static final String[] JWT_ROUTES = {"/v1/auth/**","/v1/merchants/**","/v1/admin/**","/actuator/**"}; // Razorpay dashboard endpoints, not accessed by merchant
    private static final String[] API_KEY_ROUTES = {"/v1/orders/**","/v1/payments/**","/v1/vault/**"}; //Server to server, communicate with merchant
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiKeyAuthFilter apiKeyAuthFilter;

    @Bean
    @Order(1)
    public AuthenticationManager authenticationManager(MerchantUserDetailsService userDetailsService,PasswordEncoder passwordEncoder){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authenticationProvider);
    }
    @Bean
    public SecurityFilterChain jwtChain(HttpSecurity http){
        //SecurityMatcher checks the routes and only goes through if it is satisfied
        return http.
                securityMatcher(JWT_ROUTES).
               csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/v1/auth/signup","/v1/auth/login").permitAll()
                                .anyRequest().authenticated()

                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)


//                        .formLogin(form -> form.disable())// Not required if we are adding our own fitler
        .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiKeyChain(HttpSecurity http){
        //SecurityMatcher checks the routes and only goes through if it is satisfied
        return http.
                securityMatcher(API_KEY_ROUTES).
                csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().authenticated()

                )
                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)


//                        .formLogin(form -> form.disable())// Not required if we are adding our own fitler
                .build();
    }

}
