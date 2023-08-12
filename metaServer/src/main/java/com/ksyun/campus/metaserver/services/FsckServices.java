package com.ksyun.campus.metaserver.services;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Service
public class FsckServices {

    @Autowired
    private CuratorFramework client;

    Logger logger = Logger.getLogger("log");

    //@Scheduled(cron = "0 0 0 * * ?") // 每天 0 点执行
    @Scheduled(fixedRate = 30 * 60 * 1000) // 每隔 30 分钟执行一次
    public void fsckTask() {
        try {
            FileHandler fileHandler = new FileHandler("log", true);
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fileHandler.setFormatter(simpleFormatter);
            logger.addHandler(fileHandler);
            Map<String, Object> log = new HashMap<>();
            // todo 全量扫描文件列表
            List<String> nodeList = new ArrayList<>();
            getNodePathsRecursive(client, "/metaServer/data", nodeList);
            log.put("文件列表:", nodeList);
            logger.info(log.toString());
            // todo 检查文件副本数量是否正常
            Map<String, String> log2 = new HashMap<>();
            List<String> list = client.getChildren().forPath("/dataServer");
            log2.put("副本:", list.toString());
            log2.put("副本数量:", String.valueOf(list.size()));
            if (list.size() == 4) {
                log2.put("副本状态:", "正常");
            } else {
                log2.put("副本状态:", "异常");
            }
            logger.info(log2.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getNodePathsRecursive(CuratorFramework client, String parentPath, List<String> nodeList) throws Exception {
        nodeList.add(parentPath);
        List<String> children = client.getChildren().forPath(parentPath);

        for (String child : children) {
            String childPath = parentPath + "/" + child;
            getNodePathsRecursive(client, childPath, nodeList);
        }

    }


}