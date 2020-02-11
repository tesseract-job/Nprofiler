package org.nickle.nprofiler.perf.hat.query;

import org.nickle.nprofiler.perf.hat.model.AbstractJavaHeapObjectVisitor;
import org.nickle.nprofiler.perf.hat.model.JavaClass;
import org.nickle.nprofiler.perf.hat.model.JavaField;
import org.nickle.nprofiler.perf.hat.model.JavaHeapObject;
import lombok.extern.slf4j.Slf4j;
import org.nickle.nprofiler.bean.RefsByTypeInfo;
import org.nickle.nprofiler.exception.NprofilerException;

import java.util.*;

/**
 * 根据类型查找引用查询
 * @author: 1336942608@qq.com
 * @version:
 * @date: 2020年01月18 07时17分
 */
@Slf4j
public class NRefsByTypeQuery extends NQueryHandler{
    public NRefsByTypeQuery() {
    }

    @Override
    public Object run() {
        JavaClass var1 = this.snapshot.findClass(this.query);
        if (var1 == null) {
            throw new NprofilerException("class not found: " + this.query);
        } else {
            // 引用集合
            HashMap referrersMap  = new HashMap();
            // 仲裁
            final HashMap refereesMap = new HashMap();
            Enumeration var4 = var1.getInstances(false);
            RefsByTypeInfo result = new RefsByTypeInfo();
            while(true) {
                JavaHeapObject var5;
                do {
                    if (!var4.hasMoreElements()) {
                        // 解析查询的类型元信息
                        RefsByTypeInfo.JavaClass javaClass = result.new JavaClass();
                        javaClass.setId(var1.getId());
                        javaClass.setName(var1.getName());
                        result.setRefByClass(javaClass);
                        // 排序
                        sortMap(referrersMap);
                        sortMap(refereesMap);
                        result.setReferrersMap(referrersMap);
                        result.setRefereesMap(refereesMap);
                        return result;
                    }
                    var5 = (JavaHeapObject)var4.nextElement();
                } while(var5.getId() == -1L);

                Enumeration referers = var5.getReferers();

                while(referers.hasMoreElements()) {
                    JavaHeapObject heapObject = (JavaHeapObject)referers.nextElement();
                    JavaClass var8 = heapObject.getClazz();
                    if (var8 == null) {
                        log.info("null class for " + heapObject);
                    } else {
                        RefsByTypeInfo.JavaClass javaClass = result.new JavaClass();
                        javaClass.setId(var8.getId());
                        javaClass.setName(var8.getName());
                        Long var9 = (Long)referrersMap.get(var8);
                        if (var9 == null) {
                            var9 = new Long(1L);
                        } else {
                            var9 = new Long(var9 + 1L);
                        }
                        referrersMap.put(javaClass, var9);
                    }
                }

                var5.visitReferencedObjects(new AbstractJavaHeapObjectVisitor() {
                    @Override
                    public void visit(JavaHeapObject var1) {
                        JavaClass var2 = var1.getClazz();
                        RefsByTypeInfo.JavaClass javaClass = result.new JavaClass();
                        javaClass.setId(var2.getId());
                        javaClass.setName(var2.getName());
                        Long var3x = (Long)refereesMap.get(javaClass);
                        if (var3x == null) {
                            var3x = new Long(1L);
                        } else {
                            var3x = new Long(var3x + 1L);
                        }
                        refereesMap.put(javaClass, var3x);
                    }

                    @Override
                    public boolean exclude(JavaClass clazz, JavaField f) {
                        return false;
                    }
                });
            }
        }
    }


    private void sortMap(final Map<RefsByTypeInfo.JavaClass, Long> var1) {
        Set var2 = var1.keySet();
        RefsByTypeInfo.JavaClass[] var3 = new RefsByTypeInfo.JavaClass[var2.size()];
        var2.toArray(var3);
        Arrays.sort(var3, new Comparator<RefsByTypeInfo.JavaClass>() {
            @Override
            public int compare(RefsByTypeInfo.JavaClass var1x, RefsByTypeInfo.JavaClass var2) {
                Long var3 = var1.get(var1x);
                Long var4 = var1.get(var2);
                return var4.compareTo(var3);
            }
        });
    }
}
