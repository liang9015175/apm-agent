package com.monitor.apm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitorLog {
    /**
     * 接口耗时
     */
    private String timeConsume;

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
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;
}
