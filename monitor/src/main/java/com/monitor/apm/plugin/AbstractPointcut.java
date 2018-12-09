package com.monitor.apm.plugin;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bind.annotation.*;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 抽象切面定义
 * <p>
 *     该接口定义了拦截哪个类的哪个方法，做方法增强
 *     同时定义了如何增强该方法 分为 before, after, throw
 * </p>
 *
 */
@Slf4j
public abstract class AbstractPointcut {

    /**
     * 定义切面拦截类
     * @return
     */
    public abstract ElementMatcher<? super TypeDescription>  point();

    /**
     * 定义增强方法
     * @return
     */
    public abstract ElementMatcher<? super MethodDescription> method();

    /**
     * 拦截前
     * @param instance 要拦截的实例
     * @param method    拦截的方法签名
     * @param params    方法参数
     */
    public abstract void before(Object instance , Method method,Object[] params) throws Exception;

    /**
     * 拦截后
     * @param instance  要拦截的实例
     * @param method    方法
     * @param params    参数
     * @param result    返回结构
     */
    public abstract void after(Object instance , Method method,Object[] params,Object result) throws Exception;

    /**
     * 处理异常
     * @param instance   拦截实例
     * @param method    拦截方法
     * @param params    拦截参数
     * @param throwable 异常信息
     */
    public abstract void handleThrow(Object instance , Method method,Object[] params,Throwable throwable) throws Exception;
    @RuntimeType
    public void doHandler(@This Object instance, @Origin  Method method, @AllArguments Object[] args,@SuperCall Callable<?> callable){
        try{
            before(instance,method,args);
        }catch (Throwable t){
            log.error("enhance class:{}, method:{} got exception:{}",instance.getClass(),method.getName(),t);
        }
        Object ret=null;
        try{
            ret= callable.call();
        } catch (Throwable e) {
            try{
                handleThrow(instance,method,args,e);
            }catch (Throwable throwable){
                log.error("enhance class:{}, method:{} exception got error:{}",instance.getClass(),method.getName(),throwable);
            }
        }finally {
            try{
                after(instance,method,args,ret);
            }catch (Throwable throwable){
                log.error("enhance class:{},method:{} got exception:{}",instance.getClass(),method.getName(),throwable);
            }
        }
    }
}
