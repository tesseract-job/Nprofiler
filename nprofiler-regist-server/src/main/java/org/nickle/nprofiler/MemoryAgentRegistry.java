package org.nickle.nprofiler;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.nickle.nprofiler.bean.AgentInfo;

import java.util.List;
import java.util.Map;

@Slf4j
public class MemoryAgentRegistry implements IAgentRegistry {
    private final Map<String, AgentInfo> AGENT_MAP = Maps.newConcurrentMap();

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
}
