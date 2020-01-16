package org.nickle.nprofiler.perf.hat.query;

import com.sun.tools.hat.internal.model.JavaClass;
import com.sun.tools.hat.internal.model.JavaHeapObject;
import org.nickle.nprofiler.bean.InstanceInfo;
import org.nickle.nprofiler.bean.InstancesResultInfo;
import org.nickle.nprofiler.exception.NprofilerException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 实体信息查询
 * @author: 1336942608@qq.com
 * @version:
 * @date: 2020年01月16 20时04分
 */
public class NInstancesQuery extends NQueryHandler{

    private boolean includeSubclasses;
    private boolean newObjects;

    public NInstancesQuery(boolean includeSubclasses) {
        this.includeSubclasses = includeSubclasses;
    }

    public NInstancesQuery(boolean includeSubclasses, boolean newObjects) {
        this.includeSubclasses = includeSubclasses;
        this.newObjects = newObjects;
    }

    @Override
    public Object run() {
        JavaClass javaClass = this.snapshot.findClass(this.query);
        InstancesResultInfo info = new InstancesResultInfo();
        if (this.newObjects) {
            info.setNewObjects((byte)1);
        }

        if (this.includeSubclasses) {
            info.setIncludeSubclasses((byte)1);
        }

        if (javaClass == null) {
            throw new NprofilerException("请求的类找不到");
        } else {
            info.setClassLink(this.urlStart+"class/"+this.encodeForURL(javaClass));
            Enumeration var3 = javaClass.getInstances(this.includeSubclasses);

            long var4 = 0L;
            long var6 = 0L;
            int instancesCount = javaClass.getInstancesCount(this.includeSubclasses);
            List<InstanceInfo> list = new ArrayList<>(instancesCount);
            label32:
            while(true) {
                JavaHeapObject var8;
                do {
                    if (!var3.hasMoreElements()) {
                        break label32;
                    }
                    var8 = (JavaHeapObject)var3.nextElement();
                } while(this.newObjects && !var8.isNew());
                list.add(this.parseThing(var8));
                var4 += (long)var8.getSize();
                ++var6;
            }
            info.setBytesCount(var4);
            info.setInstancesCount(var6);
            info.setInstanceInfos(list);
        }
        return info;
    }
}
