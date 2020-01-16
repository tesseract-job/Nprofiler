package org.nickle.nprofiler.perf.hat.query;

import com.sun.tools.hat.internal.model.JavaClass;
import com.sun.tools.hat.internal.model.JavaHeapObject;
import com.sun.tools.hat.internal.model.JavaThing;
import com.sun.tools.hat.internal.model.Snapshot;
import com.sun.tools.hat.internal.util.Misc;
import org.nickle.nprofiler.bean.InstanceInfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 抽象查询
 * @author: 1336942608@qq.com
 * @version:
 * @date: 2020年01月15 20时29分
 */
public abstract class NQueryHandler {
    protected String urlStart;
    protected String query;
    protected Snapshot snapshot;

    NQueryHandler() {
    }

    abstract Object run();

    void setUrlStart(String var1) {
        this.urlStart = var1;
    }

    void setQuery(String var1) {
        this.query = var1;
    }

    void setSnapshot(Snapshot var1) {
        this.snapshot = var1;
    }

    protected String encodeForURL(String var1) {
        try {
            var1 = URLEncoder.encode(var1, "UTF-8");
        } catch (UnsupportedEncodingException var3) {
            var3.printStackTrace();
        }

        return var1;
    }


    protected String encodeForURL(JavaClass var1) {
        return var1.getId() == -1L ? this.encodeForURL(var1.getName()) : var1.getIdString();
    }


    protected long parseHex(String var1) {
        return Misc.parseHex(var1);
    }

    protected String parseLink(long var1) {
        return "object/"+ this.parseHex(var1);
    }

    protected String parseHex(long var1) {
        if (this.snapshot.getIdentifierSize() == 4) {
            return Misc.toHex((int)var1);
        } else {
            return Misc.toHex(var1);
        }
    }

    /**
     * 解析输出实体信息
     * @param var1
     * @return
     */
    protected InstanceInfo parseThing(JavaThing var1) {
        if (var1 == null) {
            return new InstanceInfo("");
        } else {
            if (var1 instanceof JavaHeapObject) {
                InstanceInfo instanceInfo = new InstanceInfo();
                JavaHeapObject var2 = (JavaHeapObject)var1;
                long var3 = var2.getId();
                if (var3 != -1L) {
                    if (var2.isNew()) {
                        instanceInfo.setNewFlag((byte)1);
                    }
                    instanceInfo.setObjectLink(parseLink(var3));
                    instanceInfo.setBytesCount((long)var2.getSize());
                }
                instanceInfo.setObjectName(var1.toString());
                return instanceInfo;
            } else {
                return new InstanceInfo(var1.toString());
            }

        }
    }


}
