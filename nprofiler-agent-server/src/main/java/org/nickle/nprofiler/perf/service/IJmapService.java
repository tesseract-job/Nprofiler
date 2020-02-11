package org.nickle.nprofiler.perf.service;

import org.nickle.nprofiler.bean.JmapHeapInfo;

public interface IJmapService {
    JmapHeapInfo getProcessHeapSummary(int processId) throws Exception;
    void writeHeapHprofBin(String fileName) throws Exception;
}
