package com.ksyun.campus.client;

import com.ksyun.campus.client.domain.StatInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class main {
    public static void main(String[] args) throws IOException {
        EFileSystem eFileSystem = new EFileSystem();
        eFileSystem.mkdir("/test/c");
        System.out.println(eFileSystem.getFileStats("/test/y"));
        List<StatInfo> list = eFileSystem.listFileStats("/test");
        list.forEach(e -> {
            System.out.println(e.toString());
        });
//        eFileSystem.delete("D:/yya");
//        System.out.println(statInfo.toString());
    }
}
