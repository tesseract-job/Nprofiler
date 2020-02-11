package org.nickle.nprofiler.bean;

import lombok.Data;

/**
 * 实体信息
 * @author: 1336942608@qq.com
 * @version:
 * @date: 2020年01月16 20时51分
 */
@Data
public class InstanceInfo {
    private Long id;
    private Byte newFlag;
    private String objectName;
    private Long byteSize;
    /** 无法解析的对象描述 */
    private String unSolvableDesc;
    /** 引用描述 */
    private String referenceToDesc;

    public InstanceInfo(){}

    public InstanceInfo(String unSolvableDesc){
        this.unSolvableDesc = unSolvableDesc;
    }


}