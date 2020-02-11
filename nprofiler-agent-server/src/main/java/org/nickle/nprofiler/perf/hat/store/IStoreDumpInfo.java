package org.nickle.nprofiler.perf.hat.store;

import org.nickle.nprofiler.bean.*;
import org.nickle.nprofiler.perf.hat.model.Snapshot;

import java.util.List;

public interface IStoreDumpInfo {

    AllClassesInfo storeAllClassesInfo(Snapshot snapshot);
    List<ClassInfo> storeClassInfo(Snapshot snapshot, AllClassesInfo allClassesInfo);
    List<HistogramInfo> storeHistogramInfo(Snapshot snapshot);
    InstancesCountResultInfo storeInstancesCountResultInfo(Snapshot snapshot);
    List<RootsInfo> storeRootsInfo(Snapshot snapshot, AllClassesInfo allClassesInfo);
    List<RefsByTypeInfo> storeRefsByTypeInfo(Snapshot snapshot,AllClassesInfo allClassesInfo);
    List<ReachableInfo> storeReachableInfo(Snapshot snapshot,AllClassesInfo allClassesInfo);
    List<InstancesResultInfo> storeInstanceInfo(Snapshot snapshot, AllClassesInfo allClassesInfo);
}
