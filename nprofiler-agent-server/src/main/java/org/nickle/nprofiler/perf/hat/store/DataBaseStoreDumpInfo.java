package org.nickle.nprofiler.perf.hat.store;

import com.sun.tools.hat.internal.model.Snapshot;
import org.nickle.nprofiler.bean.*;

import java.util.List;

/**
 * 数据库存储DumpInfo
 * @author wesley
 * @create 2020-02-10
 */
public class DataBaseStoreDumpInfo implements IStoreDumpInfo{

    @Override
    public AllClassesInfo storeAllClassesInfo(Snapshot snapshot) {
        return null;
    }

    @Override
    public List<ClassInfo> storeClassInfo(Snapshot snapshot, AllClassesInfo allClassesInfo) {
        return null;
    }

    @Override
    public List<HistogramInfo> storeHistogramInfo(Snapshot snapshot) {
        return null;
    }

    @Override
    public InstancesCountResultInfo storeInstancesCountResultInfo(Snapshot snapshot) {
        return null;
    }

    @Override
    public List<RootsInfo> storeRootsInfo(Snapshot snapshot, AllClassesInfo allClassesInfo) {
        return null;
    }

    @Override
    public List<RefsByTypeInfo> storeRefsByTypeInfo(Snapshot snapshot, AllClassesInfo allClassesInfo) {
        return null;
    }
}
