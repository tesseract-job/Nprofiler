package org.nickle.nprofiler.bean;

import com.google.common.collect.Lists;
import lombok.Data;
import org.nickle.nprofiler.common.NprofilerUtils;

import java.util.List;

@Data
public class JmapHeapInfo {
    private Long minHeapFreeRatio;
    private Long maxHeapFreeRatio;
    private Long maxHeapSize;
    private Long newSize;
    private Long maxNewSize;
    private Long oldSize;
    private Long newRatio;
    private Long survivorRatio;


    private Long metaspaceSize;
    private Long compressedClassSpaceSize;
    private Long maxMetaspaceSize;
    private Long g1HeapRegionSize;
    @NprofilerUtils.MapToObjIgnore
    private List<GcInfo> gcInfoList = Lists.newArrayList();

    private HeapUsageInfo heapUsageInfo = new HeapUsageInfo();

    @Data
    public static class GcInfo {
        private String description;
    }

    @Data
    public static class HeapUsageInfo {
        private String name;
        private List<GenInfo> genInfoList = Lists.newArrayList();
    }

    @Data
    public static class GenInfo<T> {
        private String name;
        private List<T> genInfoValueList = Lists.newArrayList();
    }

    @Data
    public static class GenInfoValue {
        private String name;
        private Double value;
    }
}
