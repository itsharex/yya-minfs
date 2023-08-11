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

    public void write(DataTransferInfo dataTransferInfo){
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
        } catch (Exception e) {
            e.printStackTrace();
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

    public byte[] read(String path,int offset,int length){
        //todo 根据path读取指定大小的内容
        File file = new File(path);
        if (!file.exists()) {
            log.error("文件不存在: {}", path);
            return null;
        }

        if (!file.isFile()) {
            log.error("给定路径不是一个文件: {}", path);
            return null;
        }

        try (FileInputStream fileInputStream = new FileInputStream(file);){
            long skipped = fileInputStream.skip(offset);
            if (skipped != offset) {
                log.error("无法跳过请求的字节数.");
                return null;
            }

            byte[] buffer = new byte[length];
            int bytesRead = fileInputStream.read(buffer);

            if (bytesRead == -1) {
                log.error("已到达文件末尾.");
                return null;
            }
            return buffer;
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void otherWrite() {
        try {
            List<String> dslist = registService.getDslist();
//            dslist.forEach(e -> {
//                forwardingPost(e, dataTransferInfo);
//            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public boolean mkdir(String path) {
        File directory = new File(path);
        directory.mkdirs();
        return true;
    }

    public boolean create(String pre) throws IOException {
        File file = new File(pre);
        mkdir(file.getParent());
        file.createNewFile();
        return true;
    }

    public boolean delete(String pre) {
        File file = new File(pre);
        if (file.exists()) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
