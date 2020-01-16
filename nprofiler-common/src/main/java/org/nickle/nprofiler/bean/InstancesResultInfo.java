package org.nickle.nprofiler.bean;

import lombok.Data;

import java.util.List;

/**
 * 实体结果信息实体
 * @author: 1336942608@qq.com
 * @version:
 * @date: 2020年01月16 20时07分
 */
@Data
public class InstancesResultInfo {

    private Byte includeSubclasses;
    private Byte newObjects;
    private String classLink;
    private List<InstanceInfo> instanceInfos;
    private Long bytesCount;
    private Long instancesCount;
}
