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

        Set<Integer> selectedIndices = new HashSet<>();
        Random random = new Random();

        while (selectedIndices.size() < count) {
            int randomIndex = random.nextInt(cache.size());
            selectedIndices.add(randomIndex);
        }

        List<String> keys = new ArrayList<>(cache.keySet());

        for (Integer index : selectedIndices) {
            String randomKey = keys.get(index);
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
