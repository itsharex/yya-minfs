package com.ksyun.campus.client;

import com.ksyun.campus.client.FileSystem;
import com.ksyun.campus.client.domain.DataTransferInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class FSOutputStream extends OutputStream {

    private static final int BUFFER_SIZE = 1024; // 1KB buffer size

    private String path;
    private FileSystem fileSystem;
    private List<Byte> buffer = new ArrayList<>();

    public FSOutputStream(String path, FileSystem fileSystem) {
        this.path = path;
        this.fileSystem = fileSystem;
    }

    @Override
    public void write(int b) throws IOException {
        buffer.add((byte) b);
        flushIfBufferFull();
    }

    @Override
    public void write(byte[] b) throws IOException {
        for (byte value : b) {
            buffer.add(value);
        }
        flushIfBufferFull();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (int i = off; i < off + len; i++) {
            buffer.add(b[i]);
        }
        flushIfBufferFull();
    }

    @Override
    public void close() throws IOException {
        flushBuffer();
        super.close();
    }

    private void flushIfBufferFull() throws IOException {
        if (buffer.size() >= BUFFER_SIZE) {
            flushBuffer();
        }
    }

    private void flushBuffer() throws IOException {
        if (!buffer.isEmpty()) {
            byte[] data = new byte[buffer.size()];
            for (int i = 0; i < buffer.size(); i++) {
                data[i] = buffer.get(i);
            }
            DataTransferInfo dataTransferInfo = new DataTransferInfo();
            dataTransferInfo.setPath(path);
            dataTransferInfo.setData(data);
            fileSystem.callRemote(path, "write", dataTransferInfo);
            buffer.clear();
        }
    }
}
