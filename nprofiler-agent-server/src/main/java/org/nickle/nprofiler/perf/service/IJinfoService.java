package org.nickle.nprofiler.perf.service;

import org.nickle.nprofiler.bean.JinfoConfiguration;
import org.nickle.nprofiler.bean.JmapHeapInfo;

public interface IJinfoService {


    JinfoConfiguration getInfoConfiguration(int processId) throws Exception;



}
