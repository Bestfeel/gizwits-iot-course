package com.gizwits.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by feel on 2017/1/17.
 * A cache implementing a least recently used policy.
 * 最频繁访问驻留缓存算法 LinkedHashMap，内部已经帮我们实现了该算法
 * sun.misc.LRUCache
 * py4j.reflection.LRUCache
 * org.apache.kafka.common.cache.LRUCache
 * ch.qos.logback.classic.turbo.LRUMessageCache
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private int cacheSize;

    /**
     * true代表使用访问顺序
     *
     * @param cacheSize
     */
    public LRUCache(int cacheSize) {
        super(16, 0.75f, true);
        this.cacheSize = cacheSize;
    }

    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() >= cacheSize;
    }
}
