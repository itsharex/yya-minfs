package com.ksyun.campus.metaserver.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StatInfo
{
    public String path;
    public long size;
    public long mtime;
    public FileType type;
    public List<ReplicaData> replicaData;

    public List<String> getDsNodes() {
        List<String> res = new ArrayList<>();
        replicaData.forEach(e -> {
            res.add(e.dsNode);
        });
        return res;
    }

    @Override
    public String toString() {
        return "StatInfo{" +
                "path='" + path + '\'' +
                ", size=" + size +
                ", mtime=" + mtime +
                ", type=" + type +
                ", replicaData=" + replicaData +
                '}';
    }
}
