package com.ksyun.campus.client.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.campus.client.cache.DataServerCache;
import com.ksyun.campus.client.cache.MetaServerCache;
import com.ksyun.campus.client.domain.ServerInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class ZkUtil {

    private MetaServerCache metaServerCache = MetaServerCache.getInstance();

    private DataServerCache dataServerCache = DataServerCache.getInstance();


    private void zkWatchMeta(String path, CuratorFramework client) throws Exception {
        List<String> list = client.getChildren().forPath(path);
        ObjectMapper objectMapper = new ObjectMapper();
        for (String child : list) {
            System.out.println(path + "/" + child);
            byte[] childData = client.getData().forPath(path + "/" + child);
            ServerInfo serverInfo = objectMapper.readValue(new String(childData), ServerInfo.class);
            metaServerCache.add(path + "/" + child, serverInfo);
        };
    }

    private void zkGetData(String path, CuratorFramework client) throws Exception {
        List<String> list = client.getChildren().forPath(path);
        ObjectMapper objectMapper = new ObjectMapper();
        for (String child : list) {
            System.out.println(path + "/" + child);
            byte[] childData = client.getData().forPath(path + "/" + child);
            ServerInfo serverInfo = objectMapper.readValue(new String(childData), ServerInfo.class);
            dataServerCache.add(path + "/" + child, serverInfo);
        }
    }

    public void postCons() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("8.130.138.230:2181")
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        client.start();
        client.blockUntilConnected();
        // todo 初始化，与zk建立连接，注册监听路径，当配置有变化随时更新
        zkGetData("/dataServer", client);
        zkWatchMeta("/metaServer/server", client);
        client.close();
    }

    public CuratorFramework getClient() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("8.130.138.230:2181")
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        client.start();
        client.blockUntilConnected();
        return client;
    }

}
