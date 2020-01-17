package org.nickle.nprofiler;

import com.alibaba.fastjson.JSON;
import org.junit.Before;
import org.junit.Test;
import org.nickle.nprofiler.bean.JinfoConfiguration;
import org.nickle.nprofiler.bean.JmapHeapInfo;
import org.nickle.nprofiler.bean.JpsProcessInfo;
import org.nickle.nprofiler.perf.service.IJavaProcessService;
import org.nickle.nprofiler.perf.service.IJinfoService;
import org.nickle.nprofiler.perf.service.IJmapService;
import org.nickle.nprofiler.perf.service.impl.DefaultJavaProcessServiceImpl;
import org.nickle.nprofiler.perf.service.impl.DefaultJinfoServiceImpl;
import org.nickle.nprofiler.perf.service.impl.DefaultJmapServiceImpl;

import java.util.List;

public class TestClass {
    private IJavaProcessService javaProcessService;
    private IJmapService jmapService;
    private IJinfoService iJinfoService;

    @Before
    public void set() {
        javaProcessService = new DefaultJavaProcessServiceImpl();
        jmapService = new DefaultJmapServiceImpl();
        iJinfoService = new DefaultJinfoServiceImpl();
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

    @Test
    public void getInfoConfiguration() throws Exception {
        JinfoConfiguration jinfoConfiguration = iJinfoService.getInfoConfiguration(19700);
        System.out.println(JSON.toJSON(jinfoConfiguration));
    }

}
