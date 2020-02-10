package org.nickle.nprofiler.spring.web;

import org.nickle.nprofiler.bean.*;
import org.nickle.nprofiler.perf.service.IJavaProcessService;
import org.nickle.nprofiler.perf.service.IJhatService;
import org.nickle.nprofiler.perf.service.IJmapService;
import org.nickle.nprofiler.perf.service.IJstatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.io.IOException;
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
    @Autowired
    private IJhatService jhatService;


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

    @GetMapping(JHAT_DUMP_INFO_MAPPING+"/allClassesInfo")
    public AllClassesInfo getAllClassesInfo() throws IOException {
        return jhatService.storeAllClassesInfo("F:\\444.hprof");
    }

    @GetMapping(JHAT_DUMP_INFO_MAPPING+"/classInfo")
    public List<ClassInfo> getClassInfo() throws IOException {
        return jhatService.storeClassInfo("F:\\444.hprof");
    }

    @GetMapping(JHAT_DUMP_INFO_MAPPING+"/histogramInfo")
    public List<HistogramInfo> getHistogramInfo() throws IOException {
        return jhatService.storeHistogramInfo("F:\\444.hprof");
    }

    @GetMapping(JHAT_DUMP_INFO_MAPPING+"/instancesCountResultInfo")
    public InstancesCountResultInfo getInstancesCountResultInfo() throws IOException {
        return jhatService.storeInstancesCountResultInfo("F:\\444.hprof");
    }

    @GetMapping(JHAT_DUMP_INFO_MAPPING+"/rootsInfo")
    public List<RootsInfo> getRootsInfo() throws IOException {
        return jhatService.storeRootsInfo("F:\\444.hprof");
    }

    @GetMapping(JHAT_DUMP_INFO_MAPPING+"/RefsByTypeInfo")
    public List<RefsByTypeInfo> getRefsByTypeInfo() throws IOException {
        return jhatService.storeRefsByTypeInfo("F:\\444.hprof");
    }


}
