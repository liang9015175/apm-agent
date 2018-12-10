package com.monitor.apm.monitor;

import com.monitor.apm.plugin.AbstractPointcut;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;

import java.lang.instrument.Instrumentation;
import java.util.ServiceLoader;

public class ApmAgent {

    public static void premain(String agentArgs, Instrumentation instrumentation){
        if(agentArgs!=null){
            String[] args = agentArgs.split(",");
            if(args.length<2){
                setConfigFile(args[0]);
            }else {
                setConfigFile(args[0]);
                if(args[1].trim().equals("false")){
                    setTest(false);
                }else {
                    setTest(true);
                }

            }
        }
        //加载配置
        ServiceLoader<AbstractPointcut> load = ServiceLoader.load(AbstractPointcut.class);
        for (AbstractPointcut pointcut:load){
            new AgentBuilder
                    .Default()
                    .type(pointcut.point())
                    .transform((builder, typeDescription, classLoader, module) -> builder.method(pointcut.method()).intercept(MethodDelegation.to(ApmInterceptor.class)))
                    .installOn(instrumentation);
        }
        System.out.println("args:"+agentArgs+"\n");
    }
    private static String logFilePath="";

    //是否是测试,如果是测试则不向console 发送监控数据，监控数据只存在在本地monitor.log
    private static Boolean test=true;

    private static void setConfigFile(String filePath){
        logFilePath=filePath;
    }
    public static String getConfigFile(){
        return logFilePath;
    }
    public static void setTest(Boolean needTest){
        test=needTest;
    }
    public static Boolean getTest(){
        return test;
    }
}
