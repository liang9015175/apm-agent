package com.monitor.apm.console.enumn;

import lombok.Getter;

/**
 * @program: monitor
 * @description: ${description}
 * @author: liang.song
 * @create: 2018-12-05-14:12
 **/
@Getter
public enum DateTypeEnum {
    last_1_second(1,"最近1秒钟"),last_1_min(2,"最近1分钟");
    private Integer code;
    private String desc;
    DateTypeEnum(Integer code, String desc){
        this.code=code;
        this.desc=desc;
    }
    public static DateTypeEnum codeOf(Integer code){
        for(DateTypeEnum v:values()){
            if(v.code.equals(code)){
                return v;
            }
        }
        throw new RuntimeException("不支持的时间选择");
    }
}
