package org.nickle.nprofiler.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 实例数统计结果实体
 * @author: 1336942608@qq.com
 * @version:
 * @date: 2020年01月15 21时40分
 */
@Data
public class InstancesCountResultInfo {

    /** 是否排除平台类 */
    private Byte excludePlatform;
    /** 实例数统计 */
    private Long instancesCount;
    /** 字节数统计 */
    private Long bytesCount;
    /** 实例信息列表 */
    private List<InstancesCountInfo> instancesCountInfoList;

    @Data
    @NoArgsConstructor
    public class InstancesCountInfo{
        private Long id;
        /** 包含实例数 */
        private Long instancesCount;
        private Byte hasNewSet;
        private Long newInstancesCount;
        /** 类名 */
        private String className;
    }
}
