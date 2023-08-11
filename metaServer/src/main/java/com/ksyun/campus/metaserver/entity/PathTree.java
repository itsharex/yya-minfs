package com.ksyun.campus.metaserver.entity;

import com.ksyun.campus.metaserver.domain.StatInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PathTree {
    private String name;
    private boolean isDirectory;
    private StatInfo statInfo;
    private List<PathTree> children;

    public PathTree(String name, boolean isDirectory, StatInfo statInfo) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.statInfo = statInfo;
        this.children = new ArrayList<>();
    }

}

