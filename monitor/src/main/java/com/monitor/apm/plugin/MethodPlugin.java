package com.monitor.apm.plugin;

import com.alibaba.fastjson.JSON;
import com.monitor.apm.collector.HttpClient;
import com.monitor.apm.monitor.ApmAgent;
import com.monitor.apm.monitor.MonitorLog;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.tomcat.jni.Local;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class MethodPlugin extends AbstractPointcut {
    private static RandomAccessFile randomAccessFile=null;

    static {
        try {
            System.out.println(ApmAgent.getConfigFile());

            File file=new File(ApmAgent.getConfigFile());
            if(!file.exists()){
                file.createNewFile();
                file.setReadable(true);
                file.setWritable(true);
            }
            randomAccessFile=new RandomAccessFile(file,"rw");
            System.out.println(randomAccessFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    ThreadLocal<MonitorLog> threadLocal=ThreadLocal.withInitial(()-> new MonitorLog());
    @Override
    public ElementMatcher<? super TypeDescription>  point() {
        return (ElementMatcher<TypeDescription>) target -> target.getName().startsWith("com.monitor.apm.service.controller.UserController");
    }

    @Override
    public ElementMatcher<? super MethodDescription> method() {
        return (ElementMatcher<MethodDescription>) target -> target.getName().startsWith("getUserById");
    }

    /**
     * 创建拦截日志
     * @param instance 要拦截的实例
     * @param method    拦截的方法签名
     * @param params    方法参数
     */
    @Override
    public void before(Object instance, Method method, Object[] params) {
        log.debug("start before interceptor for class:{},method:{}",instance.getClass(),method.getName());
        MonitorLog build = MonitorLog.builder().startTime(String.valueOf(new Date().getTime())).methodName(method.getName()).build();
        threadLocal.set(build);
    }

    @Override
    public void after(Object instance, Method method, Object[] params, Object result) throws IOException {
        log.debug("start after interceptor for class:{},method:{}",instance.getClass(),method.getName());
        MonitorLog monitorLog = threadLocal.get();
        if(monitorLog==null){
            log.error("no  any  before monitor log existed");
            monitorLog=MonitorLog.builder().methodName(method.getName()).endTime(String.valueOf(new Date().getTime())).timeConsume("N/A").startTime("N/A").build();
            threadLocal.set(monitorLog);
        }else {
            long endTime= new Date().getTime();
            monitorLog.setEndTime(String.valueOf(endTime));
            String startTime = monitorLog.getStartTime();
            long timeConsume=endTime-Long.valueOf(startTime);//计算方法耗时
            monitorLog.setTimeConsume(String.valueOf(timeConsume));
        }
        String format = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        randomAccessFile.write((format+"-"+JSON.toJSONString(monitorLog,false)+"\r\n").getBytes());
        if(!ApmAgent.getTest()){
            //发起异步请求，发送日志
            MonitorLog finalMonitorLog = monitorLog;
            CompletableFuture.runAsync(()->{
                HttpClient.post(format+"-"+JSON.toJSONString(finalMonitorLog,false));
            });
        }
        threadLocal.remove();
    }

    /**
     * 记录异常到日志中
     * @param instance   拦截实例
     * @param method    拦截方法
     * @param params    拦截参数
     * @param throwable 异常信息
     */
    @Override
    public void handleThrow(Object instance, Method method, Object[] params, Throwable throwable) {
        log.debug("start throw interceptor for class:{},method:{}",instance.getClass(),method.getName());
        MonitorLog monitorLog = threadLocal.get();
        if(monitorLog==null){
            log.error("no  any  before monitor log existed");
            monitorLog=MonitorLog.builder().methodName(method.getName()).endTime(String.valueOf(new Date().getTime())).timeConsume("N/A").startTime("N/A").timeConsume("N/A").exception(throwable.getLocalizedMessage()).build();
            threadLocal.set(monitorLog);
        }else {
            monitorLog.setException(throwable.getLocalizedMessage());
        }
    }
}
