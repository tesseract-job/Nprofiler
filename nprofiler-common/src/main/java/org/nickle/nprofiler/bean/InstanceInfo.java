package org.nickle.nprofiler.bean;

import lombok.Data;

/**
 * 实体信息
 * @author: 1336942608@qq.com
 * @version:
 * @date: 2020年01月16 20时51分
 */
@Data
public  class InstanceInfo {
    /** [Ljava.lang.Class;@0x6c253f9e0 (24 bytes) */
    private Byte newFlag;
    private String objectName;
    private String objectLink;
    private Long bytesCount;
    /** 无法解析的对象描述 */
    private String unSolvableDesc;

    public InstanceInfo(){}

    public InstanceInfo(String unSolvableDesc){
        this.unSolvableDesc = unSolvableDesc;
    }


}