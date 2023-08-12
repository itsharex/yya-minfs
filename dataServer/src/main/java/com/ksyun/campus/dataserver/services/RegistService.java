package com.ksyun.campus.dataserver.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.campus.dataserver.entity.ServerInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class RegistService implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(RegistService.class);
    private static final String IP = "127.0.0.1";
    private static final int INITIAL_CAPACITY = 100 * 1024 * 1024;

    @Value("${server.port}")
    private int port;

    @Value("${az.rack}")
    private String rack;

    @Value("${az.zone}")
    private String zone;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CuratorFramework client;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        registToCenter();
    }

    public void registToCenter() throws Exception {
        // todo 将本实例信息注册至zk中心，包含信息 ip、port、capacity、rack、zone
        // 创建 CuratorFramework 客户端
        String ZK_REGISTRY_PATH = "/dataServer"; // 实例注册的路径
        String instanceNode = ZK_REGISTRY_PATH + "/" + rack + "_" + zone;

        // 创建父节点（如果不存在）
        if (client.checkExists().forPath(ZK_REGISTRY_PATH) == null) {
            client.create().forPath(ZK_REGISTRY_PATH);
        }

        // 创建实例临时顺序节点
        byte[] instanceData = getInstanceData().getBytes(StandardCharsets.UTF_8);

        // 使用create()方法创建临时节点
        client.create()
                .creatingParentsIfNeeded() // 如果父节点不存在，自动创建
                .withMode(CreateMode.EPHEMERAL) // 创建临时节点
                .forPath(instanceNode, instanceData);

        // 应用程序保持运行状态，直到被中断
        client.blockUntilConnected(); // 保持连接状态，阻塞线程
    }

    // 获取实例数据
    private String getInstanceData() throws JsonProcessingException {
        ServerInfo serverInfo = new ServerInfo(IP, port, INITIAL_CAPACITY, 0, 0, rack, zone);
        return objectMapper.writeValueAsString(serverInfo);
    }

    public ServerInfo getCurrentNodeData() throws Exception {
        byte[] childData = client.getData().forPath("/dataServer/" + rack + "_" + zone);
        if (childData != null) {
            String json = new String(childData, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, ServerInfo.class);
        }
        return null;
    }

    public void updateNodeData(ServerInfo serverInfo) throws Exception {
        String instanceNode = "/dataServer/" + rack + "_" + zone;
        Stat stat = client.setData().forPath(instanceNode, objectMapper.writeValueAsBytes(serverInfo));
        logger.info("Node data updated, version: {}", stat.getVersion());
    }

    public List<String> getDslist() throws Exception {
        String currentNode = rack + "_" + zone;
        List<String> nodeList = client.getChildren().forPath("/dataServer");

        // 过滤掉当前节点
        nodeList.remove(currentNode);
        List<String> res = new ArrayList<>();
        for(int i = 0; i < nodeList.size(); ++i) {
            byte[] Data = client.getData().forPath("/dataServer/" + nodeList.get(i));
            String json = new String(Data, StandardCharsets.UTF_8);
            ServerInfo serverInfo = objectMapper.readValue(json, ServerInfo.class);
            res.add(serverInfo.getIp() + ":" +serverInfo.getPort());
        }
        return res;
    }


}
