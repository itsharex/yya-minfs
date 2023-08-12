package com.ksyun.campus.client.domain;

import lombok.Data;

@Data
public class DataServerMsg {
    private String host;
    private int port;
    private int fileTotal;
    private int capacity;
    private int useCapacity;

    public DataServerMsg getDataMsg(String host,int port,int fileTotal,int capacity,int useCapacity){
        DataServerMsg data = new DataServerMsg();
        data.host = host;
        data.port = port;
        data.fileTotal = fileTotal;
        data.capacity = capacity;
        data.useCapacity = useCapacity;
        return data;
    }
}
