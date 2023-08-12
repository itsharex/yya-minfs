package com.ksyun.campus.client.domain;

import lombok.Data;

import java.util.List;

@Data
public class ClusterInfo {
    private MetaServerMsg masterMetaServer;
    private MetaServerMsg slaveMetaServer;
    private List<DataServerMsg> dataServer;


    @Override
    public String toString() {
        return "ClusterInfo{" +
                "masterMetaServer=" + masterMetaServer +
                ", slaveMetaServer=" + slaveMetaServer +
                ", dataServer=" + dataServer +
                '}';
    }
}
