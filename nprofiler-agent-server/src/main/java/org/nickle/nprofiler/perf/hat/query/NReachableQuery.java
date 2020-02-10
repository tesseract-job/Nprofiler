package org.nickle.nprofiler.perf.hat.query;

import com.sun.tools.hat.internal.model.JavaHeapObject;
import com.sun.tools.hat.internal.model.JavaThing;
import com.sun.tools.hat.internal.model.ReachableObjects;
import lombok.extern.slf4j.Slf4j;
import org.nickle.nprofiler.bean.InstanceInfo;
import org.nickle.nprofiler.bean.ReachableInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 可达性查询
 * @author: 1336942608@qq.com
 * @version:
 * @date: 2020年01月18 01时17分
 */
@Slf4j
public class NReachableQuery extends NQueryHandler{

    public NReachableQuery() {
    }

    @Override
    public Object run() {
        log.info("可达性分析来源 " + this.query);
        long queryHex = this.parseHex(this.query);
        JavaHeapObject heapObject = this.snapshot.findThing(queryHex);
        ReachableObjects reachableObjects = new ReachableObjects(heapObject, this.snapshot.getReachableExcludes());
        ReachableInfo reachableInfo = new ReachableInfo();
        // 内存大小
        long size = reachableObjects.getTotalSize();
        reachableInfo.setByteSize(size);
        // 可达对象数组
        JavaThing[] javaThings = reachableObjects.getReachables();
        // 对象数
        long instanceCount = (long)javaThings.length;
        reachableInfo.setInstanceCount(instanceCount);
        InstanceInfo instanceInfo = this.parseThing(heapObject);
        reachableInfo.setRootInstanceInfo(instanceInfo);
        List<InstanceInfo> reachableList = new ArrayList<>();
        for(int i = 0; i < javaThings.length; ++i) {
            reachableList.add(this.parseThing(javaThings[i]));
        }
        reachableInfo.setReachableList(reachableList);
        reachableInfo.setUsedFields(reachableObjects.getUsedFields());
        reachableInfo.setExcludedFields(reachableObjects.getExcludedFields());
        return reachableInfo;
    }
}
