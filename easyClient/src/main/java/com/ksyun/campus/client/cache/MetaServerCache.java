package com.ksyun.campus.client.cache;

import com.ksyun.campus.client.domain.ServerInfo;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MetaServerCache {
    public static final MetaServerCache instance = new MetaServerCache();
    private Map<String, ServerInfo> cache;

    public MetaServerCache() {
        cache = new HashMap<>();
    }

    public static MetaServerCache getInstance() {
        return instance;
    }

    public void add(String path, ServerInfo serverInfo) {
        cache.put(path, serverInfo);
    }

    public ServerInfo get(String path) {
        return cache.get(path);
    }

    public void remove(String path) {
        cache.remove(path);
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }

    public boolean containsKey(String key) {
        return cache.containsKey(key);
    }

}
