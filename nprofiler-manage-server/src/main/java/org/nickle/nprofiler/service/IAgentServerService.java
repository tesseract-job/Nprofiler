package org.nickle.nprofiler.service;

import feign.Param;
import feign.RequestLine;
import org.nickle.nprofiler.bean.JmapHeapInfo;
import org.nickle.nprofiler.bean.JpsProcessInfo;

import java.util.List;

import static org.nickle.nprofiler.constant.CommonConstant.*;

public interface IAgentServerService {

    @RequestLine("GET " + JMAP_HEAP_INFO_MAPPING+"/{processId}")
    JmapHeapInfo getJmapHeapInfo(@Param("processId") String processId);

    @RequestLine("GET " + JPS_PROCESS_INFO_MAPPING)
    List<JpsProcessInfo> getJpsProcessInfo();
}
