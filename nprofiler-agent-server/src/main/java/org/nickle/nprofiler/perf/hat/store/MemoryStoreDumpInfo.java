package org.nickle.nprofiler.perf.hat.store;

import com.sun.tools.hat.internal.model.Snapshot;
import lombok.Data;
import org.nickle.nprofiler.bean.*;
import org.nickle.nprofiler.perf.hat.query.*;

import java.util.*;

/**
 * 内存存储DumpInfo
 * @author wesley
 * @create 2020-02-10
 */
@Data
public class MemoryStoreDumpInfo implements IStoreDumpInfo {

    private NQueryHandler queryHandler;

    @Override
    public AllClassesInfo storeAllClassesInfo(Snapshot snapshot) {
        queryHandler = new NAllClassesQuery(false,false);
        queryHandler.setSnapshot(snapshot);
        return (AllClassesInfo)queryHandler.run();
    }

    @Override
    public List<ClassInfo> storeClassInfo(Snapshot snapshot, AllClassesInfo allClassesInfo) {
        List<ClassInfo> list = new ArrayList<>();
        Map<String, List<AllClassesInfo.JavaClass>> info = allClassesInfo.getInfo();
        Collection<List<AllClassesInfo.JavaClass>> values = info.values();
        Iterator<List<AllClassesInfo.JavaClass>> iterator = values.iterator();
        while (iterator.hasNext()){
            List<AllClassesInfo.JavaClass> next = iterator.next();
            for (int i = 0; i < next.size() ; i++) {
                AllClassesInfo.JavaClass javaClass = next.get(i);
                queryHandler = new NClassQuery("0x"+Long.toHexString(javaClass.getId()));
                queryHandler.setSnapshot(snapshot);
                list.add((ClassInfo)queryHandler.run());
            }

        }

        return list;
    }

    @Override
    public List<HistogramInfo> storeHistogramInfo(Snapshot snapshot) {
        queryHandler = new NHistogramQuery();
        queryHandler.setSnapshot(snapshot);
        return (List<HistogramInfo>) queryHandler.run();
    }

    @Override
    public InstancesCountResultInfo storeInstancesCountResultInfo(Snapshot snapshot) {
        queryHandler = new NInstancesCountQuery(false);
        queryHandler.setSnapshot(snapshot);
        return (InstancesCountResultInfo) queryHandler.run();
    }

    @Override
    public List<RootsInfo> storeRootsInfo(Snapshot snapshot, AllClassesInfo allClassesInfo) {
        List<RootsInfo> list = new ArrayList<>();
        Map<String, List<AllClassesInfo.JavaClass>> info = allClassesInfo.getInfo();
        Collection<List<AllClassesInfo.JavaClass>> values = info.values();
        Iterator<List<AllClassesInfo.JavaClass>> iterator = values.iterator();
        while (iterator.hasNext()){
            List<AllClassesInfo.JavaClass> next = iterator.next();
            for (int i = 0; i < next.size() ; i++) {
                AllClassesInfo.JavaClass javaClass = next.get(i);
                queryHandler = new NRootsQuery(true);
                queryHandler.setQuery("0x"+Long.toHexString(javaClass.getId()));
                System.out.println("0x"+Long.toHexString(javaClass.getId()));
                queryHandler.setSnapshot(snapshot);
                list.add((RootsInfo) queryHandler.run());
            }

        }
        return list;
    }

    @Override
    public List<RefsByTypeInfo> storeRefsByTypeInfo(Snapshot snapshot,AllClassesInfo allClassesInfo) {
        List<RefsByTypeInfo> list = new ArrayList<>();
        Map<String, List<AllClassesInfo.JavaClass>> info = allClassesInfo.getInfo();
        Collection<List<AllClassesInfo.JavaClass>> values = info.values();
        Iterator<List<AllClassesInfo.JavaClass>> iterator = values.iterator();
        while (iterator.hasNext()){
            List<AllClassesInfo.JavaClass> next = iterator.next();
            for (int i = 0; i < next.size() ; i++) {
                AllClassesInfo.JavaClass javaClass = next.get(i);
                queryHandler = new NRefsByTypeQuery();
                queryHandler.setQuery("0x"+Long.toHexString(javaClass.getId()));
                queryHandler.setSnapshot(snapshot);
                list.add((RefsByTypeInfo) queryHandler.run());
            }

        }
        return list;
    }

}
