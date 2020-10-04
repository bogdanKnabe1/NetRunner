package com.ninpou.packetcapture.core.util.common;

import java.util.LinkedHashMap;

public class LruCache<K, V> extends LinkedHashMap<K, V> {
    private int maxSize;
    private transient CleanupCallback<V> callback;

    public LruCache(int maxSize, CleanupCallback<V> callback) {
        super(maxSize + 1, 1, true);

        this.maxSize = maxSize;
        this.callback = callback;
    }


    @Override
    protected boolean removeEldestEntry(Entry<K, V> eldest) {
        if (size() > maxSize) {
            callback.cleanUp(eldest.getValue());
            return true;
        }
        return false;
    }

    public interface CleanupCallback<V> {
        void cleanUp(V v);
    }
}