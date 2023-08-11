package com.ksyun.campus.client;

import com.ksyun.campus.client.domain.StatInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class main {
    public static void main(String[] args) throws IOException {
        File file = new File("/b/");
//        File parentDirectory = file.getParentFile();
//
//        if (!parentDirectory.exists()) {
//            boolean created = parentDirectory.mkdirs();
//        }
        file.createNewFile();
//        EFileSystem eFileSystem = new EFileSystem();
//        FSOutputStream fsOutputStream = eFileSystem.create("/yya/aa.txt");
//        byte[] data = "yyalvyd".getBytes();
//        fsOutputStream.write(data);
//        eFileSystem.mkdir("D:/yya/aa.txt");
//        StatInfo statInfo = eFileSystem.getFileStats("D:/yya/aa.txt");
//        List<StatInfo> list = eFileSystem.listFileStats("D:\\Redis\\Redis-x64-5.0.14.1");
//        list.forEach(e -> {
//            System.out.println(e.toString());
//        });
//        eFileSystem.delete("D:/yya");
//        System.out.println(statInfo.toString());
    }
}
