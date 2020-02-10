package org.nickle.nprofiler.perf.hat.query;

import com.sun.tools.hat.internal.model.JavaClass;
import com.sun.tools.hat.internal.model.JavaHeapObject;
import com.sun.tools.hat.internal.server.PlatformClasses;
import com.sun.tools.hat.internal.util.ArraySorter;
import com.sun.tools.hat.internal.util.Comparer;
import org.nickle.nprofiler.bean.InstancesCountResultInfo;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 实例数统计查询接口
 * @author: 1336942608@qq.com
 * @version:
 * @date: 2020年01月15 20时26分
 */
public class NInstancesCountQuery extends  NQueryHandler{
    private boolean excludePlatform;

    public NInstancesCountQuery(boolean excludePlatform) {
        this.excludePlatform = excludePlatform;
    }

    @Override
    public Object run() {

        // 1. excludePlatform
        InstancesCountResultInfo resultInfo = new InstancesCountResultInfo();
        // 查找类
        JavaClass[] classes = this.snapshot.getClassesArray();
        if (this.excludePlatform) {
            resultInfo.setExcludePlatform((byte)1);
            int index = 0;
            for(int var3 = 0; var3 < classes.length; ++var3) {
                if (!PlatformClasses.isPlatformClass(classes[var3])) {
                    classes[index++] = classes[var3];
                }
            }

            JavaClass[] var14 = new JavaClass[index];
            System.arraycopy(classes, 0, var14, 0, var14.length);
            classes = var14;
        }

        // sort order by desc
        ArraySorter.sort(classes, new Comparer() {
            @Override
            public int compare(Object var1, Object var2) {
                JavaClass var3 = (JavaClass)var1;
                JavaClass var4 = (JavaClass)var2;
                int var5 = var3.getInstancesCount(false) - var4.getInstancesCount(false);
                if (var5 != 0) {
                    return -var5;
                } else {
                    String var6 = var3.getName();
                    String var7 = var4.getName();
                    if (var6.startsWith("[") != var7.startsWith("[")) {
                        return var6.startsWith("[") ? 1 : -1;
                    } else {
                        return var6.compareTo(var7);
                    }
                }
            }
        });


        long bytesTotal = 0L;
        long instancesTotal = 0L;

        List<InstancesCountResultInfo.InstancesCountInfo> list = new ArrayList<>(classes.length);

        for(int var7 = 0; var7 < classes.length; ++var7) {
            InstancesCountResultInfo.InstancesCountInfo info = new InstancesCountResultInfo().new InstancesCountInfo();
            JavaClass var8 = classes[var7];
            // count
            int instancesCount = var8.getInstancesCount(false);
            info.setInstancesCount((long)instancesCount);
            info.setId(classes[var7].getId());
            if (this.snapshot.getHasNewSet()) {
                info.setHasNewSet((byte)1);
                Enumeration var10 = var8.getInstances(false);
                int var11 = 0;

                while(var10.hasMoreElements()) {
                    JavaHeapObject var12 = (JavaHeapObject)var10.nextElement();
                    if (var12.isNew()) {
                        ++var11;
                    }
                }
                info.setNewInstancesCount((long)var11);
            }

            info.setClassName(classes[var7] == null ? "null":classes[var7].toString());
            instancesTotal += (long)instancesCount;
            bytesTotal += classes[var7].getTotalInstanceSize();
            list.add(info);
        }

        resultInfo.setBytesCount(bytesTotal);
        resultInfo.setInstancesCount(instancesTotal);
        resultInfo.setInstancesCountInfoList(list);
        return resultInfo;
    }

}
