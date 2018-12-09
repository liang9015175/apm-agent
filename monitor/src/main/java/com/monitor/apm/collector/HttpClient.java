package com.monitor.apm.collector;

import com.alibaba.fastjson.JSON;
import com.monitor.apm.monitor.MonitorLog;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;

/**
 * 日志搜集器，将日志发送到控制台服务
 */
@Slf4j
public class HttpClient {
    /**
     * console 接受日志文件接口
     */
    private static final String url="http://localhost:8081/console/collect";

    /**
     * 发送monitor log
     * <p>
     *     使用http 发送日志 有问题
     *     1.每次都要创建client 消耗资源
     *     2.效率不高，无法应对高并发的情况
     *     3.建议采用消息队列的方式,或者将日志拼接成buffer，分块分发
     * </p>
     * @param monitorLog  监控日志
     */
    public static void post(MonitorLog monitorLog){
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody=RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JSON.toJSONString(monitorLog));
        Request request=new Request.Builder().url(url).post(requestBody).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                   log.error("send monitor log to console get error",e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                log.info("send monitor log to console got response:{}",response.body().string());
            }
        });
    }
}
