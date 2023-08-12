package com.ksyun.campus.dataserver.controller;

import com.ksyun.campus.dataserver.entity.DataTransferInfo;
import com.ksyun.campus.dataserver.services.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController("/")
public class DataController {

    @Autowired
    private DataService dataService;

    @Value("${az.rack}")
    private String rack;

    @Value("${az.zone}")
    private String zone;

    @RequestMapping("mkdir")
    public ResponseEntity mkdir(@RequestBody String path){
        String cleanedPath = path.replace("\"", "");
        String pre = "/" + rack + "/" + zone + cleanedPath;
        System.out.println(pre);
        if(dataService.mkdir(pre)) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity<>("错误", HttpStatus.valueOf(500));
        }
    }

    @RequestMapping("delete")
    public ResponseEntity delete(@RequestBody String path) throws Exception {
        String cleanedPath = path.replace("\"", "");
        String pre = "/" + rack + "/" + zone + cleanedPath;
        if(dataService.delete(pre)) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity<>("错误", HttpStatus.valueOf(500));
        }
    }

    @RequestMapping("create")
    public ResponseEntity create(@RequestBody String path) throws Exception {
        String cleanedPath = path.replace("\"", "");
        String pre = "/" + rack + "/" + zone + cleanedPath;
        if(dataService.create(pre)) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity<>("错误", HttpStatus.valueOf(500));
        }
    }


    /**
     * 1、读取request content内容并保存在本地磁盘下的文件内
     * 2、同步调用其他ds服务的write，完成另外2副本的写入
     * 3、返回写成功的结果及三副本的位置
     * @return
     */
    @RequestMapping("write")
    public ResponseEntity writeFile(@RequestBody DataTransferInfo dataTransferInfo){
        String pre = "/" + rack + "/" + zone + dataTransferInfo.getPath();
        dataTransferInfo.setPath(pre);
        boolean res = dataService.write(dataTransferInfo);
        if(!res) {
            return new ResponseEntity<>("插入数据失败！", HttpStatus.valueOf(500));
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * 在指定本地磁盘路径下，读取指定大小的内容后返回
     * @return
     */
    @RequestMapping("read")
    public ResponseEntity readFile(@RequestBody DataTransferInfo dataTransferInfo){
        String pre = "/" + rack + "/" + zone + dataTransferInfo.getPath();
        dataTransferInfo.setPath(pre);

        byte[] res = dataService.read(dataTransferInfo);
        if(res == null) {
            return new ResponseEntity<>("读取数据失败！", HttpStatus.valueOf(500));
        }
        return ResponseEntity.ok(res);
    }
    /**
     * 关闭退出进程
     */
    @RequestMapping("shutdown")
    public void shutdownServer(){
        System.exit(-1);
    }
}
