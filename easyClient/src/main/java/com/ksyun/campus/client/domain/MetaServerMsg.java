package com.ksyun.campus.client.domain;

import lombok.Data;

@Data
public class MetaServerMsg {
    private String host;
    private int port;

    public MetaServerMsg getMetaServer(String host, int port){
        MetaServerMsg data = new MetaServerMsg();
        data.setHost(host);
        data.setPort(port);
        return data;
    }
}
