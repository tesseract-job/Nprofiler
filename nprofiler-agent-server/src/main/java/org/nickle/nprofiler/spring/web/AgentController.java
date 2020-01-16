package org.nickle.nprofiler.spring.web;

import org.nickle.nprofiler.bean.JmapHeapInfo;
import org.nickle.nprofiler.bean.JpsProcessInfo;
import org.nickle.nprofiler.bean.JstatGCInfo;
import org.nickle.nprofiler.perf.service.IJavaProcessService;
import org.nickle.nprofiler.perf.service.IJmapService;
import org.nickle.nprofiler.perf.service.IJstatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.nickle.nprofiler.constant.CommonConstant.*;

@RestController
@Validated
public class AgentController {

    @Autowired
    private IJmapService jmapService;
    @Autowired
    private IJavaProcessService javaProcessService;
    @Autowired
    private IJstatService jstatService;


    @GetMapping(JMAP_HEAP_INFO_MAPPING + "/{processId}")
    public JmapHeapInfo getJmapHeapInfo(@PathVariable("processId") @NotNull Integer processId) throws Exception {
        return jmapService.getProcessHeapSummary(processId);
    }

    @GetMapping(JPS_PROCESS_INFO_MAPPING)
    public List<JpsProcessInfo> getJpsProcessInfo() throws Exception {
        return javaProcessService.getAllJavaProcess();
    }

    @GetMapping(JSTAT_GC_INFO_MAPPING+ "/{processId}")
    public JstatGCInfo getJstatGCInfo(@PathVariable("processId") @NotNull Integer processId) throws Exception {
        return jstatService.getGCSummary(processId);
    }


}
