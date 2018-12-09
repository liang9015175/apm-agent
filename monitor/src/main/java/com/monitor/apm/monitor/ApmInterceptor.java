package com.monitor.apm.monitor;

import com.monitor.apm.plugin.AbstractPointcut;
import net.bytebuddy.implementation.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;

public class ApmInterceptor {
    @RuntimeType
    public static void intercept(@This Object instance, @AllArguments Object[] args,  @Origin Method method, @SuperCall Callable<?> callable) throws IOException {
        ServiceLoader<AbstractPointcut> load = ServiceLoader.load(AbstractPointcut.class);
        Iterator<AbstractPointcut> iterator = load.iterator();
        while (iterator.hasNext()){
            AbstractPointcut next = iterator.next();
            next.doHandler(instance, method, args, callable);
        }
    }
}
