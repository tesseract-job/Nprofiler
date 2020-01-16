package org.nickle.nprofiler.perf.hat.query;

import com.sun.tools.hat.internal.model.JavaClass;
import org.nickle.nprofiler.bean.HistogramInfo;

import java.util.Arrays;
import java.util.Comparator;

/**
 * class概要查询接口
 * @author wesley
 * @create 2020-01-16
 */
public class NHistogramQuery extends NQueryHandler {

    @Override
    Object run() {
        HistogramInfo info = new HistogramInfo();
        JavaClass[] classes = snapshot.getClassesArray();
        Comparator<JavaClass> comparator;
        if (query.equals("count")) {
            comparator = (first, second) -> {
                long diff = (second.getInstancesCount(false) -
                        first.getInstancesCount(false));
                return (diff == 0)? 0: ((diff < 0)? -1 : + 1);
            };
        } else if (query.equals("class")) {
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
            info.setClassName(clazz.toString());
            info.setCount(clazz.getInstancesCount(false));
            info.setSize(clazz.getTotalInstanceSize());

        }
        return info;
    }
}
