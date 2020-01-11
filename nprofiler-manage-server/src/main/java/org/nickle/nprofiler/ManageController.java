package org.nickle.nprofiler;

import org.nickle.nprofiler.bean.AgentInfo;
import org.nickle.nprofiler.bean.JpsProcessInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ManageController {
    @Autowired
    private IAgentRegistry iAgentRegistry;
    @Autowired
    private IAgentServerService iAgentServerService;

    @GetMapping("/agentInfo")
    public List<AgentInfo> getAllAgentInfo() throws Exception {
        return iAgentRegistry.getAllAgentInfo();
    }

    @GetMapping("/jvmProcess/{processId}")
    public List<JpsProcessInfo> getAllJVMProcess() throws Exception {
        return iAgentServerService.getJpsProcessInfo();
    }

}
