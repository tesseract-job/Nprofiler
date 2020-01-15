package org.nickle.nprofiler.bean;

import lombok.Data;

import java.util.List;

/**
 * 实例数统计结果实体
 * @author: 1336942608@qq.com
 * @version:
 * @date: 2020年01月15 21时40分
 */
@Data
public class InstancesCountResultInfo {

    /** 是否排平台类 */
    private Byte excludePlatform;
    /** 实例数统计 */
    private Long instancesCount;
    /** 字节数统计 */
    private Long bytesCount;
    /** 实例信息列表 */
    private List<InstancesCountInfo> instancesCountInfoList;

    @Data
    public static class InstancesCountInfo{
        /** 包含实例数 */
        private Long instancesCount;
        private String instancesName;
        private String instancesLink;

        private Byte hasNewSet;
        private Long newInstancesCount;
        private String newInstancesName;
        private String newInstancesLink;
        /** 类名 */
        private String className;
        private String classLink;
    }
}
