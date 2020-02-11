package org.nickle.nprofiler.perf.hat.query;

import org.nickle.nprofiler.perf.hat.model.Root;
import org.nickle.nprofiler.perf.hat.model.StackTrace;
import org.nickle.nprofiler.bean.RootStackInfo;
import org.nickle.nprofiler.exception.NprofilerException;

/**
 * 根调用栈查询
 * @author: 1336942608@qq.com
 * @version:
 * @date: 2020年01月17 22时48分
 */
public class NRootStackQuery extends NQueryHandler{

    public NRootStackQuery() {
    }

    @Override
    public Object run() {
        int hexVal = (int)this.parseHex(this.query);
        Root root = this.snapshot.getRootAt(hexVal);
        if (root == null) {
            throw new NprofilerException(String.format("找不到以{%s}为根的引用",query));
        } else {
            StackTrace stackTrace = root.getStackTrace();
            RootStackInfo rootStackInfo = new RootStackInfo();
            if (stackTrace != null && stackTrace.getFrames().length != 0) {
//                rootStackInfo.setFrames(stackTrace.getFrames());
            } else {
                throw new NprofilerException(String.format("找不到{%s}的调用栈",root.getDescription()));
            }
            return rootStackInfo;
        }
    }
}
