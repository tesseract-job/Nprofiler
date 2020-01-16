package org.nickle.nprofiler.perf.hat.query;

import com.sun.tools.hat.internal.model.JavaClass;
import com.sun.tools.hat.internal.server.PlatformClasses;
import org.nickle.nprofiler.bean.AllClassesInfo;

import java.util.*;

/**
 * 所有class信息查询接口
 * @author wesley
 * @create 2020-01-16
 */
public class NAllClassesQuery extends NQueryHandler{

    boolean excludePlatform;
    boolean oqlSupported;

    public NAllClassesQuery(boolean excludePlatform, boolean oqlSupported) {
        this.excludePlatform = excludePlatform;
        this.oqlSupported = oqlSupported;
    }


    @Override
    Object run() {
        AllClassesInfo allClassesInfo = new AllClassesInfo();
        Iterator classes = snapshot.getClasses();
        Map<String,List<AllClassesInfo.JavaClass>> info = new HashMap<>();
        allClassesInfo.setInfo(info);
        String lastPackage = null;
        while (classes.hasNext()) {
            JavaClass clazz = (JavaClass)classes.next();
            if (excludePlatform && PlatformClasses.isPlatformClass(clazz)) {
                // skip this..
                continue;
            }
            String name = clazz.getName();
            int pos = name.lastIndexOf(".");
            String pkg;
            if (name.startsWith("[")) {
                pkg = "Arrays ";
            } else if (pos == -1) {
                pkg = "Default Package";
            } else {
                pkg = name.substring(0, pos);
            }
            if (!pkg.equals(lastPackage)) {
                String packageName = "Package " + pkg;
                if (!allClassesInfo.getInfo().containsKey(packageName)){
                    List<AllClassesInfo.JavaClass> list = new ArrayList<>();
                    AllClassesInfo.JavaClass javaClass = allClassesInfo.new JavaClass();
                    javaClass.setId(clazz.getId());
                    javaClass.setName(classes.toString());
                    list.add(javaClass);
                    allClassesInfo.getInfo().put(packageName,list);
                }else {
                    AllClassesInfo.JavaClass javaClass = allClassesInfo.new JavaClass();
                    javaClass.setId(clazz.getId());
                    javaClass.setName(classes.toString());
                    allClassesInfo.getInfo().get(packageName).add(javaClass);
                }
            }
        }
        return allClassesInfo;
    }
}
