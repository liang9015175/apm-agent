package com.monitor.apm.dto;

import com.alibaba.fastjson.annotation.JSONField;
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
    @JSONField(name = "方法名")
    @ApiModelProperty("方法名")
    private String methodName;

    @JSONField(name = "平均耗时")
    @ApiModelProperty("平均耗时")
    private BigDecimal avgTimeout;

    @JSONField(name = "调用次数")
    @ApiModelProperty("调用次数")
    private Integer count;

    @JSONField(name = "统计开始时间")
    @ApiModelProperty("统计开始时间")
    private Date startTime;

    @JSONField(name = "统计结束时间")
    @ApiModelProperty("统计结束时间")
    private Date endTime;

    @JSONField(name = "平均每分钟调用次数")
    @ApiModelProperty("平均每分钟调用次数")
    private BigDecimal avgMinuteCount;

    @JSONField(name = "平均每秒调用次数")
    @ApiModelProperty("平均每秒调用次数")
    private BigDecimal avgSecondCount;

    @JSONField(name = "成功次数")
    @ApiModelProperty("成功次数")
    private Integer successCount;

    @JSONField(name = "失败次数")
    @ApiModelProperty("失败次数")
    private Integer failCount;

    @JSONField(name = "成功率")
    @ApiModelProperty("成功率")
    private BigDecimal successRation;

    @JSONField(name = "异常信息")
    @ApiModelProperty("异常信息")
    private List<String> exceptions;
}
