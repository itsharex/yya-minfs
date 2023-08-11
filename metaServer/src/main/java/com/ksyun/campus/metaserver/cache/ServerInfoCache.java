package com.ksyun.campus.metaserver.cache;

import com.ksyun.campus.metaserver.domain.ReplicaData;
import com.ksyun.campus.metaserver.entity.ServerInfo;
import org.springframework.stereotype.Component;

import java.util.*;

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

    public List<ReplicaData> getRandomServerInfos(int count) {
        List<ReplicaData> result = new ArrayList<>();

        List<String> keys = new ArrayList<>(cache.keySet());
        Random random = new Random();

        int keysCount = keys.size();
        if (count > keysCount) {
            count = keysCount; // 保证不超过缓存中的数量
        }

        for (int i = 0; i < count; i++) {
            int randomIndex = random.nextInt(keysCount);
            String randomKey = keys.get(randomIndex);
            ServerInfo serverInfo = cache.get(randomKey);
            ReplicaData replicaData = new ReplicaData();
            replicaData.setPath(randomKey);
            replicaData.setId(serverInfo.getRack() + "_" + serverInfo.getZone());
            replicaData.setDsNode(serverInfo.getIp() + ":" + serverInfo.getPort());
            result.add(replicaData);
        }

        return result;
    }

}
