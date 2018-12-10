package com.monitor.apm.service.controller;

import com.monitor.apm.service.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {


    @GetMapping("/get")
    public UserDto getUserById(final Integer userId){
        ThreadLocalRandom random=ThreadLocalRandom.current();
        final int randomSleep = random.nextInt(100, 500);//随机数模拟接口耗时
        try{
            Thread.sleep(randomSleep);
            if(randomSleep>100&&randomSleep<200){
                //模拟接口发生异常
                throw new RuntimeException("mock exception");
            }
            return new UserDto(){
                {
                    setId(userId);
                    setName(randomSleep+"-");
                }
            };
        } catch (Exception e) {
            //throw exception ,so that we can catch it in our proxy
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }
}
