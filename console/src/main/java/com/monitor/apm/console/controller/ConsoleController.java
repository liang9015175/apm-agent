package com.monitor.apm.console.controller;

import com.alibaba.fastjson.JSON;
import com.monitor.apm.console.enumn.DateTypeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/console")
@Api(tags = "监控平台",description = "数据监控")
@Slf4j
public class ConsoleController {
    private static final String logPath="./apm.log";
    private static RandomAccessFile randomAccessFile=null;
    static {
        try {
            File file=new File(logPath);
            if(!file.exists()){
                file.createNewFile();
                file.setReadable(true);
                file.setWritable(true);
            }
            randomAccessFile=new RandomAccessFile(file,"rw");
        } catch (Exception e) {
            try {
                if(randomAccessFile!=null){
                    randomAccessFile.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
    /**
     *  收集监控日志(日志太多的时候可以采用消息队列收集日志)
     * @param log  日志
     * <p>
     *     1.日志  这里的日志需要序列化，建议将monitor Log 放在公共模块中,方便多个服务可以序列化，否则
     *     监控服务端也要写一个monitor log 实体
     *     2.这个接口可以写成接受批量的日志，比如，当客户端收集到buffer中，当buffer 满的的时候再批量发送过来，
     *     防止日志太多，请求接口太频繁
     * </p>
     *
     */
    @PostMapping("/collect")
    public void collectLog(@RequestBody String log) throws IOException {
        if(StringUtils.isEmpty(log)){
            return;
        }
        System.out.println(log);
        randomAccessFile.write((log+"\n").getBytes());
    }

    @GetMapping(value = "/get")
    @ApiOperation(value = "监控数据获取")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dateType", value = "日期类型(1:最近1秒钟 2:最近1分钟 )", required = false, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "method", value = "方法名", required = false, dataType = "long", paramType = "query"),
    })
    public List<ApmDto>  print(@RequestParam(required = false) Integer dateType, @RequestParam(required = false) String method) throws IOException {
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
                    //log.error("parse monitor log got error");
                    continue;
                }

            }

        } catch (IOException e) {
            //log.error("parse monitor log got error");
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
