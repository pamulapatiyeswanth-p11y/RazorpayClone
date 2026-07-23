package com.codingshuttle.razorpay.merchant.cache;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface ApiKeyCache {

    Optional<ApiKeyCacheEntry> get(String keyId);

    void put(String keyId, ApiKeyCacheEntry entry);

    void evict(String keyId);
}
