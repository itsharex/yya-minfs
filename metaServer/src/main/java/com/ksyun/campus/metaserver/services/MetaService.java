package com.ksyun.campus.metaserver.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.campus.metaserver.cache.ServerInfoCache;
import com.ksyun.campus.metaserver.domain.FileType;
import com.ksyun.campus.metaserver.domain.ReplicaData;
import com.ksyun.campus.metaserver.domain.StatInfo;
import com.ksyun.campus.metaserver.entity.DataTransferInfo;
import com.ksyun.campus.metaserver.entity.ServerInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.checkerframework.checker.units.qual.Current;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MetaService {

    @Autowired
    private CuratorFramework client;

    @Autowired
    private ServerInfoCache cache;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ForwardService forwardService;

    @Autowired
    private RegistService registService;

    final String ZK_REGISTRY_PATH = "/metaServer/data"; // 实例注册的路径


    /**
     * 从注册的数据服务器列表中选择一个数据服务器
     *
     * @return 选中的数据服务器IP
     */
    public String pickDataServer() {
        // todo 通过zk内注册的ds列表，选择出来一个ds，用来后续的wirte
        // 需要考虑选择ds的策略？负载
        try {
            List<String> dolist = client.getChildren().forPath(ZK_REGISTRY_PATH);
            byte[] catchData = client.getData().forPath(ZK_REGISTRY_PATH + random(dolist));
            ObjectMapper objectMapper = new ObjectMapper();
            ServerInfo service = objectMapper.readValue(catchData, ServerInfo.class);
            return service.getIp() + ":" + service.getPort();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String random(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        int index = ThreadLocalRandom.current().nextInt(list.size());
        return list.get(index);
    }

    //创建文件
    public boolean create(String path) {
        boolean allRequestsSuccessful = true;
        // 逐级创建节点，并存储数据
        String[] pathParts = path.split("/");
        StringBuilder pathBuilder = new StringBuilder(ZK_REGISTRY_PATH);
        List<ReplicaData> randomServerInfos = cache.getRandomServerInfos(3);
        for (String part : pathParts) {
            if (!part.isEmpty()) {
                pathBuilder.append("/").append(part);
                String nodePath = pathBuilder.toString();
                StatInfo statInfo = creatDirStatInfo(nodePath, randomServerInfos);
                // 检查节点是否存在
                try {
                    System.out.println(nodePath);
                    // 检查节点是否存在
                    if (client.checkExists().forPath(nodePath) != null) continue;
                    String data = objectMapper.writeValueAsString(statInfo);
                    String createdNodePath = client
                            .create()
                            .forPath(nodePath, data.getBytes());
                    System.out.println("Created node: " + createdNodePath);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        String nodePath = pathBuilder.toString();
        try {
            byte[] bytes = client.getData().forPath(nodePath);
            StatInfo statInfo = objectMapper.readValue(new String(bytes), StatInfo.class);
            statInfo.setType(FileType.File);
            String data = objectMapper.writeValueAsString(statInfo);
            // 检查节点是否存在
            if (client.checkExists().forPath(nodePath) != null) {
                client.setData().forPath(nodePath, data.getBytes());
                System.out.println("Node data updated successfully.");
            } else {
                System.out.println("Node does not exist.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        for (ReplicaData e : randomServerInfos) {
            HttpStatus statusCode = forwardService.call(e.getDsNode(), "create", path).getStatusCode();
            if (statusCode != HttpStatus.OK) {
                allRequestsSuccessful = false;
                break; // Exit the loop since we encountered a failure
            }
        }

        return allRequestsSuccessful;
    }

    public boolean mkdir(String path) {
        boolean allRequestsSuccessful = true;
        // 逐级创建节点，并存储数据
        String[] pathParts = path.split("/");
        StringBuilder pathBuilder = new StringBuilder(ZK_REGISTRY_PATH);
        List<ReplicaData> randomServerInfos = cache.getRandomServerInfos(3);
        for (String part : pathParts) {
            if (!part.isEmpty()) {
                pathBuilder.append("/").append(part);
                String nodePath = pathBuilder.toString();
                StatInfo statInfo = creatDirStatInfo(nodePath, randomServerInfos);
                // 检查节点是否存在
                try {
                    System.out.println(nodePath);
                    // 检查节点是否存在
                    if (client.checkExists().forPath(nodePath) != null) continue;
                    String data = objectMapper.writeValueAsString(statInfo);
                    // 创建临时顺序节点，并存储数据
                    String createdNodePath = client
                            .create()
                            .forPath(nodePath, data.getBytes());
                    System.out.println("Created node: " + createdNodePath);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        for (ReplicaData e : randomServerInfos) {
            HttpStatus statusCode = forwardService.call(e.getDsNode(), "mkdir", path).getStatusCode();
            System.out.println(statusCode);
            if (statusCode != HttpStatus.OK) {
                allRequestsSuccessful = false;
                break; // Exit the loop since we encountered a failure
            }
        }

        return allRequestsSuccessful;
    }

    public StatInfo creatDirStatInfo(String path, List<ReplicaData> replicaDatas) {
        StatInfo statInfo = new StatInfo();
        statInfo.setPath(path);
        statInfo.setMtime(System.currentTimeMillis());
        statInfo.setSize(0);
        statInfo.setType(FileType.Directory);
        replicaDatas.forEach(e -> {
            String[] site = e.getId().split("_");
            String dataPath = "/" + site[0] + "/" + site[1] + path;
            e.setPath(dataPath);
        });
        statInfo.setReplicaData(replicaDatas);
        return statInfo;
    }

    public List<StatInfo> listdir(String path) {
        try {
            List<StatInfo> res = new ArrayList<>();
            List<String> children = client.getChildren().forPath(ZK_REGISTRY_PATH + path);
            for (String e : children) {
                System.out.println(e);
                byte[] bytes = client.getData().forPath(ZK_REGISTRY_PATH + path + "/" + e);
                String data = new String(bytes);
                System.out.println(data);
                StatInfo statInfo = objectMapper.readValue(data, StatInfo.class);
                res.add(statInfo);
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean delete(String path) {
        // 获取当前节点的子节点

        // 不存在子节点，删除当前节点
        try {
            int childrenCount = client.getChildren().forPath(ZK_REGISTRY_PATH + path).size();

            if (childrenCount == 0) {
                StatInfo statInfo = getStats(path);
                statInfo.getReplicaData().forEach(e -> {
                    forwardService.call(e.dsNode, "delete", path);
                });
                client.delete().forPath(ZK_REGISTRY_PATH + path);
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 在选定的数据服务器上写入文件
     *
     * @param dataTransferInfo 文件信息
     * @return 写入是否成功
     */
    public boolean write(DataTransferInfo dataTransferInfo) {
        StatInfo statInfo = getStats(dataTransferInfo.getPath());
        List<String> ipList = statInfo.getDsNodes();
        boolean isSuccessful = true;
        for (String ip : ipList) {
            ResponseEntity write = forwardService.call(ip, "write", dataTransferInfo);
            if (write.getStatusCode() != HttpStatus.OK) {
                isSuccessful = false;
            }
        }
        statInfo.setMtime(System.currentTimeMillis());
        statInfo.setSize(statInfo.getSize() + dataTransferInfo.getData().length);
        registService.updateNodeData(dataTransferInfo.getPath(), statInfo);
        return isSuccessful;
    }

    public StatInfo getStats(String path) {
        String res = "";
        StatInfo statInfo = null;
        try {
            byte[] data = client.getData().forPath(ZK_REGISTRY_PATH + path);
            res = new String(data);
            System.out.println(res);
            statInfo = objectMapper.readValue(res, StatInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return statInfo;
    }
}
