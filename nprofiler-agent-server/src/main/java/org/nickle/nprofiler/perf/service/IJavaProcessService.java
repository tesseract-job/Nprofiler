package org.nickle.nprofiler.perf.service;

import org.nickle.nprofiler.perf.bean.JpsProcessInfo;

import java.util.List;

public interface IJavaProcessService {
    List<JpsProcessInfo> getAllJavaProcess() throws Exception;
}
