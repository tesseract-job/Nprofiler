package org.nickle.nprofiler.registry;

import org.nickle.nprofiler.bean.AgentInfo;
import org.nickle.nprofiler.service.IAgentServerService;

import java.util.Collection;

public interface IAgentRegistry {
    void regist(AgentInfo agentInfo) throws Exception;

    void delete(String socketInfo) throws Exception;

    Collection<AgentInfo> getAllAgentInfo() throws Exception;

    IAgentServerService getAgentService(String id) throws Exception;

}
