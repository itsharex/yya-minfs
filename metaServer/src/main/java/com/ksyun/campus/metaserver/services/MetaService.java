package com.ksyun.campus.metaserver.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.campus.metaserver.cache.ServerInfoCache;
import com.ksyun.campus.metaserver.domain.FileType;
import com.ksyun.campus.metaserver.domain.ReplicaData;
import com.ksyun.campus.metaserver.domain.StatInfo;
import com.ksyun.campus.metaserver.entity.ServerInfo;
import org.apache.curator.framework.CuratorFramework;
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

    /**
     * 从注册的数据服务器列表中选择一个数据服务器
     * @return 选中的数据服务器IP
     */
    public String pickDataServer(){
        // todo 通过zk内注册的ds列表，选择出来一个ds，用来后续的wirte
        // 需要考虑选择ds的策略？负载
        try {
            List<String> dolist = client.getChildren().forPath("/dataServer");
            byte[] catchData = client.getData().forPath("/dataServer/" + random(dolist));
            ObjectMapper objectMapper = new ObjectMapper();
            ServerInfo service = objectMapper.convertValue(catchData, ServerInfo.class);
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
    public boolean create(String fileSystem, String path) {
        File file = new File(path);
        File parentDirectory = file.getParentFile();

        if (!parentDirectory.exists()) {
            boolean created = parentDirectory.mkdirs();
            if (!created) {
                System.err.println("Failed to create parent directories for: " + path);
                return false;
            }
        }

        if (file.exists()) {
            System.err.println("File already exists: " + path);
            return false;
        }

        try {
            boolean created = file.createNewFile();
            if (!created) {
                System.err.println("Failed to create file: " + path);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean mkdir(String fileSystem, String path) {
        File directory = new File(path);

        if (directory.exists()) {
            System.err.println("Directory already exists: " + path);
            return false;
        }

        boolean created = directory.mkdirs();
        if (!created) {
            System.err.println("Failed to create directory: " + path);
            return false;
        }

        return true;
    }

    public List<String> listdir(String fileSystem, String path) {
        File directory = new File(path);

        if (!directory.exists()) {
            System.err.println("Directory does not exist: " + path);
            return null;
        }

        if (!directory.isDirectory()) {
            System.err.println("Given path is not a directory: " + path);
            return null;
        }

        File[] files = directory.listFiles();
        List<String> fileList = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                fileList.add(file.getName());
            }
        }

        return fileList;
    }

    public boolean delete(String fileSystem, String path) {
        File file = new File(path);

        if (!file.exists()) {
            System.err.println("File or directory does not exist: " + path);
            return false;
        }

        boolean deleted = false;
        if (file.isDirectory()) {
            deleted = deleteDirectory(file);
        } else {
            deleted = file.delete();
        }

        if (!deleted) {
            System.err.println("Failed to delete: " + path);
            return false;
        }

        return true;
    }

    private boolean deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return directory.delete();
    }

    /**
     * 在选定的数据服务器上写入文件
     * @param fileSystem 文件系统标识
     * @param path 文件路径
     * @param offset 写入起始偏移量
     * @param length 写入长度
     * @return 写入是否成功
     */
    public boolean write(String fileSystem, String path, int offset, int length) {
        String dataIp = pickDataServer();
        String uri = String.format("http://%s/write", dataIp);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("fileSystem", fileSystem);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> requestData = new HashMap<>();

        requestData.put("path", path);
        requestData.put("offset", offset);
        requestData.put("length", length);

        String requestBody = null;

        try {
            requestBody = mapper.writeValueAsString(requestData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(requestBody, httpHeaders);
        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static final String DATA_PATH = "/dataServer";
    public StatInfo getStats(String fileSystem, String path) {
        File file = new File(path);
        StatInfo statInfo = new StatInfo();
        statInfo.setPath(path);
        statInfo.setSize(file.length());
        statInfo.setType(file.isFile() ? FileType.File : FileType.Directory);
        statInfo.setMtime(System.currentTimeMillis());
        List<ReplicaData> list = new ArrayList<>();

        for (String key : cache.getAllPaths()) {
            ServerInfo serverInfo = cache.get(key);
            ReplicaData replicaData = new ReplicaData();
            replicaData.setPath(key);
            replicaData.setId(serverInfo.getRack() + "_" + serverInfo.getZone());
            replicaData.setDsNode(serverInfo.getIp() + ":" + serverInfo.getPort());
            list.add(replicaData);
        }

        statInfo.setReplicaData(list);
        return statInfo;
    }
}
