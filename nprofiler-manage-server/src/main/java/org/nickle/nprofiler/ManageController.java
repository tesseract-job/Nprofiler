package org.nickle.nprofiler;

import org.nickle.nprofiler.bean.AgentInfo;
import org.nickle.nprofiler.bean.JmapHeapInfo;
import org.nickle.nprofiler.bean.JpsProcessInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
public class ManageController {
    @Autowired
    private IAgentRegistry iAgentRegistry;
    @Autowired
    private IAgentServerService iAgentServerService;

    @GetMapping("/agentInfo")
    public List<AgentInfo> getAllAgentInfo() throws Exception {
        return iAgentRegistry.getAllAgentInfo();
    }

    @GetMapping("/jvmProcess/{id}")
    public List<JpsProcessInfo> getAllJVMProcess(@PathVariable("id") String id) throws Exception {

        return iAgentServerService.getJpsProcessInfo();
    }

    @GetMapping("/heapInfo/{processId}")
    public JmapHeapInfo getJmapHeapInfo(@PathVariable("processId") String processId) throws Exception {
        return iAgentServerService.getJmapHeapInfo(processId);
    }
}
