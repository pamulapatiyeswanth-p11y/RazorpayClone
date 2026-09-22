package com.codingshuttle.razorpay.common.idempotency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisIdempotencyStore implements IdempotencyStore{
    private static final String PREFIX = "idempotency";
    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean setIfAbsent(String key, Duration ttl) {
        try{
            // returns true if key is not present already and also stores the key and its value in redis at the same time
            Boolean set = redisTemplate.opsForValue().setIfAbsent(PREFIX+key,IN_PROGRESS,ttl);
            return Boolean.TRUE.equals(set);
        }
        catch (DateTimeException ex){
            log.warn("Idempotency store is unavailable, failing open for key={}",key,ex);
            return true;
        }

    }

    @Override
    public void store(String key, String value, Duration ttl) {
        try{
            redisTemplate.opsForValue().set(PREFIX+key,value,ttl);
        }
        catch (DateTimeException ex){
            log.warn("Failed to store the idempotency key, failing open for key={}",key,ex);
        }

        }



    @Override
    public Optional<String> get(String key) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(PREFIX + key));
        } catch (DateTimeException ex){
            log.warn("Failed to get the idempotency key, failing open for key={}",key,ex);
            return Optional.empty();
        }

    }

    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(PREFIX+key);
        } catch (DateTimeException ex){
            log.warn("Failed to delete the idempotency key, failing open for key={}",key,ex);

        }
    }
}
