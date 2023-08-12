package com.ksyun.campus.client.util;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class ZkUtil {

    @Value("${zookeeper.addr:8.130.138.230:2181}")
    private String zookeeperAddr;;

    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework curatorFramework() {
        System.out.println(zookeeperAddr);
        // 创建Curator客户端
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(zookeeperAddr)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        return client;
    }

    @PostConstruct
    public void postCons() throws Exception {
        // todo 初始化，与zk建立连接，注册监听路径，当配置有变化随时更新
    }
}
