package org.nickle.nprofiler;

import org.junit.Before;
import org.junit.Test;
import org.nickle.nprofiler.perf.service.IJavaProcessService;
import org.nickle.nprofiler.perf.service.impl.DefaultJavaProcessServiceImpl;

public class TestClass {
    private IJavaProcessService javaProcessService;

    @Before
    public void set() {
        javaProcessService = new DefaultJavaProcessServiceImpl();
    }

    @Test
    public void testJps() throws Exception {
        System.out.println(javaProcessService.getAllJavaProcess());
    }
}
