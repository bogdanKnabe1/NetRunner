package com.ninpou.packetcapture.core.util.common

import java.util.*

class LruCache<K, V>(private val maxSize: Int, @field:Transient private val callback: CleanupCallback<V>) : LinkedHashMap<K, V>(maxSize + 1, 1f, true) {
    override fun removeEldestEntry(eldest: Map.Entry<K, V>): Boolean {
        if (size > maxSize) {
            callback.cleanUp(eldest.value)
            return true
        }
        return false
    }

    interface CleanupCallback<V> {
        fun cleanUp(v: V)
    }
}