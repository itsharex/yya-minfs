package com.ksyun.campus.metaserver.cache;

import com.ksyun.campus.metaserver.domain.FileType;
import com.ksyun.campus.metaserver.domain.ReplicaData;
import com.ksyun.campus.metaserver.domain.StatInfo;
import com.ksyun.campus.metaserver.entity.PathTree;

import java.util.List;

public class MetaDataCache {
    private PathTree root;

    public MetaDataCache() {
        this.root = new PathTree("/", true, null);
    }

    // 增加节点
    public void addPath(String path, StatInfo statInfo, List<ReplicaData> ReplicaDatas) {
        String[] parts = path.split("/");
        PathTree currentNode = root;
        StringBuilder currentPath = new StringBuilder("/");
        for (String part : parts) {
            currentPath.append(part + "/");
            if (!part.isEmpty()) {
                PathTree childNode = findChild(currentNode, part);
                if (childNode == null) {
                    StatInfo currentStat = new StatInfo();
                    currentStat.setPath(currentPath.toString());
                    currentStat.setMtime(System.currentTimeMillis());
                    currentStat.setSize(0);
                    currentStat.setType(FileType.Directory);
                    currentStat.setReplicaData(ReplicaDatas);
                    childNode = new PathTree(part, false, currentStat);
                    currentNode.getChildren().add(childNode);
                }
                currentNode = childNode;
            }
        }
        boolean isDirectory = path.endsWith("/");
        if(!isDirectory) {
            currentNode.setDirectory(isDirectory);
            currentNode.setStatInfo(statInfo);
        }
    }

    // 删除节点
    public void deletePath(String path) {
        String[] parts = path.split("/");
        PathTree currentNode = root;

        for (String part : parts) {
            if (!part.isEmpty()) {
                PathTree childNode = findChild(currentNode, part);
                if (childNode == null) {
                    return; // Node not found
                }
                currentNode = childNode;
            }
        }

        currentNode.setStatInfo(null);
    }

    // 查询节点
    public PathTree getPath(String path) {
        String[] parts = path.split("/");
        PathTree currentNode = root;

        for (String part : parts) {
            if (!part.isEmpty()) {
                PathTree childNode = findChild(currentNode, part);
                if (childNode == null) {
                    return null; // Node not found
                }
                currentNode = childNode;
            }
        }

        return currentNode;
    }

    // 在给定节点查找子节点
    private PathTree findChild(PathTree node, String name) {
        for (PathTree child : node.getChildren()) {
            if (child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }
}
