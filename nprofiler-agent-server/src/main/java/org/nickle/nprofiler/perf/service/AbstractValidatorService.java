package org.nickle.nprofiler.perf.service;

import com.google.common.collect.Sets;
import org.nickle.nprofiler.exception.NprofilerException;
import org.nickle.nprofiler.perf.validator.IJVMInfoValiator;

import java.util.Iterator;
import java.util.Set;

public abstract class AbstractValidatorService<T> {
    /**
     * 非线程安全，外面调用ADD请保证线程安全性
     */
    private Set<IJVMInfoValiator> valiatorSet = Sets.newHashSet();


    protected final void addValidator(IJVMInfoValiator<T> valiator) {
        valiatorSet.add(valiator);
    }

    protected final boolean fireValidator(T t) throws Exception {
        if (t == null) {
            throw new NprofilerException("校验参数不能问为空");
        }
        final Iterator<IJVMInfoValiator> iterator = valiatorSet.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().validate(t)) {
                return false;
            }
        }
        return true;
    }

}
