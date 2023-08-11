package com.ksyun.campus.metaserver.entity;

import lombok.Data;

@Data
public class DataTransferInfo {
    private String path;
    private int offset;
    private int length;
    private byte[] data;

}
