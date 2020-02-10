package org.nickle.nprofiler.perf.hat.query;

import com.sun.tools.hat.internal.model.JavaClass;
import org.nickle.nprofiler.bean.HistogramInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * class概要查询接口
 * @author wesley
 * @create 2020-01-16
 */
public class NHistogramQuery extends NQueryHandler {

    public NHistogramQuery() {
    }

    @Override
    public Object run() {
        List<HistogramInfo> list = new ArrayList<>();
        JavaClass[] classes = snapshot.getClassesArray();
        Comparator<JavaClass> comparator;
        if ("count".equals(query)) {
            comparator = (first, second) -> {
                long diff = (second.getInstancesCount(false) -
                        first.getInstancesCount(false));
                return (diff == 0)? 0: ((diff < 0)? -1 : + 1);
            };
        } else if ("class".equals(query)) {
            comparator = (first, second) -> first.getName().compareTo(second.getName());
        } else {
            // default sort is by total size
            comparator = (first, second) -> {
                long diff = (second.getTotalInstanceSize() -
                        first.getTotalInstanceSize());
                return (diff == 0)? 0: ((diff < 0)? -1 : + 1);
            };
        }
        Arrays.sort(classes, comparator);
        for (int i = 0; i < classes.length; i++) {
            JavaClass clazz = classes[i];
            HistogramInfo info = new HistogramInfo();
            info.setClassName(clazz.toString());
            info.setCount(clazz.getInstancesCount(true));
            info.setSize(clazz.getTotalInstanceSize());
            list.add(info);
        }
        return list;
    }
}
