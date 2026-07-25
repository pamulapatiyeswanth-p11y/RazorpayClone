package com.codingshuttle.razorpay.common.ratelimit;

/**
 * FIXED WINDOW RATE LIMITER
 *
 * One Redis key = one whole time window (e.g. 60s), shared by ALL requests
 * that land inside it. There is no per-request record — just a single
 * counter that every request increments.
 *
 * How the "window" actually forms (no polling, no scheduled check):
 *   - 1st request  -> INCR creates the key, count = 1 -> EXPIRE is set HERE, for the full windowSeconds
 *   - 2nd, 3rd...  -> INCR the SAME key, count keeps climbing, no new expiry is set
 *   - Redis itself deletes the key automatically once the TTL hits 0
 *     (nothing in our code "checks" for this — Redis just removes it)
 *   - Next request after that -> INCR on a missing key -> Redis treats it as 0,
 *     creates it fresh -> count = 1 again -> a brand NEW window starts right here
 *
 * Why the INCR + EXPIRE must be ONE Lua script, not two separate calls:
 *   - redisTemplate.increment() then redisTemplate.expire() = TWO round trips
 *   - if the app crashes / GC pauses / network blips between those two calls,
 *     the key is left with NO expiry -> it becomes permanent -> every future
 *     request against it gets rejected forever
 *   - wrapping both INCR and the conditional EXPIRE in one Lua script makes
 *     Redis run them as a single atomic unit -> no gap for that to happen
 *
 * KNOWN FLAW — boundary burst:
 *   Window resets are a hard cliff at a fixed clock boundary, not a rolling
 *   check. So (maxRequests) at 0:59 of window A + (maxRequests) at 0:01 of
 *   window B = up to 2x the limit within ~2 real seconds, even though each
 *   window individually reported "under the limit."
 *   -> acceptable for low-stakes / generous limits (admin endpoints)
 *   -> NOT acceptable for security-sensitive or hard-capacity-limited
 *      endpoints (login/OTP, outbound bank calls) -> use sliding window /
 *      token bucket / leaky bucket there instead.
 *
 * Redis key shape used here: "ratelimit:fixed:{key}"
 *   KEYS[1] in the Lua script = this Redis key
 *   ARGV[1] in the Lua script = windowSeconds (as a string)
 *   script return value        = the count AFTER incrementing (as Long)
 */


import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.method",havingValue = "fixed")
public class FixedWindowRateLimiter implements RateLimiter{
    private final StringRedisTemplate redisTemplate;


    private static final String LUA_SCRIPT = """
        local count = redis.call("INCR", KEYS[1])
        if count == 1 then
            redis.call("EXPIRE", KEYS[1], ARGV[1])
        end
        return count
        """;

    private final RedisScript<Long> incrementAndExpireScript =
            RedisScript.of(LUA_SCRIPT, Long.class);


    @Override
    public RateLimitResult check(String key, int maxRequestsAllowed, long windowSeconds) {
        String redisKey = "ratelimit:fixed:"+key;
//        Long count = redisTemplate.opsForValue().increment(redisKey);
        Long count = redisTemplate.execute(incrementAndExpireScript,
                List.of(redisKey),
                String.valueOf(windowSeconds));
        if(count == null){
                return RateLimitResult.allowed(maxRequestsAllowed);
        }

//        if(count == 1){
//            redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds)); // Set expiry
//        };
        if(count > maxRequestsAllowed){
            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);// Gives when this key expires in the redis store.
            int retryAfter = (ttl !=null && ttl>0) ? ttl.intValue() : (int) windowSeconds;
            return RateLimitResult.denied(retryAfter);

        }
        return RateLimitResult.allowed((int) (maxRequestsAllowed-count)); // return remaining req allowed count
    }
}
