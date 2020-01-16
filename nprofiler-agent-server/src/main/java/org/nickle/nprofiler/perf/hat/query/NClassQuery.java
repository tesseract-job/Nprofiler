package org.nickle.nprofiler.perf.hat.query;

import com.sun.tools.hat.internal.model.JavaClass;
import com.sun.tools.hat.internal.model.JavaField;
import com.sun.tools.hat.internal.model.JavaStatic;
import com.sun.tools.hat.internal.util.ArraySorter;
import com.sun.tools.hat.internal.util.Comparer;
import lombok.extern.slf4j.Slf4j;
import org.nickle.nprofiler.bean.ClassInfo;

/**
 * class详细信息查询接口
 * @author wesley
 * @create 2020-01-16
 */
@Slf4j
public class NClassQuery extends NQueryHandler {

    public NClassQuery() {
    }

    @Override
    Object run() {
        JavaClass clazz = snapshot.findClass(query);
        ClassInfo classInfo = new ClassInfo();
        if (clazz == null) {
            log.error("class not found: " + query);
        } else {
            classInfo.setName(clazz.toString());
            classInfo.setSuperclass(clazz.getSuperclass().toString());
            classInfo.setLoader(clazz.getLoader().toString());
            classInfo.setSigners(clazz.getSigners().toString());
            classInfo.setProtectionDomain(clazz.getProtectionDomain().toString());
            JavaClass[] sc = clazz.getSubclasses();
            for (int i = 0; i < sc.length; i++) {
                classInfo.getSubclasses().add(sc[i].getName());
            }
            JavaField[] ff = clazz.getFields().clone();
            ArraySorter.sort(ff, new Comparer() {
                public int compare(Object lhs, Object rhs) {
                    JavaField left = (JavaField) lhs;
                    JavaField right = (JavaField) rhs;
                    return left.getName().compareTo(right.getName());
                }
            });
            for (int i = 0; i < ff.length; i++) {
                ClassInfo.JavaField  javaField = classInfo.new JavaField();
                javaField.setName(ff[i].getName());
                javaField.setSignature(ff[i].getSignature());
                classInfo.getFields().add(javaField);
            }

            JavaStatic[] ss = clazz.getStatics();
            for (int i = 0; i < ss.length; i++) {
                ClassInfo.JavaStatic javaStatic = classInfo.new JavaStatic();
                ClassInfo.JavaField  javaField = classInfo.new JavaField();
                javaField.setName(ss[i].getField().getName());
                javaField.setSignature(ss[i].getField().getSignature());
                javaStatic.setField(javaField);
                javaStatic.setValue(ss[i].getValue().toString());
            }
        }
        return classInfo;
    }
}
