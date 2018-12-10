package com.monitor.apm.service.schedule;

import com.monitor.apm.service.controller.UserController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @program: monitor
 * @description: 定时任务访问接口,便于输出监控日志
 * @author: liang.song
 * @create: 2018-12-05-15:42
 **/
@Service
public class ScheduleService {
    @Autowired
    private UserController userService;
    @Autowired
    ScheduleService(){
        Random random=new Random();
        Executors.newScheduledThreadPool(10).scheduleAtFixedRate(new Runnable(){

            @Override
            public void run() {
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                Future<Boolean> future = executorService.submit(() -> {
                    for(int i=0;i<10;i++){
                        userService.getUserById(random.nextInt() );
                    }
                    return true;
                });
                try{
                    future.get(5,TimeUnit.SECONDS);
                }catch (Exception e){
                    System.out.println("exception");
                }finally {
                    executorService.shutdown();
                }

            }
        },0,1,TimeUnit.SECONDS);

    }

}
