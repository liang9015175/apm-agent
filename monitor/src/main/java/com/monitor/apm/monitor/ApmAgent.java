package com.monitor.apm.monitor;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.any;

public class ApmAgent {

    public static void premain(String agentArgs, Instrumentation instrumentation){
        AgentBuilder.Transformer transformer=new AgentBuilder.Transformer() {
            @Override
            public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
                return builder.method(ElementMatchers.<MethodDescription>nameStartsWith("invoke")).intercept(MethodDelegation.to(ApmInterceptor.class));
            }
        };
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
        new AgentBuilder
                .Default()
                .type(ElementMatchers.<TypeDescription>nameStartsWith("org.apache.catalina.core.StandardHostValve"))
                .transform(transformer)
                .with(listener)
                .installOn(instrumentation);

        System.out.println("args:"+agentArgs+"\n");
        setConfigFile(agentArgs);
    }
    private static String logFilePath="";

    private static void setConfigFile(String filePath){
        logFilePath=filePath;
    }
    public static String getConfigFile(){
        return logFilePath;
    }
}
