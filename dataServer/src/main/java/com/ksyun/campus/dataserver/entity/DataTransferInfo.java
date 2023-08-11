package com.ksyun.campus.dataserver.entity;

import lombok.Data;

import java.util.Arrays;

@Data
public class DataTransferInfo {
    private String path;
    private int offset;
    private int length;
    private byte[] data;

    @Override
    public String toString() {
        return "DataTransferInfo{" +
                "path='" + path + '\'' +
                ", offset=" + offset +
                ", length=" + length +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
