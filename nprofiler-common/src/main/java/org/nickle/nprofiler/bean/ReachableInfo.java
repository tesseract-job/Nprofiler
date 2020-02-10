package org.nickle.nprofiler.bean;

import lombok.Data;

import java.util.List;

/**
 * 可达性信息
 * @author: 1336942608@qq.com
 * @version:
 * @date: 2020年02月10 10时17分
 */
@Data
public class ReachableInfo {
    private InstanceInfo rootInstanceInfo;
    private Long byteSize;
    private Long instanceCount;
    private String[] usedFields;
    private String[] excludedFields;
    /** 可达对象列表 */
    private List<InstanceInfo> reachableList;


}
