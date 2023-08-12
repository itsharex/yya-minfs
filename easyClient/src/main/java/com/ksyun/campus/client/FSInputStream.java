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

    private byte[] localBuffer; // 用于缓存数据
    private int localBufferPosition; // 缓存中数据的位置

    public FSInputStream(String path, FileSystem fileSystem, String dataPath) {
        this.path = path;
        this.fileSystem = fileSystem;
        this.dataPath = dataPath;
        this.readPosition = 0;
        this.localBuffer = new byte[1024 * 1024];
        this.localBufferPosition = 0;
        fillLocalBuffer();
    }

    @Override
    public int read() throws IOException {
        if (localBufferPosition >= localBuffer.length) {
            fillLocalBuffer();

            if (localBufferPosition >= localBuffer.length) {
                return -1; // 缓冲区中没有更多数据可读
            }
        }

        int byteValue = localBuffer[localBufferPosition] & 0xFF;
        if(byteValue == 0) {
            return -1;
        }
        localBufferPosition++;
        return byteValue;
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

    // 在本地缓存中填充数据
    private void fillLocalBuffer() {
        DataTransferInfo dataTransferInfo = new DataTransferInfo();
        dataTransferInfo.setPath(path);
        dataTransferInfo.setOffset(readPosition);
        dataTransferInfo.setLength(localBuffer.length);
        ResponseEntity<String> data = fileSystem.forwardingPost(dataPath, "read", dataTransferInfo);

        if (data.getStatusCode() == HttpStatus.OK) {
            byte[] responseData = data.getBody().getBytes();
            if (responseData.length > 0) {
                System.arraycopy(responseData, 0, localBuffer, 0, responseData.length);
                localBufferPosition = 0;
                readPosition += responseData.length;
            }
        }
    }

    @Override
    public void close() throws IOException {
        // 可以在这里进行资源的释放
        super.close();
    }
}
