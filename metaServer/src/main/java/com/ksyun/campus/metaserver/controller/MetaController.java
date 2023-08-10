package com.ksyun.campus.metaserver.controller;

import com.ksyun.campus.metaserver.domain.StatInfo;
import com.ksyun.campus.metaserver.services.MetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/")
public class MetaController {

    @Autowired
    private MetaService metaService;

    @RequestMapping("stats")
    public ResponseEntity stats(@RequestHeader String fileSystem,@RequestParam String path){
        StatInfo statInfo = metaService.getStats(fileSystem, path);
        return new ResponseEntity(statInfo, HttpStatus.OK);
    }
    @RequestMapping("create")
    public ResponseEntity createFile(@RequestHeader String fileSystem, @RequestParam String path){
        if(metaService.create(fileSystem, path)) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @RequestMapping("mkdir")
    public ResponseEntity mkdir(@RequestHeader String fileSystem, @RequestParam String path){
        if(metaService.mkdir(fileSystem, path)) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity<>("zookeeper连接失败或找不到对应结点", HttpStatus.valueOf(500));
        }
    }
    @RequestMapping("listdir")
    public ResponseEntity listdir(@RequestHeader String fileSystem,@RequestParam String path){
        List<String> list = metaService.listdir(fileSystem, path);
        if(list == null) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(list);
    }
    @RequestMapping("delete")
    public ResponseEntity delete(@RequestHeader String fileSystem, @RequestParam String path){
        if(metaService.delete(fileSystem, path)) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 保存文件写入成功后的元数据信息，包括文件path、size、三副本信息等
     * @param fileSystem
     * @param path
     * @param offset
     * @param length
     * @return
     */
    @RequestMapping("write")
    public ResponseEntity commitWrite(@RequestHeader String fileSystem, @RequestParam String path, @RequestParam int offset, @RequestParam int length){
        if(metaService.write(fileSystem, path, offset, length)) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据文件path查询三副本的位置，返回客户端具体ds、文件分块信息
     * @param fileSystem
     * @param path
     * @return
     */
    @RequestMapping("open")
    public ResponseEntity open(@RequestHeader String fileSystem,@RequestParam String path){
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * 关闭退出进程
     */
    @RequestMapping("shutdown")
    public void shutdownServer(){
        System.exit(-1);
    }

}
