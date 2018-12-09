package com.monitor.apm.console.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

@RestController
@RequestMapping("/console")
public class ConsoleController {
    private static final String logPath="./apm.log";
    private static RandomAccessFile randomAccessFile=null;
    static {
        try {
            File file=new File(logPath);
            if(!file.exists()){
                file.createNewFile();
                file.setReadable(true);
                file.setWritable(true);
            }
            randomAccessFile=new RandomAccessFile(file,"rw");
        } catch (Exception e) {

        }
    }
    /**
     *  收集监控日志(日志太多的时候可以采用消息队列收集日志)
     * @param log  日志
     * <p>
     *     1.日志  这里的日志需要序列化，建议将monitor Log 放在公共模块中,方便多个服务可以序列化，否则
     *     监控服务端也要写一个monitor log 实体
     *     2.这个接口可以写成接受批量的日志，比如，当客户端收集到buffer中，当buffer 满的的时候再批量发送过来，
     *     防止日志太多，请求接口太频繁
     * </p>
     *
     */
    @PostMapping("/collect")
    public void collectLog(@RequestBody String log) throws IOException {
        if(StringUtils.isEmpty(log)){
            return;
        }
        System.out.println(log);
        randomAccessFile.write(log.getBytes());
    }
}
