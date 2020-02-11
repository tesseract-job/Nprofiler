package org.nickle.nprofiler.perf.service.impl;

import org.nickle.nprofiler.perf.hat.model.Snapshot;
import lombok.Data;
import org.nickle.nprofiler.bean.*;
import org.nickle.nprofiler.perf.hat.io.Reader;
import org.nickle.nprofiler.perf.hat.query.NQueryHandler;
import org.nickle.nprofiler.perf.hat.store.MemoryStoreDumpInfo;
import org.nickle.nprofiler.perf.service.IJhatService;

import java.io.IOException;
import java.util.List;

/**
 * 内存查询
 * @author wesley
 * @create 2020-02-10
 */
@Data
public class MemoryJhatSerivceImpl implements IJhatService {

    private Snapshot snapshot;
    private MemoryStoreDumpInfo dumpInfo;
    private NQueryHandler queryHandler;


    @Override
    public AllClassesInfo storeAllClassesInfo(String filename) throws IOException {
        snapshot = Reader.readFile(filename,true,1);
        dumpInfo = new MemoryStoreDumpInfo();
        return dumpInfo.storeAllClassesInfo(snapshot);
    }

    @Override
    public List<ClassInfo> storeClassInfo(String filename) throws IOException {
        snapshot = Reader.readFile(filename,true,1);
        dumpInfo = new MemoryStoreDumpInfo();
        MemoryStoreDumpInfo info = new MemoryStoreDumpInfo();
        AllClassesInfo allClassesInfo = info.storeAllClassesInfo(snapshot);
        return dumpInfo.storeClassInfo(snapshot,allClassesInfo);
    }

    @Override
    public List<HistogramInfo> storeHistogramInfo(String filename) throws IOException {
        snapshot = Reader.readFile(filename,true,1);
        dumpInfo = new MemoryStoreDumpInfo();
        return dumpInfo.storeHistogramInfo(snapshot);
    }

    @Override
    public InstancesCountResultInfo storeInstancesCountResultInfo(String filename) throws IOException {
        snapshot = Reader.readFile(filename,true,1);
        dumpInfo = new MemoryStoreDumpInfo();
        return dumpInfo.storeInstancesCountResultInfo(snapshot);
    }

    @Override
    public List<RootsInfo> storeRootsInfo(String filename) throws IOException {
        snapshot = Reader.readFile(filename,true,1);
        dumpInfo = new MemoryStoreDumpInfo();
        MemoryStoreDumpInfo info = new MemoryStoreDumpInfo();
        AllClassesInfo allClassesInfo = info.storeAllClassesInfo(snapshot);
        return dumpInfo.storeRootsInfo(snapshot,allClassesInfo);
    }

    @Override
    public List<RefsByTypeInfo> storeRefsByTypeInfo(String filename) throws IOException {
        snapshot = Reader.readFile(filename,true,1);
        dumpInfo = new MemoryStoreDumpInfo();
        MemoryStoreDumpInfo info = new MemoryStoreDumpInfo();
        AllClassesInfo allClassesInfo = info.storeAllClassesInfo(snapshot);
        return dumpInfo.storeRefsByTypeInfo(snapshot,allClassesInfo);
    }

}
