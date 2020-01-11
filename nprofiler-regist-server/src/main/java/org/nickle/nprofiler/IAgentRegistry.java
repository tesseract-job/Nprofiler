package org.nickle.nprofiler;

import org.nickle.nprofiler.bean.AgentInfo;

import java.util.List;

public interface IAgentRegistry {
    void regist(AgentInfo agentInfo) throws Exception;

    void delete(String socketInfo) throws Exception;

    List<AgentInfo> getAllAgentInfo() throws Exception;

    IAgentServerService getAgentService() throws Exception;

}
