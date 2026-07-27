package com.codingshuttle.razorpay.common.ratelimit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.UUID;

/**
 * SLIDING WINDOW LOG RATE LIMITER
 *
 * Redis structure: a SORTED SET (ZSET), NOT a single counter like fixed window.
 *   - one entry PER REQUEST: member = timestamp string, score = timestamp (ms)
 *   - key never gets wiped wholesale — only individual old entries get
 *     removed as they age out, so there's no single "reset moment" the way
 *     fixed window has -> this is what closes the boundary-burst flaw
 *
 * Per-request flow:
 *   1. floorMs = now - windowSeconds*1000        -> the start of the rolling window
 *   2. removeRangeByScore(-inf, floorMs)          -> prune only entries OLDER than
 *      the window start; anything newer is left untouched
 *   3. zCard()                                    -> count of what's LEFT after pruning
 *      = count of requests still within the last `windowSeconds`
 *   4. compare count vs maxRequestsAllowed, THEN (if allowed) add this request's
 *      own timestamp as a new entry
 *
 * *** OFF-BY-ONE BUG — FIXED: must use >=, not > ***
 *   The zCard() count is taken BEFORE this request's own entry is added.
 *   So "current > maxRequestsAllowed" is really asking "were there already
 *   MORE than the limit, not counting this one?" -> lets exactly ONE extra
 *   request slip through every time (e.g. maxAllowed=2 -> 3rd request still
 *   gets allowed, because 2 > 2 is false).
 *   Correct check: if (current >= maxRequestsAllowed) -> deny.
 *   (Contrast with FixedWindowRateLimiter: there the INCR already happened
 *   before the check, so `count > max` is correct there — don't copy that
 *   comparison here without adjusting it.)
 *
 * retryAfter calculation:
 *   - look at the OLDEST surviving entry's score (rangeWithScores(0,0))
 *   - that oldest entry will itself fall out of the window at
 *     (oldestScore + windowSeconds*1000)
 *   - retryAfter = time remaining until THAT happens
 *   -> correct because the count can only decrease once the oldest entry
 *      finally ages out — not at some fixed clock boundary like fixed window
 *
 * CLEANUP GAP — no EXPIRE set on the ZSET key anywhere.
 *   If a caller stops sending requests, its last few entries (and the key
 *   itself) sit in Redis FOREVER — nothing ever prunes them again, since
 *   pruning only happens as a side effect of a NEW request arriving.
 *   -> should set/refresh an EXPIRE on the key (e.g. windowSeconds) on every
 *      write, purely for memory hygiene (doesn't affect correctness of the
 *      count itself, since removeRangeByScore already handles that).
 */

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.method",havingValue = "sliding")
@Slf4j
public class SlidingWindowRateLimiter implements RateLimiter{
    private final StringRedisTemplate redisTemplate;

    @Override
    public RateLimitResult check(String key, int maxRequestsAllowed, long windowSeconds) {
        String redisKey = "ratelimit:sliding:"+key;
        long nowMs = System.currentTimeMillis();
        long floorMs = nowMs - windowSeconds*1000; // Calculate the timestamp 'windowSeconds' ago from the current time.
        var zSet = redisTemplate.opsForZSet();
        // Remove all requests older than the start of the sliding window.
        zSet.removeRangeByScore(redisKey,Double.NEGATIVE_INFINITY,floorMs);// Remove all requests whose timestamp is older than (current time - window duration).
        Long count = zSet.zCard(redisKey);
        long current = count!= null ? count:0; // Current count
        if(current >= maxRequestsAllowed){
            var oldest = zSet.rangeWithScores(redisKey,0,0);// Gets first element's score (millisecs) from the set
            int retryAfter = 1;
            if(oldest != null && !oldest.isEmpty()){
                Double oldestScore = oldest.iterator().next().getScore();
                if(oldestScore!=null){
                long windowExpireMs = oldestScore.longValue() + windowSeconds*1000; // First timestamp + windowseconds (in millisec)
                retryAfter = (int)Math.ceil((windowExpireMs - nowMs)/1000.0);
            }}
            return RateLimitResult.denied(retryAfter);

        }
        zSet.add(redisKey, UUID.randomUUID().toString(),nowMs);
        redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds+1));
        return RateLimitResult.allowed((int)(maxRequestsAllowed-current-1));
    }
}
