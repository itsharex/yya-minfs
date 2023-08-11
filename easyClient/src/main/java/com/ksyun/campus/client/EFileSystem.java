package com.ksyun.campus.client;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.campus.client.domain.ClusterInfo;
import com.ksyun.campus.client.domain.StatInfo;
import com.ksyun.campus.client.util.JacksonMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class EFileSystem extends FileSystem{

    private String fileName="default";
    public EFileSystem() {
    }

    public EFileSystem(String fileName) {
        this.fileName = fileName;
    }

    public FSInputStream open(String path){
        return null;
    }
    public FSOutputStream create(String path){
        this.callRemote(path, "create", null);
        FSOutputStream fsOutputStream = new FSOutputStream(path, this);
        return fsOutputStream;
    }
    public boolean mkdir(String path){
        HttpStatus status = this.callRemote(path, "mkdir", null).getStatusCode();
        return status == HttpStatus.OK ? true : false;
    }
    public boolean delete(String path){
        HttpStatus status = this.callRemote(path, "delete", null).getStatusCode();
        return status == HttpStatus.OK ? true : false;
    }
    public StatInfo getFileStats(String path){
        HttpEntity entity = this.callRemote(path, "stats", null);
        // 从JSON字符串转换为StatInfo对象
        JacksonMapper INSTANCE = new JacksonMapper(JsonInclude.Include.NON_NULL);
        StatInfo statInfo = INSTANCE.fromJson((String) entity.getBody(), StatInfo.class);
        return statInfo; // 返回获取到的StatInfo对象
    }
    public List<StatInfo> listFileStats(String path){
        HttpEntity entity = this.callRemote(path, "listdir", null);
        JacksonMapper INSTANCE = new JacksonMapper(JsonInclude.Include.NON_NULL);
        List<String> fileList = INSTANCE.fromJson((String) entity.getBody(), List.class);
        List<StatInfo> StatInfos = new ArrayList<>();
        fileList.forEach(e -> {
            StatInfos.add(getFileStats(path + "/" + e));
        });
        return StatInfos;
    }
    public ClusterInfo getClusterInfo(){
        return null;
    }
}
