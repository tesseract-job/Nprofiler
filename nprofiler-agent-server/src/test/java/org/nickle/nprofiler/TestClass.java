package org.nickle.nprofiler;

import org.junit.Before;
import org.junit.Test;
import org.nickle.nprofiler.perf.service.IJavaProcessService;
import org.nickle.nprofiler.perf.service.IJmapService;
import org.nickle.nprofiler.perf.service.impl.DefaultJavaProcessServiceImpl;
import org.nickle.nprofiler.perf.service.impl.DefaultJmapServiceImpl;

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
        System.out.println(javaProcessService.getAllJavaProcess());
    }

    @Test
    public void testJmapHeapSummary() throws Exception {
        System.out.println(jmapService.getProcessHeapSummary(20300));
    }

}
