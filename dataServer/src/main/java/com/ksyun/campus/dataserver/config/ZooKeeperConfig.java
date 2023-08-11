package com.ksyun.campus.dataserver.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZooKeeperConfig {

    @Value("${zookeeper.addr:10.0.0.201:2181}")
    private String zookeeperAddr;

    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework curatorFramework() {
        // 创建Curator客户端
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(zookeeperAddr)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        return client;
    }

}
