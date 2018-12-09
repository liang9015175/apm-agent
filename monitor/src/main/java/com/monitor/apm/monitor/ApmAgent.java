package com.monitor.apm.monitor;

import com.monitor.apm.plugin.AbstractPointcut;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.util.ServiceLoader;

public class ApmAgent {

    public static void premain(String agentArgs, Instrumentation instrumentation){

        AgentBuilder.Listener listener=new AgentBuilder.Listener() {
            public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
                //System.out.println("discovery...");

            }

            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded, DynamicType dynamicType) {
                System.out.println("transform....");
            }

            public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {
                //System.out.println("ignored...");
            }

            public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
                //System.out.println("error...");
            }

            public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
                //System.out.println("complete...");
            }
        };
        setConfigFile(agentArgs);
        //加载配置
        ServiceLoader<AbstractPointcut> load = ServiceLoader.load(AbstractPointcut.class);
        for (AbstractPointcut pointcut:load){
            new AgentBuilder
                    .Default()
                    .type(pointcut.point())
                    .transform((builder, typeDescription, classLoader, module) -> builder.method(pointcut.method()).intercept(MethodDelegation.to(ApmInterceptor.class)))
                    .with(listener).installOn(instrumentation);
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
