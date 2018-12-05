package com.monitor.apm.monitor;

import com.alibaba.fastjson.JSON;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.Callable;

public class ApmInterceptor {
    private static RandomAccessFile randomAccessFile=null;
    static {
        try {
            File file=new File(ApmAgent.getConfigFile());
            if(!file.exists()){
                file.createNewFile();
                file.setReadable(true);
                file.setWritable(true);
            }
            randomAccessFile=new RandomAccessFile(file,"rw");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @RuntimeType
    public static Object intercept(@Origin Method method, @SuperCall Callable<?> callable) throws IOException {
        long start=System.currentTimeMillis();
        String name = method.getName();

        MonitorLog build = MonitorLog.builder().methodName(name).createTime(new Date()).build();
        try{
            return callable.call();
        }catch (Exception e){
            //记录异常到日志中
            build.setException(e.getLocalizedMessage());
            throw new RuntimeException(e);
        } finally{
            long timeConsume = System.currentTimeMillis() - start;
            build.setTimeConsume(timeConsume);
            System.out.println(JSON.toJSONString(build));
            randomAccessFile.write((JSON.toJSONString(build,false)+"\n").getBytes());
        }
    }
}
