package org.nickle.nprofiler.perf.service;

import org.nickle.nprofiler.perf.bean.JmapHeapInfo;

public interface IJmapService {
    JmapHeapInfo getProcessHeapSummary(int processId) throws Exception;
}
