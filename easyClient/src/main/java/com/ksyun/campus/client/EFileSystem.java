package com.ksyun.campus.client;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.campus.client.cache.DataServerCache;
import com.ksyun.campus.client.cache.MetaServerCache;
import com.ksyun.campus.client.domain.*;
import com.ksyun.campus.client.util.JacksonMapper;
import com.ksyun.campus.client.util.ZkUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class EFileSystem extends FileSystem{

    private String fileName="default";

    public EFileSystem() {
    }

    public EFileSystem(String fileName) {
        this.fileName = fileName;
    }

    public FSInputStream open(String path) throws IOException {
        if (getFileStats(path) == null) {
            throw new IOException("文件不存在！");
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        ResponseEntity<String> open = this.callRemote(path, "open", null);
        FSInputStream fsInputStream = new FSInputStream(path, this, open.getBody());
        return fsInputStream;
    }
    public FSOutputStream create(String path) throws IOException {
        if (getFileStats(path) != null) {
            throw new IOException("请不要重复创建！");
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        HttpStatus status = this.callRemote(path, "create", null).getStatusCode();
        if(status != HttpStatus.OK) {
            return null;
        }
        FSOutputStream fsOutputStream = new FSOutputStream(path, this);
        return fsOutputStream;
    }
    public boolean mkdir(String path) {
        HttpStatus status = this.callRemote(path, "mkdir", null).getStatusCode();
        return status == HttpStatus.OK ? true : false;
    }
    public boolean delete(String path){
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        HttpStatus status = this.callRemote(path, "delete", null).getStatusCode();
        return status == HttpStatus.OK ? true : false;
    }
    public StatInfo getFileStats(String path){
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        ResponseEntity<String> entity = this.callRemote(path, "stats", null);
        if(entity.getStatusCode() != HttpStatus.OK) {
            return null;
        }
        // 从JSON字符串转换为StatInfo对象
        JacksonMapper INSTANCE = new JacksonMapper(JsonInclude.Include.NON_NULL);
        StatInfo statInfo = INSTANCE.fromJson(entity.getBody(), StatInfo.class);
        return statInfo; // 返回获取到的StatInfo对象
    }
    public List<StatInfo> listFileStats(String path){
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        HttpEntity entity = this.callRemote(path, "listdir", null);
        JacksonMapper INSTANCE = new JacksonMapper(JsonInclude.Include.NON_NULL);
        List<StatInfo> fileList = INSTANCE.fromJson((String) entity.getBody(), new TypeReference<List<StatInfo>>() {});
        return fileList;
    }
    public ClusterInfo getClusterInfo() throws Exception {
        ZkUtil zkUtil = new ZkUtil();
        zkUtil.postCons();
        MetaServerCache metaServerCache = MetaServerCache.getInstance();
        DataServerCache dataServerCache = DataServerCache.getInstance();

        ClusterInfo clusterInfo = new ClusterInfo();
        if (metaServerCache.containsKey("/metaServer/server/master")) {
            MetaServerMsg metaServerMsg = new MetaServerMsg();
            ServerInfo master = metaServerCache.get("/metaServer/server/master");
            metaServerMsg.setHost(master.getIp());
            metaServerMsg.setPort(master.getPort());
            clusterInfo.setMasterMetaServer(metaServerMsg);
        }
        if (metaServerCache.containsKey("/metaServer/server/slave")) {
            MetaServerMsg metaServerMsg = new MetaServerMsg();
            ServerInfo slave = metaServerCache.get("/metaServer/server/slave");
            metaServerMsg.setHost(slave.getIp());
            metaServerMsg.setPort(slave.getPort());
            clusterInfo.setSlaveMetaServer(metaServerMsg);
        }
        List<ServerInfo> servers = dataServerCache.getServers();
        List<DataServerMsg> dataList = new ArrayList<>();
        for (ServerInfo serverInfo : servers) {
            DataServerMsg dataServerMsg = new DataServerMsg();
            dataServerMsg.setCapacity(serverInfo.getCapacity());
            dataServerMsg.setFileTotal(serverInfo.getFileTotal());
            dataServerMsg.setUseCapacity(serverInfo.getUseCapacity());
            dataServerMsg.setPort(serverInfo.getPort());
            dataServerMsg.setHost(serverInfo.getIp());
            dataList.add(dataServerMsg);
        }
        clusterInfo.setDataServer(dataList);
        metaServerCache.clear();
        dataServerCache.clear();
        return clusterInfo;
    }
}
