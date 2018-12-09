package com.monitor.apm.monitor;

import com.monitor.apm.plugin.AbstractPointcut;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;

import java.lang.instrument.Instrumentation;
import java.util.ServiceLoader;

public class ApmAgent {

    public static void premain(String agentArgs, Instrumentation instrumentation){

        setConfigFile(agentArgs);
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

    private static void setConfigFile(String filePath){
        logFilePath=filePath;
    }
    public static String getConfigFile(){
        return logFilePath;
    }
}
