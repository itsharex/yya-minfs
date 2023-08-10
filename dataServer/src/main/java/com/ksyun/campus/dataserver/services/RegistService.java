package com.ksyun.campus.dataserver.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.campus.dataserver.entity.ServerInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class RegistService implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(RegistService.class);
    private static final String IP = "127.0.0.1";
    private static final int INITIAL_CAPACITY = 0;

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
        ServerInfo serverInfo = new ServerInfo(IP, port, INITIAL_CAPACITY, rack, zone);
        return objectMapper.writeValueAsString(serverInfo);
    }

    public List<Map<String, Integer>> getDslist() throws Exception {
        List<String> doList = client.getChildren().forPath("/dataServer");
        return null;
    }


}
