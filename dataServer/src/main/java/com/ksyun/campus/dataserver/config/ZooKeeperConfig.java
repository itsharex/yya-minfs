package com.ksyun.campus.dataserver.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZooKeeperConfig {

    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework curatorFramework() {
        // 创建Curator客户端
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("8.130.138.230" + ":2181")
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        return client;
    }

}
