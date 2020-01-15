package org.nickle.nprofiler.perf.hat.query;

import com.sun.tools.hat.internal.model.*;
import com.sun.tools.hat.internal.util.Misc;
import lombok.Data;

import java.io.PrintWriter;
import java.io.StringWriter;
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



}
