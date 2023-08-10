package com.ksyun.campus.metaserver.entity;

import lombok.Data;

@Data
public class ServerInfo {
    private String ip;
    private int port;
    private int capacity;
    private String rack;
    private String zone;

    public ServerInfo() {}

    public ServerInfo(String ip, int port, int capacity, String rack, String zone) {
        this.ip = ip;
        this.port = port;
        this.capacity = capacity;
        this.rack = rack;
        this.zone = zone;
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", capacity=" + capacity +
                ", rack='" + rack + '\'' +
                ", zone='" + zone + '\'' +
                '}';
    }
}