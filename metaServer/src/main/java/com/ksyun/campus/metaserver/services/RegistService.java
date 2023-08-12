package com.ksyun.campus.metaserver.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.campus.metaserver.cache.ServerInfoCache;
import com.ksyun.campus.metaserver.domain.StatInfo;
import com.ksyun.campus.metaserver.entity.ServerInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RegistService implements ApplicationRunner {

    private static final String IP = "127.0.0.1";

    @Value("${server.port}")
    private int prot;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CuratorFramework client;

    @Autowired
    private ServerInfoCache cache;

    final String ZK_REGISTRY_PATH = "/metaServer"; // 实例注册的路径
    final String instanceNode = ZK_REGISTRY_PATH + "/with-port-" + prot;
    final String META_PATH = ZK_REGISTRY_PATH + "/data";

    public void registToCenter() throws Exception {
        // todo 将本实例信息注册至zk中心，包含信息 ip、port
        // 创建 CuratorFramework 客户端

        // 创建父节点（如果不存在）
        if (client.checkExists().forPath(ZK_REGISTRY_PATH) == null) {
            client.create().forPath(ZK_REGISTRY_PATH);
        }

        // 创建存储节点（如果不存在）
        if (client.checkExists().forPath(META_PATH) == null) {
            client.create().forPath(META_PATH);
        }

        // 创建实例临时顺序节点
        byte[] instanceData = getInstanceData().getBytes(StandardCharsets.UTF_8);

        // 使用create()方法创建临时节点
        client.create()
                .creatingParentsIfNeeded() // 如果父节点不存在，自动创建
                .withMode(CreateMode.EPHEMERAL) // 创建临时节点
                .forPath(instanceNode, instanceData);

        // 应用程序保持运行状态，直到被中断
        client.blockUntilConnected();
        zkWatch("/dataServer");
    }

    /**
     * 注册监听
     * TreeCache: 可以将指定的路径节点作为根节点（祖先节点），对其所有的子节点操作进行监听，
     * 呈现树形目录的监听，可以设置监听深度，最大监听深度为 int 类型的最大值。
     */
    private void zkWatch(String path) throws Exception {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path, true);
        pathChildrenCache.start();

        PathChildrenCacheListener listener = (client1, event) -> {
            String childPath = event.getData().getPath();
            byte[] childData = event.getData().getData();
            ServerInfo serverInfo = objectMapper.readValue(new String(childData), ServerInfo.class);
            System.out.println(serverInfo.getIp());
            switch (event.getType()) {
                case CHILD_ADDED:
                    cache.add(childPath, serverInfo);
                    System.out.println("Child added: " + childPath);
                    System.out.println("Child data: " + new String(childData));
                    System.out.println(event.getType());
                    break;
                case CHILD_UPDATED:
                    System.out.println("Child updated: " + childPath);
                    System.out.println("Child data: " + new String(childData));
                    break;
                case CHILD_REMOVED:
                    cache.remove(childPath);
                    System.out.println("Child removed: " + childPath);
                    break;
                default:
                    break;
            }
        };

        pathChildrenCache.getListenable().addListener(listener);

        System.in.read(); // 暂停程序，保持监听状态
        pathChildrenCache.close();
        client.close();
    }

    // 获取实例数据
    private String getInstanceData() throws JsonProcessingException {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setIp(IP);
        serverInfo.setPort(prot);
        return objectMapper.writeValueAsString(serverInfo);
    }

    public ServerInfo getCurrentNodeData() throws Exception {
        byte[] childData = client.getData().forPath(instanceNode);
        if (childData != null) {
            String json = new String(childData, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, ServerInfo.class);
        }
        return null;
    }

    public void updateNodeData(String path, StatInfo statInfo){
        try {
            client.setData().forPath(META_PATH + path, objectMapper.writeValueAsBytes(statInfo));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        registToCenter();
    }
}
