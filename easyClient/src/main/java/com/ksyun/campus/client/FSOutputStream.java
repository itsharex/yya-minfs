package com.ksyun.campus.client;

import com.ksyun.campus.client.domain.DataTransferInfo;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.OutputStream;

public class FSOutputStream extends OutputStream {

    private String path;

    private FileSystem fileSystem;

    public FSOutputStream(String path, FileSystem fileSystem){
        this.path = path;
        this.fileSystem = fileSystem;
    }

    @Override
    public void write(int b) throws IOException {

    }

    @Override
    public void write(byte[] b) throws IOException {
        super.write(b);
        DataTransferInfo dataTransferInfo = new DataTransferInfo();
        dataTransferInfo.setPath(path);
        dataTransferInfo.setData(b);
        fileSystem.callRemote(path, "write", dataTransferInfo);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
//        super.write(b, off, len);
        DataTransferInfo dataTransferInfo = new DataTransferInfo();
        dataTransferInfo.setPath(path);
        dataTransferInfo.setData(b);
        dataTransferInfo.setOffset(off);
        dataTransferInfo.setLength(len);
        fileSystem.callRemote(path, "write", dataTransferInfo);
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
