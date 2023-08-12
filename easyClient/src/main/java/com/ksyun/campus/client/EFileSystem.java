package com.ksyun.campus.client;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.campus.client.domain.ClusterInfo;
import com.ksyun.campus.client.domain.StatInfo;
import com.ksyun.campus.client.util.JacksonMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EFileSystem extends FileSystem{

    private String fileName="default";
    public EFileSystem() {
    }

    public EFileSystem(String fileName) {
        this.fileName = fileName;
    }

    public FSInputStream open(String path){
        if (path.endsWith("/")) {
            path.substring(0, path.length() - 1);
        }
        ResponseEntity<String> open = this.callRemote(path, "open", null);
        FSInputStream fsInputStream = new FSInputStream(path, this, open.getBody());
        return fsInputStream;
    }
    public FSOutputStream create(String path){
        if (path.endsWith("/")) {
            path.substring(0, path.length() - 1);
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
        HttpStatus status = this.callRemote(path, "delete", null).getStatusCode();
        return status == HttpStatus.OK ? true : false;
    }
    public StatInfo getFileStats(String path){
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
        HttpEntity entity = this.callRemote(path, "listdir", null);
        JacksonMapper INSTANCE = new JacksonMapper(JsonInclude.Include.NON_NULL);
        List<StatInfo> fileList = INSTANCE.fromJson((String) entity.getBody(), new TypeReference<List<StatInfo>>() {});
        return fileList;
    }
    public ClusterInfo getClusterInfo(){
        return null;
    }
}
