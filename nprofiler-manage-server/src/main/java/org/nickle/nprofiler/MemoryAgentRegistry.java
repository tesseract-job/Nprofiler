package org.nickle.nprofiler;

import com.google.common.collect.Maps;
import feign.Feign;
import feign.gson.GsonDecoder;
import lombok.extern.slf4j.Slf4j;
import org.nickle.nprofiler.bean.AgentInfo;
import org.nickle.nprofiler.exception.NprofilerException;

import java.util.List;
import java.util.Map;

@Slf4j
public class MemoryAgentRegistry implements IAgentRegistry {
    private final Map<String, AgentInfo> AGENT_MAP = Maps.newConcurrentMap();
    private final Map<String, IAgentServerService> AGENT_SERVICE_MAP = Maps.newConcurrentMap();

    @Override
    public void regist(AgentInfo agentInfo) throws Exception {
        AgentInfo oldAgentInfo = AGENT_MAP.put(agentInfo.getSocketInfo(), agentInfo);
        if (oldAgentInfo != null) {
            log.info("重复注册:{}", oldAgentInfo);
        }
    }

    @Override
    public void delete(String socketInfo) throws Exception {
        AGENT_MAP.remove(socketInfo);
    }

    @Override
    public List<AgentInfo> getAllAgentInfo() throws Exception {
        return (List<AgentInfo>) AGENT_MAP.values();
    }

    @Override
    public IAgentServerService getAgentService(String socketInfo) throws Exception {
        AgentInfo agentInfo = AGENT_MAP.get(socketInfo);
        IAgentServerService agentServerService;
        if (agentInfo == null) {
            throw new NprofilerException("机器没有注册");
        }
        agentServerService = AGENT_SERVICE_MAP.get(socketInfo);
        if (agentServerService == null) {
            agentServerService = Feign.builder()
                    .decoder(new GsonDecoder())
                    .target(IAgentServerService.class, socketInfo);
            AGENT_SERVICE_MAP.put(socketInfo, agentServerService);
        }
        return agentServerService;
    }
}
