package com.ksyun.campus.metaserver.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ksyun.campus.metaserver.utils.JacksonMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class ForwardService {

    public ResponseEntity call(String url, String interfaceName, Object param) {
        return forwardingPost(url, interfaceName, param);
    }

    /**
     * post的请求转发方法，根据给定的接口URL和参数，进行请求转发并返回响应结果
     *
     * @param url           接口URL
     * @param interfaceName 接口名称
     * @param param 数据
     * @return 转发后接口的响应结果
     */
    private ResponseEntity forwardingPost(String url, String interfaceName, Object param) {
        try {
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
