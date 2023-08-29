package com.ksyun.campus.client;

import com.ksyun.campus.client.domain.StatInfo;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        EFileSystem eFileSystem = new EFileSystem();
        System.out.println(eFileSystem.getClusterInfo().toString());
        eFileSystem.mkdir("/ee/kk/");
        System.out.println(eFileSystem.getFileStats("/ee/kk"));
        FSOutputStream fsOutputStream = eFileSystem.create("/ee/kk/e.txt");
        for (int i = 0; i < 100; ++i) {
            fsOutputStream.write("aaaa".getBytes());
        }
        fsOutputStream.close();
        System.out.println("////////////");
        System.out.println(eFileSystem.getClusterInfo().toString());
        StatInfo fileStats = eFileSystem.getFileStats("/ee/kk/e.txt");
        System.out.println("aaaaa");
        System.out.println(fileStats);
        FSInputStream fsInputStream = eFileSystem.open("/ee/kk/e.txt");
        int byteValue;
        while ((byteValue = fsInputStream.read()) != -1) {
            System.out.print((char) byteValue);
        }
        byte[] buffer = new byte[100]; // 创建一个字节数组用于存储读取的数据
        int bytesRead;

//         从输入流中读取数据到字节数组
        bytesRead = fsInputStream.read(buffer); // 最多读取5个字节

        if (bytesRead != -1) {
            String data = new String(buffer, 0, bytesRead);
            System.out.println("Read data: " + data);
        } else {
            System.out.println("No more data to read.");
        }
        fsInputStream.close();
        List<StatInfo> list = eFileSystem.listFileStats("/ee");
        list.forEach(e -> {
            System.out.println(e.toString());
        });
        eFileSystem.delete("/ee/kk/e.txt");
        System.out.println("////////////");
        System.out.println(eFileSystem.getClusterInfo().toString());
    }
}