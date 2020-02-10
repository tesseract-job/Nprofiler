package org.nickle.nprofiler.perf.service;

import org.nickle.nprofiler.bean.*;

import java.io.IOException;
import java.util.List;

/**
 * dump文件分析
 * @author wesley
 * @create 2020-02-10
 */
public interface IJhatService {

    AllClassesInfo storeAllClassesInfo(String filename) throws IOException;
    List<ClassInfo> storeClassInfo(String filename) throws IOException;
    List<HistogramInfo> storeHistogramInfo(String filename) throws IOException;
    InstancesCountResultInfo storeInstancesCountResultInfo(String filename) throws IOException;
    List<RootsInfo> storeRootsInfo(String filename) throws IOException;
    List<RefsByTypeInfo> storeRefsByTypeInfo(String filename) throws IOException;

}
