package com.codingshuttle.razorpay.common.ratelimit.luascripts;

import com.codingshuttle.razorpay.common.ratelimit.RateLimitResult;
import com.codingshuttle.razorpay.common.ratelimit.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "token-bucket")
@Slf4j
public class SlidingWindowLuaRateLimiter implements RateLimiter {

    private final StringRedisTemplate redisTemplate;

    private static final String SLIDING_LUA_SCRIPT = """
        local key = KEYS[1]
        local floorMs = tonumber(ARGV[1])
        local nowMs = tonumber(ARGV[2])
        local maxRequests = tonumber(ARGV[3])
        local windowSeconds = tonumber(ARGV[4])
        local member = ARGV[5]
        
        -- prune expired numbers
        redis.call("ZREMRANGEBYSCORE", key, "-inf", floorMs)
        
        -- count current members
        local count = redis.call("ZCARD", key)

        if count >= maxRequests then
            local oldest = redis.call("ZRANGE", key, 0, 0, "WITHSCORES")
            local oldestScore = 0
            if #oldest > 0 then
                 oldestScore = tonumber(oldest[2])
            end
            return {0,0, oldestScore}
        end
        
        -- add this request and reset ttl automatically
        redis.call("ZADD", key, nowMs, member)
        redis.call("EXPIRE", key, windowSeconds + 1)
        
        local remaining =  maxRequests - count -1
        return {1, remaining,0}
        """;

    @SuppressWarnings("unchecked")
    private final RedisScript<List> slidingWindowScript =
            (RedisScript<List>) (RedisScript<?>) RedisScript.of(SLIDING_LUA_SCRIPT, List.class);

    @Override
    public RateLimitResult check(String key, int maxRequestsAllowed, long windowSeconds) {
        String redisKey = "ratelimit:sliding:" + key;
        try{
        long nowMs = System.currentTimeMillis();
        long floorMs = nowMs - windowSeconds * 1000;
        String member = UUID.randomUUID().toString();

        List<Long> result = redisTemplate.execute(
                slidingWindowScript,
                List.of(redisKey),
                String.valueOf(floorMs),
                String.valueOf(nowMs),
                String.valueOf(maxRequestsAllowed),
                String.valueOf(windowSeconds),
                member
        );

        if (result == null || result.isEmpty()) {
            // Redis unreachable / script failed to return — fail-open, same policy
            // discussed earlier for the API-key filter
            return RateLimitResult.allowed(maxRequestsAllowed);
        }
        boolean allowed = result.get(0) == 1L;
        int remaining = result.get(1).intValue();
        long oldestScoreMs = result.get(2);


        if (!allowed) {
            // DENIED — result.get(0) is the oldest surviving entry's score (as a String)

            int retryAfter = oldestScoreMs >0 ?
                    (int) Math.max(1,(oldestScoreMs + windowSeconds*1000 - nowMs) / 1000.0)
                    : (int) windowSeconds;
            return RateLimitResult.denied(retryAfter);
        }
        return RateLimitResult.allowed(remaining);


    }catch (Exception e)
        {
            log.warn("Rate limiter unavailable, failing open for key: {}, with exception: {}",key,e.getMessage());
            return RateLimitResult.allowed(maxRequestsAllowed);
        }

    }
}