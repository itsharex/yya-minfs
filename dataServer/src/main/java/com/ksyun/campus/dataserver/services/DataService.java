package com.ksyun.campus.dataserver.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ksyun.campus.dataserver.entity.DataTransferInfo;
import com.ksyun.campus.dataserver.entity.ServerInfo;
import com.ksyun.campus.dataserver.utils.JacksonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.URI;
import java.util.List;

@Slf4j
@Service
public class DataService {

    @Autowired
    private RegistService registService;

    public boolean write(DataTransferInfo dataTransferInfo){
        //todo 写本地
        //todo 调用远程ds服务写接口，同步副本，已达到多副本数量要求
        //todo 选择策略，按照 az rack->zone 的方式选取，将三副本均分到不同的az下
        //todo 支持重试机制
        //todo 返回三副本位置
        File file = new File(dataTransferInfo.getPath());

        try {
            // 确保父目录存在
            file.getParentFile().mkdirs();

            // 创建文件
            file.createNewFile();

            // 创建一个 FileOutputStream 对象来写入数据到文件
            OutputStream fos = new FileOutputStream(file, true);

            // 将数据的字节数组写入文件
            byte[] b = dataTransferInfo.getData();
            int off = dataTransferInfo.getOffset();
            int len = dataTransferInfo.getLength();
            // 根据偏移量和长度进行写入
            if (dataTransferInfo.getLength() > 0) {
                fos.write(b, off, len);
            } else {
                fos.write(b);
            }

            // 关闭 FileOutputStream
            fos.close();
            System.out.println("Data has been written to the file.");
            ServerInfo currentNodeData = registService.getCurrentNodeData();
            currentNodeData.setUseCapacity(currentNodeData.getUseCapacity() + dataTransferInfo.getData().length);
            registService.updateNodeData(currentNodeData);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }

    /**
     * post的请求转发方法，根据给定的接口URL和参数，进行请求转发并返回响应结果
     *
     * @param url           接口URL
     * @return 转发后接口的响应结果
     */
    private ResponseEntity forwardingPost(String url, Object param) {
        try {
            // 使用UriComponentsBuilder构建URL
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
            uriBuilder.pathSegment("write");

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            JacksonMapper jacksonMapper = new JacksonMapper(JsonInclude.Include.NON_NULL);
            String requestBody = jacksonMapper.toJson(param);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, httpHeaders);

            URI uri = uriBuilder.build().toUri();
            ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
            return responseEntity;
        } catch (HttpServerErrorException e) {
            HttpStatus status = e.getStatusCode(); // 获取异常中的状态码
            String responseBody = e.getResponseBodyAsString(); // 获取异常响应的内容
            return ResponseEntity.status(status)
                    .body("An error occurred: " + responseBody);
        }
    }

    public byte[] read(DataTransferInfo dataTransferInfo){
        //todo 根据path读取指定大小的内容
        File file = new File(dataTransferInfo.getPath());
        try {
            // 创建一个 FileInputStream 对象来读取文件
            FileInputStream fis = new FileInputStream(file);

            // 跳过偏移量
            fis.skip(dataTransferInfo.getOffset());

            int len = 0;
            if(dataTransferInfo.getLength() != 0) {
                len = dataTransferInfo.getLength();
            } else {
                len = dataTransferInfo.getData().length;
            }
            // 创建一个字节数组来存储读取的数据
            byte[] data = new byte[len];

            // 读取数据到字节数组
            int bytesRead = fis.read(data);

            // 关闭 FileInputStream
            fis.close();

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 发生异常
        }
    }


    public boolean mkdir(String path) {
        File directory = new File(path);
        directory.mkdirs();
        return true;
    }

    public boolean create(String pre) throws Exception {
        File file = new File(pre);
        mkdir(file.getParent());
        file.createNewFile();
        ServerInfo currentNodeData = registService.getCurrentNodeData();
        currentNodeData.setFileTotal(currentNodeData.getFileTotal() + 1);
        registService.updateNodeData(currentNodeData);
        return true;
    }

    public boolean delete(String pre) throws Exception {
        File file = new File(pre);
        if (file.exists()) {
            if (file.delete()) {
                ServerInfo currentNodeData = registService.getCurrentNodeData();
                currentNodeData.setFileTotal(currentNodeData.getFileTotal() - 1);
                registService.updateNodeData(currentNodeData);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
