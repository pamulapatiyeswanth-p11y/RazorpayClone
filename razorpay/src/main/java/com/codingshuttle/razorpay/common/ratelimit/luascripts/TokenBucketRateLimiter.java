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
/**
 * TOKEN BUCKET RATE LIMITER
 *
 * Redis structure: a HASH holding two fields per key:
 *   tokens      -> how many tokens are currently in the bucket (a float,
 *                  since fractional refill amounts accumulate between calls)
 *   lastRefill  -> the timestamp (ms) tokens were last topped up
 *
 * There is no fixed "window" at all here, unlike fixed/sliding window.
 * Instead: tokens continuously trickle back into the bucket over time,
 * up to a max of `capacity`. Every request costs 1 token. If the bucket
 * has one, the request is allowed immediately — even a big burst gets
 * served instantly as long as enough tokens have accumulated. Only once
 * the bucket is empty do requests start getting denied.
 *
 * `maxRequestsAllowed` (from the shared RateLimiter interface) is reused
 * here as the bucket CAPACITY. `windowSeconds` is used to derive the
 * REFILL RATE: refillPerSec = capacity / windowSeconds — i.e. "capacity
 * tokens fully replenish over windowSeconds", same overall throughput as
 * a fixed/sliding window with the same two numbers, but bursts are
 * allowed instead of being flattened.
 *
 * Why this MUST be one Lua script (same reasoning as fixed window's INCR
 * script, and the fix applied to sliding window):
 *   - "read current tokens -> compute refill -> check >= requested ->
 *     deduct -> write back" is a classic check-then-act sequence.
 *   - Done as separate Redis calls, two concurrent threads could both
 *     read the same token count before either writes back, both pass
 *     the check, and both deduct — over-admitting requests past capacity,
 *     identical failure mode to the sliding-window race we traced earlier.
 *   - Wrapping the whole read+refill+check+deduct+write in one script
 *     makes Redis run it as a single atomic unit — no interleaving
 *     possible between two callers' scripts.
 *
 * EXPIRE is set to enough time for the bucket to fully refill from empty
 * (capacity / refillPerSec), plus a small buffer — purely cleanup, not
 * correctness (if the key vanishes between requests, the script's
 * "bucket doesn't exist yet -> start full" branch handles it safely).
 */

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "token-bucket")
@Slf4j
public class TokenBucketRateLimiter implements RateLimiter {


        private final StringRedisTemplate redisTemplate;

        private static final String TOKEN_BUCKET_LUA_SCRIPT = """
        local key = KEYS[1]
        local capacity = tonumber(ARGV[1])
        local refillPerSec = tonumber(ARGV[2])
        local nowMs = tonumber(ARGV[3])
        local ttlSeconds = tonumber(ARGV[4])

        local bucket = redis.call("HMGET", key, "tokens", "lastRefill")
        local tokens = tonumber(bucket[1])
        local lastRefill = tonumber(bucket[2])

        if tokens == nil then
            -- bucket doesn't exist yet -> start full
            tokens = capacity
            lastRefill = nowMs
        end

        -- top up tokens based on elapsed time since last refill
       
        local elapsedSeconds = (nowMs - lastRefill) / 1000
        if elapsedSeconds > 0 then
            local refillAmount = elapsedSeconds * refillPerSec
            tokens = math.min(capacity, tokens + refillAmount)
            lastRefill = nowMs
        end
        local allowed = 0
        local retryAfterMs = 0

        if tokens >= 1 then
            tokens = tokens - 1
            allowed = 1
        else
            -- not enough tokens -> compute how long until enough refill
            local shortfall = 1 - tokens
            retryAfterMs = math.ceil((shortfall / refillPerSec) * 1000)
        end

        redis.call("HMSET", key, "tokens", tokens, "lastRefill", lastRefill)

        -- TTL: time to refill from empty, plus a small safety buffer
        redis.call("EXPIRE", key, ttlSeconds)

        return {allowed, math.floor(tokens), retryAfterMs}
        """;

        @SuppressWarnings("unchecked")
        private final RedisScript<List> tokenBucketScript =
                (RedisScript<List>) (RedisScript<?>) RedisScript.of(TOKEN_BUCKET_LUA_SCRIPT, List.class);

        @Override
        public RateLimitResult check(String key, int maxRequestsAllowed, long windowSeconds) {
            String redisKey = "ratelimit:tokenbucket:" + key;
            long nowMs = System.currentTimeMillis();
            long ttlSeconds = windowSeconds * 2;
            int capacity = maxRequestsAllowed;
            double refillPerSec = (double) capacity / windowSeconds;

            List<?> result;
            try {
                result = redisTemplate.execute(
                        tokenBucketScript,
                        List.of(redisKey),
                        String.valueOf(capacity),
                        String.valueOf(refillPerSec),
                        String.valueOf(nowMs),
                        String.valueOf(ttlSeconds) // requesting 1 token for this request
                );
            } catch (Exception e) {
                log.warn("Token bucket rate limit check failed, allowing request through. key: {}", key, e);
                return RateLimitResult.allowed(capacity); // fail-open, same policy as elsewhere
            }

            if (result == null || result.isEmpty()) {
                return RateLimitResult.allowed(capacity);
            }

            long allowedFlag = ((Number) result.get(0)).longValue();
            long remainingTokens = ((Number) result.get(1)).longValue();
            long retryAfterMs = ((Number) result.get(2)).longValue();

            if (allowedFlag == 0) {
                int retryAfterSeconds = (int) Math.max(1, Math.ceil(retryAfterMs / 1000.0));
                return RateLimitResult.denied(retryAfterSeconds);
            }

            return RateLimitResult.allowed((int) remainingTokens);
        }
}
