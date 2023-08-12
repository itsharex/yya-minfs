package com.ksyun.campus.client.cache;

import com.ksyun.campus.client.domain.ServerInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataServerCache {
    private static final DataServerCache instance = new DataServerCache();
    private Map<String, ServerInfo> cache;

    public static DataServerCache getInstance() {
        return instance;
    }

    public DataServerCache() {
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

    public boolean containsKey(String key) {
        return cache.containsKey(key);
    }

    public List<ServerInfo> getServers() {
        List<ServerInfo> list = new ArrayList<>();
        for(String key : cache.keySet()) {
            list.add(cache.get(key));
        }
        return list;
    }
}
