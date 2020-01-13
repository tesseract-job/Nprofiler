package org.nickle.nprofiler.perf.service;

import org.nickle.nprofiler.bean.JstatGCInfo;

/**
 * @author wesley
 * @create 2020-01-12
 */
public interface IJstatService {
    JstatGCInfo getGCSummary(int processId) throws Exception;
}
