package org.nickle.nprofiler.bean;

import lombok.Data;

import java.util.List;

/**
 * 根信息
 * @author: 1336942608@qq.com
 * @version:
 * @date: 2020年01月17 23时02分
 */
@Data
public class RootsInfo {
    private Byte includeWeak;
    private InstanceInfo refInstance;
    private List<RootInfo> rootInfoList;

    @Data
    public static class RootInfo {
        private Long rootStackId;
        private String referenceTypeName;
        private String description;
        private String fromObject;
        private Long fromObjectId;
        List<InstanceInfo> referenceChains;

    }
}
