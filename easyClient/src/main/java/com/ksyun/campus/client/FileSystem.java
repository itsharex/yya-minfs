package com.ksyun.campus.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ksyun.campus.client.domain.DataTransferInfo;
import com.ksyun.campus.client.util.JacksonMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

public abstract class FileSystem {
    private String fileSystem = "";

    protected ResponseEntity callRemote(String path, String type, Object param) {
        if(param == null) {
            return forwardingGet("127.0.0.1:8000", path, type);
        } else {
            return forwardingPost("127.0.0.1:8000", type, param);
        }

    }

    /**
     * post的请求转发方法，根据给定的接口URL和参数，进行请求转发并返回响应结果
     *
     * @param url           接口URL
     * @param path          请求参数对象
     * @param interfaceName 接口名称
     * @return 转发后接口的响应结果
     */
    protected ResponseEntity forwardingGet(String url, String path, String interfaceName) {
        try {
            // 使用UriComponentsBuilder构建URL
            String[] parts = url.split(":");

            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();
            uriBuilder.scheme("http") // 设置协议为http
                    .host(host)
                    .port(port)
                    .pathSegment(interfaceName)
                    .queryParam("path", path);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("fileSystem", fileSystem);

            HttpEntity<String> entity = new HttpEntity<>(httpHeaders);

            URI uri = uriBuilder.build().toUri();
            ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            return responseEntity;
        } catch (HttpServerErrorException e) {
            HttpStatus status = e.getStatusCode(); // 获取异常中的状态码
            String responseBody = e.getResponseBodyAsString(); // 获取异常响应的内容

            // 可以进行错误处理，如记录日志等

            // 创建一个带异常信息的ResponseEntity并返回
            return ResponseEntity.status(status)
                    .body("An error occurred: " + responseBody);
        }
    }

    /**
     * post的请求转发方法，根据给定的接口URL和参数，进行请求转发并返回响应结果
     *
     * @param url           接口URL
     * @param interfaceName 接口名称
     * @return 转发后接口的响应结果
     */
    protected ResponseEntity forwardingPost(String url, String interfaceName, Object param) {
        try {
            // 使用UriComponentsBuilder构建URL
            String[] parts = url.split(":");

            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();
            uriBuilder.scheme("http") // 设置协议为http
                    .host(host)
                    .port(port)
                    .pathSegment(interfaceName);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("fileSystem", fileSystem);
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

}
