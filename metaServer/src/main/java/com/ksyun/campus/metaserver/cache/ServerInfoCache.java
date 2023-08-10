package com.ksyun.campus.metaserver.cache;

import com.ksyun.campus.metaserver.entity.ServerInfo;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class ServerInfoCache {
    private Map<String, ServerInfo> cache;

    public ServerInfoCache() {
        cache = new HashMap<>();
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

    public Collection<String> getAllPaths() {
        return cache.keySet();
    }

}
