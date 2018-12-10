package com.monitor.apm;


import com.alibaba.fastjson.JSON;
import com.monitor.apm.dto.ApmDto;
import com.monitor.apm.dto.MonitorLog;
import com.monitor.apm.enumn.DateTypeEnum;
import com.monitor.apm.service.ServiceApp;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit test for simple ServiceApp.
 */
@SpringBootTest(classes = ServiceApp.class)
@RunWith(SpringRunner.class)
@ComponentScan(basePackages = {"com.monitor.apm.*"})
@Slf4j
public class ServiceAppTest {
    private static final String logPath="./monitor.log";

    /**
     * Rigorous Test :-)
     */
    @Test
    public void test() throws InterruptedException {

        Executors.newScheduledThreadPool(10).scheduleAtFixedRate(() -> {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<List<ApmDto>> future = executorService.submit(()-> print(2,null));
            try{
                List<ApmDto> apmDtos = future.get(5, TimeUnit.SECONDS);
                System.out.println(JSON.toJSONString(apmDtos,true));
            }catch (Exception e){
               log.error("got exception",e);
            }finally {
                executorService.shutdown();
            }

        },0,2,TimeUnit.SECONDS);
        Thread.sleep(1000*60);
    }

    private List<ApmDto>  print( Integer dateType,String method) throws IOException {
        List<ApmDto> list=new ArrayList<>();//结果
        Map<String,List<MonitorLog>> methodMap=new HashMap<>();//存放监控方法名->调用队列
        LocalDateTime startTime=LocalDateTime.now();//开始时间
        LocalDateTime endTime=LocalDateTime.now();//结束时间
        LocalDateTime firstLineTime=null;//找到的第一行记录的时间,用户计算duration
        switch (DateTypeEnum.codeOf(dateType)){
            case last_1_second:
                startTime= LocalDateTime.now().minusSeconds(1);
                break;
            case last_1_min:
                startTime=LocalDateTime.now().minusMinutes(1);
                break;
            default:
                startTime= LocalDateTime.now().minusSeconds(1);
                break;
        }
        RandomAccessFile rw=null;
        try {
            rw= new RandomAccessFile(logPath, "rw");
            //读取监控日志信息
            String line="";
            while ((line=rw.readLine())!=null) {
                try{
                    String substring =line.substring(0, 19);//根据日期取
                    if(!substring.matches("\\d{4}[-.]\\d{1,2}[-.]\\d{1,2}(\\s\\d{2}:\\d{2}(:\\d{2})?)?")){
                        continue;
                    }
                    LocalDateTime startDate = LocalDateTime.parse(substring, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    if(startDate.compareTo(startTime)>=1&&startDate.compareTo(endTime)<=1){
                        MonitorLog log=null;
                        if(StringUtils.isEmpty(method)){
                            try{
                                log= JSON.parseObject(line.substring(20), MonitorLog.class);
                            }catch (Exception e){
                                continue;
                            }
                            if(firstLineTime==null){
                                firstLineTime=startDate;
                            }
                        }else {
                            if(line.contains(method)){
                                log= JSON.parseObject(line.substring(20), MonitorLog.class);
                                if(firstLineTime==null){
                                    firstLineTime=startDate;
                                }

                            }else {
                                if(firstLineTime==null){
                                    firstLineTime=startTime;

                                }
                            }
                        }

                        if(log!=null){
                            List<MonitorLog> logs = methodMap.getOrDefault(log.getMethodName(), new ArrayList<>());
                            logs.add(log);
                            methodMap.put(log.getMethodName(),logs);
                        }
                    }
                }catch (Exception e){
                    // log.error("parse monitor log got exception");
                    continue;
                }

            }

        } catch (IOException e) {
            // log.error("parse monitor log got exception");
        }finally {
            if(rw!=null){
                rw.close();
            }
        }
        if(methodMap.isEmpty()){
            return list;
        }

        LocalDateTime finalFirstLineTime = firstLineTime;
        methodMap.forEach((methodName, logs) -> {
            List<String> exceptions = new ArrayList<>();
            AtomicReference<Integer> count = new AtomicReference<>(0);
            AtomicReference<Integer> totalConsume = new AtomicReference<>(0);
            logs.forEach(vv -> {
                Integer timeConsume = Integer.valueOf(vv.getTimeConsume());
                totalConsume.updateAndGet(v1 -> v1 + timeConsume);
                count.getAndSet(count.get() + 1);
                String exception = vv.getException();
                if (!StringUtils.isEmpty(exception)) {
                    exceptions.add(exception);
                }

            });
            long minuteDuration = Duration.between(finalFirstLineTime, endTime).toMinutes();
            minuteDuration=minuteDuration==0?1:minuteDuration;
            long secondDuration = Duration.between(finalFirstLineTime, endTime).toMillis() / 1000;
            secondDuration=secondDuration==0?1:secondDuration;
            ApmDto build = ApmDto.builder()
                    .count(count.get())
                    .endTime(Date.from(endTime.toInstant(ZoneOffset.of("+8"))))
                    .startTime(Date.from(finalFirstLineTime.toInstant(ZoneOffset.of("+8"))))
                    .exceptions(exceptions.isEmpty()?null:exceptions.subList(exceptions.size()-1,exceptions.size()))
                    .failCount(exceptions.size())
                    .successCount(count.get() - exceptions.size())
                    .methodName(methodName)
                    .successRation(new BigDecimal(count.get() - exceptions.size()).divide(new BigDecimal(count.get()), 2, BigDecimal.ROUND_HALF_UP)).
                            avgMinuteCount(BigDecimal.valueOf(count.get()))
                    .avgSecondCount(BigDecimal.valueOf(count.get()).divide(BigDecimal.valueOf(secondDuration),2,BigDecimal.ROUND_HALF_UP))
                    .avgTimeout(BigDecimal.valueOf(totalConsume.get()).divide(BigDecimal.valueOf(count.get()), 2, BigDecimal.ROUND_HALF_UP)).build();
            list.add(build);
        });
        return list;
    }

}
