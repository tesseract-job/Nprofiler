package org.nickle.nprofiler.perf.hat.query;

import com.sun.tools.hat.internal.model.*;
import com.sun.tools.hat.internal.util.Misc;
import org.nickle.nprofiler.bean.InstanceInfo;
import org.nickle.nprofiler.bean.RootsInfo;

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


    protected String encodeForURL(JavaClass javaClass) {
        return javaClass.getId() == -1L ? this.encodeForURL(javaClass.getName()) : javaClass.getIdString();
    }


    protected long parseHex(String var1) {
        return Misc.parseHex(var1);
    }

    protected String parseThingLink(long var1) {
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
                long id = var2.getId();
                if (id != -1L) {
                    if (var2.isNew()) {
                        instanceInfo.setNewFlag((byte)1);
                    }
                    instanceInfo.setId(id);
                    instanceInfo.setByteSize((long)var2.getSize());
                }
                instanceInfo.setObjectName(var1.toString());
                return instanceInfo;
            } else {
                return new InstanceInfo(var1.toString());
            }

        }
    }


    protected RootsInfo.RootInfo parseRoot(Root var1) {
        RootsInfo.RootInfo rootInfo = new RootsInfo.RootInfo();
        StackTrace var2 = var1.getStackTrace();
        boolean var3 = var2 != null && var2.getFrames().length != 0;
        if (var3) {
            // String rootStackLink = parseRootStackLink((long) var1.getIndex());
            // rootInfo.setRootStackLink(rootStackLink);
            rootInfo.setRootStackId((long) var1.getIndex());
        }
        rootInfo.setDescription(var1.getDescription());
        return rootInfo;
    }

    protected String parseRootStackLink(long var1){
        return "rootStack/"+ this.parseHex(var1);
    }



}
