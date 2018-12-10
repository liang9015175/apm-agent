package com.monitor.apm.console.controller;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @program: monitor
 * @description: 监控数据可视化
 * @author: liang.song
 * @create: 2018-12-04-13:37
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel
public class ApmDto {
    @ApiModelProperty("方法名")
    private String methodName;
    @ApiModelProperty("平均耗时")
    private BigDecimal avgTimeout;

    @ApiModelProperty("调用次数")
    private Integer count;
    @ApiModelProperty("统计开始时间")
    private Date startTime;
    @ApiModelProperty("统计结束时间")
    private Date endTime;
    @ApiModelProperty("平均每分钟调用次数")
    private BigDecimal avgMinuteCount;
    @ApiModelProperty("平均每秒调用次数")
    private BigDecimal avgSecondCount;
    @ApiModelProperty("成功次数")
    private Integer successCount;
    @ApiModelProperty("失败次数")
    private Integer failCount;
    @ApiModelProperty("成功率")
    private BigDecimal successRation;
    @ApiModelProperty("异常信息")
    private List<String> exceptions;
}
