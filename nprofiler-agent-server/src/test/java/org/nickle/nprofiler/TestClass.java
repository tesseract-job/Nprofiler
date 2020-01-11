package org.nickle.nprofiler;

import com.alibaba.fastjson.JSON;
import org.junit.Before;
import org.junit.Test;
import org.nickle.nprofiler.bean.JmapHeapInfo;
import org.nickle.nprofiler.bean.JpsProcessInfo;
import org.nickle.nprofiler.perf.service.IJavaProcessService;
import org.nickle.nprofiler.perf.service.IJmapService;
import org.nickle.nprofiler.perf.service.impl.DefaultJavaProcessServiceImpl;
import org.nickle.nprofiler.perf.service.impl.DefaultJmapServiceImpl;

import java.util.List;

public class TestClass {
    private IJavaProcessService javaProcessService;
    private IJmapService jmapService;

    @Before
    public void set() {
        javaProcessService = new DefaultJavaProcessServiceImpl();
        jmapService = new DefaultJmapServiceImpl();
    }

    @Test
    public void testJps() throws Exception {
        List<JpsProcessInfo> allJavaProcess = javaProcessService.getAllJavaProcess();
        System.out.println(JSON.toJSON(allJavaProcess));
    }

    @Test
    public void testJmapHeapSummary() throws Exception {
        JmapHeapInfo processHeapSummary = jmapService.getProcessHeapSummary(18308);
        System.out.println(JSON.toJSON(processHeapSummary));
    }

}
