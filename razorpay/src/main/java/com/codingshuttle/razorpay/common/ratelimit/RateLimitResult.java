package com.codingshuttle.razorpay.common.ratelimit;


// This decides if current request can be allowed and when can the next one is allowed
public record RateLimitResult(

        boolean isAllowed,
        int remaining,
        int retryAfterSeconds
) {
   public static RateLimitResult allowed(int remaining){
       return new RateLimitResult(true,remaining,0);

   }

    public static RateLimitResult denied(int retryAfterSeconds){
        return new RateLimitResult(false,0,retryAfterSeconds);

    }

}
