package com.monitor.apm.monitor;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class MonitorLog {
    /**
     * 接口耗时
     */
    private long timeConsume;

    /**
     * 接口名称(方便根据接口维度进行统计)
     */
    private String methodName;

    /**
     * 异常信息
     */
    private String exception;
    /**
     * 创建时间
     */
    private Date createTime;
}
