package com.ksyun.campus.client;

import com.ksyun.campus.client.domain.DataTransferInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;

public class FSInputStream extends InputStream {

    private String path;
    private FileSystem fileSystem;
    private String dataPath;
    private int readPosition; // 用于记录已读取的位置

    public FSInputStream(String path, FileSystem fileSystem, String dataPath) {
        this.path = path;
        this.fileSystem = fileSystem;
        this.dataPath = dataPath;
        this.readPosition = 0;
    }

    @Override
    public int read() throws IOException {
        // 该方法需要返回一个单字节，表示一个字节的数据
        // 您需要根据 readPosition 来决定读取哪个位置的字节数据
        return -1; // 返回-1表示无数据可读（已经读取完毕）
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length); // 调用下面的 read 方法
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        DataTransferInfo dataTransferInfo = new DataTransferInfo();
        dataTransferInfo.setPath(path);
        dataTransferInfo.setOffset(readPosition); // 设置偏移量
        dataTransferInfo.setLength(len);
        ResponseEntity<String> data = fileSystem.forwardingPost(dataPath, "read", dataTransferInfo);

        if (data.getStatusCode() == HttpStatus.OK) {
            byte[] responseData = data.getBody().getBytes();
            if (responseData.length > 0) {
                System.arraycopy(responseData, 0, b, off, responseData.length);
                readPosition += responseData.length; // 更新已读取的位置
                return responseData.length;
            }
        }

        return -1;
    }

    @Override
    public void close() throws IOException {
        // 可以在这里进行资源的释放
        super.close();
    }
}
