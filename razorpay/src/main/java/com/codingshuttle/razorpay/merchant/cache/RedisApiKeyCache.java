package com.codingshuttle.razorpay.merchant.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisApiKeyCache implements ApiKeyCache{
    private final StringRedisTemplate stringRedisTemplate;
    private static final String PREFIX = "apikey:";
    private static final Duration TTL = Duration.ofMinutes(5); //Store API key in cache only for 3 minutes
    private final ObjectMapper objectMapper;
    @Override
    public Optional<ApiKeyCacheEntry> get(String keyId) {
        try{
            String json = stringRedisTemplate.opsForValue().get(PREFIX+keyId);
            if ((json==null)){
                log.info("Api Key doesn't exist in the cache {}",keyId);
                return Optional.empty();
            }
           return Optional.of(objectMapper.readValue(json,ApiKeyCacheEntry.class)); // Parse the json and maps it to the provided class format
        }
        catch (Exception ex){
                log.warn("Api Key cache read fail, keyId: {}",keyId);
                return Optional.empty();
        }

    }

    @Override
    public void put(String keyId, ApiKeyCacheEntry entry) {
        try{
            log.info("Api Key cache put started, keyId: {}",keyId);
            stringRedisTemplate.opsForValue().set(PREFIX+keyId,
                    objectMapper.writeValueAsString(entry),
                    TTL);
        } catch (Exception e)
        {
            log.warn("Api Key cache put fail, keyId: {}",keyId);

        }

    }

    @Override
    public void evict(String keyId) {
        stringRedisTemplate.delete(PREFIX+keyId);

    }
}
